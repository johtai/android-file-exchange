package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.screens.LoginScreen
import com.example.myapplication.screens.RegisterScreen
import com.example.myapplication.screens.Routes
import com.example.myapplication.screens.SendingDataScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = false) {
                LoginAndRegistration()
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            createClient()
        }
        TokenStorage.init(this)
    }
}

@Composable
fun LoginAndRegistration() {
    val navController = rememberNavController()
    val beggingScreen = if(loadAccessToken() == "")  Routes.Login.route else Routes.Send.route

    NavHost(navController = navController, startDestination = beggingScreen, builder = {
        composable(Routes.Login.route, content =  { LoginScreen(navController) })
        composable(Routes.Regist.route, content = { RegisterScreen(navController) })
        composable(Routes.Send.route, content = { SendingDataScreen(navController) })
    })
}