package com.example.tasksapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tasksapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksHeader(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineMedium
            )
        },
        actions = {
            IconButton(onClick = onThemeToggle) {
                Icon(
                    imageVector = if (isDarkTheme) {
                        Icons.Default.LightMode
                    } else {
                        Icons.Default.DarkMode
                    },
                    contentDescription = if (isDarkTheme) "Switch to light theme" else "Switch to dark theme"
                )
            }
        },
        modifier = modifier
    )
}
