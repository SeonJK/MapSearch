package com.example.mymap

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mymap.databinding.ActivityMapBinding
import com.example.mymap.model.Place
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView

class MapActivity : AppCompatActivity(),
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {

    private val TAG: String = "로그"

    companion object {
        private const val REQUEST_CODE = 101
        private const val REQUEST_CHECK_SETTINGS = 103
    }

    private var isLastLocationServed = false
    private var requestingLocationUpdates = false

    private lateinit var binding: ActivityMapBinding
    private lateinit var mapView: MapView
    private lateinit var locationManager: LocationManager
    private lateinit var itemMapPoint: MapPoint
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var googleApiClient: GoogleApiClient

    private val requiredPermission = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 권한 확인
//        checkLocationPermission()

        initLocationInstances()
        initMapView()
        initFloatingActionButton()
    }

    override fun onResume() {
        super.onResume()
        if (requestingLocationUpdates) startLocationUpdates()
    }

    // 권한요청 메소드
    private fun checkLocationPermission() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            TedPermission.create()
                .setPermissionListener(permissionListener)
                .setDeniedMessage("권한 거절")
                .setPermissions(Manifest.permission.READ_CONTACTS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                .check()
        }
    }

    // onResume 시에 실행되는 메소드
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationRequest = LocationRequest.create().apply {
            interval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun initLocationInstances() {
        googleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build()

        // fusedLocation 객체 생성
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                for (location in locationResult.locations) {
                    val model = Place(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        buildingName = "현재 위치",
                        address = "null",
                        roadAddress = "null"
                    )
                    setPin(model)
                    break
                }
            }
        }
    }

    private fun initMapView() {
        // 지도 레이아웃 생성
        mapView = MapView(this)
        binding.mapView.addView(mapView)

        // 인텐트로 넘어온 place데이터 변수 저장
        val model = intent.getParcelableExtra<Place>("place")
        setPin(model)
    }

    private fun setPin(model: Place?) {
        itemMapPoint = MapPoint.mapPointWithGeoCoord(model!!.latitude, model.longitude)
        // 지도의 중심점 변경 + 줌 레벨 변경
        mapView.setMapCenterPointAndZoomLevel(itemMapPoint, 2, true)

        // 지도에 마커 찍기
        val marker = MapPOIItem().apply {
            itemName = model.buildingName
            tag = 0
            mapPoint = itemMapPoint
            markerType = MapPOIItem.MarkerType.BluePin
            selectedMarkerType = MapPOIItem.MarkerType.BluePin
        }
        mapView.addPOIItem(marker)
    }

    private fun initFloatingActionButton() {
        binding.currenLocationFAB.setOnClickListener {

            checkLocationServiceStatus()

            if (!isLastLocationServed) {
                requestLastLocation()
            } else {
                createLocationRequest()
            }
        }
    }

    private fun checkLocationServiceStatus(): Boolean {
        // locationManager가 초기화되어있지 않으면
        if (::locationManager.isInitialized.not()) {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            Log.d(TAG, "MapActivity - checkLocationServiceStatus() called")
        }

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun requestLastLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener {
                googleApiClient.connect()
            }

        requestingLocationUpdates = true
        isLastLocationServed = true
        Log.d(TAG,
            "MapActivity - requestLastLocation() called :: requestingLocationUpdates=${requestingLocationUpdates}, isLastLocationServed=${isLastLocationServed}")
    }

    private fun createLocationRequest() {
        googleApiClient.connect()

        // 위치 요청 설정
        locationRequest = LocationRequest.create().apply {
            interval = 10000            // 위치 업데이트 수신 간격 (ms)
            fastestInterval = 5000      // 가장 빠른 위치 업데이트 처리 간격 (ms)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            // ACCESS_FINE_LOCATION 권한 + 빠른 업데이트 간격 5000 + 우선순위 HIGH_ACCURACY 조합
            // => 정확한 위치 반환, 실시간으로 위치 표시하는 앱에 적합
        }

        // 위치 설정을 위한 빌더
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            requestPermissions(requiredPermission, REQUEST_CODE)
            Log.d(TAG, "MapActivity - createLocationRequest() called")
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // 위치 정보 제공자가 사용 가능 상태가 되었을 때
    // 한번만 호출됨
    @SuppressLint("MissingPermission")
    override fun onConnected(p0: Bundle?) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    Log.d(TAG,
                        "MapActivity - onConnected() called :: 위도=${it.latitude} 경도=${it.longitude}")

                    val model = Place(
                        latitude = it.latitude,
                        longitude = it.longitude,
                        buildingName = "현재 위치",
                        address = "null",
                        roadAddress = "null"
                    )
                    setPin(model)
                }
            }
    }

    // 함수와 사용 불가능 상태가 되었을 때
    override fun onConnectionSuspended(p0: Int) {
        TODO("Not yet implemented")
    }

    // 위치 정보 제공자를 얻지 못할 때
    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("Not yet implemented")
    }

    private val permissionListener = object : PermissionListener {
        override fun onPermissionGranted() {
            Toast.makeText(applicationContext, "권한 허가됨", Toast.LENGTH_SHORT).show()
        }

        override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
            Toast.makeText(applicationContext, "권한 거부됨", Toast.LENGTH_SHORT).show()
        }
    }
}