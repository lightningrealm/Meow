package com.lr.meow.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
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
fun Profile() {
    val colorScheme = MaterialTheme.colorScheme

    val publicPlaylists = listOf(
        "Late Night Coding" to "24 tracks",
        "Workout Hype" to "50 tracks",
        "Coffee Shop Vibes" to "112 tracks",
        "Focus Flow" to "30 tracks"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(top = 20.dp, bottom = LocalBottomBarPadding.current),
            modifier = Modifier.fillMaxSize()
        ) {
            // Profile Header
            item {
                Spacer(Modifier.statusBarsPadding())
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = colorScheme.onBackground
                        )
                    }
                }
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(colorScheme.primary.copy(alpha = 0.2f))
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Lightning Duke",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "128 Followers",
                            fontSize = 14.sp,
                            color = colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "42 Following",
                            fontSize = 14.sp,
                            color = colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { /*TODO*/ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.onBackground,
                            contentColor = colorScheme.background
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.padding(horizontal = 32.dp).height(48.dp)
                    ) {
                        Text("Edit Profile", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(32.dp))
            }

            // Public Playlists
            item {
                Text(
                    text = "Public Playlists",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onBackground,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
                )
            }

            items(publicPlaylists.size) { index ->
                val (title, trackCount) = publicPlaylists[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colorScheme.surfaceVariant)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.onBackground
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = trackCount,
                            fontSize = 14.sp,
                            color = colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}