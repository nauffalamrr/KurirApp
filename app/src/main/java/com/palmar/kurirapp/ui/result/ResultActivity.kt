package com.palmar.kurirapp.ui.result

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.palmar.kurirapp.MainActivity
import com.palmar.kurirapp.R
import com.palmar.kurirapp.adapter.ResultAdapter
import com.palmar.kurirapp.data.SimpleLocation
import com.palmar.kurirapp.databinding.ActivityResultBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var adapter: ResultAdapter
    private var startMarker: Marker? = null
    private var roadPolyline: Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val startLocation = intent.getParcelableExtra<SimpleLocation>("startLocation")
        val orderedDestinations = intent.getParcelableArrayListExtra<SimpleLocation>("orderedDestinations") ?: arrayListOf()
        val vehicleType = intent.getStringExtra("vehicleType")?.lowercase() ?: "car"

        val allLocations = if (startLocation != null) {
            arrayListOf(startLocation).apply { addAll(orderedDestinations) }
        } else {
            orderedDestinations
        }

        setupRecyclerView(orderedDestinations)
        displayMarkersAndRoute(allLocations, vehicleType)

        binding.btnClose.setOnClickListener {
            binding.popupCard.visibility = View.GONE
            binding.btnShowPopup.visibility = View.VISIBLE
        }

        binding.btnShowPopup.setOnClickListener {
            binding.popupCard.visibility = View.VISIBLE
            binding.btnShowPopup.visibility = View.GONE
        }

        binding.btnCenterOnUser.setOnClickListener {
            startMarker?.let {
                binding.mapView.controller.animateTo(it.position)
            }
        }

        binding.btnGo.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finishAffinity()
        }

        binding.mapView.setMultiTouchControls(true)
        binding.mapView.zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.ALWAYS)
    }

    private fun setupRecyclerView(destinations: List<SimpleLocation>) {
        adapter = ResultAdapter(destinations)
        binding.rvResult.layoutManager = LinearLayoutManager(this)
        binding.rvResult.adapter = adapter
    }

    private fun displayMarkersAndRoute(destinations: List<SimpleLocation>, vehicleType: String) {
        binding.mapView.overlays.clear()

        val desIcons = listOf(
            ContextCompat.getDrawable(this, R.drawable.ic_des1),
            ContextCompat.getDrawable(this, R.drawable.ic_des2),
            ContextCompat.getDrawable(this, R.drawable.ic_des3)
        )
        val startIcon = ContextCompat.getDrawable(this, R.drawable.ic_from)

        destinations.forEachIndexed { index, destination ->
            val geoPoint = GeoPoint(destination.latitude, destination.longitude)
            val marker = Marker(binding.mapView).apply {
                position = geoPoint
                title = if (index == 0) "Start Position" else "Destination $index"
                icon = if (index == 0) startIcon else desIcons[(index -1) % desIcons.size]
            }
            binding.mapView.overlays.add(marker)
            if (index == 0) startMarker = marker
        }

        if (destinations.size > 1) {
            drawRoute(destinations, vehicleType)
        } else {
            binding.mapView.invalidate()
        }
    }

    private fun drawRoute(destinations: List<SimpleLocation>, vehicleType: String) {
        val roadManager = OSRMRoadManager(this, "KurirAppUserAgent")

        roadManager.setMean(
            when(vehicleType) {
                "motorcycle" -> OSRMRoadManager.MEAN_BY_BIKE
                else -> OSRMRoadManager.MEAN_BY_CAR
            }
        )

        val colors = listOf(
            ContextCompat.getColor(this, android.R.color.holo_blue_bright),
            ContextCompat.getColor(this, android.R.color.holo_blue_dark),
            ContextCompat.getColor(this, android.R.color.holo_purple)
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val overlays = mutableListOf<Polyline>()
                val routeGeoPoints = mutableListOf<GeoPoint>()

                for (i in 0 until destinations.size - 1) {
                    val segmentPoints = listOf(
                        GeoPoint(destinations[i].latitude, destinations[i].longitude),
                        GeoPoint(destinations[i+1].latitude, destinations[i+1].longitude)
                    )

                    val road: Road = roadManager.getRoad(ArrayList(segmentPoints))
                    val segmentColor = colors[i % colors.size]

                    val polyline = Polyline().apply {
                        setPoints(road.mRouteHigh)
                        color = segmentColor
                        width = 10f
                    }
                    overlays.add(polyline)
                    routeGeoPoints.addAll(segmentPoints)
                }

                withContext(Dispatchers.Main) {
                    roadPolyline?.let { binding.mapView.overlays.remove(it) }
                    overlays.forEach { binding.mapView.overlays.add(it) }

                    val bounds = BoundingBox.fromGeoPoints(routeGeoPoints)
                    binding.mapView.zoomToBoundingBox(bounds, true)
                    binding.mapView.invalidate()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ResultActivity, "Error loading route: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
