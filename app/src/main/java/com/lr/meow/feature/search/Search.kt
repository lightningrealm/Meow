package com.lr.meow.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lr.meow.ui.theme.LocalBottomBarPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search() {
    val colorScheme = MaterialTheme.colorScheme
    var searchQuery by remember { mutableStateOf("") }

    val recentSearches = listOf("Taylor Swift", "Lofi Beats", "Podcast", "Jazz", "Top 50")
    val genres = listOf(
        "Pop" to listOf(Color(0xFFFF512F), Color(0xFFDD2476)),
        "Hip-Hop" to listOf(Color(0xFF1CB5E0), Color(0xFF000851)),
        "Rock" to listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)),
        "Electronic" to listOf(Color(0xFF00B4DB), Color(0xFF0083B0)),
        "Classical" to listOf(Color(0xFF1D976C), Color(0xFF93F9B9)),
        "Jazz" to listOf(Color(0xFFDA4453), Color(0xFF89216B)),
        "R&B" to listOf(Color(0xFF654EA3), Color(0xFFEAAFC8)),
        "Indie" to listOf(Color(0xFFFFD700), Color(0xFFFF8C00))
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header & Search Bar
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    Spacer(Modifier.statusBarsPadding())
                    Text(
                        text = "Search",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        placeholder = { Text("Artists, songs, or podcasts") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search Icon")
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.onSurface.copy(alpha = 0.2f),
                            focusedContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        singleLine = true
                    )
                }
            }

            // Recent Searches (Tags)
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        text = "Recent Searches",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        recentSearches.forEach { search ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = colorScheme.surfaceVariant,
                                modifier = Modifier.height(32.dp)
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

            // Browse All Header
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Browse All",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Browse Categories
            items(genres.size) { index ->
                val (genre, gradient) = genres[index]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(gradient))
                        .padding(16.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    Text(
                        text = genre,
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