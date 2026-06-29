package com.lr.meow.feature.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.lr.meow.data.cardface.CardFaceData
import com.lr.meow.ui.common.card.CardFace
import com.lr.meow.ui.theme.LocalBottomBarPadding
import com.lr.meow.ui.theme.LocalSharedTransitionScope

@Composable
fun Home(
    onNavigateToDetail:(Int)-> Unit
){
    val sharedScope = LocalSharedTransitionScope.current?:return
    val animatedScope = LocalNavAnimatedContentScope.current
    with(sharedScope) {
        Box(
            Modifier
                .fillMaxSize()
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
                            letterSpacing = 0.3.sp
                        )
                        Text(
                            text = "Today",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(Modifier.height(22.dp))
                }
                items(3) { index->
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
                            CardFaceData(
                                "TopTitle",
                                "Title",
                                "SubTitle"
                            ),
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
}