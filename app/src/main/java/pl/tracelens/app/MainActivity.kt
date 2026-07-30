package pl.tracelens.app

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.view.WindowCompat
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.delay
import pl.tracelens.app.ui.TraceLensApp

class MainActivity : AppCompatActivity() {
    private var incomingIntent by mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        incomingIntent = intent.takeIf { it.action != Intent.ACTION_MAIN }
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            val app = application as TraceLensApplication
            val viewModel: AppViewModel = viewModel(factory = AppViewModelFactory(app.repository))
            var showSplash by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                delay(1_100)
                showSplash = false
            }
            LaunchedEffect(showSplash) {
                val bars = WindowCompat.getInsetsController(window, window.decorView)
                bars.isAppearanceLightStatusBars = !showSplash
                bars.isAppearanceLightNavigationBars = !showSplash
                window.navigationBarColor = if (showSplash) {
                    android.graphics.Color.rgb(33, 21, 48)
                } else {
                    android.graphics.Color.WHITE
                }
            }
            Crossfade(targetState = showSplash, label = "app-launch") { splashVisible ->
                if (splashVisible) {
                    Box(
                        Modifier.fillMaxSize().background(Color(0xFF211530)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.splash_portrait),
                            contentDescription = stringResource(R.string.splash_description),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 44.dp),
                            color = Color(0xFFDFD0FF),
                            trackColor = Color(0x44704DB7),
                        )
                    }
                } else {
                    TraceLensApp(
                        viewModel = viewModel,
                        incomingIntent = incomingIntent,
                        onIncomingIntentConsumed = { incomingIntent = null },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        incomingIntent = intent
    }
}
