package com.example.myapplication.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.Message
import com.example.myapplication.R
import com.example.myapplication.registResponse
import com.example.myapplication.ui.theme.MyApplicationTheme
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val name = remember {                                           //Lovak
        mutableStateOf(TextFieldValue("Lovak2"))
    }
    //val email = remember { mutableStateOf(TextFieldValue()) }
    val password = remember { mutableStateOf(TextFieldValue("BestUser03")) }    //BestUser03
    val confirmPassword = remember { mutableStateOf(TextFieldValue("BestUser03")) }

    val nameErrorState = remember { mutableStateOf(false) }
    //val emailErrorState = remember { mutableStateOf(false) }
    val passwordErrorState = remember { mutableStateOf(false) }
    val confirmPasswordErrorState = remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    var showDialog by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Center,
    ) {


        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.regist_head_text),
                fontSize = 30.sp,
                fontFamily = HeadingFont,
                color = colorResource(R.color.grey_text)
            )
        }
        Spacer(Modifier.size(16.dp))
        OutlinedTextField(
            value = name.value,
            onValueChange = {
                if (nameErrorState.value) {
                    nameErrorState.value = false
                }
                name.value = it
            },

            modifier = Modifier.fillMaxWidth(),
            isError = nameErrorState.value,
            label = {
                Text(text = stringResource(R.string.user_name))
            },
        )
        //Spacer(Modifier.size(16.dp))
//        OutlinedTextField(
//            value = email.value,
//            onValueChange = {
//                if (emailErrorState.value) {
//                    emailErrorState.value = false
//                }
//                email.value = it
//            },
//
//            modifier = Modifier.fillMaxWidth(),
//            isError = emailErrorState.value,
//            label = {
//                Text(text = stringResource(R.string.email))
//            },
//        )
        Spacer(Modifier.size(16.dp))
        val passwordVisibility = remember { mutableStateOf(true) }
        OutlinedTextField(
            value = password.value,
            onValueChange = {
                if (passwordErrorState.value) {
                    passwordErrorState.value = false
                }
                password.value = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = stringResource(R.string.password))
            },
            isError = passwordErrorState.value,
            trailingIcon = {
                IconButton(onClick = {
                    passwordVisibility.value = !passwordVisibility.value
                }) {
                    Icon(
                        painter = if (passwordVisibility.value) painterResource(R.drawable.ic_visibility_off) else painterResource(
                            R.drawable.ic_visibility
                        ),
                        //imageVector = if (passwordVisibility.value) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "visibility"
                    )
                }
            },
            visualTransformation = if (passwordVisibility.value) PasswordVisualTransformation() else VisualTransformation.None
        )

        Spacer(Modifier.size(16.dp))
        val cPasswordVisibility = remember { mutableStateOf(true) }
        OutlinedTextField(
            value = confirmPassword.value,
            onValueChange = {
                if (confirmPasswordErrorState.value) {
                    confirmPasswordErrorState.value = false
                }
                confirmPassword.value = it
            },
            modifier = Modifier.fillMaxWidth(),
            isError = confirmPasswordErrorState.value,
            label = {
                Text(text = stringResource(R.string.confirm_password))
            },
            trailingIcon = {
                IconButton(onClick = {
                    cPasswordVisibility.value = !cPasswordVisibility.value
                }) {
                    Icon(
                        painter = if (passwordVisibility.value) painterResource(R.drawable.ic_visibility_off) else painterResource(
                            R.drawable.ic_visibility
                        ),
                        //imageVector = if (cPasswordVisibility.value) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "visibility",
                    )
                }
            },
            visualTransformation = if (cPasswordVisibility.value) PasswordVisualTransformation() else VisualTransformation.None
        )
        if (confirmPasswordErrorState.value) {
            if (confirmPassword.value.text != password.value.text) {
                Text(text = stringResource(R.string.not_match_pass), color = Color.Red)
            }
        }
        Spacer(Modifier.size(16.dp))
        Button(
            onClick = {

                when {
                    name.value.text.isEmpty() -> {
                        nameErrorState.value = true
                    }
//
//                    email.value.text.isEmpty() -> {
//                        emailErrorState.value = true
//                    }

                    password.value.text.isEmpty() -> {
                        passwordErrorState.value = true
                    }

                    confirmPassword.value.text.isEmpty() -> {
                        confirmPasswordErrorState.value = true
                    }

                    confirmPassword.value.text != password.value.text -> {
                        confirmPasswordErrorState.value = true
                    }

                    else -> {
                        showDialog = true
                        isFinished = false
                        errorMessage = ""
                        scope.launch {
                            try {
                                val status = registResponse(name.value.text, password.value.text)
                                if (status == HttpStatusCode.Created) {
                                    isFinished = true
                                }
                                else{
                                    throw Exception("Код ${status.value}\n${status.description}")
                                }
//                                    delay(100)
//                                    snackbarHostState.showSnackbar(
//                                        message = "Ошибка ${status.description}, код ${status.value}",
//                                        actionLabel = "Закрыть",
//                                        duration = SnackbarDuration.Indefinite
//                                    )

                            } catch (e: Exception) {
                                isFinished = true
                                errorMessage = e.message.toString()
                            }

                        }
                    }
                }
            },
            content = {
                Text(text = stringResource(R.string.to_regist), fontFamily = HeadingFont)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.blue_button))
        )
        Spacer(Modifier.size(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TextButton(onClick = {
                navController.navigate(Routes.Login.route) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }) {
                Text(
                    text = stringResource(R.string.to_enter),
                    color = colorResource(R.color.grey_text),
                    fontFamily = HeadingFont
                )
            }
        }
    }


    if (showDialog) {
        LoadingRegistDialog(
            isFinished = isFinished,
            onDismiss = { showDialog = false },
            navController,
            errorMessage
        )
    }
}


@Composable
fun LoadingRegistDialog(
    isFinished: Boolean,
    onDismiss: () -> Unit,
    navController: NavController,
    errorMessage: String
) {

    AlertDialog(
        onDismissRequest = { },
        confirmButton = {
            if (isFinished) {
                Button(
                    onClick = {
                        onDismiss()
                        if (errorMessage == "")
                            navController.navigate(Routes.Login.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.blue_button))
                ) {
                    Text("ОК")
                }
            }
        },
        title = {
            Text(
                text =
                if (isFinished && errorMessage == "")
                    "Успешно!"
                else if (errorMessage == "")
                    "Попытка регистрации..."
                else
                    "Ошибка"
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if(!isFinished) Alignment.Center else Alignment.CenterStart
                ) {
                    if (!isFinished) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        Text(errorMessage)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    )
}

@Preview
@Composable
fun RegistPrewiew() {
    MyApplicationTheme {
        RegisterScreen(rememberNavController())
    }
}