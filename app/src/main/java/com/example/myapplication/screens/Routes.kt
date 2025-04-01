package com.example.myapplication.screens

sealed class Routes(val route:String) {
    object Login: Routes("login_screen")
    object Regist: Routes("register_screen")
    object Send: Routes("sending_data_screen")
    object Receive: Routes("receive_data_screen")
}