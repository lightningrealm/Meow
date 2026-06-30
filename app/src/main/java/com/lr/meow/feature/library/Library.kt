package com.lr.meow.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lr.meow.ui.theme.LocalBottomBarPadding

@Composable
fun Library() {
    val colorScheme = MaterialTheme.colorScheme

    val filters = listOf("Playlists", "Podcasts", "Albums", "Artists", "Downloaded")
    val recentItems = listOf(
        "Liked Songs" to "Playlist • 120 songs",
        "Daily Mix 1" to "Made for you",
        "Your Top Songs 2023" to "Playlist",
        "Discover Weekly" to "New music updated every Monday",
        "Rain Sounds" to "Podcast • Sleep",
        "Jazz Vibes" to "Playlist",
        "Lofi Hip Hop" to "Playlist",
        "Rock Classics" to "Playlist"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(top = 20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            item {
                Spacer(Modifier.statusBarsPadding())
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        modifier = Modifier.size(40.dp),
                        tint = colorScheme.onBackground
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = "Your Library",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                }
            }

            // Filters
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    items(filters.size) { index ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = colorScheme.surfaceVariant,
                            modifier = Modifier.height(36.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = filters[index],
                                    color = colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // List
            items(recentItems.size) { index ->
                val (title, subtitle) = recentItems[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Placeholder Image
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(if (index == 0 || index == 4) CircleShape else RoundedCornerShape(8.dp))
                            .background(colorScheme.primary.copy(alpha = 0.2f))
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onBackground
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = subtitle,
                            fontSize = 14.sp,
                            color = colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Bottom Padding
            item {
                Spacer(Modifier.height(LocalBottomBarPadding.current))
            }
        }
    }
}