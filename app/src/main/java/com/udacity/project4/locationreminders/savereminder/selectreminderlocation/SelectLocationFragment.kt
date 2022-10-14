package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.PermissionUtils
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by sharedViewModel()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_container) as SupportMapFragment
        mapFragment.getMapAsync(this)


        binding.saveButton.setOnClickListener { onLocationSelected() }

        return binding.root
    }

    private fun onLocationSelected() {
        if (_viewModel.selectedPOI.value != null || _viewModel.latitude.value != null) {
            val navController = Navigation.findNavController(binding.root)
            navController.popBackStack()
        } else {
            Toast.makeText(
                requireContext(), "Please choose either a POI or a location",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { isGranted ->
        val requiredPermissionsGranted =
            isGranted[Manifest.permission.ACCESS_FINE_LOCATION] == true
                    || isGranted[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (requiredPermissionsGranted) {
            moveCameraToUserLocation()
        } else {
            PermissionUtils.showSettingsSnackBar(binding.root)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val zoomLevel = 15f
        // Add a marker in Sydney and move the camera
        val startLatLng = LatLng(30.04980068547143, 31.23562083973427)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(startLatLng, zoomLevel))

        setOnMapClickListener(googleMap)
        setOnPOIClickListener(googleMap)
        enableMyLocation()
        setMapStyle(map)
    }

    private fun setOnMapClickListener(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            map.clear()
            val snippet = String.format(
                Locale.getDefault(), "Lat: %1$.5f, Long: %2$.5f", latLng.latitude, latLng.longitude
            )
            map.addMarker(
                MarkerOptions().position(latLng).title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            _viewModel.selectCustomLocation(latLng)
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(), R.raw.map_style
                )
            )
        } catch (e: Resources.NotFoundException) {
            Log.e("SelectLocationFragment", "Can't find style. Error: ", e)
        }
    }

    private fun setOnPOIClickListener(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear()

            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                poi.latLng.latitude,
                poi.latLng.longitude
            )
            map.addMarker(
                MarkerOptions().position(poi.latLng).title(poi.name).snippet(snippet)
            )
            _viewModel.selectPOI(poi)
        }
    }

    private fun enableMyLocation() {
        if (!PermissionUtils.foregroundLocationPermissionApproved(requireContext())) {
            val permissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            requestPermissionLauncher.launch(permissions)
            return
        }
        moveCameraToUserLocation()
    }

    @SuppressLint("MissingPermission")
    fun moveCameraToUserLocation() {
        map.isMyLocationEnabled = true
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val cameraPosition = CameraPosition.Builder()
                        .target(
                            LatLng(
                                location.latitude,
                                location.longitude
                            )
                        )
                        .zoom(15f)
                        .build()
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                }

            }
    }

}
