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
import androidx.compose.material3.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.myapplication.hello
import com.example.myapplication.loginResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(navController: NavController) {

    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(20.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val username = remember { mutableStateOf(TextFieldValue("Lovak")) }
        val password = remember { mutableStateOf(TextFieldValue("BestUser03")) }
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
                    showDialog = true
                    scope.launch {
                        try {
                            val status = loginResponse(username.value.text, password.value.text)
                            if (status == HttpStatusCode.OK) {
                                showDialog = false
                                errorMessage = ""
                                navController.navigate(Routes.Send.route){
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }

                        } catch (e:Exception){
                            errorMessage = e.message.toString()
                            showDialog = false
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
//    if(showDialog){
//        LoadingLoginDialog(onDismiss = {showDialog = false}, errorMessage)
//    }
}


@Composable
fun LoadingLoginDialog(
    onDismiss: () -> Unit,
    errorMessage: String
) {

    AlertDialog(
        onDismissRequest = { },
        confirmButton = {
            if (errorMessage != "") {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.blue_button))
                ) {
                    Text("ОК")
                }
            }
        },
        title = {
            Text(
                text =
                if (errorMessage == "")
                    "Вход..."
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
                    contentAlignment = Alignment.Center
                ) {
                    if (errorMessage == "") {
                        CircularProgressIndicator()
                    } else {
                        Text(errorMessage)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun AuthPreview() {
    MyApplicationTheme {
        LoginScreen(rememberNavController())
    }
}