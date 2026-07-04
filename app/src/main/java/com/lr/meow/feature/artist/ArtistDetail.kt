package com.lr.meow.feature.artist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.lr.animation.diysharedelement.component.LocalCardAnimState
import com.lr.animation.diysharedelement.modifier.SharedElementTarget
import com.lr.meow.R
import com.lr.meow.feature.player.PlayerViewModel
import com.lr.meow.ui.components.bouncyClickable
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun ArtistDetail(
    artistId: Long = -1,
    artistName: String = "",
    artistAvatarUrl: String = "",
    onBack: () -> Unit = {}
) {
    val viewModel: ArtistDetailViewModel = koinViewModel()
    val playerViewModel: PlayerViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(artistId) {
        if (artistId != -1L) {
            viewModel.handleIntent(ArtistDetailIntent.LoadArtist(artistId))
        }
    }

    val cardAnimState = LocalCardAnimState.current
    BackHandler {
        cardAnimState.prepareCollapse()
        onBack()
        cardAnimState.animScope.launch { cardAnimState.runCollapse() }
    }
    
    // 使用 Box 作为根布局，实现沉浸式头部和悬浮 TopBar
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        val detail = uiState.artistInfo
        val displayCover = detail?.picUrl ?: artistAvatarUrl
        val displayName = detail?.name ?: artistName
        val topSongs = uiState.topSongs
        val albums = uiState.hotAlbums

        // 1. 可滚动的主体内容
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp) // 底部留白，防挡播放条
        ) {
            // --- 头部大图与渐变 ---
            item {
                ArtistHeader(
                    artistId = artistId,
                    artistName = displayName,
                    imageUrl = displayCover,
                    onPlayAll = {
                        if (topSongs.isNotEmpty()) {
                            playerViewModel.playSongs(topSongs, 0)
                        }
                    }
                )
            }

            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colorScheme.primary)
                    }
                }
            } else if (uiState.isError) {
                item {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = uiState.errorMessage?.asString() ?: stringResource(id = R.string.load_failed),
                            color = colorScheme.error
                        )
                    }
                }
            } else {
                if (topSongs.isNotEmpty()) {
                    // --- 热门五十曲 ---
                    item {
                        SectionTitle(title = "热门单曲", subtitle = "Top 50")
                    }
                    
                    itemsIndexed(topSongs) { index, song -> 
                        SongListItem(
                            index = index + 1,
                            songName = song.name ?: stringResource(id = R.string.unknown_song),
                            albumName = song.al?.name ?: stringResource(id = R.string.unknown_album),
                            onClick = {
                                playerViewModel.playSongs(topSongs, index)
                            }
                        )
                    }
                }

                if (albums.isNotEmpty()) {
                    // --- 专辑 (横向滑动) ---
                    item {
                        SectionTitle(title = "最新专辑", subtitle = "查看全部 >")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(bottom = 24.dp)
                        ) {
                            items(albums) { album ->
                                AlbumItem(
                                    albumName = album.name ?: stringResource(id = R.string.unknown_album),
                                    year = "${album.size} 首歌曲",
                                    picUrl = album.picUrl ?: ""
                                )
                            }
                        }
                    }
                }
                
                // --- 歌手信息/简介 ---
                item {
                    SectionTitle(title = "关于歌手")
                    Text(
                        text = "暂无简介",
                        fontSize = 14.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // 2. 悬浮在最顶部的导航栏 (不受滑动影响，固定在顶部)
        FloatingTopBar(onBack = {
            cardAnimState.prepareCollapse()
            onBack()
            cardAnimState.animScope.launch { cardAnimState.runCollapse() }
        })
    }
}

@Composable
private fun ArtistHeader(artistId: Long, artistName: String, imageUrl: String, onPlayAll: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp) // 头部高度
    ) {
        // 背景大图
        SharedElementTarget(
            cardId = "artist_cover_${artistId}",
            modifier = Modifier.fillMaxSize(),
            cornerRadius = 0.dp
        ) {
            AsyncImage(
                model = imageUrl.ifEmpty { "https://via.placeholder.com/400" }, // 占位图
                contentDescription = "Artist Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 底部渐变遮罩 (让文字清晰，并平滑过渡到页面背景色)
        val backgroundColor = MaterialTheme.colorScheme.background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.2f),
                            backgroundColor
                        ),
                        startY = 100f
                    )
                )
        )

        // 歌手名字与播放控制区
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text(
                text = artistName,
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 播放全部按钮
                Button(
                    onClick = onPlayAll,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("播放全部", fontWeight = FontWeight.Bold)
                }

                // 关注按钮
                OutlinedButton(
                    onClick = { /* TODO */ },
                    shape = CircleShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("关注")
                }
            }
        }
    }
}

@Composable
private fun FloatingTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding() // 仅在此处加状态栏高度，防止被状态栏遮挡
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 圆形半透明返回键
        IconButton(
            onClick = { onBack() },
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                .size(40.dp)
        ) {
            Icon(Icons.Default.ArrowBackIosNew, contentDescription = "More", tint = Color.White)
        }


        // 圆形半透明更多键
        IconButton(
            onClick = { /* TODO */ },
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                .size(40.dp)
        ) {
            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { }
            )
        }
    }
}

@Composable
private fun SongListItem(index: Int, songName: String, albumName: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 排名数字
        Text(
            text = index.toString(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (index <= 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.width(32.dp)
        )
        
        // 歌曲信息
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = songName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = albumName,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 更多操作
        IconButton(onClick = { /*TODO*/ }) {
            Icon(
                Icons.Default.MoreVert, 
                contentDescription = "More",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun AlbumItem(albumName: String, year: String, picUrl: String) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { }
    ) {
        // 专辑封面
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = picUrl.ifEmpty { "https://via.placeholder.com/150" }, // 占位图
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        // 专辑名
        Text(
            text = albumName,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        // 发行年份
        Text(
            text = year,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
