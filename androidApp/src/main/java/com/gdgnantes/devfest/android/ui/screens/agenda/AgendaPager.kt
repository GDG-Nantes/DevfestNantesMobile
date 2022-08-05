package com.gdgnantes.devfest.android.ui.screens.agenda

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.gdgnantes.devfest.android.utils.pagerTabIndicatorOffset
import com.gdgnantes.devfest.model.AgendaDay
import com.gdgnantes.devfest.model.Session
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun AgendaPager(
    initialPageIndex: Int,
    days: Map<Int, AgendaDay>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onSessionClick: (Session) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        val pagerState = rememberPagerState(initialPage = initialPageIndex)

        TabRow(
            // Our selected tab is our current page
            selectedTabIndex = pagerState.currentPage,
            // Override the indicator, using the provided pagerTabIndicatorOffset modifier
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            val daysNames = days.keys.toList().map { key -> key.toString() }
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
            count = days.size,
            state = pagerState,
        ) { page ->
            SwipeRefresh(
                state = SwipeRefreshState(isRefreshing = isRefreshing),
                onRefresh = onRefresh,
            ) {
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
        }
    }
}