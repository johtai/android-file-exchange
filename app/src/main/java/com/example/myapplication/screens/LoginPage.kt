package com.example.myapplication.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.*
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.R
import com.example.myapplication.TokenStorage
import com.example.myapplication.hello
import com.example.myapplication.loginResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(navController: NavController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    val nameErrorState = remember { mutableStateOf(false) }
    val passwordErrorState = remember { mutableStateOf(false) }
    BackHandler(enabled = true) {  }

    Column(
        modifier = Modifier
            .padding(20.dp)
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
            fontSize = 7.em,
            lineHeight = 1.em)

        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            label = { Text(text = stringResource(R.string.login_mes)) },
            value = username.value,
            onValueChange = {
                if (nameErrorState.value) {
                    nameErrorState.value = false
                }
                username.value = it },
            isError = nameErrorState.value)

        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = password.value,
            onValueChange = {
                if (passwordErrorState.value) {
                    passwordErrorState.value = false
                }
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
            visualTransformation = if (passwordVisibility.value) PasswordVisualTransformation() else VisualTransformation.None,
            isError = passwordErrorState.value
        )

        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
            Button(
                onClick = {
                    if(username.value.text.trim().isEmpty())
                        nameErrorState.value = true
                    else if(password.value.text.trim().isEmpty())
                        passwordErrorState.value = true
                    else {
                        showDialog = true
                        scope.launch {
                            try {
                                val status = loginResponse(
                                    username.value.text.trim(),
                                    password.value.text.trim()
                                )
                                showDialog = false
                                if (status == HttpStatusCode.OK) {
                                //    if(true){
                                    TokenStorage.saveUser(username.value.text.trim())
                                    navController.navigate(Routes.Send.route) {
                                        popUpTo(navController.graph.startDestinationId)
                                        launchSingleTop = true
                                    }
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
                    } },
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.blue_button))
            ) {
                Text(text = stringResource(R.string.to_enter), fontFamily = HeadingFont)
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
    }
    if(showDialog){
        LoadingLoginDialog()
    }
}


@Composable
fun LoadingLoginDialog() {
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
                Text(stringResource(R.string.login_loading),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = HeadingFont
                    )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthPreview() {
    MyApplicationTheme {
        //LoadingLoginDialog()
        LoginScreen(rememberNavController())
    }
}