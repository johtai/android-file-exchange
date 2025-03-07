import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay


@Composable
fun LoadingDialog(onDismiss: () -> Unit) {
    val messages = listOf(
        "Инициализация...",
        "Загрузка данных...",
        "Обработка информации...",
        "Финализация..."
    )

    var currentMessage by remember { mutableStateOf(messages[0]) }
    var step by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (step < messages.size) {
            delay(1000) // Ждем 1 секунду

            currentMessage = messages[step]
            step++
        }
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
                Text(text = currentMessage, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}