package com.lr.meow.feature.album

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.lr.animation.diysharedelement.component.LocalCardAnimState
import com.lr.animation.diysharedelement.modifier.SharedElementTarget
import com.lr.meow.R
import com.lr.meow.feature.player.PlayerViewModel
import com.lr.meow.ui.components.bouncyClickable
import com.lr.meow.ui.theme.LocalBottomBarPadding
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetail(
    albumId: Long,
    coverImgUrl: String? = null,
    onBack: () -> Unit
) {
    val viewModel: AlbumDetailViewModel = koinViewModel()
    val playerViewModel: PlayerViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(albumId) {
        viewModel.handleIntent(AlbumDetailIntent.LoadAlbum(albumId))
    }

    val cardAnimState = LocalCardAnimState.current

    BackHandler {
        cardAnimState.prepareCollapse()
        onBack()
        cardAnimState.animScope.launch { cardAnimState.runCollapse() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val detail = uiState.albumDetail
        val songs = uiState.songs
        val displayCover = detail?.picUrl ?: coverImgUrl
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
                            .bouncyClickable {
                                if (songs.isNotEmpty()) {
                                    playerViewModel.playSongs(songs, 0)
                                }
                            }
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
                            text = stringResource(id = R.string.play_all),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onBackground
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.song_count_format, detail.size),
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
                            text = uiState.errorMessage?.asString() ?: stringResource(id = R.string.load_failed),
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
                            .bouncyClickable {
                                playerViewModel.playSongs(songs, index)
                            }
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
                                text = song.name ?: stringResource(id = R.string.unknown_song),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${song.artistName} - ${song.al?.name ?: stringResource(id = R.string.unknown_album)}",
                                fontSize = 12.sp,
                                color = colorScheme.onBackground.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
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

            TopAppBar(
                title = { Text(stringResource(id = R.string.album), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = {
                        cardAnimState.prepareCollapse()
                        onBack()
                        cardAnimState.animScope.launch { cardAnimState.runCollapse() }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // Album Info
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
                SharedElementTarget(
                    cardId = "album_cover_${albumId}",
                    cornerRadius = 16.dp,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = detail?.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
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
                        val artistAvatar = detail?.artist?.picUrl ?: detail?.artists?.firstOrNull()?.picUrl
                        if (artistAvatar != null) {
                            AsyncImage(
                                model = artistAvatar,
                                contentDescription = "Artist Avatar",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        val artistNameText = detail?.artist?.name ?: detail?.artists?.firstOrNull()?.name ?: ""
                        Text(
                            text = artistNameText,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
