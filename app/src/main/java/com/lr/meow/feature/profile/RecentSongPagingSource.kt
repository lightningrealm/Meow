package com.lr.meow.feature.profile

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.lr.core.network.api.MeowUserService
import com.lr.core.network.model.RecentSongItem

class RecentSongPagingSource(
    private val userService: MeowUserService
) : PagingSource<Int, RecentSongItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RecentSongItem> {
        return try {
            val offset = params.key ?: 0
            val limit = params.loadSize
            
            val response = userService.getRecentSongs(limit = limit, offset = offset)
            val songs = response.data?.list ?: emptyList()
            
            // Assuming we paginate if we got exactly the limit we asked for.
            val nextKey = if (songs.size == limit) offset + limit else null
            
            LoadResult.Page(
                data = songs,
                prevKey = if (offset == 0) null else offset - limit,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, RecentSongItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(state.config.pageSize)
                ?: anchorPage?.nextKey?.minus(state.config.pageSize)
        }
    }
}
