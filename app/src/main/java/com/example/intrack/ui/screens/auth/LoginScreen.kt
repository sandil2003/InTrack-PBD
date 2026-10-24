package com.example.intrack.ui.screens.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.intrack.viewmodel.AuthViewModel

/**
 * LoginScreen backwards-compatible wrapper delegating to Figma-accurate SignInScreen
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit = {},
    viewModel: AuthViewModel = viewModel(),
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
