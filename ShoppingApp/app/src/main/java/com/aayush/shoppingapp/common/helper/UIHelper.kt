package com.aayush.shoppingapp.common.helper

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp


@Composable
fun ShowProgressBar(isProgressBarVisible: Boolean) {
    if (isProgressBarVisible) {
        Box(
            modifier = Modifier.fillMaxSize()
                .alpha(0.5f)
                .background(MaterialTheme.colorScheme.inverseSurface, RectangleShape)
                .clickable(
                    false, null, Role.Image,
                    onClick = {}
                ),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                strokeWidth = 5.dp,
                color = MaterialTheme.colorScheme.inverseOnSurface
            )
        }
    }
}

@Composable
fun ShowAlertDialog(
    showDialog: MutableState<Boolean>, // State to control visibility
    title: String,
    message: String,
    confirmButtonText: String? = null,
    onConfirm: (() -> Unit?)?,
    dismissButtonText: String? = null,
) : Boolean {
    if(showDialog.value) {
        AlertDialog(
            dismissButton = {
                if (dismissButtonText?.isNotEmpty() == true) {
                    Text(dismissButtonText)
                }
            },
            onDismissRequest = {
                showDialog.value = false
            },
            modifier = Modifier
                .wrapContentSize()
                .padding(5.dp),
            title = { Text(title) },
            text = { Text(message) },
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = {
                TextButton(onClick = {
                    showDialog.value = false
                    onConfirm?.invoke()
                }) {
                    if (confirmButtonText?.isNotEmpty() == true) {
                        Text(confirmButtonText)
                    }
                }
            },
        )
    }
    return  showDialog.value
}