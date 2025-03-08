import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.sendingData
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

@Composable
fun LoadingDialog(onDismiss: () -> Unit) {
    val maxCountPackages = 6

    var step by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        sendingData.sendData()
        onDismiss() // Закрываем диалог после последнего шага
    }

    Dialog(onDismissRequest = { /* Блокируем закрытие пользователем */ }) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "hi", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun LoadingDialog(isFinished: Boolean, onDismiss: () -> Unit) {

    AlertDialog(
        onDismissRequest = { },
        confirmButton = {
            if (isFinished) {
                Button(onClick = onDismiss) {
                    Text("ОК")
                }
            }
        },
        title = { Text(if (isFinished) "Готово" else "Отправляем...") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isFinished) {
                    CircularProgressIndicator()
                } else {
                    Text("Загрузка завершена!")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Всего пакетов: ${sendingData.allPackeges}")
                Text("Количество отправленных: ${sendingData.sendingPackeges}")
                Text("Повторно запрошено: ${sendingData.requestedPackages}")
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        var showDialog by remember { mutableStateOf(false) }
        var isFinished by remember { mutableStateOf(false) }
        LoadingDialog(isFinished, {showDialog = false})
    }
}