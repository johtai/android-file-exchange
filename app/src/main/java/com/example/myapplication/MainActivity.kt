package com.example.myapplication

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

var sendingData:SendingData = SendingData()

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
fun FilePickerScreen() {
    val context = LocalContext.current
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> selectedFileUri = uri
            sendingData = SendingData(context, selectedFileUri!!)      //выбирает любой файл из системы и записывает его в data
        }
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
    ) {
        Button(onClick = {
            filePickerLauncher.launch(arrayOf("*/*"))
        }) {
            Text("Выбрать файл")
        }

        Spacer(modifier = Modifier.height(16.dp))

        selectedFileUri?.let {
            Text(text = "Выбранный файл: $it", modifier = Modifier.clickable {})
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Body(){
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var adress by remember { mutableStateOf("") }                   //адрес отправления

    Scaffold(modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.greeting_text))
                },
                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    scrolledContainerColor = Color.Transparent,
                    navigationIconContentColor = Color.Transparent,
                    titleContentColor = Color.Black,
                    actionIconContentColor = Color.Transparent
                )
            )
        },
        snackbarHost =  {
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

            Row {
                Text(stringResource(R.string.whom))
                InputField(adress, { adress = it}, stringResource(R.string.adress))
            }
            Spacer(modifier = Modifier.padding(5.dp))
            Row {
                Text(stringResource(R.string.what))
                FilePickerScreen()
            }
            Spacer(modifier = Modifier.padding(5.dp))

            Box(
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Button(
                    onClick = { scope.launch {                                  //самая главная кнопка "отправить"
                        val result = snackbarHostState.showSnackbar(
                            message = "Отправлено",
                            actionLabel = "Закрыть",  // Добавляем кнопку "Закрыть"
                            duration = SnackbarDuration.Indefinite // Ожидание нажатия
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            // Действие при нажатии на кнопку
                        }

                    } },
                ) {
                    Text(stringResource(R.string.Send_data))
                }
            }
        }
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