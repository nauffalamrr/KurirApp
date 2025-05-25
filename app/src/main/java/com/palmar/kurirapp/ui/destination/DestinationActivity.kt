package com.palmar.kurirapp.ui.destination

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.palmar.kurirapp.R
import com.palmar.kurirapp.adapter.DestinationAdapter
import com.palmar.kurirapp.data.Destination
import com.palmar.kurirapp.data.Location
import com.palmar.kurirapp.data.OptimizeRouteRequest
import com.palmar.kurirapp.data.OptimizeRouteResponse
import com.palmar.kurirapp.data.SimpleLocation
import com.palmar.kurirapp.data.retrofit.ApiConfig
import com.palmar.kurirapp.databinding.ActivityDestinationBinding
import com.palmar.kurirapp.ui.result.ResultActivity
import com.palmar.kurirapp.ui.selectlocation.SelectLocationActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DestinationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDestinationBinding
    private val destinations = mutableListOf<Destination>()
    private lateinit var adapter: DestinationAdapter
    private lateinit var vehicleTypeFromHome: String

    private val REQUEST_SELECT_LOCATION = 100

    private val accessToken: String
        get() {
            val sharedPref = getSharedPreferences("userPrefs", MODE_PRIVATE)
            return sharedPref.getString("access_token", "") ?: ""
        }

    @RequiresApi(35)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDestinationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vehicleTypeFromHome = intent.getStringExtra("vehicleType") ?: "motorcycle"

        supportActionBar?.hide()

        when (vehicleTypeFromHome.lowercase()) {
            "motorcycle" -> {
                binding.optionVehicle.setImageResource(R.drawable.ic_motor)
                binding.textVehicleOption.text = getString(R.string.vehicle_motorcycle)
            }

            "car" -> {
                binding.optionVehicle.setImageResource(R.drawable.ic_car)
                binding.textVehicleOption.text = getString(R.string.vehicle_car)
            }

            else -> {
                binding.optionVehicle.setImageResource(R.drawable.ic_motor)
                binding.textVehicleOption.text = vehicleTypeFromHome
            }
        }

        binding.backButton.setOnClickListener { finish() }

        if (destinations.size < 2) {
            destinations.clear()
            destinations.add(
                Destination(
                    id = -1,
                    location = Location(0.0, 0.0, "Your Location"),
                    sequence_order = 1,
                    latitude = 0.0,
                    longitude = 0.0
                )
            )
            destinations.add(
                Destination(
                    id = -1,
                    location = Location(0.0, 0.0, "Your Destination"),
                    sequence_order = 2,
                    latitude = 0.0,
                    longitude = 0.0
                )
            )
        }

        adapter = DestinationAdapter(destinations.drop(2).toMutableList()) { position ->
            openSelectLocation(position + 2)
        }

        binding.rvAddDestination.layoutManager = LinearLayoutManager(this)
        binding.rvAddDestination.adapter = adapter

        binding.firstLocation.setOnClickListener {
            openSelectLocation(0)
        }
        binding.firstDestination.setOnClickListener {
            openSelectLocation(1)
        }

        binding.buttonAddDestination.setOnClickListener {
            val newDestination = Destination(
                id = -1,
                location = Location(0.0, 0.0, "Your Destination"),
                sequence_order = destinations.size + 1,
                latitude = 0.0,
                longitude = 0.0
            )
            destinations.add(newDestination)
            adapter.updateData(destinations.drop(2).toMutableList())
            adapter.notifyDataSetChanged()
            updateRemoveButtonVisibility()
        }

        binding.buttonRemoveDestination.setOnClickListener {
            if (destinations.size > 2) {
                destinations.removeLast()
                adapter.updateData(destinations.drop(2).toMutableList())
                adapter.notifyDataSetChanged()
                updateRemoveButtonVisibility()
            }
        }

        binding.buttonOptimize.setOnClickListener {
            if (!isAllLocationsValid()) {
                Toast.makeText(this, "Harap masukkan semua lokasi terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            callOptimizeRouteApi()
        }

        binding.progressBar.visibility = android.view.View.GONE

        updateFirstLocationsUI()
        updateRemoveButtonVisibility()
    }

    private fun openSelectLocation(position: Int) {
        val intent = Intent(this, SelectLocationActivity::class.java).apply {
            putExtra("requestCode", REQUEST_SELECT_LOCATION)
            putExtra("position", position)
        }
        startActivityForResult(intent, REQUEST_SELECT_LOCATION)
    }

    private fun updateRemoveButtonVisibility() {
        val visible = destinations.size > 2
        binding.cardRemoveDestination.visibility = if (visible) View.VISIBLE else View.GONE
        binding.textRemoveDestination.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun updateFirstLocationsUI() {
        binding.tvLocation.text = "Your Location"
        binding.tvDestination.text = "Your Destination"

        binding.tvLocationDetail.text = destinations[0].location.name.ifEmpty { "Pilih lokasi" }
        binding.tvDestinationDetail.text = destinations[1].location.name.ifEmpty { "Pilih lokasi" }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_SELECT_LOCATION && resultCode == Activity.RESULT_OK) {
            val location = data?.getParcelableExtra<Location>("location")
            val position = data?.getIntExtra("position", -1) ?: -1
            if (location != null && position in destinations.indices) {
                destinations[position] = destinations[position].copy(
                    location = location,
                    latitude = location.latitude,
                    longitude = location.longitude
                )
                if (position == 0 || position == 1) {
                    updateFirstLocationsUI()
                } else {
                    adapter.updateData(destinations.drop(2).toMutableList())
                    adapter.notifyDataSetChanged()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun isAllLocationsValid(): Boolean {
        return destinations.all { dest ->
            val name = dest.location.name.lowercase()
            dest.latitude != 0.0 && dest.longitude != 0.0 &&
                    name.isNotEmpty() &&
                    name != "your location" && name != "your destination" && name != "pilih lokasi"
        }
    }

    private fun callOptimizeRouteApi() {
        if (accessToken.isBlank()) {
            Toast.makeText(this, "Token tidak ditemukan. Silakan login ulang.", Toast.LENGTH_SHORT).show()
            return
        }

        val start = SimpleLocation(
            latitude = destinations[0].latitude,
            longitude = destinations[0].longitude,
            name = destinations[0].location.name
        )

        val destinationLocations = destinations.drop(1).map {
            SimpleLocation(it.latitude, it.longitude, it.location.name)
        }

        val request = OptimizeRouteRequest(
            start = start,
            vehicle_type = vehicleTypeFromHome,
            destinations = destinationLocations
        )

        binding.buttonOptimize.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        val apiService = ApiConfig.getApiService(this)
        val call = apiService.optimizeRoute(request)

        call.enqueue(object : Callback<OptimizeRouteResponse> {
            override fun onResponse(
                call: Call<OptimizeRouteResponse>,
                response: Response<OptimizeRouteResponse>
            ) {
                binding.buttonOptimize.isEnabled = true
                binding.progressBar.visibility = android.view.View.GONE
                if (response.isSuccessful) {
                    val orderedDestinations = ArrayList(response.body()?.ordered_destinations?.map {
                        SimpleLocation(it.latitude, it.longitude, it.name ?: "")
                    } ?: emptyList())

                    if (orderedDestinations.isNotEmpty()) {
                        val intent = Intent(this@DestinationActivity, ResultActivity::class.java).apply {
                            putParcelableArrayListExtra("orderedDestinations", orderedDestinations)
                            putExtra("startLocation", start)
                            putExtra("vehicleType", vehicleTypeFromHome)
                        }
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@DestinationActivity, "Rute tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@DestinationActivity, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<OptimizeRouteResponse>, t: Throwable) {
                binding.buttonOptimize.isEnabled = true
                binding.progressBar.visibility = android.view.View.GONE
                Toast.makeText(this@DestinationActivity, "Gagal: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
