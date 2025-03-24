package com.example.myapplication.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.*
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.R
import com.example.myapplication.loginResponse
import kotlinx.coroutines.launch


@Composable
fun PasswordTextField(field: String, onValueChange: (String) -> Unit, text: String) {

    val passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = field,
        onValueChange = onValueChange,
        label = { Text(text) },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
        trailingIcon = {
           // val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
//            IconButton(onClick = { passwordVisible = !passwordVisible }) {
//                Icon(imageVector = icon, contentDescription = "Показать/скрыть пароль")
//            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@Composable
fun LoginScreen(navController: NavController) {

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Column(
        modifier = Modifier.padding(20.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val username = remember { mutableStateOf(TextFieldValue()) }
        val password = remember { mutableStateOf(TextFieldValue()) }
        val passwordVisibility = remember { mutableStateOf(true) }

        Text(text = stringResource(R.string.login_head_text),
            fontFamily = HeadingFont,
            color = colorResource(R.color.grey_text),
            fontSize = 7.em)

        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            label = { Text(text = stringResource(R.string.login_mes)) },
            value = username.value,
            onValueChange = { username.value = it })

        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = password.value,
            onValueChange = {
                password.value = it
            },
            label = {
                Text(text = stringResource(R.string.password))
            },
            trailingIcon = {
                IconButton(onClick = {
                    passwordVisibility.value = !passwordVisibility.value
                }) {
                    Icon(
                        painter =  if (passwordVisibility.value) painterResource(R.drawable.ic_visibility_off) else painterResource(R.drawable.ic_visibility),
                        //imageVector = if (passwordVisibility.value) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "visibility",
                    )
                }
            },
            visualTransformation = if (passwordVisibility.value) PasswordVisualTransformation() else VisualTransformation.None
        )

        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
            Button(
                onClick = {
                    scope.launch {
                        try {
                            val success = loginResponse(username.toString(), password.toString())
                            if (success) {
                                navController.navigate(Routes.Send.route){
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        } catch (e:Exception){
                            snackbarHostState.showSnackbar(
                                message = e.message.toString(),
                                actionLabel = "Закрыть",
                                duration = SnackbarDuration.Indefinite
                            )
                        }

                } },
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.blue_button))
            ) {
                Text(text = stringResource(R.string.to_enter))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Box(contentAlignment = Alignment.BottomCenter) {
            TextButton(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(20.dp),
                onClick = {
                    navController.navigate(Routes.Regist.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },)
            {
                Text(text = AnnotatedString(stringResource(R.string.to_regist)), style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Default,
                    textDecoration = TextDecoration.Underline,
                    color = colorResource(R.color.purple_200)
                ))
            }
        }
//        ClickableText(
//            text = AnnotatedString("Forgot password?"),
//            onClick = { },
//            style = TextStyle(
//                fontSize = 14.sp,
//                fontFamily = FontFamily.Default
//            )
//        )
    }
}

@Preview(showBackground = true)
@Composable
fun AuthPreview() {
    MyApplicationTheme {
        LoginScreen(rememberNavController())
    }
}