package com.lr.meow.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lr.core.network.api.MeowAuthService
import com.lr.meow.R
import com.lr.meow.ui.common.util.UiText
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LoginMode {
    PHONE, QR_CODE
}

data class LoginUiState(
    val loginMode: LoginMode = LoginMode.PHONE,
    val phone: String = "",
    val captcha: String = "",
    val isSendingCode: Boolean = false,
    val isLoggingIn: Boolean = false,
    val countdownSeconds: Int = 0,
    val errorMessage: UiText? = null,
    
    // QR Code state
    val qrImgBase64: String? = null,
    val qrStatus: Int = 0,
    val qrStatusMessage: UiText? = null,
    val qrKey: String? = null
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
            _uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.phone_empty_error)) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSendingCode = true, errorMessage = null) }
            try {
                val response = authService.sendCaptcha(phone = phone)
                if (response.code == 200) {
                    startCountdown()
                } else {
                    _uiState.update { it.copy(errorMessage = response.message?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.send_captcha_failed)) }
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                var displayMsg: UiText = UiText.StringResource(R.string.send_captcha_failed)
                if (!errorBody.isNullOrBlank()) {
                    try {
                        val json = org.json.JSONObject(errorBody)
                        val msg = if (json.has("message")) json.getString("message") else if (json.has("msg")) json.getString("msg") else null
                        if (msg != null) {
                            displayMsg = UiText.DynamicString(msg)
                        }
                    } catch (ignored: Exception) {}
                }
                _uiState.update { it.copy(errorMessage = displayMsg) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.network_error)) }
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
            _uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.phone_captcha_empty_error)) }
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
                    _uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.login_failed_code, response.code)) }
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                var displayMsg: UiText = UiText.StringResource(R.string.login_failed)
                if (!errorBody.isNullOrBlank()) {
                    try {
                        val json = org.json.JSONObject(errorBody)
                        val msg = if (json.has("message")) json.getString("message") else if (json.has("msg")) json.getString("msg") else null
                        if (msg != null) {
                            displayMsg = UiText.DynamicString(msg)
                        }
                    } catch (ignored: Exception) {}
                }
                _uiState.update { it.copy(errorMessage = displayMsg) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.network_error)) }
            } finally {
                _uiState.update { it.copy(isLoggingIn = false) }
            }
        }
    }

    fun setLoginMode(mode: LoginMode) {
        _uiState.update { it.copy(loginMode = mode, errorMessage = null) }
        if (mode == LoginMode.QR_CODE) {
            refreshQrCode()
        } else {
            qrPollingJob?.cancel()
        }
    }

    private var qrPollingJob: kotlinx.coroutines.Job? = null

    fun refreshQrCode() {
        qrPollingJob?.cancel()
        viewModelScope.launch {
            _uiState.update { it.copy(qrStatusMessage = UiText.DynamicString("正在加载二维码..."), qrImgBase64 = null) }
            try {
                val keyResponse = authService.getQrKey()
                val key = keyResponse.data?.unikey
                if (key.isNullOrBlank()) {
                    _uiState.update { it.copy(qrStatusMessage = UiText.DynamicString("获取二维码失败")) }
                    return@launch
                }
                
                val createResponse = authService.createQrCode(key)
                val base64 = createResponse.data?.qrimg
                if (base64.isNullOrBlank()) {
                    _uiState.update { it.copy(qrStatusMessage = UiText.DynamicString("生成二维码失败")) }
                    return@launch
                }
                
                _uiState.update { it.copy(qrKey = key, qrImgBase64 = base64, qrStatusMessage = UiText.DynamicString("请使用网易云音乐App扫码登录")) }
                startQrPolling(key)
            } catch (e: Exception) {
                _uiState.update { it.copy(qrStatusMessage = UiText.DynamicString(e.message ?: "网络异常")) }
            }
        }
    }

    private fun startQrPolling(key: String) {
        qrPollingJob = viewModelScope.launch {
            while(true) {
                try {
                    val res = authService.checkQrStatus(key, noCookie = true)
                    when (res.code) {
                        800 -> {
                            _uiState.update { it.copy(qrStatus = 800, qrStatusMessage = UiText.DynamicString("二维码已过期，请刷新")) }
                            break
                        }
                        801 -> {
                            _uiState.update { it.copy(qrStatus = 801, qrStatusMessage = UiText.DynamicString("等待扫码")) }
                        }
                        802 -> {
                            _uiState.update { it.copy(qrStatus = 802, qrStatusMessage = UiText.DynamicString("待确认")) }
                        }
                        803 -> {
                            _uiState.update { it.copy(qrStatus = 803, qrStatusMessage = UiText.DynamicString("授权登录成功")) }
                            _loginSuccessEvent.emit(Unit)
                            break
                        }
                    }
                } catch (e: Exception) {
                    // Ignore transient network exceptions during polling
                }
                delay(3000)
            }
        }
    }

    fun anonymousLogin() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingIn = true, errorMessage = null) }
            try {
                val res = authService.anonymousLogin()
                if (res.code == 200) {
                    _loginSuccessEvent.emit(Unit)
                } else {
                    _uiState.update { it.copy(errorMessage = res.message?.let { UiText.DynamicString(it) } ?: UiText.DynamicString("游客登录失败")) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message?.let { UiText.DynamicString(it) } ?: UiText.DynamicString("网络异常")) }
            } finally {
                _uiState.update { it.copy(isLoggingIn = false) }
            }
        }
    }
}
