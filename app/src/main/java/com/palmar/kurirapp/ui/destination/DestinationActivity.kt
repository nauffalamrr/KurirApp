package com.palmar.kurirapp.ui.destination

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.palmar.kurirapp.R
import com.palmar.kurirapp.adapter.Destination
import com.palmar.kurirapp.adapter.DestinationAdapter
import com.palmar.kurirapp.data.Location
import com.palmar.kurirapp.data.OptimizeRouteRequest
import com.palmar.kurirapp.data.OptimizeRouteResponse
import com.palmar.kurirapp.data.retrofit.ApiConfig
import com.palmar.kurirapp.databinding.ActivityDestinationBinding
import retrofit2.Call
import retrofit2.Response

class DestinationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDestinationBinding
    private lateinit var adapter: DestinationAdapter
    private val destinationList = mutableListOf<Destination>()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    companion object {
        const val REQUEST_CODE_FIRST_LOCATION = 100
        const val REQUEST_CODE_FIRST_DESTINATION = 101
        const val REQUEST_CODE_NEXT_DESTINATION = 102
        const val SELECT_LOCATION_REQUEST_CODE = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDestinationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        setupRecyclerView()

        binding.cardRemoveDestination.visibility = View.GONE
        binding.textRemoveDestination.visibility = View.GONE

        binding.buttonAddDestination.setOnClickListener {
            addNewDestination()
        }

        binding.buttonRemoveDestination.setOnClickListener {
            removeLastDestination()
        }

        binding.firstLocation.setOnClickListener {
            openSelectLocationActivity(REQUEST_CODE_FIRST_LOCATION)
        }

        binding.firstDestination.setOnClickListener {
            openSelectLocationActivity(REQUEST_CODE_FIRST_DESTINATION)
        }

        val vehicleType = intent.getStringExtra("vehicleType") ?: "car"
        if (vehicleType == "motorcycle") {
            binding.optionVehicle.setImageResource(R.drawable.ic_motor)
            binding.textVehicleOption.text = getString(R.string.vehicle_motorcycle)
        } else {
            binding.optionVehicle.setImageResource(R.drawable.ic_car)
            binding.textVehicleOption.text = getString(R.string.vehicle_car)
        }

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.buttonOptimize.setOnClickListener {
            if (!isGpsEnabled()) {
                promptEnableGps()
            } else {
                val startLoc = binding.tvLocationDetail.tag as? Location
                if (startLoc == null) {
                    Toast.makeText(this, "Please select start location", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val dests = destinationList.mapNotNull { it.detail }.filter { it != startLoc }
                if (dests.isEmpty()) {
                    Toast.makeText(this, "Please add at least one destination", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                callOptimizeRouteApi(vehicleType, startLoc, dests)
            }
        }

        isLoading.observe(this) { loading ->
            showLoading(loading)
        }
    }

    private fun setupRecyclerView() {
        adapter = DestinationAdapter(destinationList) { position ->
            openSelectLocationActivity(REQUEST_CODE_NEXT_DESTINATION, position)
        }
        binding.rvAddDestination.layoutManager = LinearLayoutManager(this)
        binding.rvAddDestination.adapter = adapter
    }

    private fun addNewDestination() {
        val maxDestinations = 9
        if (destinationList.size < maxDestinations) {
            val newDestination = Destination(
                title = "Your Destination",
                detail = null
            )
            destinationList.add(newDestination)
            adapter.notifyItemInserted(destinationList.size - 1)

            if (destinationList.size == maxDestinations) {
                binding.cardAddDestination.visibility = View.GONE
                binding.textAddDestination.visibility = View.GONE
            }

            if (destinationList.size == 1) {
                binding.cardRemoveDestination.visibility = View.VISIBLE
                binding.textRemoveDestination.visibility = View.VISIBLE
            }
        }
    }

    private fun removeLastDestination() {
        if (destinationList.isNotEmpty()) {
            destinationList.removeAt(destinationList.size - 1)
            adapter.notifyItemRemoved(destinationList.size)

            if (destinationList.size < 9) {
                binding.cardAddDestination.visibility = View.VISIBLE
                binding.textAddDestination.visibility = View.VISIBLE
            }

            if (destinationList.isEmpty()) {
                binding.cardRemoveDestination.visibility = View.GONE
                binding.textRemoveDestination.visibility = View.GONE
            }
        }
    }

    private fun openSelectLocationActivity(requestCode: Int, position: Int? = null) {
        val intent = Intent(this, com.palmar.kurirapp.ui.selectlocation.SelectLocationActivity::class.java)
        intent.putExtra("requestCode", requestCode)
        position?.let { intent.putExtra("position", it) }
        startActivityForResult(intent, SELECT_LOCATION_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_LOCATION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val location = data?.getParcelableExtra<Location>("location")
            val reqCode = data?.getIntExtra("requestCode", 0) ?: 0

            location?.let {
                when (reqCode) {
                    REQUEST_CODE_FIRST_LOCATION -> {
                        binding.tvLocationDetail.text = it.name
                        binding.tvLocationDetail.tag = it
                    }
                    REQUEST_CODE_FIRST_DESTINATION -> {
                        binding.tvDestinationDetail.text = it.name
                        binding.tvDestinationDetail.tag = it
                    }
                    REQUEST_CODE_NEXT_DESTINATION -> {
                        val position = data?.getIntExtra("position", -1) ?: -1
                        if (position != -1) {
                            destinationList[position].detail = it
                            adapter.notifyItemChanged(position)
                        }
                    }
                }
            }
        }
    }

    private fun isGpsEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun promptEnableGps() {
        AlertDialog.Builder(this)
            .setTitle("Enable GPS")
            .setMessage("GPS is required to optimize the route. Please enable GPS and try again.")
            .setPositiveButton("OK") { _, _ ->
                val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun callOptimizeRouteApi(vehicleType: String, startLocation: Location, destinations: List<Location>) {
        val apiService = ApiConfig.getApiService()

        val allDestinations = mutableListOf(startLocation)
        if (destinations.isNotEmpty()) {
            allDestinations.addAll(destinations)
        }

        val request = OptimizeRouteRequest(
            vehicleType = vehicleType,
            startLocation = startLocation,
            destinations = allDestinations
        )

        _isLoading.value = true

        apiService.optimizeRoute(request).enqueue(object : retrofit2.Callback<OptimizeRouteResponse> {
            override fun onResponse(call: Call<OptimizeRouteResponse>, response: Response<OptimizeRouteResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val optimizedList = response.body()?.orderedDestinations ?: emptyList()
                    updateDestinations(optimizedList)
                } else {
                    Toast.makeText(this@DestinationActivity, "Failed to optimize route", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<OptimizeRouteResponse>, t: Throwable) {
                _isLoading.value = false
                Toast.makeText(this@DestinationActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateDestinations(optimizedList: List<Location>) {
        destinationList.clear()
        optimizedList.forEach { loc ->
            destinationList.add(Destination(title = loc.name, detail = loc))
        }
        adapter.notifyDataSetChanged()
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.buttonOptimize.isEnabled = false
        } else {
            binding.progressBar.visibility = View.GONE
            binding.buttonOptimize.isEnabled = true
        }
    }
}
