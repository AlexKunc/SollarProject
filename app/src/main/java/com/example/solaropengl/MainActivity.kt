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
import com.example.solaropengl.news.NewsScreen
import com.example.solaropengl.scene.SceneScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.solaropengl.ui.theme.SolarOpenGLTheme

private object Routes {
    const val NEWS = "news"
    const val SCENE = "scene"
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
                                onInfoClick = { idx ->
                                    navController.navigate("info/$idx")
                                }
                            )
                        }

                        composable(
                            route = "info/{bodyIndex}",
                            arguments = listOf(navArgument("bodyIndex") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val idx = backStackEntry.arguments?.getInt("bodyIndex") ?: 0
                            com.example.solaropengl.info.InfoScreen(
                                bodyIndex = idx,
                                onBack = { navController.popBackStack() }
                            )
                        }

                    }
                }
            }
        }
    }
}
