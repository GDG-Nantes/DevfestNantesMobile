@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)

package com.gdgnantes.devfest.androidapp.ui.screens.agenda

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.gdgnantes.devfest.androidapp.ui.UiState
import com.gdgnantes.devfest.androidapp.ui.components.LoadingLayout
import com.gdgnantes.devfest.androidapp.utils.getDayFromIso8601
import com.gdgnantes.devfest.model.AgendaDay
import com.gdgnantes.devfest.model.Session
import kotlinx.coroutines.launch

@Composable
fun AgendaPager(
    modifier: Modifier = Modifier,
    initialPageIndex: Int,
    days: Map<Int, AgendaDay>,
    uiState: UiState,
    onRefresh: () -> Unit,
    onSessionClick: (Session) -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        val pagerState =
            rememberPagerState(initialPage = initialPageIndex) {
                days.size
            }

        TabRow(
            // Our selected tab is our current page
            selectedTabIndex = pagerState.currentPage,
            // Override the indicator, using the provided pagerTabIndicatorOffset modifier
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(
                        tabPositions[pagerState.currentPage]
                    )
                )
            }
        ) {
            val daysNames =
                days.values.toList()
                    .map { getDayFromIso8601(it.date, context = LocalContext.current) ?: "" }
            repeat(daysNames.size) {
                val coroutineScope = rememberCoroutineScope()
                Tab(
                    text = { Text(daysNames[it]) },
                    selected = pagerState.currentPage == it,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(it)
                        }
                    },
                )
            }
        }

        HorizontalPager(
            state = pagerState,
        ) { page ->
            val isRefreshing = uiState == UiState.LOADING
            val pullRefreshState = rememberPullRefreshState(isRefreshing, onRefresh)
            Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
                if (uiState == UiState.STARTING) {
                    LoadingLayout()
                } else {
                    val sessions = days[page + 1]?.sessions ?: emptyList()
                    if (sessions.isEmpty()) {
                        EmptyLayout()
                    } else {
                        AgendaColumn(
                            sessionsPerStartTime = sessions,
                            onSessionClick = onSessionClick
                        )
                    }
                }
                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    contentColor = MaterialTheme.colorScheme.primary,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    scale = true // enables Material3-style scaling animation
                )
            }
        }
    }
}
