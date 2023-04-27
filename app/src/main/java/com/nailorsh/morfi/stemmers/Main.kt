import kotlin.math.min
import java.io.File

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

fun LongNonSingleRoot(Words : ArrayList<String>, lang : String = "English", n : Int = 10) : ArrayList<Pair<String, Int>>{
    val big = BigWords(Words)
    val stemmer = Class.forName( lang + "Stemmer").newInstance() as Stem
    val result = ArrayList<Pair<String, Int>>()
    val resultStem = ArrayList<String>()
    while (result.size != n && big.size() != 0) {
        val buf = big.nextWord()
        val bufStem = if (buf.length > 4)  stemmer.getStem(buf) else buf
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
            val min = stemmer.getStem(big.nextMinWord())
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

fun main(args: Array<String>) {
    val Arr = ArrayList<String>()
    File("C:/Users/Viktor/Downloads/libstemmer-main/libstemmer-main/input.txt").forEachLine { line ->
        val lineWords = line.split("[\\p{Punct}\\s]+".toRegex())
        lineWords.forEach { word ->
            Arr.add(word.lowercase())
        }
    }

    for (i in LongNonSingleRoot(Arr, n = 10))
        println(i.first + " " + i.first.length + " " + i.second)
}