package `in`.antef.geonote.ui.screens.map

import android.app.Activity
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavController
import `in`.antef.geonote.ui.components.DataTopBar
import `in`.antef.geonote.ui.components.LocationPermissionDialog
import `in`.antef.geonote.ui.components.LocationSettingsDialog
import `in`.antef.geonote.ui.components.MapTopBar
import `in`.antef.geonote.ui.screens.navigation.Navigation
import `in`.antef.geonote.ui.theme.BG_COLOR
import `in`.antef.geonote.ui.viewmodel.CoordinatesViewModel
import `in`.antef.geonote.ui.viewmodel.LocationViewModel
import `in`.antef.geonote.utils.LocationUtils
import org.koin.androidx.compose.koinViewModel

@Composable
fun GoogleMapScreen(
    navStack: MutableList<Any>,
    projectId: Long,
    viewModel: LocationViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.locationState.collectAsState()
    var showPermissionDialog by remember { mutableStateOf(false) }

    val handleLocationUpdates = {
        if (LocationUtils.hasLocationPermissions(context)) {
            viewModel.checkLocationPermissionsAndStartUpdates(context)
        } else {
            showPermissionDialog = true
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        handleLocationUpdates()
        println("ProjectId:  $projectId")
        viewModel.updateProject(projectId)
        viewModel.getAllCoordinates(projectId)
    }

    //Navigate to coordinates Screen
    LaunchedEffect(key1 = uiState) {
        if (uiState.coordinateId > 0) {
            navStack.add(Navigation.CoordinatesAction(uiState.coordinateId))
            viewModel.resetCoordinateId()
        }
    }

    // Permission handling
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        viewModel.onPermissionResult(permissions.values.all { it })
    }

    // GPS resolution handling
    val resolutionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.startLocationUpdates()
        }
        viewModel.clearGpsResolution()
    }

    // Handle GPS resolution
    uiState.gpsResolution?.let { exception ->
        LaunchedEffect(exception) {
            val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
            resolutionLauncher.launch(intentSenderRequest)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            MapTopBar(
                text = uiState.projectName,
                onBack = { navStack.removeLastOrNull() },
                goToData = { navStack.add(Navigation.DataAction(projectId)) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            MapComponent(
                viewModel = viewModel,
                onClickCurrentLocation = handleLocationUpdates,
                onMarkerClick = { coordinate ->
                    navStack.add(Navigation.CoordinatesAction(coordinate.id))
                }
            )

            if (uiState.isLoading) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // Permission dialog
        if (showPermissionDialog) {
            LocationPermissionDialog(
                onDismiss = { showPermissionDialog = false },
                onConfirm = {
                    showPermissionDialog = false
                    permissionLauncher.launch(LocationUtils.REQUIRED_PERMISSIONS)
                }
            )
        }

        // Settings dialog
        if (uiState.showSettingsDialog) {
            LocationSettingsDialog(
                onDismiss = { viewModel.dismissSettingsDialog() }
            )
        }
    }
}
