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
import com.nailorsh.morfi.stemmers.BigWords
import com.nailorsh.morfi.stemmers.SStem
import com.nailorsh.morfi.ui.theme.MorfiTheme
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.math.min
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

//import nl.siegmann.epublib.domain.Book
//import nl.siegmann.epublib.epub.EpubReader

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

fun rootCompare(str1: String, str2: String): Boolean {
    val d : Int
    val lenStr1 : Int = str1.length
    val lenStr2 : Int = str2.length
    val minLen : Int = min(lenStr1, lenStr2)

    when (minLen) {
        1 -> d = 1
        in 2..3 -> d = 2
        in 4..5 -> d = 3
        in 6..7 -> d = 4
        in 8..12 -> d = 5
        else -> d = 6
    }

    for (i in 0..(str1.length-d)) {
        val subStr = str1.substring(i, i+d)
        if (str2.contains(subStr)) {
            return true
        }
    }
    return false
}

fun LongNonSingleRoot(Words : ArrayList<String>,lang : String = "english", n : Int = 10) : ArrayList<Pair<String, Int>>{
    val big = BigWords(Words)
    val stemmer = SStem(lang)
    val result = ArrayList<Pair<String, Int>>()
    val resultStem = ArrayList<String>()
    while (result.size != n && big.size() != 0) {
        val buf = big.nextWord()
        val bufStem = if (buf.length > 4)  stemmer.stem(buf) else buf
        var flag = true
        val del = ArrayList<Int>()
        for (i in 0 until resultStem.size)
            if (rootCompare(bufStem, resultStem[i])) {
                if (!flag && bufStem.length < 6){
                    del.add(i)
                }
                flag = false
            }
        del.reverse()
        for (i in del){
            result.remove(result[i])
            resultStem.remove(resultStem[i])
        }
        if (flag) {
            result.add(Pair(buf, big.count_in_text(buf)))
            resultStem.add(bufStem)
        }
        var len = 0
        var cou = 0
        while (result.size == n && big.canNextMin() && len < 6 && cou < 1000){
            val min = stemmer.stem(big.nextMinWord())
            flag = true
            for (i in 0 until resultStem.size)
                if (rootCompare(min, resultStem[i])) {
                    if (!flag && min.length in 4..6){
                        del.add(i)
                    }
                    flag = false
                }
            del.reverse()
            for (i in del){
                result.remove(result[i])
                resultStem.remove(resultStem[i])
            }
            cou++
            len = min.length
        }
        big.startMin()
    }
    return result
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
            val bufferedReader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
            val wordsList = mutableListOf<String>()

            when(mimeType) {
                "text/plain" -> {
                    val fileText = inputStream?.bufferedReader()?.readText()
                    val words = fileText?.split("[\\W\\d]+".toRegex())
                        ?.map { it.lowercase(Locale.getDefault()) }
                        ?.toCollection(ArrayList<String>()) ?: emptyList()

                    val resultList = LongNonSingleRoot(words as ArrayList<String>, lang = "russian", n = 10)
                    val (strings, ints) = resultList.map { (string, int) -> string to int }.unzip()

                    text = strings.toString()
//                    val resultText = words.toString() // ?: "Empty file"
//                    val Arr = ArrayList<String>()
//                    File("").forEachLine { line ->
//                        val lineWords = line.split("[\\p{Punct}\\s]+".toRegex())
//                        lineWords.forEach { word ->
//                            Arr.add(word.lowercase())
//                        }
//                    }
//                    val utf8Bytes = fileText?.toByteArray(Charsets.UTF_8)
//                    val cyrillicText = utf8Bytes?.let { String(it, Charsets.UTF_8) }
//                    if (cyrillicText != null) {
//                        text = cyrillicText
//                    }
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