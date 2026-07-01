package com.lr.meow.feature.profile

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.lr.core.network.api.MeowUserService
import com.lr.core.network.model.Playlist

class PlaylistPagingSource(
    private val userService: MeowUserService,
    private val uid: Long
) : PagingSource<Int, Playlist>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Playlist> {
        return try {
            val offset = params.key ?: 0
            val limit = params.loadSize
            
            val response = userService.getUserPlaylists(uid = uid, limit = limit, offset = offset)
            val playlists = response.playlist ?: emptyList()
            
            val nextKey = if (response.more) offset + limit else null
            
            LoadResult.Page(
                data = playlists,
                prevKey = if (offset == 0) null else offset - limit,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Playlist>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(state.config.pageSize)
                ?: anchorPage?.nextKey?.minus(state.config.pageSize)
        }
    }
}
