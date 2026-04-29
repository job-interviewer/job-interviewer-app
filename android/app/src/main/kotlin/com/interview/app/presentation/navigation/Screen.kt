package com.interview.app.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Upload : Screen("upload")
    data object InterviewSetup : Screen("interview_setup")
    data object Interview : Screen("interview/{sessionId}/{followUpEnabled}") {
        fun createRoute(sessionId: String, followUpEnabled: Boolean) =
            "interview/$sessionId/$followUpEnabled"
    }
    data object Completion : Screen("completion/{sessionId}") {
        fun createRoute(sessionId: String) = "completion/$sessionId"
    }
}
