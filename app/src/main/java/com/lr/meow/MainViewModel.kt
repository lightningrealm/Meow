package com.lr.meow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lr.core.datastore.CookieStorage
import com.lr.core.network.AuthEvent
import com.lr.core.network.AuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val cookieStorage: CookieStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    
    // Combine our mutable UI state with the reactive isLoggedInFlow from CookieStorage
    val uiState: StateFlow<MainUiState> = combine(
        _uiState,
        cookieStorage.isLoggedInFlow
    ) { state, isLoggedIn ->
        state.copy(isLoggedIn = isLoggedIn)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    init {
        // Listen to global auth events (like 401 Unauthorized)
        viewModelScope.launch {
            AuthManager.authEvents.collect { event ->
                when (event) {
                    is AuthEvent.LoggedOut -> {
                        dispatch(MainIntent.RequestLogin)
                    }
                }
            }
        }
    }

    fun dispatch(intent: MainIntent) {
        when (intent) {
            is MainIntent.ChangeTab -> {
                _uiState.update { it.copy(currentTab = intent.tab) }
            }
            is MainIntent.RequestLogin -> {
                _uiState.update { it.copy(showLoginSheet = true) }
            }
            is MainIntent.DismissLogin -> {
                _uiState.update { it.copy(showLoginSheet = false) }
            }
        }
    }
}
