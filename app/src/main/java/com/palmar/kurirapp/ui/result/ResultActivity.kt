package com.palmar.kurirapp.ui.result

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.palmar.kurirapp.R
import com.palmar.kurirapp.adapter.ResultAdapter
import com.palmar.kurirapp.data.Result
import com.palmar.kurirapp.data.RoutePoint
import com.palmar.kurirapp.databinding.ActivityResultBinding
import kotlinx.coroutines.*
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.lang.Exception

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var adapter: ResultAdapter
    private val geoPoints = mutableListOf<GeoPoint>()
    private val markers = mutableListOf<Marker>()

    private val LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
    private val LOCATION_PERMISSION_REQUEST_CODE = 123

    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mapView = binding.mapView

        supportActionBar?.hide()

        val routeDataList = intent.getParcelableArrayListExtra<RoutePoint>("routeDataList") ?: arrayListOf()
        val vehicleType = intent.getStringExtra("vehicleType")?.lowercase() ?: "car"

        setupRecyclerView(routeDataList)
        displayMarkersAndRoute(routeDataList, vehicleType)
    }

    private fun convertRoutePointsToResults(routePoints: List<RoutePoint>): List<Result> {
        return routePoints.mapIndexed { index, routePoint ->
            Result(number = index + 1, destination = routePoint.name.ifBlank { "Location ${index + 1}" })
        }
    }

    private fun setupRecyclerView(routeDataList: List<RoutePoint>) {
        val results = convertRoutePointsToResults(routeDataList)
        adapter = ResultAdapter(results)
        binding.rvResult.layoutManager = LinearLayoutManager(this)
        binding.rvResult.adapter = adapter
    }

    private fun displayMarkersAndRoute(routeDataList: List<RoutePoint>, vehicleType: String) {
        geoPoints.clear()
        mapView.overlays.clear()
        markers.clear()

        val icons = listOf(
            getDrawable(R.drawable.ic_from),
            getDrawable(R.drawable.ic_des1),
            getDrawable(R.drawable.ic_des2),
            getDrawable(R.drawable.ic_des3)
        )

        routeDataList.forEachIndexed { index, routePoint ->
            val geoPoint = GeoPoint(routePoint.latitude, routePoint.longitude)
            geoPoints.add(geoPoint)

            val icon = icons[index % icons.size]

            val marker = Marker(binding.mapView).apply {
                position = geoPoint
                title = routePoint.name.ifBlank { if (index == 0) "Start Position" else "Destination ${index}" }
                this.icon = icon
                }
            markers.add(marker)
            binding.mapView.overlays.add(marker)
        }
        mapView.invalidate()

        if (geoPoints.isNotEmpty()) {
            drawRoute(geoPoints, vehicleType)
        }
    }

    private fun drawRoute(geoPoints: List<GeoPoint>, vehicleType: String) {
        val roadManager = OSRMRoadManager(this, "KurirAppUserAgent")

        roadManager.setMean(
            when (vehicleType.lowercase()) {
                "motorcycle" -> OSRMRoadManager.MEAN_BY_BIKE
                else -> OSRMRoadManager.MEAN_BY_CAR
            }
        )

        val colors = listOf(
            resources.getColor(android.R.color.holo_blue_bright),
            resources.getColor(android.R.color.holo_blue_dark),
            resources.getColor(android.R.color.holo_purple)
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val overlayByPriority = mutableListOf<Polyline?>()
                val routeGeoPoints = mutableListOf<GeoPoint>()

                for (i in 0 until geoPoints.size - 1) {
                    val segmentPoints = listOf(geoPoints[i], geoPoints[i + 1])
                    val road: Road = roadManager.getRoad(ArrayList(segmentPoints))

                    val segmentColor = colors[i % colors.size] // Mengulang warna

                    val segmentOverlay = Polyline().apply {
                        setPoints(road.mRouteHigh)
                        color = segmentColor
                        width = 10f
                    }
                    overlayByPriority.add(segmentOverlay)
                    routeGeoPoints.addAll(segmentPoints)
                }

                withContext(Dispatchers.Main) {
                    overlayByPriority.reversed().forEach { overlay ->
                        if (overlay != null) binding.mapView.overlays.add(overlay)
                    }

                    val bounds = BoundingBox.fromGeoPoints(routeGeoPoints)
                    binding.mapView.zoomToBoundingBox(bounds, true)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ResultActivity, "Error loading route: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
