package `in`.antef.geonote.ui.screens

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import `in`.antef.geonote.ui.screens.navigation.Navigation
import `in`.antef.geonote.ui.screens.splash.SplashScreen
import `in`.antef.geonote.ui.theme.GeoNoteTheme
import org.koin.compose.KoinContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Set the status bar color to white
        window.statusBarColor = Color.WHITE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        }
        setContent {
            AppContent()
        }
    }
}

@Composable
fun AppContent() {
    var isComplete by remember { mutableStateOf(false) }
    GeoNoteTheme {
        if (!isComplete) {
            SplashScreen {
                isComplete = true
            }
        } else {
            Navigation()
        }
    }
}