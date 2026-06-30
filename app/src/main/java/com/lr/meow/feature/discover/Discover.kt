package com.lr.meow.feature.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lr.meow.ui.theme.LocalBottomBarPadding

@Composable
fun Discover() {
    val colorScheme = MaterialTheme.colorScheme

    val trendingItems = listOf("Global Top 50", "Viral Hits", "New Music Friday", "Acoustic Chill")
    val moodGradients = listOf(
        "Happy" to listOf(Color(0xFFFFD700), Color(0xFFFF8C00)),
        "Focus" to listOf(Color(0xFF00C6FF), Color(0xFF0072FF)),
        "Workout" to listOf(Color(0xFFFF416C), Color(0xFFFF4B2B)),
        "Sleep" to listOf(Color(0xFF654EA3), Color(0xFFEAAFC8)),
        "Romance" to listOf(Color(0xFFDA4453), Color(0xFF89216B)),
        "Party" to listOf(Color(0xFF1D976C), Color(0xFF93F9B9))
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    Spacer(Modifier.statusBarsPadding())
                    Text(
                        text = "Discover",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
            }

            // Trending Carousel
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    Text(
                        text = "Trending Now",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        items(trendingItems.size) { index ->
                            Box(
                                modifier = Modifier
                                    .width(140.dp)
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colorScheme.surfaceVariant)
                                    .padding(16.dp),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                Text(
                                    text = trendingItems[index],
                                    color = colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // Mood Header
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Browse by Mood",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Mood Grid Items
            items(moodGradients.size) { index ->
                val (title, gradient) = moodGradients[index]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(gradient))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            // Bottom Padding
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(Modifier.height(LocalBottomBarPadding.current))
            }
        }
    }
}