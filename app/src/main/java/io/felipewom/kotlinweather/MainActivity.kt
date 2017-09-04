package io.felipewom.kotlinweather

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import common.Common
import common.Helper
import kotlinx.android.synthetic.main.activity_main.*
import model.OpenWeatherMap


class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient
.OnConnectionFailedListener, LocationListener {

    // Const
    val PERMISSION_REQUEST_CODE = 1001
    val PLAY_SERVICES_RESOLUTION_REQUEST = 1000

    // Variables
    var mGoogleApiClient: GoogleApiClient? = null
    var mLocationRequest: LocationRequest? = null
    internal var mOpenWeatherMap = OpenWeatherMap()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermission()
        if (checkPlayServices())
            buildGoogleApiClient()
    }

    override fun onStart() {
        super.onStart()
        if (mGoogleApiClient != null)
            mGoogleApiClient!!.connect()
    }

    override fun onResume() {
        super.onResume()
        checkPlayServices()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                .ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                        .ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_REQUEST_CODE)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices())
                        buildGoogleApiClient()
                }
            }
        }
    }

    private fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build()
    }

    private fun checkPlayServices(): Boolean {
        val googleAPI = GoogleApiAvailability.getInstance()
        val result = googleAPI.isGooglePlayServicesAvailable(this)
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show()
            } else {
                Toast.makeText(applicationContext, "This device is not supported.", Toast
                        .LENGTH_LONG).show()
            }

            return false
        }
        return true
    }

    override fun onConnected(p0: Bundle?) {
        createLocationRequest()
    }

    @SuppressLint("MissingPermission")
    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 10000 // 10 sec
        mLocationRequest!!.fastestInterval = 5000 // 5 sec
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                .ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                        .ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this)
    }

    private var lat: Double? = null

    private var lon: Double? = null

    override fun onLocationChanged(location: Location?) {
        if (lat != location!!.latitude ||
                lon != location.longitude) {
            lat = location.latitude
            lon = location.longitude
            GetWeather().execute(Common.apiRequestWeather(lat.toString(), lon.toString()))
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        mGoogleApiClient!!.connect()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.i("ERROR", "Connection failed: " + p0.errorCode)
    }

    override fun onDestroy() {
        mGoogleApiClient!!.disconnect()
        super.onDestroy()
    }

    private inner class GetWeather : AsyncTask<String, Void, String>() {
        internal var pd = ProgressDialog(this@MainActivity)

        override fun onPreExecute() {
            super.onPreExecute()
            pd.setTitle("Loading weather...")
            pd.show()
        }

        override fun doInBackground(vararg params: String?): String {
            var stream: String
            var urlString = params[0]
            val http = Helper()
            stream = http.getHttpData(urlString!!)!!
            return stream
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result!!.contains("Error: Not found city")) {
                pd.dismiss()
                return
            }
            val gson = Gson()
            val mType = object : TypeToken<model.OpenWeatherMap>() {}.type
            mOpenWeatherMap = gson.fromJson<OpenWeatherMap>(result, mType)
            pd.dismiss()

            txtCity.text = "${mOpenWeatherMap.name}, ${mOpenWeatherMap.sys!!.country}"
            txtLastUpdate.text = "${Common.dateNow}"
            txtDescription.text = "${mOpenWeatherMap.weather!![0]!!.description}"
            txtHumidity.text = "${mOpenWeatherMap.main!!.humidity} %"
            txtTime.text = "${Common.unixTimeStampToDateTime(mOpenWeatherMap!!.sys!!.sunrise
                    .toDouble())} / ${Common.unixTimeStampToDateTime(mOpenWeatherMap.sys!!.sunset
                    .toDouble())}"
            txtCelsius.text = "${mOpenWeatherMap.main!!.temp} °C"
            Picasso.with(this@MainActivity)
                    .load(Common.getImage(mOpenWeatherMap.weather!![0]!!.icon!!))
                    .into(imageView)
        }
    }
}
