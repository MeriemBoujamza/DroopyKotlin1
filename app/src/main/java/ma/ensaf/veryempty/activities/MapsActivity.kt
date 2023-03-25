package ma.ensaf.veryempty.activities

import android.Manifest
import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.maps.GoogleMap
import android.os.Bundle
import ma.ensaf.veryempty.R
import kotlin.jvm.JvmStatic
import kotlin.jvm.Synchronized
import com.google.android.gms.maps.SupportMapFragment
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.ActivityResultCallback
import com.google.android.material.snackbar.Snackbar
import android.os.Build
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.google.android.gms.location.LocationServices
import androidx.annotation.RequiresApi
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.common.ConnectionResult
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationRequest
import android.view.MenuItem
import android.view.View
import android.widget.Button
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class MapsActivity : FragmentActivity(), OnMapReadyCallback, LocationListener,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private var mMap: GoogleMap? = null
    private var parent_view: View? = null
    var mLastLocation: Location? = null
    var mCurrLocationMarker: Marker? = null
    var mGoogleApiClient: GoogleApiClient? = null
    var mLocationRequest: LocationRequest? = null

    // below are the latitude and longitude
    // of delhi locations.
    var fes = LatLng(34.0181, 5.0078)

    // creating a variable for button.
    private lateinit var hybridMapBtn: Button
    private lateinit var terrainMapBtn: Button
    private lateinit var satelliteMapBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        parent_view = findViewById(android.R.id.content)


        // initialize our buttons
        hybridMapBtn = findViewById(R.id.idBtnHybridMap)
        terrainMapBtn = findViewById(R.id.idBtnTerrainMap)
        satelliteMapBtn = findViewById(R.id.idBtnSatelliteMap)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        // adding on click listener for our hybrid map button.
        hybridMapBtn.setOnClickListener(View.OnClickListener { // below line is to change
            // the type of map to hybrid.
            mMap!!.mapType = GoogleMap.MAP_TYPE_HYBRID
        })

        // adding on click listener for our terrain map button.
        terrainMapBtn.setOnClickListener(View.OnClickListener { // below line is to change
            // the type of terrain map.
            mMap!!.mapType = GoogleMap.MAP_TYPE_TERRAIN
        })
        // adding on click listener for our satellite map button.
        satelliteMapBtn.setOnClickListener(View.OnClickListener { // below line is to change the
            // type of satellite map.
            mMap!!.mapType = GoogleMap.MAP_TYPE_SATELLITE
        })
        val locationPermissionRequest = registerForActivityResult(
            RequestMultiplePermissions(),
            ActivityResultCallback<Map<String?, Boolean?>> { result: Map<String?, Boolean?> ->
                val fineLocationGranted = result.getOrDefault(
                    Manifest.permission.ACCESS_FINE_LOCATION, false
                )
                val coarseLocationGranted = result.getOrDefault(
                    Manifest.permission.ACCESS_COARSE_LOCATION, false
                )
                if (fineLocationGranted != null && fineLocationGranted) {
                    // Precise location access granted.
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    // Only approximate location access granted.
                } else {
                    // No location access granted.
                }
            }
        )
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        } else {
            Snackbar.make(parent_view!!, item.title.toString() + " clicked", Snackbar.LENGTH_SHORT)
                .show()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                buildGoogleApiClient()
                mMap!!.isMyLocationEnabled = true
            }
        } else {
            buildGoogleApiClient()
            mMap!!.isMyLocationEnabled = true
        }
    }

    @kotlin.jvm.Synchronized
    private fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API).build()
        mGoogleApiClient?.connect()
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    override fun onConnected(bundle: Bundle?) {
    }

    override fun onConnectionSuspended(i: Int) {}
    override fun onLocationChanged(location: Location) {
        mLastLocation = location
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker!!.remove()
        }
        //Place current location marker
        val latLng = LatLng(location.latitude, location.longitude)
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title("Current Position")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        mCurrLocationMarker = mMap!!.addMarker(markerOptions)

        //move map camera
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap!!.animateCamera(CameraUpdateFactory.zoomTo(11f))

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient,
                this as com.google.android.gms.location.LocationListener
            )
        }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {}

    companion object {
        @kotlin.jvm.JvmStatic
        fun start(context: Context) {
            val intent = Intent(context, MapsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
        }
    }
}