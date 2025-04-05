package com.example.myapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.example.myapplication.R
import com.example.myapplication.sendingData
import com.example.myapplication.ui.theme.MyApplicationTheme


@Composable
fun LoadingDialog(isFinished: Boolean, onDismiss: () -> Unit) {

    AlertDialog(
        onDismissRequest = { },
        confirmButton = {
            if (isFinished) {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = colorResource(R.color.purple_200))
                ) {
                    Text("ОК")
                }
            }
        },
        title = { Text(text = if (isFinished) stringResource(R.string.loading_ready) else stringResource(R.string.sent_file_loading), fontFamily = HeadingFont) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isFinished) {
                        CircularProgressIndicator()
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.filesize) +" "+sendingData.byteArray.sumOf{it.size}, fontFamily = HeadingFont)
                Text(stringResource(R.string.package_count)+" " + sendingData.allPackages, fontFamily = HeadingFont)
                Text(stringResource(R.string.sent_package_count)+" " + sendingData.sentPackages, fontFamily = HeadingFont)
                Text(stringResource(R.string.resent_package_count)+" " + sendingData.resentPackages, fontFamily = HeadingFont)
            }
        },
        containerColor = colorResource(R.color.background)
    )
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        var showDialog by remember { mutableStateOf(false) }
        var isFinished by remember { mutableStateOf(true) }
        LoadingDialog(isFinished, { showDialog = false })
    }
}