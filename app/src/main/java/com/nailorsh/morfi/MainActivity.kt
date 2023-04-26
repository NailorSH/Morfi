package com.nailorsh.morfi

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nailorsh.morfi.ui.theme.MorfiTheme
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MorfiTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting(this@MainActivity)
                }
            }
        }
    }
}

@Composable
fun Greeting(context: Context) {
    val image = painterResource(R.drawable.nv_command)

    var text by remember {
        mutableStateOf("Hello")
    }

    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            val mimeType: String? = context.contentResolver.getType(uri)
            val inputStream = context.contentResolver.openInputStream(uri)
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val wordsList = mutableListOf<String>()

            when(mimeType) {
                "text/plain" -> {
                    val fileText = inputStream?.bufferedReader()?.readText()
                    val words = fileText?.split("[\\W\\d]+".toRegex()) ?: emptyList()
                    text = words.toString() // ?: "Empty file"
                }

//                "application/epub+zip" -> {
//                    val reader = EpubReader()
//                    val book = reader.readEpub(inputStream)
//                    val content = book.contents.filter { it.mediaType == "application/xhtml+xml" }
//                    content.flatMap { it.read().words() }
////                    text = "$mimeType"
//                }
                else -> text = "$mimeType"
            }
//            val fb2Book = FictionBook.read(inputStream)
//            val byteArray = inputStream?.readBytes()

//            bufferedReader.useLines { lines ->
//                lines.forEach {
//                    wordsList.addAll(it.split("\\s".toRegex()).map { word -> word.trim() })
//                }
//            }
//            text = mimeType + " |||  " + wordsList.joinToString(", ")
        }
    )

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
                openFileLauncher.launch("*/*")
            },
            modifier = Modifier.padding(top = 100.dp)
        ) {
            Text(
                text = stringResource(R.string.choose_file_button)
            )
        }
        Text(
            text = text,
            modifier = Modifier.padding(top = 20.dp)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MorfiTheme {
    }
}