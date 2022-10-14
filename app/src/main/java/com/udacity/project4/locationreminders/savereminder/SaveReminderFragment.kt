package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.PermissionUtils
import com.udacity.project4.utils.PermissionUtils.foregroundAndBackgroundLocationPermissionApproved
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val GEOFENCE_RADIUS_IN_METERS = 100f

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by sharedViewModel()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient
    private var currentReminder: ReminderDataItem? = null
    private val geofencePendingIntent by lazy {
        val intent =
            Intent(requireActivity().applicationContext, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        var pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

            PendingIntent.getBroadcast(
                requireContext(),
                0, intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        }

        PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            pendingFlags
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude =
                _viewModel.latitude.value ?: _viewModel.selectedPOI.value?.latLng?.latitude
            val longitude =
                _viewModel.longitude.value ?: _viewModel.selectedPOI.value?.latLng?.longitude

            currentReminder = ReminderDataItem(
                title,
                description,
                location,
                latitude,
                longitude
            )

            if (_viewModel.validateEnteredData(currentReminder!!)) {
                checkPermissionsAndStartGeofencing(
                    currentReminder!!
                )

            }
        }
    }

    private fun checkPermissionsAndStartGeofencing(reminderItem: ReminderDataItem) {
        if (foregroundAndBackgroundLocationPermissionApproved(requireContext())) {
            checkDeviceLocationSettingsAndStartGeofence(reminderItem)
        } else {

            requestForegroundAndBackgroundLocationPermissions()
        }
    }

    private fun requestForegroundAndBackgroundLocationPermissions() {
        var permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (PermissionUtils.runningQOrLater) {
            permissions += Manifest.permission.ACCESS_BACKGROUND_LOCATION
        }
        requestPermissionLauncher.launch(permissions)

    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { isGranted ->
        val requiredPermissionsForQOrLater =
            isGranted[Manifest.permission.ACCESS_FINE_LOCATION] == true
                    && isGranted[Manifest.permission.ACCESS_COARSE_LOCATION] == true && isGranted[Manifest.permission.ACCESS_BACKGROUND_LOCATION] == true

        val requiredPermissionsBeforeQ =
            isGranted[Manifest.permission.ACCESS_FINE_LOCATION] == true
                    && isGranted[Manifest.permission.ACCESS_COARSE_LOCATION] == true && !PermissionUtils.runningQOrLater
        val isPermissionsGranted = requiredPermissionsBeforeQ || requiredPermissionsForQOrLater
        if (!isPermissionsGranted) {
            PermissionUtils.showSettingsSnackBar(binding.root)
        } else {
            currentReminder?.let {
                checkDeviceLocationSettingsAndStartGeofence(it)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            if (resultCode == Activity.RESULT_OK) {
                currentReminder?.let {
                    checkDeviceLocationSettingsAndStartGeofence(it)
                }
                return
            }
            Snackbar.make(
                binding.root,
                R.string.location_required_error, Snackbar.LENGTH_LONG
            ).setAction(android.R.string.ok) {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }.show()
        }
    }

    private fun checkDeviceLocationSettingsAndStartGeofence(
        reminderItem: ReminderDataItem,
        resolve: Boolean = true,
    ) {
        val locationRequest = LocationRequest.create().apply {
            priority = Priority.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        REQUEST_TURN_DEVICE_LOCATION_ON,
                        null,
                        0,
                        0,
                        0,
                        null
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(
                        "SaveReminderFragment",
                        "Error getting location settings resolution: " + sendEx.message
                    )
                }
            }
        }

        locationSettingsResponseTask.addOnSuccessListener {
            addGeofenceForReminder(reminderItem)
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofenceForReminder(reminder: ReminderDataItem) {
        val geofence = Geofence.Builder()
            .setRequestId(reminder.id)
            .setCircularRegion(
                reminder.latitude!!,
                reminder.longitude!!,
                GEOFENCE_RADIUS_IN_METERS
            )
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
            addOnSuccessListener {
                _viewModel.saveReminder(reminder)
                Log.e("Add Geofence", geofence.requestId)
            }
            addOnFailureListener {
                Toast.makeText(
                    requireActivity().applicationContext, R.string.geofences_not_added,
                    Toast.LENGTH_SHORT
                ).show()
                if ((it.message != null)) {
                    Log.w("SaveReminderFragment", it.message ?: "")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "SaveReminderFragment.action.ACTION_GEOFENCE_EVENT"
    }
}

