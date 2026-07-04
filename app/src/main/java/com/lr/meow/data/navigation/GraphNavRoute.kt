package com.lr.meow.data.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 用于stack模式匹配
 * **/
enum class MyNavTab{
    HOME,DISCOVER,LIBRARY,SEARCH,PROFILE
}

/**
 * ## HomeGraph对应的Destination
 * **/
@Serializable data object EntryHomeRoot: NavKey
@Serializable data class EntryHomeDetail(val id: Int): NavKey

/**
 * ## DiscoverGraph对应的Destination
 * **/
@Serializable data object EntryDiscoverRoot: NavKey
@Serializable data object EntryDiscoverDetail: NavKey

/**
 * ## LibraryGraph对应的Destination
 * **/
@Serializable data object EntryLibraryRoot: NavKey
@Serializable
data class EntryLibraryDetail(
    val folderId: Int
): NavKey

/**
 * ## SearchGraph对应的Destination
 * **/
@Serializable data object EntrySearchRoot: NavKey
@Serializable
data class EntrySearchDetail(
    val keyWords:String
): NavKey

/**
 * ##ProfileGraph对应的Destination
 * **/
@Serializable data object EntryProfileRoot: NavKey

/**
 * ## Playlist Detail (can be pushed to any stack)
 * **/
@Serializable data class EntryPlaylistDetail(val id: Long, val coverImgUrl: String? = null): NavKey

/**
 * ## Album Detail (can be pushed to any stack)
 * **/
@Serializable data class EntryAlbumDetail(val id: Long, val coverImgUrl: String? = null): NavKey