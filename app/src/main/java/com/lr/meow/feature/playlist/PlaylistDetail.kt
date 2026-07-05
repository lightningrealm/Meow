package com.lr.meow.feature.playlist

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.lr.animation.diysharedelement.component.LocalCardAnimState
import com.lr.animation.diysharedelement.modifier.SharedElementTarget
import com.lr.meow.R
import com.lr.meow.data.TopBarMenuItem
import com.lr.meow.feature.player.PlayerViewModel
import com.lr.meow.ui.components.bouncyClickable
import com.lr.meow.ui.theme.LocalBottomBarPadding
import com.lr.meow.ui.theme.LocalTopBarMenuItems
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetail(
    playlistId: Long,
    coverImgUrl: String? = null,
    onBack: () -> Unit,
    onAlbumClick: (Long, String?) -> Unit = { _, _ -> }
) {
    val viewModel: PlaylistDetailViewModel = koinViewModel()
    val playerViewModel: PlayerViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    // ✨ 1. 新增：搜索状态
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(playlistId) {
        viewModel.handleIntent(PlaylistDetailIntent.LoadPlaylist(playlistId))
    }

    val topBarMenuItems = LocalTopBarMenuItems.current
    DisposableEffect(Unit) {
        topBarMenuItems.add(TopBarMenuItem("开发中..", Icons.Default.Lightbulb) {})
        onDispose { topBarMenuItems.clear() }
    }

    val cardAnimState = LocalCardAnimState.current

    // ✨ 2. 新增：优化返回键逻辑。如果正在搜索，先退出搜索状态
    BackHandler {
        if (searchQuery.isNotEmpty()) {
            searchQuery = ""
        } else {
            cardAnimState.prepareCollapse()
            onBack()
            cardAnimState.animScope.launch { cardAnimState.runCollapse() }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        val detail = uiState.playlistDetail
        val songs = uiState.songs
        val displayCover = detail?.coverImgUrl ?: coverImgUrl
        val listState = rememberLazyListState()

        val density = LocalDensity.current.density
        //300dp to px
        val headerHeightPx = 300 * density

        // ✨ 3. 新增：派生出过滤后的歌曲列表
        val filteredSongs by remember(songs, searchQuery) {
            derivedStateOf {
                if (searchQuery.isBlank()) songs
                else songs.filter { song ->
                    // 匹配歌曲名或歌手名（忽略大小写）
                    song.name?.contains(
                        searchQuery,
                        ignoreCase = true
                    ) == true || song.artistName.contains(searchQuery, ignoreCase = true)
                }
            }
        }

        // Header 随列表精准跟随
        val headerTranslationY by remember {
            derivedStateOf {
                val offset = listState.firstVisibleItemScrollOffset.toFloat()
                if (listState.firstVisibleItemIndex == 0) {
                    -offset
                } else {
                    //完全刚好在屏幕外
                    -headerHeightPx
                }
            }
        }

        // 背景图 0.5x 视差
        val imageParallaxY by remember {
            derivedStateOf {
                if (listState.firstVisibleItemIndex == 0) {
                    listState.firstVisibleItemScrollOffset.toFloat() * 0.5f
                } else 0f
            }
        }

        // ==========================================
        // 1. 列表层
        // ==========================================
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = LocalBottomBarPadding.current)
        ) {
            item { Spacer(modifier = Modifier.height(300.dp)) }

            // ✨ 修改：判断过滤后的列表
            if (detail != null && filteredSongs.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colorScheme.background)
                            .bouncyClickable {
                                playerViewModel.playSongs(
                                    filteredSongs,
                                    0
                                )
                            } // ✨ 播放过滤后的歌单
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Play All",
                                tint = colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                stringResource(id = R.string.play_all),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            // ✨ 动态显示过滤后的数量
                            Text(
                                if (searchQuery.isEmpty()) stringResource(
                                    id = R.string.song_count_format,
                                    detail.trackCount
                                ) else "找到 ${filteredSongs.size} 首歌曲",
                                fontSize = 13.sp,
                                color = colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = colorScheme.primary) }
                }
            } else if (uiState.isError) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            uiState.errorMessage?.asString()
                                ?: stringResource(id = R.string.load_failed),
                            color = colorScheme.error
                        )
                    }
                }
            } else if (filteredSongs.isEmpty() && searchQuery.isNotEmpty()) {
                // ✨ 新增：搜索无结果的状态提示
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "未搜索到相关歌曲喵~",
                            color = colorScheme.onBackground.copy(alpha = 0.5f),
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                // ✨ 修改：渲染过滤后的歌曲
                itemsIndexed(filteredSongs) { index, song ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bouncyClickable { playerViewModel.playSongs(filteredSongs, index) }
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            (index + 1).toString(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onBackground.copy(alpha = 0.3f),
                            modifier = Modifier.width(36.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                song.name ?: stringResource(id = R.string.unknown_song),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${song.artistName} - ${song.al?.name ?: stringResource(id = R.string.unknown_album)}",
                                fontSize = 13.sp,
                                color = colorScheme.onBackground.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.bouncyClickable {
                                    val al = song.al; if (al != null) {
                                    onAlbumClick(al.id, al.picUrl)
                                }
                                }
                            )
                        }
                        IconButton(onClick = { /* TODO */ }) {
                            Icon(
                                Icons.Default.MoreVert,
                                "More",
                                tint = colorScheme.onBackground.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. 悬浮的视差头部层
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .graphicsLayer {
                    translationY = headerTranslationY
                    clip = true
                }
        ) {
            AsyncImage(
                model = displayCover, contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { translationY = imageParallaxY }
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.3f),
                                colorScheme.background.copy(alpha = 0.6f),
                                colorScheme.background.copy(alpha = 0.95f)
                            )
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val context = LocalContext.current
                val imageRequest = remember(displayCover) {
                    ImageRequest.Builder(context).data(displayCover).crossfade(true).build()
                }

                SharedElementTarget(
                    cardId = "playlist_cover_${playlistId}",
                    cornerRadius = 16.dp,
                    modifier = Modifier
                        .size(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = detail?.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(Modifier.width(20.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        detail?.name ?: "",
                        color = colorScheme.onBackground,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 28.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = detail?.creator?.avatarUrl,
                            contentDescription = "Creator Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            detail?.creator?.nickname ?: "",
                            color = colorScheme.onBackground.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // ==========================================
        // 3. 动态搜索栏 (Top AppBar) - 真正可用的搜索
        // ==========================================
        val showTopBar by remember {
            derivedStateOf {
                // ✨ 新增：如果正在搜索，即使列表缩短，搜索框也强制保持显示，防止突然消失
                listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > (headerHeightPx * 0.7f) || searchQuery.isNotEmpty()
            }
        }

        val topBarBgColor by animateColorAsState(
            targetValue = if (showTopBar) colorScheme.background else Color.Transparent,
            animationSpec = tween(300), label = ""
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            topBarBgColor.copy(0.7f),
                            topBarBgColor.copy(0.3f),
                            topBarBgColor.copy(alpha = 0f)
                        )
                    )
                )
                .statusBarsPadding()
                .height(56.dp)
        ) {
            // 搜索框动画显隐
            AnimatedVisibility(
                visible = showTopBar,
                enter = fadeIn(tween(300)) + slideInVertically { it / 2 },
                exit = fadeOut(tween(300)) + slideOutVertically { it / 2 },
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(start = 70.dp, end = 70.dp) // 给返回键和右侧留出空间
            ) {
                // ✨ 4. 替换为真实的 BasicTextField 进行交互
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    textStyle = TextStyle(color = colorScheme.onSurface, fontSize = 14.sp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    cursorBrush = SolidColor(colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clip(CircleShape)
                                .background(colorScheme.surfaceVariant)
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .align(Alignment.CenterVertically)
                            ) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "在歌单内搜索",
                                        fontSize = 14.sp,
                                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .wrapContentHeight(Alignment.CenterVertically)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .wrapContentHeight(Alignment.CenterVertically)
                                ) {
                                    innerTextField()
                                }
                            }

                            // ✨ 5. 输入后显示一键清除按钮
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}