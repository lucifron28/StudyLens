package com.example.modulelensmobile.ui.components

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.modulelensmobile.ui.theme.ModuleNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleLensTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = { 
            Text(
                text = title, 
                color = ModuleNavy
            ) 
        },
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = { actions() },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        )
    )
}
