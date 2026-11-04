package com.example.intrack

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.intrack.ui.navigation.FinFlowNavGraph
import com.example.intrack.ui.theme.AppThemeMode
import com.example.intrack.ui.theme.InTrackTheme
import com.example.intrack.widget.InTrackGlanceWidget
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var pendingNavRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleWidgetIntent(intent)
        enableEdgeToEdge()
        setContent {
            val systemInDark = isSystemInDarkTheme()
            var themeMode by remember { mutableStateOf(AppThemeMode.LIGHT) }

            val isDark = when (themeMode) {
                AppThemeMode.SYSTEM -> systemInDark
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            InTrackTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FinFlowNavGraph(
                        themeMode = themeMode,
                        onThemeModeChange = { themeMode = it },
                        pendingNavRoute = pendingNavRoute,
                        onClearPendingNavRoute = { pendingNavRoute = null },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                InTrackGlanceWidget.updateWidget(applicationContext)
            } catch (_: Exception) {}
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleWidgetIntent(intent)
    }

    private fun handleWidgetIntent(intent: Intent?) {
        val route = intent?.getStringExtra("NAV_ROUTE")
        val category = intent?.getStringExtra("PRESET_CATEGORY")
        if (route != null) {
            pendingNavRoute = if (!category.isNullOrEmpty()) {
                "add_expense?category=$category"
            } else {
                route
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainAppScreenPreview() {
    InTrackTheme {
        FinFlowNavGraph()
    }
}
