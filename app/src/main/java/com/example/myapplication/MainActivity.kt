package com.example.myapplication

import LoadingDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

val HeadingFont = FontFamily(
    Font(R.font.ofont_nunito, FontWeight.Normal)
)


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Body()
            }
        }
    }
}

    @Composable
    fun InputField(field: String, onValueChange: (String) -> Unit, text: String) {
        OutlinedTextField(
            value = field,
            onValueChange = onValueChange,
            label = { Text(text) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }

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

    Column(
        modifier = Modifier
            .padding(16.dp)
    ) {
        Button(onClick = {
            filePickerLauncher.launch(arrayOf("*/*"))
        }) {
            Text(stringResource(R.string.choose_file))
        }

        Spacer(modifier = Modifier.height(16.dp))

        selectedFileUri?.let {
            Text(text = stringResource(R.string.choosing_file) + it, modifier = Modifier.clickable {})
        }
    }
}
fun getFileSize(context: Context, uri: Uri): Long? {
    return context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor: ParcelFileDescriptor ->
        descriptor.statSize
    }
}


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Body() {
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }
        var ipAddress by remember { mutableStateOf("") }                   //адрес отправления
        var port by remember { mutableStateOf("") }
        var showDialog by remember { mutableStateOf(false) }
        var isFinished by remember { mutableStateOf(false) }

        Scaffold(modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = stringResource(R.string.greeting_text), fontFamily = HeadingFont)
                    },
                    colors = TopAppBarColors(
                        containerColor = colorResource(R.color.blue_head),
                        scrolledContainerColor = Color.Transparent,
                        navigationIconContentColor = Color.Transparent,
                        titleContentColor = Color.Black,
                        actionIconContentColor = Color.Transparent
                    )
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
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
            ) {

                Spacer(modifier = Modifier.padding(5.dp))


                    Text(stringResource(R.string.heading_server),
                        fontSize = 12.em,
                        fontFamily = HeadingFont,
                        color = colorResource(R.color.grey_text))
                    InputField(ipAddress, { ipAddress = it }, stringResource(R.string.address))
                    InputField(port, { port = it }, stringResource(R.string.num_of_port))

                Spacer(modifier = Modifier.padding(5.dp))

                    Text(stringResource(R.string.heading_file),
                        fontFamily = HeadingFont,
                        fontSize = 12.em,
                        lineHeight = 1.em,
                        color = colorResource(R.color.grey_text)
                    )
                    FilePickerScreen(scope, snackbarHostState)

                Spacer(modifier = Modifier.padding(5.dp))

                Box(
                    Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Button(                             //самая главная кнопка "отправить"
                        onClick = {
                            try {
                                showDialog = true
                                isFinished = false
                                scope.launch {
                                    sendingData.sendData()
                                    isFinished = true
                                }
                            } catch (e:Exception) {
                                showDialog = false
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = e.message.toString(),             //ну или что-нибудь другое можно выводить в этом сообщении
                                        actionLabel = "Закрыть",
                                        duration = SnackbarDuration.Indefinite
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                    }

                                }
                            }
                        },
                    ) {
                        Text(stringResource(R.string.Send_data))
                    }
                }
            }
        }

        if(showDialog){
            LoadingDialog(isFinished = isFinished, onDismiss = {showDialog = false})
        }
    }


    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        MyApplicationTheme {
            Body()
        }
    }
