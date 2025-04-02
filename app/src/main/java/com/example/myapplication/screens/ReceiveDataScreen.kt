package com.example.myapplication.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.example.myapplication.R
import com.example.myapplication.sendingData
import kotlinx.coroutines.launch

@Composable
fun ConfirmReceiveDialog(onDismiss: () -> Unit){

    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {TextButton(onClick = {
            scope.launch {
                onDismiss()
                sendingData.receiveData()
//                navController.navigate(Routes.Login.route) {
//                    popUpTo(navController.graph.startDestinationId)
//                    launchSingleTop = true
//                }
            }
        })
        {Text("Да")} },
        dismissButton = {
            TextButton(onClick = {
                scope.launch {
                    onDismiss()
                    sendingData.sentRejection()
                }
            })
        {Text("Нет")}},
        title = {Text(
            "Получение данных"
        )},
        text = {Text("Вам хотят отправить файл. Принять?")},
    )
}

@Composable
fun ReceiveDataDialog(onDismiss: () -> Unit){

    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            if (!sendingData.downloadingInProcess) {
                TextButton(onClick = {
                    scope.launch {
                        sendingData.saveFile()
                    }
                })
                { Text("Сохранить файл") }
            }
        },
        dismissButton = {
            if(!sendingData.downloadingInProcess){
                TextButton(onClick = {
                    scope.launch {
                        sendingData.saveFile()
                    }
                })
                { Text("Отмена") }
            }
        },
        title = {Text(
            if(sendingData.downloadingInProcess)"Получение данных" else "Файл загружен"
        )},
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (sendingData.downloadingInProcess) {
                        CircularProgressIndicator()
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Имя файла: ${sendingData.filename}")
                if(!sendingData.downloadingInProcess)Text("Размер файла в байтах: ${sendingData.byteArray.sumOf{it.size}}")
                Text("Всего пакетов: ${sendingData.allPackages}")
                Text("Количество принятых: ${sendingData.sentPackages}")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveDataScreen(navController: NavController){
    val snackbarHostState = remember { SnackbarHostState() }
    BackHandler(enabled = true) {  }

    Scaffold(modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
//                    Text(
//                        text = stringResource(R.string.greeting_text),
//                        fontFamily = HeadingFont,
//                        color = colorResource(R.color.white_button),
//                        fontSize = 7.em,
//                        fontWeight = FontWeight.Thin
//                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorResource(R.color.blue_button)),
                navigationIcon = {
                    IconButton(onClick = {
                        if(!sendingData.downloadingInProcess) {
                            navController.navigate(Routes.Send.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    }) {
                        Icon(painter = painterResource(R.drawable.baseline_arrow_back_24),
                            contentDescription = "Back",
                            tint = Color.White)
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(15.dp)
                .padding(innerPadding),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(Modifier.height(20.dp))
            Text(
                text = if (sendingData.downloadingInProcess) "Принимаем файл..." else "Загружено",
                fontSize = 9.em,
                fontFamily = HeadingFont,
                color = colorResource(R.color.grey_text)
            )
            Spacer(Modifier.height(20.dp))
            Text(text = "Имя файла: ${sendingData.filename}" ,
                fontSize = 5.em,
                fontFamily = HeadingFont,
                color = colorResource(R.color.grey_text))
            Spacer(Modifier.height(20.dp))
            Text(text = "Размер файла: ${sendingData.byteArray.sumOf { it.size }} байт",
                fontSize = 5.em,
                fontFamily = HeadingFont,
                color = colorResource(R.color.grey_text))
            Spacer(Modifier.height(20.dp))
            Text(text = "Количество пакетов: ${sendingData.allPackages}",
                fontSize = 5.em,
                fontFamily = HeadingFont,
                color = colorResource(R.color.grey_text))
            Spacer(Modifier.height(20.dp))
            Text(text = "Принято пакетов: ${sendingData.sentPackages}",
                fontSize = 5.em,
                fontFamily = HeadingFont,
                color = colorResource(R.color.grey_text))
            Spacer(Modifier.height(30.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (sendingData.downloadingInProcess) {
                        CircularProgressIndicator(modifier = Modifier.size(40.dp, 40.dp), color = colorResource(R.color.blue_button))
                    } else {

                    }
                }
                Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {

                    Button(onClick = {
                        sendingData.saveFile()
                    },
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.blue_button)),
                        modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Сохранить",
                            fontFamily = HeadingFont,
                            fontSize = 4.em)
                    }
                }


        }
    }
}

@Preview
@Composable
fun ReceiveDataAD(){
    var showRDAL by remember { mutableStateOf(false) }
    ReceiveDataDialog({showRDAL = false})
}