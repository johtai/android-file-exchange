package com.example.myapplication.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.example.myapplication.R
import com.example.myapplication.sendingData
import kotlinx.coroutines.launch

@Composable
fun ConfirmReceiveDialog(onDismiss: () -> Unit){

    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {TextButton(onClick = {
            scope.launch {
                onDismiss()
                sendingData.receiveData()
//                navController.navigate(Routes.Login.route) {
//                    popUpTo(navController.graph.startDestinationId)
//                    launchSingleTop = true
//                }
            }
        })
        {Text(stringResource(R.string.yes), fontFamily = HeadingFont)} },
        dismissButton = {
            TextButton(onClick = {
                scope.launch {
                    onDismiss()
                    sendingData.sentRejection()
                }
            })
        {Text(stringResource(R.string.no), fontFamily = HeadingFont)}},
        title = {Text(
            stringResource(R.string.receive_data),
            fontFamily = HeadingFont
        )},
        text = {Text(stringResource(R.string.receive_data_confirm), fontFamily = HeadingFont)},
        containerColor = colorResource(R.color.background)
    )
}

@Composable
fun ReceiveDataDialog(onDismiss: () -> Unit){

    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            if (!sendingData.downloadingInProcess) {
                TextButton(onClick = {
                    scope.launch {
                        sendingData.saveFile()
                    }
                })
                { Text(stringResource(R.string.save_file), fontFamily = HeadingFont) }
            }
        },
        dismissButton = {
            if(!sendingData.downloadingInProcess){
                TextButton(onClick = {
                    scope.launch {
                        sendingData.saveFile()
                    }
                })
                { Text(stringResource(R.string.cancel), fontFamily = HeadingFont) }
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painter =
                if(sendingData.downloadingInProcess)
                    painterResource(R.drawable.baseline_download_24)
                else
                    painterResource(R.drawable.baseline_download_done_24),
                    contentDescription = "download")
                Spacer(Modifier.width(15.dp))
                Text(
                    if (sendingData.downloadingInProcess) stringResource(R.string.downloading) else stringResource(
                        R.string.loading_ready
                    ), fontFamily = HeadingFont
                )
            }},
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (sendingData.downloadingInProcess) {
                        CircularProgressIndicator()
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.filename)+" " + sendingData.filename, fontFamily = HeadingFont)
                if(!sendingData.downloadingInProcess)Text(stringResource(R.string.filesize)+" " + sendingData.byteArray.sumOf{it.size}, fontFamily = HeadingFont)
                Text(stringResource(R.string.package_count)+" " + sendingData.allPackages, fontFamily = HeadingFont)
                Text(stringResource(R.string.receive_package_count)+" " + sendingData.sentPackages, fontFamily = HeadingFont)
            }
        },
        containerColor = colorResource(R.color.background)
    )
}

@Preview
@Composable
fun ReceiveDataAD(){
    var showRDAL by remember { mutableStateOf(false) }
    ReceiveDataDialog({showRDAL = false})

}