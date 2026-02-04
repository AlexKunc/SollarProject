package com.example.solaropengl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.solaropengl.info.InfoScreen
import com.example.solaropengl.news.NewsScreen
import com.example.solaropengl.scene.SceneScreen
import com.example.solaropengl.ui.theme.SolarOpenGLTheme

private object Routes {
    const val NEWS = "news"
    const val SCENE = "scene"
    const val INFO = "info"
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SolarOpenGLTheme {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Routes.NEWS,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Routes.NEWS) {
                            NewsScreen(
                                onContinue = { navController.navigate(Routes.SCENE) }
                            )
                        }
                        composable(Routes.SCENE) {
                            SceneScreen(
                                onInfoClick = { navController.navigate(Routes.INFO) }
                            )
                        }
                        composable(Routes.INFO) {
                            InfoScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
