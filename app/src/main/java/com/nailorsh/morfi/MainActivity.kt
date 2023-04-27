package com.nailorsh.morfi

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val d: Int
    val lenStr1: Int = str1.length
    val lenStr2: Int = str2.length
    val minLen: Int = min(lenStr1, lenStr2)

    when (minLen) {
        1 -> d = 1
        in 2..3 -> d = 2
        in 4..5 -> d = 3
        in 6..7 -> d = 4
        in 8..12 -> d = 5
        else -> d = 6
    }

    for (i in 0..(str1.length - d)) {
        val subStr = str1.substring(i, i + d)
        if (str2.contains(subStr)) {
            return true
        }
    }
    return false
}

fun LongNonSingleRoot(
    Words: ArrayList<String>,
    lang: String = "english",
    n: Int = 10
): ArrayList<Pair<String, Int>> {
    val big = BigWords(Words)
    val stemmer = SStem(lang)
    val result = ArrayList<Pair<String, Int>>()
    val resultStem = ArrayList<String>()
    while (result.size != n && big.size() != 0) {
        val buf = big.nextWord()
        val bufStem = if (buf.length > 4) stemmer.stem(buf) else buf
        var flag = true
        val del = ArrayList<Int>()
        for (i in 0 until resultStem.size)
            if (rootCompare(bufStem, resultStem[i])) {
                if (!flag && bufStem.length < 6) {
                    del.add(i)
                }
                flag = false
            }
        del.reverse()
        for (i in del) {
            result.remove(result[i])
            resultStem.remove(resultStem[i])
        }
        if (flag) {
            result.add(Pair(buf, big.count_in_text(buf)))
            resultStem.add(bufStem)
        }
        var len = 0
        var cou = 0
        while (result.size == n && big.canNextMin() && len < 6 && cou < 1000) {
            val min = stemmer.stem(big.nextMinWord())
            flag = true
            for (i in 0 until resultStem.size)
                if (rootCompare(min, resultStem[i])) {
                    if (!flag && min.length in 4..6) {
                        del.add(i)
                    }
                    flag = false
                }
            del.reverse()
            for (i in del) {
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

    var expanded by remember { mutableStateOf(false) }
    val languageList = listOf("Русский", "English", "Другие")
    var languageName: String by remember { mutableStateOf(languageList[0]) }

    var resultText by remember { mutableStateOf("") }

//    var word1 by remember { mutableStateOf("") }
//    var word2 by remember { mutableStateOf("") }
//    var word3 by remember { mutableStateOf("") }
//    var word4 by remember { mutableStateOf("") }
//    var word5 by remember { mutableStateOf("") }
//    var word6 by remember { mutableStateOf("") }
//    var word7 by remember { mutableStateOf("") }
//    var word8 by remember { mutableStateOf("") }
//    var word9 by remember { mutableStateOf("") }
//    var word10 by remember { mutableStateOf("") }
//
//    var frequency1 by remember { mutableStateOf("") }
//    var frequency2 by remember { mutableStateOf("") }
//    var frequency3 by remember { mutableStateOf("") }
//    var frequency4 by remember { mutableStateOf("") }
//    var frequency5 by remember { mutableStateOf("") }
//    var frequency6 by remember { mutableStateOf("") }
//    var frequency7 by remember { mutableStateOf("") }
//    var frequency8 by remember { mutableStateOf("") }
//    var frequency9 by remember { mutableStateOf("") }
//    var frequency10 by remember { mutableStateOf("") }

    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            val mimeType: String? = context.contentResolver.getType(uri)
            val inputStream = context.contentResolver.openInputStream(uri)
            val bufferedReader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))

            when (mimeType) {
                "text/plain" -> {
                    val fileText = inputStream?.bufferedReader()?.readText()
                    val words = fileText?.split("[\\W\\d]+".toRegex())
                        ?.map { it.lowercase(Locale.getDefault()) }
                        ?.toCollection(ArrayList<String>()) ?: emptyList()



                    val resultList =
                        LongNonSingleRoot(words as ArrayList<String>, lang = "russian", n = 10)
                    val (strings, ints) = resultList.map { (string, int) -> string to int }.unzip()

                    for (i in resultList) {
                        resultText += "${i.first} | ${i.first.length} | ${i.second}\n"
                    }

//                    word1 = strings[0]
//                    word2 = strings[1]
//                    word3 = strings[2]
//                    word4 = strings[3]
//                    word5 = strings[4]
//                    word6 = strings[5]
//                    word7 = strings[6]
//                    word8 = strings[7]
//                    word9 = strings[8]
//                    word10 = strings[9]
//
//                    frequency1 = "${ints[0]}"
//                    frequency2 = "${ints[1]}"
//                    frequency3 = "${ints[2]}"
//                    frequency4 = "${ints[3]}"
//                    frequency5 = "${ints[4]}"
//                    frequency6 = "${ints[5]}"
//                    frequency7 = "${ints[6]}"
//                    frequency8 = "${ints[7]}"
//                    frequency9 = "${ints[8]}"
//                    frequency10 = "${ints[9]}"
                }

            }
        }
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = image,
            contentDescription = "NV",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(top = 25.dp)
                .size(200.dp)
                .clip(CircleShape)
                .border(0.5.dp, Color.Black, CircleShape)

        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, top = 70.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    openFileLauncher.launch("*/*")
                },
                modifier = Modifier.weight(3f)
            ) {
                Text(
                    text = stringResource(R.string.choose_file_button)
                )
            }

            Box(
                modifier = Modifier
                    .weight(2f)
                    .padding(30.dp)
            ) {
                Row(
                    Modifier.clickable { // Anchor view
                        expanded = !expanded
                    }
                ) { // Anchor view
                    Text(text = languageName) // City name label
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Показать меню"
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        }
                    ) {
                        languageList.forEach { language ->
                            DropdownMenuItem(
                                onClick = {
                                    expanded = false
                                    languageName = language
                                }) {

                                val isSelected = language == languageName
                                val style = if (isSelected) {
                                    MaterialTheme.typography.body1.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colors.secondary
                                    )
                                } else {
                                    MaterialTheme.typography.body1.copy(
                                        fontWeight = FontWeight.Normal,
                                        color = MaterialTheme.colors.onSurface
                                    )
                                }
                                Text(text = language, style = style)
                            }
                        }
                    }
                }
            }

        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 5.dp, end = 5.dp, top = 50.dp, bottom = 10.dp)
        ) {
            Text(
                text = stringResource(R.string.result_text),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                modifier = Modifier
                    .padding(start = 10.dp, bottom = 5.dp)
            )
            Text(
                text = "(слово | длина | частота)",
                fontStyle = FontStyle.Italic,
                modifier = Modifier
                    .padding(start = 15.dp, bottom = 10.dp)
            )

            Text(
                text = resultText,
                modifier = Modifier
                    .padding(start = 5.dp, bottom = 10.dp)
            )
        }

//        Column(
//            Modifier
//                .fillMaxWidth()
//                .padding(start = 5.dp, end = 5.dp, top = 50.dp, bottom = 10.dp)
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Text(
//                    text = stringResource(R.string.result_words_title),
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Bold,
//                    fontStyle = FontStyle.Italic,
//                    modifier = Modifier
//                        .weight(3f)
//                        .padding(start = 10.dp, bottom = 5.dp)
//                )
//
//                Text(
//                    text = stringResource(R.string.result_frequences_title),
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Bold,
//                    fontStyle = FontStyle.Italic,
//                    modifier = Modifier
//                        .weight(1f)
//                        .padding(start = 5.dp, bottom = 5.dp)
//
//                )
//
//            }

//            ComposableResultWordsAndFrequency(word1, frequency1)
//            ComposableResultWordsAndFrequency(word2, frequency2)
//            ComposableResultWordsAndFrequency(word3, frequency3)
//            ComposableResultWordsAndFrequency(word4, frequency4)
//            ComposableResultWordsAndFrequency(word5, frequency5)
//            ComposableResultWordsAndFrequency(word6, frequency6)
//            ComposableResultWordsAndFrequency(word7, frequency7)
//            ComposableResultWordsAndFrequency(word8, frequency8)
//            ComposableResultWordsAndFrequency(word9, frequency9)
//            ComposableResultWordsAndFrequency(word10, frequency10)

    }
}


//@Composable
//fun ComposableResultWordsAndFrequency(word: String, frequency: String) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .border(width = 1.dp, color = Color.Gray),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically
//
//    ) {
//        Text(
//            text = "  $word",
//            fontStyle = FontStyle.Italic,
//            modifier = Modifier
//                .padding(start = 1.dp)
//                .weight(3f)
//                .border(width = 1.dp, color = Color.Gray)
//        )
//        Text(
//            text = "  $frequency",
//            modifier = Modifier
//                .padding(start = 2.dp)
//                .weight(1f)
//        )
//    }
//}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MorfiTheme {
    }
}