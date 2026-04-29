package com.interview.app.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Upload : Screen("upload")
    data object InterviewSetup : Screen("interview_setup")
    data object Interview : Screen("interview/{sessionId}/{followUpEnabled}") {
        /**
             * Builds the concrete route string for the Interview screen.
             *
             * @param sessionId The interview session identifier to include in the route.
             * @param followUpEnabled `true` to indicate follow-up is enabled for the session, `false` otherwise.
             * @return The route string formatted as `interview/{sessionId}/{followUpEnabled}` with the provided values.
             */
            fun createRoute(sessionId: String, followUpEnabled: Boolean) =
            "interview/$sessionId/$followUpEnabled"
    }
    data object Completion : Screen("completion/{sessionId}") {
        /**
 * Builds the completion screen route for the provided interview session.
 *
 * @param sessionId The interview session identifier to embed in the route.
 * @return The concrete route string in the form "completion/<sessionId>".
 */
fun createRoute(sessionId: String) = "completion/$sessionId"
    }
}
