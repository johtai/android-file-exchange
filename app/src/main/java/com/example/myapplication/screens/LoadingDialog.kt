package com.example.myapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import androidx.compose.ui.window.Dialog
import com.example.myapplication.sendingData
import com.example.myapplication.ui.theme.MyApplicationTheme


@Composable
fun LoadingDialog(isFinished: Boolean, onDismiss: () -> Unit) {

    AlertDialog(
        onDismissRequest = { },
        confirmButton = {
            if (isFinished) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.blue_button)
                    )
                ) {
                    Text("ОК")
                }
            }
        },
        title = { Text(if (isFinished) "Готово" else "Отправляем...") },
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
                    } else {
                        //Text("Загрузка завершена!", fontSize = 5.em)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                //Text("Размер файла в байтах: ${sendingData.byteArray.size}")
                Text("Всего пакетов: ${sendingData.allPackages}")
                Text("Количество отправленных: ${sendingData.sendingPackages}")
                Text("Повторно запрошено: ${sendingData.requestedPackages}")
            }
        }
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