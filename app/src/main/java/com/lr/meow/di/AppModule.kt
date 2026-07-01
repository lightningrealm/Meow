package com.lr.meow.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import com.lr.meow.feature.login.LoginViewModel
import com.lr.meow.MainViewModel
import com.lr.meow.feature.profile.ProfileViewModel

val appModule = module {
    viewModel { LoginViewModel(authService = get()) }
    viewModel { MainViewModel(cookieStorage = get()) }
    viewModel { ProfileViewModel(userService = get(), cookieStorage = get(), profileStorage = get()) }
}