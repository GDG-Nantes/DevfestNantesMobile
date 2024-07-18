package com.gdgnantes.devfest.androidapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdgnantes.devfest.analytics.AnalyticsPage
import com.gdgnantes.devfest.analytics.AnalyticsService
import com.gdgnantes.devfest.store.BookmarksStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val analyticsService: AnalyticsService,
    private val bookmarksStore: BookmarksStore,
) : ViewModel() {
    fun subscribe(id: String): Flow<Boolean> {
        return bookmarksStore.subscribe(id)
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    }

    fun setBookmarked(sessionId: String, bookmarked: Boolean, fromPage: AnalyticsPage) {
        bookmarksStore.setBookmarked(sessionId, bookmarked)
        analyticsService.eventBookmark(fromPage, sessionId, bookmarked)
    }
}
