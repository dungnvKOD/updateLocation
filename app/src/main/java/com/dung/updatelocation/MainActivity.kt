package com.dung.updatelocation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    
    /**
     *  rat  la ok
     *  cap ngat vi tri hien lien cu sau 10s la update 1 lan
     *
     */


    companion object {
        const val TAG = "MainActivity"

        /**
         *  constan request code get location permission
         */
        const val REQEST_CODE_LOCATION = 100
        const val REQUEST_SETTING = 200 //request code bat len set ting location

    }

    /**
     *  lang nghe cuoc goi lai de lay vi tri
     */

    private lateinit var loccationCallback: LocationCallback

    /**
     *  Luu cac tham so cac  ywu cau toi FusedLoctionProviderApi
     */
    private lateinit var mLocationRequest: LocationRequest

    /**
     *  cung cap quyen truy cap vao API nha cung cap vi tri hop nhat
     */
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    /**
     *  cung cap quyen truy cap cai dat vao API
     */

    /**
     *  Luu chu cac laoi dich vu ma khach  hnag quan tam den.dc su dung de
     *  kiem tra cai dat xac dinh xem thiet bi co cai dat hay khong
     */
    private lateinit var mLocationSettingsRequest: LocationSettingsRequest

    private lateinit var mSettingClient: SettingsClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    private fun init() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        mSettingClient = LocationServices.getSettingsClient(this)
        Log.e(TAG, "init...")


        createLocationRequset()
//        startLocationUpdate()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdate() {

        buildLocatonSettingRequest()
        Log.e(TAG, "startLocationUpdate...")

        /**
         *  kiem tra xem location da dc bat hay chu vao neu chuua dc bat la do ly do nao
         */
        mSettingClient.checkLocationSettings(mLocationSettingsRequest)
            .addOnSuccessListener(
                this
            ) {
                Toast.makeText(this, "tat ca da dc bat ....", Toast.LENGTH_LONG).show()
                Log.e(TAG, "Success...")
                locationCallback()
            }
            .addOnFailureListener { exception: Exception ->
                val statusCodes = (exception as ApiException).statusCode

                when (statusCodes) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        Log.d(TAG, "Cài đặt vị trí không hài lòng. Cố gắng nâng cấp cài đặt vị trí")
                        val rae: ResolvableApiException = exception as ResolvableApiException
                        rae.startResolutionForResult(this, REQUEST_SETTING)

                    }
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SETTING && resultCode == Activity.RESULT_OK) {
            startLocationUpdate()
        } else {
            finish()
        }

    }

    @SuppressLint("MissingPermission")
    private fun locationCallback() {
        Log.e(TAG, "locationCallback...")

        loccationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                Log.e(TAG, "ok...")

                txtLocation.text = "${locationResult!!.lastLocation}"

            }
        }

        mFusedLocationProviderClient.requestLocationUpdates(
            mLocationRequest,
            loccationCallback, Looper.myLooper()
        )

    }

    /**
     *  xay dung thang LocationSettingResquest  de kiem tra dcac dich vu cua nguoi dung
     */

    private fun buildLocatonSettingRequest() {
        Log.e(TAG, "buildLocatonSettingRequest...")

        val build: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
        build.addLocationRequest(mLocationRequest)
        mLocationSettingsRequest = build.build()

    }

    /**
     * LocationRequset luu cac tham so request toi FusedLocationProviderApi
     */

    private fun createLocationRequset() {

        Log.e(TAG, "createLocationRequset...")
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = 10000
        mLocationRequest.fastestInterval = 5000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    fun checkPermisstionLocations(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissionLocation() {
        val permissionList: Array<String> = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        ActivityCompat.requestPermissions(this, permissionList, REQEST_CODE_LOCATION)
    }

    /**
     * ok...............
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Toast.makeText(this, "ok...", Toast.LENGTH_LONG).show()

        if (grantResults.isEmpty()) {
            Toast.makeText(this, "kho co quyen dc cap", Toast.LENGTH_LONG).show()
            return
        }

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "quyen dc cap", Toast.LENGTH_LONG).show()
            startLocationUpdate()

        } else {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        if (checkPermisstionLocations()) {
            startLocationUpdate()
        } else {
            requestPermissionLocation()
        }
    }

}
