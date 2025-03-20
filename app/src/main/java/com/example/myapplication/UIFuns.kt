package com.example.myapplication

import android.widget.ImageView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


//красивенький шрифт
val HeadingFont = FontFamily(
    Font(R.font.ofont_nunito, FontWeight.Normal)
)

//поле ввода, подходит для любого
@Composable
fun InputField(field: String, onValueChange: (String) -> Unit, text: String) {
    OutlinedTextField(
        value = field,
        onValueChange = onValueChange,
        label = { Text(text) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}
//
//@Composable
//fun ProccessAnim() {
//    val context = LocalContext.current
//    val imageLoader = ImageLoader.Builder(context)
//        .components { add(GifDecoder.Factory()) } // Добавляем поддержку GIF
//        .build()
//
//    AsyncImage(
//        model = R.drawable.right_arrow_6844383,
//        contentDescription = "Загрузка",
//        modifier = Modifier.size(200.dp)
//    )
//}

//
//@Composable
//fun AcceptAnim() {
//    AsyncImage(
//        model = R.drawable.verified_7920939,
//        contentDescription = "Ок",
//        modifier = Modifier.size(200.dp)
//    )
//}