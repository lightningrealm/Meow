package com.lr.meow.feature.profile

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.lr.core.network.api.MeowUserService
import com.lr.core.network.model.RecentPlaylistItem

class RecentPlaylistPagingSource(
    private val userService: MeowUserService
) : PagingSource<Int, RecentPlaylistItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RecentPlaylistItem> {
        return try {
            val offset = params.key ?: 0
            val limit = params.loadSize
            
            val response = userService.getRecentPlaylists(limit = limit, offset = offset)
            val playlists = response.data?.list ?: emptyList()
            
            val nextKey = if (playlists.size == limit) offset + limit else null
            
            LoadResult.Page(
                data = playlists,
                prevKey = if (offset == 0) null else offset - limit,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, RecentPlaylistItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(state.config.pageSize)
                ?: anchorPage?.nextKey?.minus(state.config.pageSize)
        }
    }
}
