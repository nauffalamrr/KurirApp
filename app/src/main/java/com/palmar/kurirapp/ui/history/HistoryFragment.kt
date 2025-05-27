package com.palmar.kurirapp.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.palmar.kurirapp.adapter.TripHistoryAdapter
import com.palmar.kurirapp.data.TripHistory
import com.palmar.kurirapp.data.retrofit.ApiConfig
import com.palmar.kurirapp.databinding.FragmentHistoryBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TripHistoryAdapter
    private val tripHistoryList = mutableListOf<TripHistory>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TripHistoryAdapter(tripHistoryList)

        binding.rvTripHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTripHistory.adapter = adapter

        loadHistory()
    }

    private fun loadHistory() {
        val sharedPref = requireContext().getSharedPreferences("userPrefs", android.content.Context.MODE_PRIVATE)
        val token = sharedPref.getString("access_token", null)
        val userId = sharedPref.getInt("user_id", -1)

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak ditemukan, silakan login ulang", Toast.LENGTH_SHORT).show()
            return
        }

        if (userId == -1) {
            Toast.makeText(requireContext(), "User ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        ApiConfig.getApiService(requireContext()).getTripHistory().enqueue(object : Callback<List<TripHistory>> {
            override fun onResponse(call: Call<List<TripHistory>>, response: Response<List<TripHistory>>) {
                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()
                    val filtered = data
                        .filter { it.driver_id == userId }
                        .sortedByDescending { it.id }
                    tripHistoryList.clear()
                    tripHistoryList.addAll(filtered)
                    adapter.notifyDataSetChanged()
                    checkIfEmpty()
                } else {
                    Toast.makeText(requireContext(), "Gagal memuat riwayat", Toast.LENGTH_SHORT).show()
                    showEmptyView()
                }
            }

            override fun onFailure(call: Call<List<TripHistory>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                showEmptyView()
            }
        })
    }

    private fun checkIfEmpty() {
        if (tripHistoryList.isEmpty()) {
            showEmptyView()
        } else {
            binding.rvTripHistory.visibility = View.VISIBLE
            binding.emptyHistoryText.visibility = View.GONE
        }
    }

    private fun showEmptyView() {
        binding.rvTripHistory.visibility = View.GONE
        binding.emptyHistoryText.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
