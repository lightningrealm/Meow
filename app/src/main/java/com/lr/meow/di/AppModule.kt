package com.lr.meow.di

import com.lr.core_player.MusicController
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import com.lr.meow.feature.login.LoginViewModel
import com.lr.meow.MainViewModel
import com.lr.meow.feature.profile.SharedUserViewModel
import com.lr.meow.feature.playlist.PlaylistDetailViewModel
import com.lr.meow.feature.discover.DiscoverViewModel
import com.lr.meow.feature.home.HomeViewModel
import com.lr.meow.feature.player.PlayerViewModel
import com.lr.meow.feature.search.SearchViewModel
import com.lr.meow.feature.album.AlbumDetailViewModel

val appModule = module {
    viewModel { LoginViewModel(authService = get()) }
    viewModel { MainViewModel(cookieStorage = get()) }
    viewModel { SharedUserViewModel(userService = get(), authService = get(), persistentCookieJar = get(), profileStorage = get(), songService = get()) }
    viewModel { PlaylistDetailViewModel(playlistService = get()) }
    viewModel { DiscoverViewModel(recommendService = get()) }
    viewModel { HomeViewModel(recommendService = get()) }
    viewModel { SearchViewModel(searchService = get(), searchHistoryStorage = get()) }
    viewModel { AlbumDetailViewModel(songService = get()) }
    
    single { MusicController(get()) }
    viewModel { PlayerViewModel(musicController = get(), songService = get(), recommendService = get()) }
}