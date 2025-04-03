package com.example.myapplication.screens

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.MAX_FILE_SIZE
import com.example.myapplication.R
import com.example.myapplication.TokenStorage
import com.example.myapplication.hello
import com.example.myapplication.refreshToken
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
                        snackbarHostState.showSnackbar(
                            message = "Размер файла не должен превышать ${MAX_FILE_SIZE / 1024 / 1024} МБ",
                            actionLabel = "Закрыть",
                            duration = SnackbarDuration.Indefinite)
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

fun onLogoutClick(navController: NavController){
    
    TokenStorage.deleteToken("accessToken")
    TokenStorage.deleteToken("refreshToken")
    TokenStorage.deleteUser()
    navController.navigate(Routes.Login.route){
        popUpTo(navController.graph.startDestinationId)
        launchSingleTop = true
    }
}

@Composable
fun LogoutConfirm(navController: NavController, onDismiss: () -> Unit, ){
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                onLogoutClick(navController)
            }) { Text(stringResource(R.string.logout), color = Color.Red, fontFamily = HeadingFont) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), fontFamily = HeadingFont) }
        },
        //title = { Text(stringResource(R.string.logout)) },
        text = { Text(stringResource(R.string.logout_confirm), fontFamily = HeadingFont, fontSize = 15.sp) },
        containerColor = colorResource(R.color.background)
        )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendingDataScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var errorLogin = remember { mutableStateOf(false) }
    var ipAddress by remember { mutableStateOf("5.165.249.136") }
    var port by remember { mutableStateOf("2869") }
    var username by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    BackHandler(enabled = true) {
        if(!isFinished){

        }
    }

    Scaffold(modifier = Modifier.fillMaxSize(),
        containerColor = colorResource(R.color.background),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.greeting_text),
                        fontFamily = HeadingFont,
                        color = colorResource(R.color.white_button),
                        fontSize = 7.em,
                        fontWeight = FontWeight.Thin
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorResource(R.color.blue_button)),
                actions = {
                    IconButton(onClick = {showLogoutConfirm = true}) {
                        Icon(
                            painter = painterResource(R.drawable.logout_icon_155171),
                            contentDescription = "Logout",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp, 24.dp)
                        )
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
            horizontalAlignment = Alignment.Start,
        ) {
//            Text(text = TokenStorage.getUser().toString())
//            Spacer(modifier = Modifier.height(5.dp))

            Text(
                stringResource(R.string.whom),
                fontSize = 9.em,
                fontFamily = HeadingFont,
                color = colorResource(R.color.grey_text)
            )
            OutlinedTextField(
                value = username,
                onValueChange = {
                    if(errorLogin.value)
                        errorLogin.value = false
                    username = it },
                label = { Text(stringResource(R.string.input_username)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                isError = errorLogin.value
            )

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


//            Button(onClick = {
//                scope.launch {
//                    val res = hello()
//                    println(res.description)
//                }
//            },)
//            {
//                Text("/hello")
//            }
//            Button(onClick = {
//                scope.launch {
//                    val res = refreshToken()
//                }
//            },)
//            {
//                Text("/refresh")
//            }
//            Button(onClick = {
//                scope.launch {
//                    navController.navigate(Routes.Receive.route){
//                        popUpTo(navController.graph.startDestinationId)
//                        launchSingleTop = true
//                    }
//                }
//            },)
//            {
//                Text("GO!!")
//            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Button(                             //самая главная кнопка "отправить"
                    onClick = {
                        if(username.trim().isEmpty()){
                            errorLogin.value = true
                        } else {
                            showDialog = true
                            isFinished = false
                            scope.launch {
                                runCatching {
                                    if (sendingData.byteArray.isEmpty()) {
                                        throw Exception("Сначала выберите файл")
                                    }
                                    sendingData.sendData(username.trim())
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

    if(showLogoutConfirm){
        LogoutConfirm(navController, {showLogoutConfirm = false})
    }

    if(sendingData.receiveDataConfirm){
        //ConfirmReceiveDialog({sendingData.receiveDataConfirm = false})
        Toast.makeText(LocalContext.current, "файл принят", Toast.LENGTH_SHORT).show()
        sendingData.receiveDataConfirm = false
    }


}


@Preview(showBackground = true)
@Composable
fun SendPreview() {
    MyApplicationTheme {
        LogoutConfirm(rememberNavController()) { }
        //SendingDataScreen(rememberNavController())
    }
}