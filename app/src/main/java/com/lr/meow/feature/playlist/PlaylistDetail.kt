package com.lr.meow.feature.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import androidx.compose.ui.platform.LocalContext
import com.lr.meow.ui.components.bouncyClickable
import com.lr.meow.ui.theme.LocalBottomBarPadding
import androidx.activity.compose.BackHandler
import kotlinx.coroutines.launch
import com.lr.animation.diysharedelement.component.LocalCardAnimState
import com.lr.animation.diysharedelement.modifier.SharedElement
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetail(
    playlistId: Long,
    coverImgUrl: String? = null,
    onBack: () -> Unit,
    viewModel: PlaylistDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(playlistId) {
        viewModel.handleIntent(PlaylistDetailIntent.LoadPlaylist(playlistId))
    }

    val cardAnimState = LocalCardAnimState.current
    val coroutineScope = rememberCoroutineScope()

    BackHandler {
        cardAnimState.prepareCollapse()
        onBack()
        cardAnimState.animScope.launch { cardAnimState.runCollapse() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val detail = uiState.playlistDetail
        val songs = uiState.songs
            val displayCover = detail?.coverImgUrl ?: coverImgUrl
            val listState = rememberLazyListState()
            val headerOffset by remember {
                derivedStateOf {
                    if (listState.firstVisibleItemIndex > 0) -1000f
                    else -listState.firstVisibleItemScrollOffset.toFloat()
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = LocalBottomBarPadding.current)
            ) {
                // Header Placeholder
                item {
                    Spacer(modifier = Modifier.height(280.dp))
                }

                if (detail != null) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .bouncyClickable { viewModel.handleIntent(PlaylistDetailIntent.PlayAll) }
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play All",
                                tint = colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "播放全部",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onBackground
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "(共 ${detail.trackCount} 首)",
                                fontSize = 14.sp,
                                color = colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                if (uiState.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = colorScheme.primary)
                        }
                    }
                } else if (uiState.isError) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = uiState.errorMessage ?: "Failed to load playlist",
                                color = colorScheme.error
                            )
                        }
                    }
                } else {


                // Tracks List
                itemsIndexed(songs) { index, song ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bouncyClickable { viewModel.handleIntent(PlaylistDetailIntent.PlaySong(song)) }
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = (index + 1).toString(),
                            fontSize = 16.sp,
                            color = colorScheme.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.width(32.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.name ?: "未知歌曲",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${song.artistName} - ${song.al?.name ?: "Unknown Album"}",
                                fontSize = 12.sp,
                                color = colorScheme.onBackground.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                } // End of else block
            }

            // Hoisted Header for synchronous composition (fixes SharedElement flash)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .graphicsLayer { translationY = headerOffset }
            ) {
                // Blurred Background
                AsyncImage(
                    model = displayCover,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                )

                // Small Cover
                SharedElement(
                    cardId = "playlist_cover_${playlistId}",
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 20.dp)
                        .offset(y = (-20).dp)
                        .size(120.dp)
                ) {
                    AsyncImage(
                        model = displayCover,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                    )
                }

                // Top Bar
                TopAppBar(
                    title = { Text("歌单", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = {
                            cardAnimState.prepareCollapse()
                            onBack()
                            cardAnimState.animScope.launch { cardAnimState.runCollapse() }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )

                // Playlist Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    val context = LocalContext.current
                    val imageRequest = remember(displayCover) {
                        ImageRequest.Builder(context)
                            .data(displayCover)
                            .crossfade(true)
                            .build()
                    }
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = detail?.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = detail?.name ?: "",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = detail?.creator?.avatarUrl,
                                contentDescription = "Creator Avatar",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = detail?.creator?.nickname ?: "",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
