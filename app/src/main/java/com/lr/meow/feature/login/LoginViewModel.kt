package com.lr.meow.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lr.core.network.api.MeowAuthService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val phone: String = "",
    val captcha: String = "",
    val isSendingCode: Boolean = false,
    val isLoggingIn: Boolean = false,
    val countdownSeconds: Int = 0,
    val errorMessage: String? = null
)

class LoginViewModel(
    private val authService: MeowAuthService
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _loginSuccessEvent = MutableSharedFlow<Unit>()
    val loginSuccessEvent: SharedFlow<Unit> = _loginSuccessEvent.asSharedFlow()

    fun updatePhone(phone: String) {
        _uiState.update { it.copy(phone = phone) }
    }

    fun updateCaptcha(captcha: String) {
        _uiState.update { it.copy(captcha = captcha) }
    }
    
    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun sendCaptcha() {
        val phone = _uiState.value.phone
        if (phone.isBlank()) {
            _uiState.update { it.copy(errorMessage = "请输入手机号") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSendingCode = true, errorMessage = null) }
            try {
                val response = authService.sendCaptcha(phone = phone)
                if (response.code == 200) {
                    startCountdown()
                } else {
                    _uiState.update { it.copy(errorMessage = response.message ?: "发送验证码失败") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "网络异常") }
            } finally {
                _uiState.update { it.copy(isSendingCode = false) }
            }
        }
    }

    private fun startCountdown() {
        viewModelScope.launch {
            _uiState.update { it.copy(countdownSeconds = 60) }
            while (_uiState.value.countdownSeconds > 0) {
                delay(1000)
                _uiState.update { it.copy(countdownSeconds = it.countdownSeconds - 1) }
            }
        }
    }

    fun login() {
        val phone = _uiState.value.phone
        val captcha = _uiState.value.captcha

        if (phone.isBlank() || captcha.isBlank()) {
            _uiState.update { it.copy(errorMessage = "请输入手机号和验证码") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingIn = true, errorMessage = null) }
            try {
                val response = authService.loginWithCaptcha(phone = phone, captcha = captcha)
                if (response.code == 200) {
                    // 登录成功，触发 UI 事件关闭弹窗
                    _loginSuccessEvent.emit(Unit)
                } else {
                    _uiState.update { it.copy(errorMessage = "登录失败: Code ${response.code}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "网络异常") }
            } finally {
                _uiState.update { it.copy(isLoggingIn = false) }
            }
        }
    }
}
