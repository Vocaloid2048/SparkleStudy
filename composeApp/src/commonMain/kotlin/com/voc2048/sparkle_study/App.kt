package com.voc2048.sparkle_study

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import coil3.compose.setSingletonImageLoaderFactory
import com.voc2048.sparkle_study.ui.screens.LoginScreen
import com.voc2048.sparkle_study.ui.screens.MainScreen
import com.voc2048.sparkle_study.utils.Language
import com.voc2048.sparkle_study.utils.SparkleStudyTheme
import com.voc2048.sparkle_study.utils.UtilsTools

// 全局語言狀態
lateinit var appLanguageState: MutableState<Language.AppLanguage>
lateinit var platformContext: ContextFactory
lateinit var globalDensity: Density

/**
 * 應用程式的入口點 (Door of the App)。
 */
@Composable
fun App(contextFactory: ContextFactory) {
    platformContext = contextFactory
    
    setSingletonImageLoaderFactory { context ->
        UtilsTools.newImageLoader(context)
    }

    if (!::appLanguageState.isInitialized) {
        appLanguageState = remember { mutableStateOf(Language.AppLanguageInstance) }
    }
    
    val direction = if (appLanguageState.value == Language.AppLanguage.ARABIC_HALAL) 
        LayoutDirection.Rtl else LayoutDirection.Ltr

    // 登入狀態模擬
    var isLoggedIn by remember { mutableStateOf(true) }

    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        SparkleStudyTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                if (isLoggedIn) {
                    MainScreen()
                } else {
                    LoginScreen(onLoginSuccess = { isLoggedIn = true })
                }
            }
        }
    }
}
