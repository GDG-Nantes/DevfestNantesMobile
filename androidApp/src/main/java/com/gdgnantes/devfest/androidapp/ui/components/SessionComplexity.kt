package com.gdgnantes.devfest.androidapp.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.androidapp.ui.theme.DevFest_NantesTheme
import com.gdgnantes.devfest.model.Complexity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionComplexity(
    modifier: Modifier = Modifier,
    complexity: Complexity
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface),
        colors = cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Text(
            modifier = Modifier.padding(8.dp),
            text = stringResource(id = complexity.labelResId()),
            fontSize = 12.sp
        )
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SessionComplexityPreview() {
    DevFest_NantesTheme {
        Scaffold {
            SessionComplexity(complexity = Complexity.BEGINNER)
        }
    }
}

fun Complexity.labelResId() = when (this) {
    Complexity.BEGINNER -> R.string.complexity_beginer
    Complexity.INTERMEDIATE -> R.string.complexity_intermediate
    Complexity.EXPERT -> R.string.complexity_expert
}