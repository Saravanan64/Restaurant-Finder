package com.cygnus.qwy_asnmt_kotlin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cygnus.qwy_asnmt_kotlin.API.GoogleAPIService
import com.cygnus.qwy_asnmt_kotlin.Common.Common
import com.cygnus.qwy_asnmt_kotlin.Model.MyPlaces
import com.google.android.gms.common.api.ApiException
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
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_maps.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*

class MapsActivity() : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    companion object {
        const val PERMISSION_CODE = 25
        const val SETTING_PERMISSION_CODE = 50
    }

    lateinit var mSerive:GoogleAPIService
    var currentPlaces: MyPlaces?=null

    private lateinit var mMap: GoogleMap
    private var latitude: Double = 0.toDouble()
    private var longitude: Double = 0.toDouble()
    private lateinit var mLastLocation: Location
    private var mMarker: Marker? = null
    private var context:Context?=null
    private var mapFragment:SupportMapFragment?=null
    var locationState:Boolean = false


    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        context = this;

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment!!.getMapAsync(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        mSerive = Common.gooleApiService

        locationState = true
        println("----> getDeviceLocationState: "+locationState)

        getDeviceLocationState()

//        //Request Permission
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (checkLocationPermission()) {
//                buildLocationRequest()
//                buildLocationCallback()
//
//                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
//                fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,Looper.myLooper())
//
//            }
//        }
//        else
//        {
//            buildLocationRequest()
//            buildLocationCallback()
//
//            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
//            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,Looper.myLooper())
//        }



    }


    fun getcurrentLocation()
    {
        //Request Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkLocationPermission()) {
                buildLocationRequest()
                buildLocationCallback()

                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,Looper.myLooper())

            }
        }
        else
        {
            buildLocationRequest()
            buildLocationCallback()

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,Looper.myLooper())
        }
    }

    private fun checkLocationPermission(): Boolean {

        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.ACCESS_FINE_LOCATION))
                ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),PERMISSION_CODE)
            else
                ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),PERMISSION_CODE)
            return false
        }
        else
            return true

    }


    private fun buildLocationRequest() {

        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 10f
    }

    private fun buildLocationCallback() {

        locationCallback = object : LocationCallback(){
            override fun onLocationResult(p0: LocationResult?) {

                mLastLocation = p0!!.locations.get(p0!!.locations.size-1)

                if(mMarker != null)
                {
                    mMarker!!.remove()
                }

                latitude = mLastLocation.latitude
                longitude = mLastLocation.longitude

                val latLng = LatLng(latitude,longitude)
                val markerOptions = MarkerOptions()
                    .position(latLng)
                    .title("Your Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                mMarker = mMap!!.addMarker(markerOptions)

                mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap!!.animateCamera(CameraUpdateFactory.zoomTo(15f))
                nearbyPlace("restaurants")

            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        println("---->requestCode  "+requestCode)

        when (requestCode) {
            MapsActivity.SETTING_PERMISSION_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    println("---->requestCode1")
                    getcurrentLocation()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when(requestCode)
        {
            PERMISSION_CODE ->{
                if(grantResults.size>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        if(checkLocationPermission())
                        {
                            buildLocationRequest()
                            buildLocationCallback()

                          fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                          fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,Looper.myLooper())

                            mMap!!.isMyLocationEnabled = true
                        }
                    }

                }
                else
                {
                    Toast.makeText(this,"Permission Denied",Toast.LENGTH_LONG).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onStop() {

        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                mMap!!.isMyLocationEnabled = true;
            }
        }
        else
            mMap!!.isMyLocationEnabled = true;

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap!!.setOnMarkerClickListener(this)
    }

    private fun nearbyPlace(typeplace:String)
    {
//        mMap.clear()

        var url = getUrl(latitude,longitude,typeplace)

        mSerive.getNearpyPlaces(url)
            .enqueue(object : Callback<MyPlaces> {
                override fun onFailure(call: Call<MyPlaces>, t: Throwable) {

                    println("---->error  "+t!!.message)
                    Toast.makeText(context, "" + t!!.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(call: Call<MyPlaces>, response: Response<MyPlaces>) {
                    currentPlaces = response.body()!!
                    if(response.isSuccessful)
                    {
                        println("---->sucess "+response!!.body()!!.error_message)
                        println("---->sucess "+response!!.body()!!.status)

                        for (i in 0 until response!!.body()!!.results!!.take(10).size)
                        {
                            val markerOptions = MarkerOptions()
                            val googlePlaces = response.body()!!.results!![i]
                            val lat = googlePlaces.geometry!!.location!!.lat
                            val lng = googlePlaces.geometry!!.location!!.lng
                            val placename = googlePlaces.name
                            val latlng = LatLng(lat,lng)

                            println("---->"+googlePlaces.photos!![0])

                            markerOptions.position(latlng)
                            markerOptions.title(placename)
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                            markerOptions.snippet(i.toString())

                            mMap!!.addMarker(markerOptions)
                            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latlng))
                            mMap!!.animateCamera(CameraUpdateFactory.zoomTo(15f))
                        }
                    }
                }
            })
    }

    private fun getUrl(latitude: Double, longitude: Double, typeplace: String): String {

        val goolePlaceUrl = StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
        goolePlaceUrl.append("?location=$latitude,$longitude")
        goolePlaceUrl.append("&radious=10000")
        goolePlaceUrl.append("&type=$typeplace")
        goolePlaceUrl.append("&key=AIzaSyC_Oi07kd8kiMTn-znnHXE6mN5hcS9rHbc")

        println("----> URL: "+goolePlaceUrl.toString())

        return  goolePlaceUrl.toString()
    }

    private fun getAddressFromLocation(latLng: LatLng):String
    {
        var address = "No known address"
        val gcd = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>
        try {
            addresses = gcd.getFromLocation(latLng!!.latitude, latLng.longitude, 1)
            if (addresses.isNotEmpty()) {
                address = addresses[0].getAddressLine(0)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return address;
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onMarkerClick(p0: Marker?): Boolean {

        println("---->"+p0!!.title)
        Toast.makeText(context,""+getAddressFromLocation(p0.position),Toast.LENGTH_LONG).show()
//        Common.currentResult = currentPlaces!!.results!![Integer.parseInt(p0.snippet)]
//        println("---->" + Common.currentResult!!.name.toString())

        if (currentPlaces!=null && currentPlaces!!.results!!.size>0)
        {
            println("---->"+currentPlaces!!.results!![0].opening_hours)
        }

        showCustomPopup(p0.title,getAddressFromLocation(p0.position),"")

        return true
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun showCustomPopup(name:String,address: String,imgURL:String)
    {
        // Initialize a new layout inflater instance
        val inflater:LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // Inflate a custom view using layout inflater
        val view = inflater.inflate(R.layout.view_place_info,null)

        // Initialize a new instance of popup window
        val popupWindow = PopupWindow(
            view, // Custom view to show in popup window
            LinearLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
            LinearLayout.LayoutParams.WRAP_CONTENT // Window height
        )

        // Set an elevation for the popup window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.elevation = 10.0F
        }


        // If API level 23 or higher then execute the code
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // Create a new slide animation for popup window enter transition
            val slideIn = Slide()
            slideIn.slideEdge = Gravity.TOP
            popupWindow.enterTransition = slideIn

            // Slide animation for popup window exit transition
            val slideOut = Slide()
            slideOut.slideEdge = Gravity.RIGHT
            popupWindow.exitTransition = slideOut

        }

        // Get the widgets reference from custom view
        val place_name = view.findViewById<TextView>(R.id.place_name)
        val place_addr = view.findViewById<TextView>(R.id.place_address)
        val photo = view.findViewById<ImageView>(R.id.photo)
        val close_btn = view.findViewById<Button>(R.id.btn_close)

        place_name.text = name
        place_addr.text = address

        if(imgURL.length>0)
        {
            Picasso.get().load(imgURL).into(photo)
        }

        // Set a click listener for popup's button widget
        close_btn.setOnClickListener{
            // Dismiss the popup window
            popupWindow.dismiss()
        }

        // Set a dismiss listener for popup window
        popupWindow.setOnDismissListener {
//            Toast.makeText(applicationContext,"Popup closed",Toast.LENGTH_SHORT).show()
        }


        // Finally, show the popup window on app
        TransitionManager.beginDelayedTransition(root_layout)
        popupWindow.showAtLocation(
            root_layout, // Location to display popup window
            Gravity.CENTER, // Exact position of layout to display popup
            0, // X offset
            0 // Y offset
        )
    }

    private fun getDeviceLocationState():Boolean {

        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = (10 * 1000).toLong()
        locationRequest.fastestInterval = 2000

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        val locationSettingsRequest = builder.build()

        val result = LocationServices.getSettingsClient(this)
            .checkLocationSettings(locationSettingsRequest)
        result.addOnCompleteListener { task ->
            try {
                val response = task.getResult(ApiException::class.java)
                if (response!!.locationSettingsStates.isLocationPresent){

                    println("----> Location status: yes")
                    getcurrentLocation()
                }
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolvable = exception as ResolvableApiException
                        resolvable.startResolutionForResult(this, SETTING_PERMISSION_CODE)
                    } catch (e: IntentSender.SendIntentException) {
                    } catch (e: ClassCastException) {
                    }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> { }
                }
            }
        }
        return locationState

    }


}