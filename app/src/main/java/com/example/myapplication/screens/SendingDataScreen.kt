package com.example.myapplication.screens

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.myapplication.MAX_FILE_SIZE
import com.example.myapplication.R
import com.example.myapplication.sendingData
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun FilePickerScreen(scope: CoroutineScope, snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                val fileSize = getFileSize(context, it)
                if (fileSize != null && fileSize > MAX_FILE_SIZE) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Размер файла не должен превышать ${MAX_FILE_SIZE / 1024 / 1024} МБ")
                    }
                } else {
                    selectedFileUri = it
                    sendingData.setData(
                        context,
                        selectedFileUri!!
                    )                                       //выбирает любой файл из системы и записывает его в sendingData
                }
            }
        }
    )

    Spacer(modifier = Modifier.height(5.dp))
    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedButton(
            onClick = {
                filePickerLauncher.launch(arrayOf("*/*"))
            },
            modifier = Modifier.size(200.dp, 50.dp),
            ) {
            Text(
                stringResource(R.string.choose_file),
                fontSize = 4.em,
                fontFamily = HeadingFont,
                color = colorResource(R.color.grey_text),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(painter = painterResource(R.drawable.baseline_attach_file_24), contentDescription = "Скрепочка")
        }

        Spacer(modifier = Modifier.height(10.dp))

        selectedFileUri?.let {
            Text(
                text = stringResource(R.string.choosing_file) + " " + it,
                modifier = Modifier.clickable {})
        }

    }
}

fun getFileSize(context: Context, uri: Uri): Long? {
    return context.contentResolver.openFileDescriptor(uri, "r")
        ?.use { descriptor: ParcelFileDescriptor ->
            descriptor.statSize
        }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendingDataScreen() {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var ipAddress by remember { mutableStateOf("5.167.121.51") }
    var port by remember { mutableStateOf("2869") }
    var showDialog by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }

    Scaffold(modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.greeting_text),
                        fontFamily = HeadingFont,
                        color = colorResource(R.color.grey_text),
                        fontSize = 7.em
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorResource(R.color.blue_head))
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(5.dp)
                .padding(innerPadding),
            horizontalAlignment = Alignment.Start,
        ) {

            Text(
                stringResource(R.string.heading_server),
                fontSize = 9.em,
                fontFamily = HeadingFont,
                color = colorResource(R.color.grey_text)
            )
            InputField(ipAddress, { ipAddress = it }, stringResource(R.string.address))
            InputField(port, { port = it }, stringResource(R.string.num_of_port))

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                stringResource(R.string.heading_file),
                fontFamily = HeadingFont,
                fontSize = 8.em,
                lineHeight = 1.em,
                color = colorResource(R.color.grey_text)
            )
            Spacer(modifier = Modifier.height(5.dp))
            FilePickerScreen(scope, snackbarHostState)


            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Button(                             //самая главная кнопка "отправить"
                    onClick = {
                        showDialog = true
                        isFinished = false
                        scope.launch {
                            runCatching {
                                if (sendingData.filename == "") {
                                    throw Exception("Сначала выберите файл")
                                }
                                sendingData.sendData()
                                isFinished = true
                            }.onFailure { e ->
                                showDialog = false
                                snackbarHostState.showSnackbar(
                                    message = e.message.toString(),
                                    actionLabel = "Закрыть",
                                    duration = SnackbarDuration.Indefinite
                                )
                            }
                        }

                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.blue_button)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.Send_data),
                        fontFamily = HeadingFont,
                        fontSize = 4.em
                    )
                }
            }
        }
    }

    if (showDialog) {
        LoadingDialog(isFinished = isFinished, onDismiss = { showDialog = false })
    }
}


@Preview(showBackground = true)
@Composable
fun SendPreview() {
    MyApplicationTheme {
        SendingDataScreen()
    }
}