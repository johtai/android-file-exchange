package com.example.myapplication.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.example.myapplication.R
import com.example.myapplication.sendingData

@Composable
fun ReceiveDataAlertDialog(navController: NavController, username:String, onDismiss: () -> Unit){

    AlertDialog(
        onDismissRequest = {},
        confirmButton = {TextButton(onClick = {
            navController.navigate(Routes.Login.route) {
                popUpTo(navController.graph.startDestinationId)
                launchSingleTop = true
            }
        })
        {Text("Да")} },
        dismissButton = {
            TextButton(onClick = onDismiss)
        {Text("Нет")}},
        title = {Text(
            "Получение данных"
        )},
        text = {Text("Пользователь $username хочет отправить вам файл. Принять?")},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveDataScreen(navController: NavController){

    val scope = rememberCoroutineScope()
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
                        if(!sendingData.douwnloadingInProcces) {
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
                .padding(5.dp)
                .padding(innerPadding),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(Modifier.height(20.dp))
            Text(
                text = if (sendingData.douwnloadingInProcces) "Принимаем файл..." else "Загружено",
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
                    if (sendingData.douwnloadingInProcces) {
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
    ReceiveDataScreen(rememberNavController())
}