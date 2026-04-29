package com.interview.app.presentation.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.interview.app.presentation.screen.CompletionScreen
import com.interview.app.presentation.screen.HomeScreen
import com.interview.app.presentation.screen.InterviewScreen
import com.interview.app.presentation.screen.InterviewSetupScreen
import com.interview.app.presentation.screen.UploadScreen
import com.interview.app.presentation.viewmodel.InputMode

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    var uploadText by rememberSaveable { mutableStateOf<String?>(null) }
    var uploadUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var uploadMode by rememberSaveable { mutableStateOf(InputMode.TEXT) }

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(onStartClick = { navController.navigate(Screen.Upload.route) })
        }
        composable(Screen.Upload.route) {
            UploadScreen(onProceed = { text, uri, mode ->
                uploadText = text
                uploadUri = uri
                uploadMode = mode
                navController.navigate(Screen.InterviewSetup.route)
            })
        }
        composable(Screen.InterviewSetup.route) {
            InterviewSetupScreen(
                coverLetterText = uploadText,
                coverLetterUri = uploadUri,
                inputMode = uploadMode,
                onInterviewStarted = { sessionId, followUpEnabled ->
                    navController.navigate(Screen.Interview.createRoute(sessionId, followUpEnabled))
                }
            )
        }
        composable(
            route = Screen.Interview.route,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.StringType },
                navArgument("followUpEnabled") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val followUpEnabled = backStackEntry.arguments?.getBoolean("followUpEnabled") ?: true
            InterviewScreen(
                sessionId = sessionId,
                questions = emptyList(),
                jobField = "",
                followUpEnabled = followUpEnabled,
                onComplete = { sid ->
                    navController.navigate(Screen.Completion.createRoute(sid)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }
        composable(
            route = Screen.Completion.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            CompletionScreen(
                sessionId = sessionId,
                onRestart = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
