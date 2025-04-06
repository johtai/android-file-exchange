package com.example.myapplication.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
    val name = remember {                                           //Lovak
        mutableStateOf(TextFieldValue(""))
    }
    val password = remember { mutableStateOf(TextFieldValue("")) }    //BestUser03
    val confirmPassword = remember { mutableStateOf(TextFieldValue("")) }

    val nameErrorState = remember { mutableStateOf(false) }
    val passwordErrorState = remember { mutableStateOf(false) }
    val confirmPasswordErrorState = remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    var showDialog by remember { mutableStateOf(false) }
    BackHandler(enabled = true) {  }

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
                    name.value.text.trim().isEmpty() -> {
                        nameErrorState.value = true
                    }
//
//                    email.value.text.isEmpty() -> {
//                        emailErrorState.value = true
//                    }

                    password.value.text.trim().isEmpty() -> {
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
                        scope.launch {
                            try {
                                val status = registResponse(name.value.text.trim(), password.value.text.trim())
                                showDialog = false
                                if (status == HttpStatusCode.Created) {

                                    Toast.makeText(
                                        context,
                                        "Пользователь успешно зарегистрирован!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.navigate(Routes.Login.route) {
                                        popUpTo(navController.graph.startDestinationId)
                                        launchSingleTop = true
                                    }
                                }
                                else{
                                    throw Exception("Код ${status.value}\n${status.description}")
                                }
                            } catch (e: Exception) {
                                showDialog = false
                                Toast.makeText(
                                    context,
                                    e.message,
                                    Toast.LENGTH_SHORT
                                ).show()
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
                    color = colorResource(R.color.purple_200),
                    fontFamily = HeadingFont
                )
            }
        }
    }


    if (showDialog) {
        LoadingRegisterDialog()
    }
}

@Composable
fun LoadingRegisterDialog() {
    Dialog(onDismissRequest = { }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = colorResource(R.color.background))
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .wrapContentSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(stringResource(R.string.register_loading), fontSize = 18.sp, fontWeight = FontWeight.Medium,
                    fontFamily = HeadingFont)
            }
        }
    }
}


@Preview
@Composable
fun RegistPrewiew() {
    MyApplicationTheme {
        LoadingRegisterDialog()
        //RegisterScreen(rememberNavController())
    }
}