package com.palmar.kurirapp.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.palmar.kurirapp.adapter.RecentHistoryAdapter
import com.palmar.kurirapp.data.TripHistory
import com.palmar.kurirapp.data.retrofit.ApiConfig
import com.palmar.kurirapp.databinding.FragmentHomeBinding
import com.palmar.kurirapp.ui.destination.DestinationActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var recentHistoryAdapter: RecentHistoryAdapter
    private val tripHistoryList = mutableListOf<TripHistory>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recentHistoryAdapter = RecentHistoryAdapter(tripHistoryList)

        binding.rvRecentHistory.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecentHistory.adapter = recentHistoryAdapter

        loadRecentHistory()

        binding.optionMotorcycle.setOnClickListener {
            navigateToDestinationActivity("motorcycle")
        }

        binding.optionCar.setOnClickListener {
            navigateToDestinationActivity("car")
        }
    }

    private fun loadRecentHistory() {
        val sharedPref = requireContext().getSharedPreferences("userPrefs", android.content.Context.MODE_PRIVATE)
        val token = sharedPref.getString("access_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak ditemukan, silakan login ulang", Toast.LENGTH_SHORT).show()
            return
        }

        ApiConfig.getApiService(requireContext()).getTripHistory().enqueue(object : Callback<List<TripHistory>> {
            override fun onResponse(call: Call<List<TripHistory>>, response: Response<List<TripHistory>>) {
                if (response.isSuccessful) {
                    val recentHistory = response.body()?.take(1) ?: emptyList()
                    tripHistoryList.clear()
                    tripHistoryList.addAll(recentHistory)
                    recentHistoryAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(requireContext(), "Gagal memuat riwayat", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<TripHistory>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun navigateToDestinationActivity(vehicleType: String) {
        val intent = Intent(requireContext(), DestinationActivity::class.java)
        intent.putExtra("vehicleType", vehicleType)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
