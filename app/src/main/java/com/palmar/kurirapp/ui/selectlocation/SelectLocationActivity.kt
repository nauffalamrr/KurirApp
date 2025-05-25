package com.palmar.kurirapp.ui.selectlocation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import com.palmar.kurirapp.R
import com.palmar.kurirapp.adapter.LocationSuggestionAdapter
import com.palmar.kurirapp.data.Location
import com.palmar.kurirapp.data.LocationSuggestion
import com.palmar.kurirapp.data.SimpleLocation
import com.palmar.kurirapp.data.retrofit.NominatimApiConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import retrofit2.HttpException
import java.util.*

class SelectLocationActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var searchView: SearchView
    private lateinit var geocoder: Geocoder
    private lateinit var locationManager: LocationManager
    private lateinit var selectLocationButton: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var recyclerView: RecyclerView
    private lateinit var locationSuggestionsAdapter: LocationSuggestionAdapter
    private val locationCache = mutableMapOf<String, List<SimpleLocation>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_location)

        supportActionBar?.hide()

        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))

        mapView = findViewById(R.id.map_view)
        searchView = findViewById(R.id.search_view)
        selectLocationButton = findViewById(R.id.select_location_button)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(this, Locale.getDefault())
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        mapView.setMultiTouchControls(true)
        mapView.zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)

        recyclerView = findViewById(R.id.rv_suggestion)
        recyclerView.layoutManager = LinearLayoutManager(this)
        locationSuggestionsAdapter = LocationSuggestionAdapter { selectedLocation ->
            val geoPoint = GeoPoint(selectedLocation.latitude, selectedLocation.longitude)
            updateLocationOnMap(geoPoint, selectedLocation.name)
            recyclerView.visibility = View.GONE
        }
        recyclerView.adapter = locationSuggestionsAdapter

        val initialPoint = GeoPoint(-7.9546714, 112.6100617)
        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(initialPoint)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                val markerPresent = mapView.overlays.any { it is Marker }
                selectLocationButton.isEnabled = markerPresent || !newText.isNullOrEmpty()
                if (!newText.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val cachedSuggestions = locationCache[newText]
                        if (cachedSuggestions != null) {
                            locationSuggestionsAdapter.updateSuggestions(cachedSuggestions)
                            recyclerView.visibility = View.VISIBLE
                        } else {
                            try {
                                val api = NominatimApiConfig.getNominatimApiService()
                                val result = api.searchLocations(newText)
                                val simplified = result.map {
                                    SimpleLocation(
                                        latitude = it.latitude,
                                        longitude = it.longitude,
                                        name = it.displayName
                                    )
                                }
                                locationCache[newText] = simplified
                                locationSuggestionsAdapter.updateSuggestions(simplified)
                                recyclerView.visibility = View.VISIBLE
                            } catch (e: HttpException) {
                                if (e.code() == 429) {
                                    Toast.makeText(this@SelectLocationActivity, "Too many requests, please wait", Toast.LENGTH_SHORT).show()
                                } else e.printStackTrace()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                } else {
                    recyclerView.visibility = View.GONE
                }
                return false
            }
        })

        selectLocationButton.setOnClickListener {
            val marker = mapView.overlays.find { it is Marker } as? Marker
            if (marker != null) {
                val geoPoint = marker.position
                val name = marker.title ?: "Selected Location"
                val selected = Location(
                    latitude = geoPoint.latitude,
                    longitude = geoPoint.longitude,
                    name = name
                )

                val requestCode = intent.getIntExtra("requestCode", 0)
                val position = intent.getIntExtra("position", -1)

                val resultIntent = Intent().apply {
                    putExtra("location", selected)
                    putExtra("requestCode", requestCode)
                    putExtra("position", position)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Select Location First!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<CardView>(R.id.button_current_location).setOnClickListener {
            getCurrentLocation()
        }

        mapView.overlays.add(object : Overlay() {
            override fun onSingleTapConfirmed(e: android.view.MotionEvent?, mapView: MapView?): Boolean {
                e?.let {
                    val projection = mapView?.projection
                    val geoPoint = projection?.fromPixels(it.x.toInt(), it.y.toInt()) as? GeoPoint
                    geoPoint?.let { point ->
                        val locationName = getLocationName(point) ?: "Unknown Location"
                        updateLocationOnMap(point, locationName)
                    }
                }
                return true
            }
        })
    }

    private fun getCurrentLocation() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder(this).apply {
                setTitle("Enable GPS")
                setMessage("GPS is required for this feature. Please enable it in the settings.")
                setPositiveButton("Enable") { _, _ ->
                    val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
                setNegativeButton("Cancel", null)
                create()
                show()
            }
            return
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
            return
        }

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 0
            fastestInterval = 0
            numUpdates = 1
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation
                    if (location != null) {
                        val geoPoint = GeoPoint(location.latitude, location.longitude)
                        val locationName = getLocationName(geoPoint) ?: "Unknown Location"
                        updateLocationOnMap(geoPoint, locationName)
                        mapView.controller.animateTo(geoPoint)
                    } else {
                        Toast.makeText(this@SelectLocationActivity, "Unable to get current location", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            Looper.getMainLooper()
        )
    }

    private fun updateLocationOnMap(geoPoint: GeoPoint, locationName: String) {
        mapView.overlays.clear()
        addMapClickListener()

        val marker = Marker(mapView).apply {
            position = geoPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = locationName
        }
        mapView.overlays.add(marker)
        mapView.controller.animateTo(geoPoint)
    }

    private fun getLocationName(geoPoint: GeoPoint): String? {
        return try {
            val addresses = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
            if (!addresses.isNullOrEmpty()) addresses[0].getAddressLine(0) else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun addMapClickListener() {
        mapView.overlays.add(object : Overlay() {
            override fun onSingleTapConfirmed(e: android.view.MotionEvent?, mapView: MapView?): Boolean {
                e?.let {
                    val projection = mapView?.projection
                    val geoPoint = projection?.fromPixels(it.x.toInt(), it.y.toInt()) as? GeoPoint
                    geoPoint?.let { point ->
                        val locationName = getLocationName(point) ?: "Unknown Location"
                        updateLocationOnMap(point, locationName)
                    }
                }
                return true
            }
        })
    }
}
