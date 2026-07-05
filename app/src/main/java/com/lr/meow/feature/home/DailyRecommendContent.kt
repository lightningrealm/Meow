package com.lr.meow.feature.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.lr.meow.R
import com.lr.meow.feature.player.PlayerViewModel
import com.lr.meow.ui.components.bouncyClickable
import com.lr.meow.ui.components.shimmerEffect
import com.lr.meow.ui.theme.LocalBottomBarPadding
import com.lr.meow.ui.theme.LocalTopBarPadding

@Composable
fun DailyRecommendContent(
    viewModel: HomeViewModel,
    playerViewModel: PlayerViewModel,
    uiState: HomeUiState,
    isLoggedIn: Boolean,
    requireAuth: () -> Unit,
    colorScheme: ColorScheme
) {
    if (!isLoggedIn || uiState.needsLogin) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (uiState.needsLogin) stringResource(id = R.string.login_expired) else stringResource(id = R.string.login_view_recs),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Button(onClick = { requireAuth() }) {
                Text(if (uiState.needsLogin) stringResource(id = R.string.relogin) else stringResource(id = R.string.login_now))
            }
        }
    } else if (uiState.isLoading && uiState.recommendSongs.isEmpty()) {
        DailyRecommendSkeleton()
    } else if (uiState.isError && uiState.recommendSongs.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = uiState.errorMessage?.asString() ?: stringResource(id = R.string.load_failed),
                color = colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(onClick = { viewModel.fetchRecommendSongs() }) {
                Text(stringResource(id = R.string.retry))
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Spacer(Modifier
                    .padding(top= LocalTopBarPadding.current)
                )
                Column(modifier = Modifier.padding(bottom = 24.dp, top = 16.dp)) {
                    Text(
                        text = "DAILY RECOMMEND",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.daily_recommend),
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.2f),
                                offset = Offset(0f, 4f),
                                blurRadius = 8f
                            )
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.daily_recommend_desc),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.85f),
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.15f),
                                offset = Offset(0f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )
                }
            }

            items(uiState.recommendSongs, key = { it.id }) { song ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .bouncyClickable {
                            val index = uiState.recommendSongs.indexOf(song)
                            if (index >= 0) {
                                playerViewModel.playSongs(uiState.recommendSongs, index)
                            }
                        }
                        .background(colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = song.al?.picUrl,
                        contentDescription = "Song Cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colorScheme.surfaceVariant)
                    )
                    Spacer(Modifier.width(16.dp))
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
                            text = song.artistName + (song.reason?.let { " · $it" } ?: ""),
                            fontSize = 14.sp,
                            color = colorScheme.onBackground.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { viewModel.dislikeSong(song.id) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dislike",
                            tint = colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(LocalBottomBarPadding.current))
            }
        }
    }
}

@Composable
fun DailyRecommendSkeleton() {
    LazyColumn(
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 20.dp+LocalTopBarPadding.current,
            bottom = 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box(Modifier.width(160.dp).height(40.dp).clip(RoundedCornerShape(8.dp)).shimmerEffect())
            Spacer(Modifier.height(16.dp))
        }
        items(10) {
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                Box(Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).shimmerEffect())
                Spacer(Modifier.width(16.dp))
                Column(verticalArrangement = Arrangement.Center, modifier = Modifier.height(56.dp)) {
                    Box(Modifier.width(150.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.width(100.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                }
            }
        }
    }
}
