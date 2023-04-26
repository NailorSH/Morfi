package com.nailorsh.morfi

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nailorsh.morfi.ui.theme.MorfiTheme

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {
    private val REQUEST_CODE_FILE_PICKER = 100

    @SuppressLint("Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MorfiTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting(onFileSelected = { uri ->
                        val inputStream = contentResolver.openInputStream(uri)
                        val fileContent = inputStream?.bufferedReader().use { it?.readText() }
                        Toast.makeText(this, fileContent, Toast.LENGTH_LONG).show()
                    })
                }
            }
        }
    }

    fun onFileSelected() {

    }

    @Composable
    fun Greeting(onFileSelected: (Uri) -> Unit) {
        val image = painterResource(R.drawable.nv_command)

        var text by remember {
            mutableStateOf("Hello")
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = image,
                contentDescription = "NV",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .border(0.5.dp, Color.Black, CircleShape)
            )
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "*/*" // все типы файлов
                        putExtra(
                            Intent.EXTRA_MIME_TYPES,
                            arrayOf("text/plain", "application/x-fictionbook", "application/epub+zip")
                        )
                    }
                    startActivityForResult(Intent.createChooser(intent, "Выберите файл"), REQUEST_CODE_FILE_PICKER)
                },
                modifier = Modifier.padding(top = 100.dp)
            ) {
                Text(
                    text = "Выбрать файл"
                )
            }

            Text(
                text = text,
                modifier = Modifier.padding(top = 20.dp)
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_FILE_PICKER && resultCode == Activity.RESULT_OK) {
            val uri = data?.data // Uri выбранного файла
            if (uri != null) {
//                onFileSelected(uri)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MorfiTheme {
    }
}