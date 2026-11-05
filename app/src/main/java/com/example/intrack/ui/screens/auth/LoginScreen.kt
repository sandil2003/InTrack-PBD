package com.example.intrack.ui.screens.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.intrack.viewmodel.AuthViewModel

/**
 * LoginScreen backwards-compatible wrapper delegating to Figma-accurate SignInScreen
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    SignInScreen(
        onLoginSuccess = onLoginSuccess,
        onNavigateToSignUp = onNavigateToSignUp,
        onBackClick = null,
        viewModel = viewModel,
        modifier = modifier
    )
}
