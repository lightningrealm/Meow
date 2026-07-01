package com.lr.meow.feature.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Login(
    showBottomSheet: Boolean,
    onDismissRequest: () -> Unit = {},
    viewModel: LoginViewModel = koinViewModel()
) {
    if (showBottomSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = {
                // iOS-style subtle drag handle
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(36.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(Color.Gray.copy(alpha = 0.3f))
                )
            }
        ) {
            LoginContent(viewModel, onDismissRequest)
        }
    }
}

@Composable
private fun LoginContent(viewModel: LoginViewModel, onDismissRequest: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loginSuccessEvent.collectLatest {
            Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show()
            onDismissRequest()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.dismissError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 48.dp, top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "Welcome Back",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "验证手机号以同步您的音乐库。",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        // Phone Field
        IosStyleTextField(
            value = uiState.phone,
            onValueChange = { viewModel.updatePhone(it) },
            placeholder = "Phone Number",
            keyboardType = KeyboardType.Phone
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Captcha Field with Get Code Button
        Box(contentAlignment = Alignment.CenterEnd) {
            IosStyleTextField(
                value = uiState.captcha,
                onValueChange = { viewModel.updateCaptcha(it) },
                placeholder = "Verification Code",
                keyboardType = KeyboardType.Number,
                paddingEnd = 100.dp
            )
            
            Text(
                text = if (uiState.countdownSeconds > 0) "${uiState.countdownSeconds}s" else "获取验证码",
                color = if (uiState.countdownSeconds > 0 || uiState.isSendingCode) Color.Gray else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .clickable(
                        enabled = uiState.countdownSeconds == 0 && !uiState.isSendingCode
                    ) {
                        viewModel.sendCaptcha()
                    }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))

        // Login Button
        Button(
            onClick = { viewModel.login() },
            enabled = !uiState.isLoggingIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            if (uiState.isLoggingIn) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Sign In",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Secondary Action
        TextButton(onClick = { /* TODO: Use Guest Account */ }) {
            Text(
                text = "随便看看 (游客登录)",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun IosStyleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    paddingEnd: androidx.compose.ui.unit.Dp = 0.dp
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.Gray) },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}