package com.badaboomi.acustomgpt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.badaboomi.acustomgpt.domain.repository.SettingsRepository
import com.badaboomi.acustomgpt.presentation.navigation.AppNavigation
import com.badaboomi.acustomgpt.presentation.ui.theme.ACustomGPTTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ACustomGPTTheme {
                AppNavigation(settingsRepository = settingsRepository)
            }
        }
    }
}
