package `in`.antef.geonote.ui.screens.map

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import `in`.antef.geonote.R
import `in`.antef.geonote.domain.model.Coordinate
import `in`.antef.geonote.ui.components.CustomBottomSheet
import `in`.antef.geonote.ui.components.CustomFloatingButton
import `in`.antef.geonote.ui.components.MapListCoordinates
import `in`.antef.geonote.ui.components.MapTopBar
import `in`.antef.geonote.ui.components.MapZoom
import `in`.antef.geonote.ui.viewmodel.LocationViewModel
import `in`.antef.geonote.utils.LocationUtils
import kotlinx.coroutines.launch

@Composable
fun MapComponent(
    viewModel: LocationViewModel,
    onClickCurrentLocation: () -> Unit,
    onMarkerClick: (Coordinate) -> Unit
) {

    val uiState by viewModel.locationState.collectAsState()
    val cameraPositionState = rememberCameraPositionState()
    val scope = rememberCoroutineScope()
    val defaultLocation = LatLng(0.0, 0.0)
    var isCurrentLocationStatus by remember { mutableStateOf(true) }

    // Track selected marker from state
    val selectedCoordinate = uiState.selectedCoordinate

    LaunchedEffect(uiState.location) {
        if (isCurrentLocationStatus) {
            val currentLocation = uiState.location?.let {
                LatLng(it.latitude, it.longitude)
            } ?: defaultLocation

            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLocation, 18f)
            cameraPositionState.animate(cameraUpdate, 1000) // Duration in milliseconds
        }
    }

    // Move camera to selected marker
    LaunchedEffect(selectedCoordinate) {
        selectedCoordinate?.let { coordinate ->
            val markerPosition = LatLng(coordinate.latitude, coordinate.longitude)
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(markerPosition, 18f)
            cameraPositionState.animate(cameraUpdate, 500)
        }
    }

    val context = LocalContext.current
    val hasLocationPermission = remember {
        LocationUtils.hasLocationPermissions(context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        GoogleMap(
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            ),
            onMapLoaded = { }
        ) {
            uiState.coordinates.forEach { coordinate ->
                val isSelected = selectedCoordinate?.id == coordinate.id
                Marker(
                    state = MarkerState(
                        position = LatLng(
                            coordinate.latitude,
                            coordinate.longitude
                        )
                    ),
                    title = coordinate.title,
                    snippet = coordinate.description,
                    icon = if (isSelected) {
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    } else {
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    },
                    onClick = {
                        viewModel.onMarkerSelected(coordinate)
                        onMarkerClick(coordinate)
                        false
                    }
                )
            }
        }


        MapListCoordinates(
            count = uiState.coordinates.size,
            modifier = Modifier
                .padding(start = 12.dp, top = 12.dp)
                .size(50.dp)
                .clickable {
                    scope.launch {
                        isCurrentLocationStatus = false
                        val coordinates = uiState.coordinates
                        if (coordinates.isNotEmpty()) {
                            val builder = com.google.android.gms.maps.model.LatLngBounds.builder()
                            coordinates.forEach { coordinate ->
                                builder.include(LatLng(coordinate.latitude, coordinate.longitude))
                            }
                            val bounds = builder.build()
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngBounds(bounds, 100), // 100px padding
                                1000
                            )
                        }
                    }
                }
                .align(Alignment.TopStart)
        )

        CustomFloatingButton(
            R.drawable.ic_gps,
            modifier = Modifier
                .padding(end = 12.dp, top = 12.dp)
                .size(50.dp)
                .align(Alignment.TopEnd),
            onClick = {
                isCurrentLocationStatus = true
                onClickCurrentLocation()
            }
        )

        CustomFloatingButton(
            R.drawable.ic_add,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .size(50.dp),
            onClick = {
                isCurrentLocationStatus = true
                onClickCurrentLocation()
                if (LocationUtils.hasLocationPermissions(context)) {
                    if (uiState.location != null) {
                        viewModel.addCoordinate(
                            title = "Point ${uiState.coordinateCountId+1}",
                            description = "",
                            projectId = uiState.projectId,
                            location = uiState.location,
                        )
                    } else {
                        Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}
