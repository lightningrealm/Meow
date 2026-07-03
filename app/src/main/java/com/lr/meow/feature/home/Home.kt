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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import com.lr.animation.diysharedelement.component.LocalCardAnimState
import com.lr.animation.diysharedelement.modifier.SharedElement
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.lr.meow.R

import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.lr.animation.diysharedelement.model.CardAnimTransform
import com.lr.meow.data.cardface.CardFaceData
import com.lr.meow.ui.common.card.CardFace
import com.lr.meow.ui.theme.LocalBottomBarPadding

@Composable
fun Home(
    onNavigateToDetail: (Int) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    val mockCards = listOf(
        CardFaceData(
            topTitle = "DAILY RECOMMEND",
            title = stringResource(id = R.string.daily_recommend),
            subTitle = stringResource(id = R.string.daily_recommend_desc),
            backgroundGradient = listOf(Color(0xFFFF512F), Color(0xFFDD2476))
        ),
        CardFaceData(
            topTitle = "PERSONAL FM",
            title = stringResource(id = R.string.private_fm),
            subTitle = stringResource(id = R.string.private_fm_desc),
            backgroundGradient = listOf(Color(0xFF1CB5E0), Color(0xFF000851))
        )
    )

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

                val cardAnimState = LocalCardAnimState.current
                val coroutineScope = rememberCoroutineScope()

                SharedElement(
                    cardId = "card_hero_$index",
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .scale(scale)
                        .height(430.dp)
                ) {
                    CardFace(
                        cardFaceData = card
                    ) {
                        // 1. 同步建立 session，phase 立即变为 EXPANDING
                        val ready = cardAnimState.prepareExpand("card_hero_$index") { _ ->
                            CardAnimTransform(
                                x = 0f,
                                y = 0f,
                                width = screenWidthPx,
                                height = screenHeightPx,
                                cornerRadius = 0f
                            )
                        }
                        if (ready) {
                            // 2. 触发导航（此时 phase != IDLE，transitionSpec 走 instant）
                            onNavigateToDetail(index)
                            // 3. 协程里跑弹簧动画
                            coroutineScope.launch { cardAnimState.runExpand() }
                        }
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