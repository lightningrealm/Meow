package com.lr.meow.feature.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.lr.meow.data.cardface.CardFaceData
import com.lr.meow.ui.common.card.CardFace
import com.lr.meow.ui.theme.LocalBottomBarPadding

@Composable
fun Home(
    onNavigateToDetail:(Int)-> Unit
){
    val colorScheme = MaterialTheme.colorScheme
    Box(
        Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        LazyColumn {
            item {
                Spacer(Modifier.statusBarsPadding())
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(top = 16.dp, bottom = 10.dp)
                ) {
                    Text(
                        text = "Wednesday, 25 June",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp,
                        color = colorScheme.onBackground
                    )
                    Text(
                        text = "Today",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(Modifier.height(22.dp))
            }
            val mockCards = listOf(
                CardFaceData(
                    topTitle = "NEW RELEASE",
                    title = "The Weekend Mix",
                    subTitle = "Catch up on the latest tracks tailored for your weekend.",
                    backgroundGradient = listOf(Color(0xFFFF512F), Color(0xFFDD2476))
                ),
                CardFaceData(
                    topTitle = "EDITOR'S PICK",
                    title = "Lo-Fi Beats",
                    subTitle = "Relax and focus with these chilled out tunes.",
                    backgroundGradient = listOf(Color(0xFF1CB5E0), Color(0xFF000851))
                ),
                CardFaceData(
                    topTitle = "TOP 50 GLOBAL",
                    title = "Chart Toppers",
                    subTitle = "The most played songs around the world right now.",
                    backgroundGradient = listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
                ),
                CardFaceData(
                    topTitle = "NEW EPISODE",
                    title = "Tech Talks",
                    subTitle = "Deep dive into the future of artificial intelligence.",
                    backgroundGradient = listOf(Color(0xFF00B4DB), Color(0xFF0083B0))
                )
            )

            items(mockCards.size) { index ->
                val card = mockCards[index]
                var pressed by remember { mutableStateOf(false) }
                val scale by animateFloatAsState(
                    targetValue = if (pressed) 0.954f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .scale(scale)
                        .height(430.dp)
                ) {
                    CardFace(
                        cardFaceData = card,
                        sharedKey = "card_hero_$index"
                    ) {
                        onNavigateToDetail(index)
                    }
                }
                Spacer(Modifier.height(30.dp))
            }
            item {
                Spacer(Modifier.height(LocalBottomBarPadding.current))
            }
        }
    }
}