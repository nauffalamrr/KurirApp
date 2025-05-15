package com.palmar.kurirapp.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.palmar.kurirapp.adapter.RecentHistoryAdapter
import com.palmar.kurirapp.data.TripHistory
import com.palmar.kurirapp.data.retrofit.ApiConfig
import com.palmar.kurirapp.databinding.FragmentHomeBinding
import com.palmar.kurirapp.ui.destination.DestinationActivity
import retrofit2.*

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private lateinit var recentHistoryAdapter: RecentHistoryAdapter
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recentHistoryAdapter = RecentHistoryAdapter(mutableListOf())

        binding.rvRecentHistory.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecentHistory.adapter = recentHistoryAdapter

        loadRecentHistory()

        binding.optionMotorcycle.setOnClickListener{
            navigateToDestinationActivity("motorcycle")
        }
        binding.optionCar.setOnClickListener{
            navigateToDestinationActivity("car")
        }
    }

    private fun loadRecentHistory() {
        ApiConfig.getApiService().getHistory().enqueue(object : Callback<List<TripHistory>> {
            override fun onResponse(call: Call<List<TripHistory>>, response: Response<List<TripHistory>>) {
                if (response.isSuccessful) {
                    val recentHistory = response.body()?.take(1)
                    recentHistory?.let {
                        recentHistoryAdapter.updateData(it)
                    }
                } else {
                }
            }

            override fun onFailure(call: Call<List<TripHistory>>, t: Throwable) {
            }
        })
    }

    private fun navigateToDestinationActivity(vehicleType: String) {
        val intent = Intent(requireContext(), DestinationActivity::class.java)
        intent.putExtra("vehicleType", vehicleType)
        startActivity(intent)
    }
}