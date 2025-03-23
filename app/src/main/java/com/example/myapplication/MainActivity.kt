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
            MyApplicationTheme {
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

    NavHost(navController = navController, startDestination = Routes.Login.route, builder = {
        composable(Routes.Login.route, content =  { LoginScreen(navController = navController) })
        composable(Routes.Regist.route, content = { RegisterScreen(navController = navController) })
        composable(Routes.Send.route, content = { SendingDataScreen() })
    })
}


