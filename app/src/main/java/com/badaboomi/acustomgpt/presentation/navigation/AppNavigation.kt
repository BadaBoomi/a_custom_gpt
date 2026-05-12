package com.badaboomi.acustomgpt.presentation.navigation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.badaboomi.acustomgpt.domain.repository.SettingsRepository
import com.badaboomi.acustomgpt.presentation.ui.screen.ChatListScreen
import com.badaboomi.acustomgpt.presentation.ui.screen.ConversationScreen
import com.badaboomi.acustomgpt.presentation.ui.screen.RoomListScreen
import com.badaboomi.acustomgpt.presentation.ui.screen.SettingsScreen
import com.badaboomi.acustomgpt.presentation.ui.screen.SetupScreen
import com.badaboomi.acustomgpt.presentation.viewmodel.SetupViewModel

sealed class Screen(val route: String) {
    object Setup : Screen("setup")
    object RoomList : Screen("room_list")
    object ChatList : Screen("chat_list/{roomId}") {
        fun createRoute(roomId: String) = "chat_list/$roomId"
    }
    object Conversation : Screen("conversation/{chatId}") {
        fun createRoute(chatId: String) = "conversation/$chatId"
    }
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation(settingsRepository: SettingsRepository) {
    val navController = rememberNavController()
    val startDestination = if (settingsRepository.isSetupComplete()) Screen.RoomList.route else Screen.Setup.route

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Setup.route) {
            val viewModel: SetupViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()
            if (state.isSetupComplete) {
                navController.navigate(Screen.RoomList.route) {
                    popUpTo(Screen.Setup.route) { inclusive = true }
                }
            }
            SetupScreen(
                uiState = state,
                onApiKeyChange = viewModel::onApiKeyChange,
                onPromptIdChange = viewModel::onPromptIdChange,
                onVectorStoreIdsChange = viewModel::onVectorStoreIdsChange,
                onSave = viewModel::onSave
            )
        }

        composable(Screen.RoomList.route) {
            RoomListScreen(
                onRoomClick = { room ->
                    navController.navigate(Screen.ChatList.createRoute(room.id))
                },
                onSettingsClick = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(
            route = Screen.ChatList.route,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) {
            ChatListScreen(
                onChatClick = { chat ->
                    navController.navigate(Screen.Conversation.createRoute(chat.id))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Conversation.route,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) {
            ConversationScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
