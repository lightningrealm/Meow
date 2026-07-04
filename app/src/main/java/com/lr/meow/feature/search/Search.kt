package com.lr.meow.feature.search


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.lr.meow.R
import com.lr.meow.feature.player.PlayerViewModel
import com.lr.meow.ui.components.bouncyClickable
import com.lr.meow.ui.theme.LocalBottomBarPadding
import org.koin.androidx.compose.koinViewModel

@Composable
fun Search(
    viewModel: SearchViewModel = koinViewModel(),
    playerViewModel: PlayerViewModel = koinViewModel(),
    onPlaylistClick: (Long, String?) -> Unit = { _, _ -> },
    onAlbumClick: (Long, String?) -> Unit = { _, _ -> },
    onArtistClick: (Long, String?, String?) -> Unit = { _, _, _ -> }
) {
    val colorScheme = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    
    BackHandler(enabled = uiState.searchState != SearchState.Idle) {
        viewModel.dispatch(SearchIntent.UpdateQuery(""))
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        Spacer(Modifier.statusBarsPadding())

        // Header Title
        Text(
            text = stringResource(id = R.string.nav_search),
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )

        // Search Bar
        TextField(
            value = uiState.query,
            onValueChange = { viewModel.dispatch(SearchIntent.UpdateQuery(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            placeholder = { Text(stringResource(id = R.string.search_placeholder)) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search Icon")
            },
            trailingIcon = {
                if (uiState.query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.dispatch(SearchIntent.UpdateQuery("")) }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                    }
                }
            },
            shape = CircleShape,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                focusManager.clearFocus()
                viewModel.dispatch(SearchIntent.SubmitSearch)
            })
        )

        // Content Area
        Box(modifier = Modifier.weight(1f)) {
            when (uiState.searchState) {
                SearchState.Idle -> IdleState(uiState, viewModel)
                SearchState.Typing -> TypingState(uiState, viewModel)
                SearchState.Results -> ResultsState(uiState, viewModel, playerViewModel, onPlaylistClick, onAlbumClick, onArtistClick)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IdleState(uiState: SearchUiState, viewModel: SearchViewModel) {
    val colorScheme = MaterialTheme.colorScheme
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Search History
        if (uiState.searchHistory.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.search_history),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onBackground
                    )
                    IconButton(onClick = { viewModel.dispatch(SearchIntent.ClearHistory) }) {
                        Icon(Icons.Default.Clear, contentDescription = stringResource(id = R.string.clear_history), tint = colorScheme.onSurfaceVariant)
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    uiState.searchHistory.forEach { search ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = colorScheme.surfaceVariant,
                            modifier = Modifier
                                .height(32.dp)
                                .bouncyClickable { viewModel.dispatch(SearchIntent.ClickHistory(search)) }
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = search,
                                    color = colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Hot Searches
        if (uiState.hotSearches.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(id = R.string.hot_searches),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            items(uiState.hotSearches.size) { index ->
                val hotItem = uiState.hotSearches[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bouncyClickable { viewModel.dispatch(SearchIntent.ClickHistory(hotItem.searchWord)) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (index < 3) Color(0xFFDD2476) else colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(32.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = hotItem.searchWord,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onBackground
                            )
                            if (!hotItem.iconUrl.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                AsyncImage(
                                    model = hotItem.iconUrl,
                                    contentDescription = null,
                                    modifier = Modifier.height(14.dp),
                                    contentScale = ContentScale.FillHeight
                                )
                            }
                        }
                        hotItem.content?.takeIf { it.isNotEmpty() }?.let { content ->
                            Text(
                                text = content,
                                fontSize = 12.sp,
                                color = colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                    Text(
                        text = "${hotItem.score}",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
        
        item {
            Spacer(Modifier.height(LocalBottomBarPadding.current))
        }
    }
}

@Composable
private fun TypingState(uiState: SearchUiState, viewModel: SearchViewModel) {
    val colorScheme = MaterialTheme.colorScheme
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
    ) {
        items(uiState.suggestions) { suggestion ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .bouncyClickable { viewModel.dispatch(SearchIntent.SelectSuggestion(suggestion.keyword)) }
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Search, 
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = suggestion.keyword,
                    fontSize = 16.sp,
                    color = colorScheme.onBackground
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultsState(
    uiState: SearchUiState, 
    viewModel: SearchViewModel,
    playerViewModel: PlayerViewModel,
    onPlaylistClick: (Long, String?) -> Unit,
    onAlbumClick: (Long, String?) -> Unit,
    onArtistClick: (Long, String?, String?) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Column {
        PrimaryTabRow(
            selectedTabIndex = SearchTab.entries.indexOf(uiState.activeTab),
            containerColor = Color.Transparent,
            contentColor = colorScheme.primary
        ) {
            SearchTab.entries.forEach { tab ->
                Tab(
                    selected = uiState.activeTab == tab,
                    onClick = { viewModel.dispatch(SearchIntent.ChangeTab(tab)) },
                    text = { Text(stringResource(id = tab.titleResId)) }
                )
            }
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.isError) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.errorMessage?.asString() ?: stringResource(id = R.string.search_failed), color = colorScheme.error)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(top = 16.dp, bottom = LocalBottomBarPadding.current + 16.dp)
            ) {
                when (uiState.activeTab) {
                    SearchTab.SONGS -> {
                        itemsIndexed(uiState.songResults) { index, song ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bouncyClickable {
                                        playerViewModel.playSongs(uiState.songResults.map { it.toSong() }, index)
                                    }
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.name,
                                        fontSize = 16.sp,
                                        color = colorScheme.onBackground,
                                        maxLines = 1
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = song.artistName,
                                            fontSize = 13.sp,
                                            color = colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            modifier = Modifier
                                                .bouncyClickable {
                                                    val firstArtist = song.ar?.firstOrNull()
                                                    if (firstArtist != null) {
                                                        onArtistClick(firstArtist.id, null, firstArtist.name)
                                                    }
                                                }
                                                .weight(1f, fill = false)
                                        )
                                        Text(
                                            text = " - ",
                                            fontSize = 13.sp,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = song.al?.name ?: "",
                                            fontSize = 13.sp,
                                            color = colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            modifier = Modifier
                                                .bouncyClickable {
                                                    val al = song.al
                                                    if (al != null) {
                                                        onAlbumClick(al.id, al.picUrl)
                                                    }
                                                }
                                                .weight(1f, fill = false)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    SearchTab.PLAYLISTS -> {
                        items(uiState.playlistResults) { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bouncyClickable { onPlaylistClick(playlist.id, playlist.coverImgUrl) }
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = playlist.coverImgUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = playlist.name,
                                        fontSize = 16.sp,
                                        color = colorScheme.onBackground,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = stringResource(id = R.string.search_playlist_info, playlist.trackCount, playlist.creator?.nickname ?: stringResource(id = R.string.unknown_artist)),
                                        fontSize = 13.sp,
                                        color = colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}