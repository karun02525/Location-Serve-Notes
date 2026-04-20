package `in`.antef.geonote.ui.screens.camera

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import `in`.antef.geonote.R
import `in`.antef.geonote.ui.viewmodel.FileUploadUiState
import `in`.antef.geonote.ui.viewmodel.FileUploadViewModel
import `in`.antef.geonote.utils.fileOperation
import `in`.antef.geonote.utils.operateVideo
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    coordinateId: Long,
    latitude: Double,
    longitude: Double,
    viewModel: FileUploadViewModel = koinViewModel(),
    onBackClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(key1 = uiState) {
        if (uiState is FileUploadUiState.Success) {
            viewModel.resetState()
           // onBackClick()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        val context = LocalContext.current
        when {
            cameraPermissionState.status.isGranted -> {
                CameraComponent(
                    onPhotoCaptured = { uri ->
                        val (file, filename) = context.fileOperation(uri, latitude, longitude)
                        viewModel.uploadFile(coordinateId, file, filename)
                        onBackClick()
                    },
                    onVideoCaptured = { uri ->
                        val (file, filename) = context.operateVideo(uri)
                        viewModel.uploadFile(coordinateId, file, filename)
                        onBackClick()
                       /* context.operateVideoWithGps(uri, latitude, longitude, onComplete = { (file, filename) ->
                            file?.let { viewModel.uploadFile(coordinateId, it, filename) }
                            onBackClick() // This will be called only after upload
                        })*/
                    },
                    onDismiss = { onBackClick() }
                )
            }

            else -> {
                CameraPermissionRequest(cameraPermissionState)
            }
        }

        if (uiState is FileUploadUiState.Error) {
            ErrorDialog(
                message = (uiState as FileUploadUiState.Error).message,
                onDismiss = { viewModel.resetState() }
            )
        }
       // ProgressDialog(uiState is FileUploadUiState.Loading)
    }
}

@Composable
fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Error") },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun CameraPermissionRequest(permissionState: PermissionState) {
    var showRationale by remember { mutableStateOf(false) }

    LaunchedEffect(permissionState.status) {
        showRationale = permissionState.status.shouldShowRationale
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_camera),
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(60.dp)
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Camera Permission Required",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (showRationale) {
                        "Camera access was previously denied. Please enable camera permission in app settings to capture photos."
                    } else {
                        "GeoNote needs access to your camera to take photos for your location notes."
                    },
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (showRationale) {
                    // If permission was denied before, we can only show info
                    Button(
                        onClick = { permissionState.launchPermissionRequest() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("Request Permission Again")
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* User declined permission */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Deny")
                        }

                        Button(
                            onClick = { permissionState.launchPermissionRequest() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Text("Allow")
                        }
                    }
                }
            }
        }
    }
}

