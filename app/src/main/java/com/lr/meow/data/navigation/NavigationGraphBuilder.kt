package com.lr.meow.data.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation

inline fun <reified T: Any>
NavGraphBuilder.buildSubGraph(
    startDestination: Any,
    noinline content: NavGraphBuilder.()-> Unit
){
    navigation<T>(startDestination = startDestination){
        content()
    }
}