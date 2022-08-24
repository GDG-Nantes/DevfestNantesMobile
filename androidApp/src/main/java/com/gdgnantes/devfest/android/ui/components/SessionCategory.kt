package com.gdgnantes.devfest.android.ui.components

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gdgnantes.devfest.android.ui.theme.DevFest_NantesTheme
import com.gdgnantes.devfest.model.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionCategory(
    modifier: Modifier = Modifier,
    category: Category
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface),
        colors = cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Text(modifier = Modifier.padding(8.dp), text = category.label, fontSize = 12.sp)
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SessionCategoryPreview() {
    DevFest_NantesTheme {
        Scaffold {
            SessionCategory(category = Category("mobile", "📱 Mobile & IoT"))
        }
    }
}