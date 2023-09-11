package com.gdgnantes.devfest.androidapp.ui.screens.speakers.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gdgnantes.devfest.androidapp.ui.UiState
import com.gdgnantes.devfest.androidapp.ui.components.LoadingLayout
import com.gdgnantes.devfest.androidapp.ui.screens.speakers.SpeakerPicture
import com.gdgnantes.devfest.model.Speaker
import kotlinx.coroutines.launch

/*
List of speakers made with the code and article from Keith Abdulla's the Github repository : https://github.com/ekeitho/compose-scrolling-bubble/tree/main
 */
@Composable
fun Speakers(
    modifier: Modifier = Modifier,
    viewModel: SpeakersViewModel = hiltViewModel(),
    onSpeakerClick: (Speaker) -> Unit
) {
    Speakers(
        modifier = modifier,
        speakers = viewModel.speakers.collectAsState(),
        uiState = viewModel.uiState.collectAsState(),
        onSpeakerClick = onSpeakerClick
    )
}

@Composable
fun Speakers(
    modifier: Modifier = Modifier,
    speakers: State<List<Speaker>>,
    uiState: State<UiState>,
    onSpeakerClick: (Speaker) -> Unit
) {
    Speakers(
        modifier = modifier,
        speakers = speakers.value,
        uiState = uiState.value,
        onSpeakerClick = onSpeakerClick
    )
}

@Composable
fun Speakers(
    modifier: Modifier = Modifier,
    speakers: List<Speaker>,
    uiState: UiState,
    onSpeakerClick: (Speaker) -> Unit
) {
    val context = LocalDensity.current
    val alphabetHeightInPixels = remember { with(context) { alphabetItemHeight.toPx() } }
    var alphabetRelativeDragYOffset: Float? by remember { mutableStateOf(null) }
    var alphabetDistanceFromTopOfScreen: Float by remember { mutableStateOf(0F) }

    Scaffold {
        if (uiState == UiState.STARTING) {
            LoadingLayout(
                modifier = modifier
                    .padding(it)
                    .fillMaxSize()
            )
        } else {
            Surface(
                modifier = modifier
                    .padding(it)
                    .fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                BoxWithConstraints {
                    SpeakersListWithScroller(
                        speakers = speakers,
                        onAlphabetListDrag = { relativeDragYOffset, containerDistance ->
                            alphabetRelativeDragYOffset = relativeDragYOffset
                            alphabetDistanceFromTopOfScreen = containerDistance
                        },
                        onSpeakerClick = onSpeakerClick
                    )

                    val yOffset = alphabetRelativeDragYOffset
                    if (yOffset != null) {
                        ScrollingBubble(
                            boxConstraintMaxWidth = this.maxWidth,
                            bubbleOffsetYFloat = yOffset + alphabetDistanceFromTopOfScreen,
                            currAlphabetScrolledOn = yOffset.getIndexOfCharBasedOnYPosition(
                                alphabetHeightInPixels = alphabetHeightInPixels,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScrollingBubble(
    modifier: Modifier = Modifier,
    boxConstraintMaxWidth: Dp,
    bubbleOffsetYFloat: Float,
    currAlphabetScrolledOn: Char,
) {
    val bubbleSize = 96.dp
    Surface(
        modifier = modifier
            .size(bubbleSize)
            .offset(
                x = (boxConstraintMaxWidth - (bubbleSize + alphabetItemHeight)),
                y = with(LocalDensity.current) {
                    bubbleOffsetYFloat.toDp() - (bubbleSize / 2)
                },
            ),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currAlphabetScrolledOn.toString(),
                style = MaterialTheme.typography.headlineLarge
            )
        }
    }
}

fun List<Speaker>.getMapOfFirstUniqueCharIndex(): Map<Char, Int> {
    val firstLetterIndexes = mutableMapOf<Char, Int>()
    this
        .map { it.name.lowercase().first() }
        .forEachIndexed { index, char ->
            if (!firstLetterIndexes.contains(char)) {
                firstLetterIndexes[char] = index
            }
            // else don't care about letters that don't exist
        }
    return firstLetterIndexes
}

internal fun Float.getIndexOfCharBasedOnYPosition(
    alphabetHeightInPixels: Float,
): Char {
    val alphabetCharList = "abcdefghijklmnopqrstuvwxyz".map { it }

    var index = ((this) / alphabetHeightInPixels).toInt()
    index = when {
        index > 25 -> 25
        index < 0 -> 0
        else -> index
    }
    return alphabetCharList[index]
}

@Composable
fun SpeakersListWithScroller(
    modifier: Modifier = Modifier,
    speakers: List<Speaker>,
    onAlphabetListDrag: (Float?, Float) -> Unit,
    onSpeakerClick: (Speaker) -> Unit
) {
    val mapOfFirstLetterIndex = remember(speakers) { speakers.getMapOfFirstUniqueCharIndex() }
    val alphabetHeightInPixels = with(LocalDensity.current) { alphabetItemHeight.toPx() }
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpeakersList(
            Modifier
                .fillMaxHeight()
                .weight(1F),
            speakers,
            lazyListState,
            mapOfFirstLetterIndex,
            onSpeakerClick
        )

        AlphabetScroller(
            onAlphabetListDrag = { relativeDragYOffset, containerDistanceFromTopOfScreen ->
                onAlphabetListDrag(relativeDragYOffset, containerDistanceFromTopOfScreen)
                coroutineScope.launch {
                    // null case can happen if we go through list
                    // and we don't have a name that starts with I
                    val indexOfChar = relativeDragYOffset?.getIndexOfCharBasedOnYPosition(
                        alphabetHeightInPixels = alphabetHeightInPixels,
                    )
                    mapOfFirstLetterIndex[indexOfChar]?.let {
                        lazyListState.scrollToItem(it)
                    }
                }
            },
        )
    }
}

@Composable
fun SpeakersList(
    modifier: Modifier = Modifier,
    speakers: List<Speaker>,
    lazyListState: LazyListState,
    firstLetterIndexes: Map<Char, Int>,
    onSpeakerClick: (Speaker) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        state = lazyListState
    ) {
        itemsIndexed(speakers) { index, speaker ->
            SpeakerRow(
                modifier = Modifier.clickable { onSpeakerClick(speaker) },
                speaker = speaker,
                isAlphabeticallyFirstInCharGroup =
                firstLetterIndexes[speaker.name.lowercase().first()] == index,
            )
        }
    }
}

@Composable
fun SpeakerRow(
    modifier: Modifier = Modifier,
    speaker: Speaker,
    isAlphabeticallyFirstInCharGroup: Boolean
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (isAlphabeticallyFirstInCharGroup) {
                Text(
                    text = speaker.name.first().toString(),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        SpeakerPicture(
            modifier = Modifier.size(32.dp),
            speaker = speaker
        )

        Text(
            modifier = Modifier.padding(16.dp),
            text = speaker.name,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

val alphabetItemHeight = 24.dp

@Composable
private fun AlphabetScroller(
    modifier: Modifier = Modifier,
    onAlphabetListDrag: (yOffset: Float?, containerPositionOffset: Float) -> Unit,
) {
    val alphabetCharList = "abcdefghijklmnopqrstuvwxyz".map { it }
    var containerPositionOffset by remember { mutableStateOf(0F) }

    Column(
        modifier = modifier
            .width(16.dp)
            .onGloballyPositioned { layoutCoordinates ->
                containerPositionOffset = layoutCoordinates.positionInRoot().y
            }
            .pointerInput(alphabetCharList) {
                detectVerticalDragGestures(
                    onDragStart = {
                        onAlphabetListDrag(it.y, containerPositionOffset)
                    },
                    onDragEnd = {
                        onAlphabetListDrag(null, containerPositionOffset)
                    }
                ) { change, _ ->
                    onAlphabetListDrag(
                        change.position.y,
                        containerPositionOffset
                    )
                }
            },
        verticalArrangement = Arrangement.Center,
    ) {
        for (i in alphabetCharList) {
            Text(
                modifier = Modifier.height(alphabetItemHeight),
                text = i.toString(),
            )
        }
    }
}