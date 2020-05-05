package br.com.victoriasantos.womensafe.view.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import br.com.victoriasantos.womensafe.R
import br.com.victoriasantos.womensafe.viewmodel.FirebaseViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private val viewModel: FirebaseViewModel by lazy {
        ViewModelProvider(this).get(FirebaseViewModel::class.java)
    }

    private lateinit var map: GoogleMap
    private lateinit var lastLocation: Location
    private var currentLocation: Marker? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false

    val REQUEST_CHECK_SETTINGS = 2
    val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation
                placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))
            }
        }
        createLocationRequest()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)
        map.setOnMapClickListener(object : GoogleMap.OnMapClickListener {
            override fun onMapClick(latLng: LatLng) {
                configureAlertDialog(latLng)
            }
        })
        setUpMap()
        getMarkers()
    }

    fun configureAlertDialog(latLng: LatLng) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.marcar_local_perigoso))
        builder.setMessage(getString(R.string.ask_dangerous_local))
        val marker = MarkerOptions().position(latLng)
        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        marker.title(getString(R.string.local_perigoso_marcado))
        val marker2 = map.addMarker(marker);

        builder.apply {
            setPositiveButton("SIM", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    val intent = Intent(this@MapsActivity, DangerousSpotActivity::class.java)
                    val endereco = getAddress(latLng)
                    intent.putExtra("latitude", latLng.latitude.toString())
                    intent.putExtra("longitude", latLng.longitude.toString())
                    intent.putExtra("endereco", endereco)
                    marker2.remove()
                    startActivity(intent)
                }
            })
            setNegativeButton("NÃO", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    marker2.remove()
                }
            })
        }
        builder.show()
    }


    private fun placeMarkerOnMap(location: LatLng) {

        if (currentLocation != null) {
            currentLocation!!.remove()
        }

        val markerOptions = MarkerOptions().position(location)
        markerOptions.icon(
            BitmapDescriptorFactory.fromBitmap(
                BitmapFactory.decodeResource(
                    resources,
                    R.mipmap.ic_user_location
                )
            )
        )
        val titleStr = getAddress(location).toString()
        markerOptions.title(titleStr)
        currentLocation = map.addMarker(markerOptions)

    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        GPSlocation()
    }

    fun GPSlocation() {
        map.isMyLocationEnabled = true
        map.mapType = GoogleMap.MAP_TYPE_TERRAIN


        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->

            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLng)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
            }
        }
    }

     fun getAddress(latLng: LatLng): String? {

        val geocoder = Geocoder(this)
        var addresses: List<Address>? = null
        var Address1: String? = null

        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        } catch (e: IOException) {
            Log.e("MapsActivity", e.localizedMessage)
        }
        if (addresses != null) {
            Address1 = addresses[0].getAddressLine(0)
        }

        return Address1
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())


        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(
                        this@MapsActivity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }

    fun searchLocation(view: View?) {
        val locationSearch = findViewById(R.id.editText) as EditText
        val location = locationSearch.text.toString()
        var addressList: List<Address>? = null
        if (location != null || location != "") {
            val geocoder = Geocoder(this)
            try {
                addressList = geocoder.getFromLocationName(location, 1)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (!addressList.isNullOrEmpty()) {
                val address = addressList!![0]
                val latLng = LatLng(address.latitude, address.longitude)
                map.addMarker(MarkerOptions().position(latLng).title(location))
                map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            } else {
                Toast.makeText(applicationContext, getString(R.string.local_not_found), Toast.LENGTH_LONG).show()
            }
            //Toast.makeText(applicationContext,address.latitude.toString() + " " + address.longitude,Toast.LENGTH_LONG).show()
        }
    }

    private fun getMarkers() {
        viewModel.getMarkers { markers ->
            markers?.forEach { m ->
                val markerOptions =
                    MarkerOptions().position(m)
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                markerOptions.title(getString(R.string.local_perigoso_marcado))
                map.addMarker(markerOptions)
            }
        }
    }

    override fun onMarkerClick(p0: Marker?) = false
    //TODO: CHAMAR OUTRA ATIVIDADE PARA MOSTRAR AVALIAÇÕES SOBRE O MARKER
}