class BigWords(Words: ArrayList<String>) {
    private val wordCount = HashMap<String, Int>()
    private val maxLen = 10000;
    private val sortedArr = IntArray(maxLen)
    private var BigWordArr = listOf("")
    private var count = 0
    private var sortWords = Array<String?>(1){it -> ""}
    private var countBig = 0
    private var countMin = 0
    init {
        val bigWordArr = ArrayList<String>()
        for (word in Words) {
            if (wordCount.containsKey(word)) {
                wordCount[word] = wordCount[word]!! + 1
            } else {
                wordCount[word] = 1
            }
        }
        for (word in wordCount) {
            if (word.key.length >= maxLen) {
                bigWordArr.add(word.key)
                countBig++
            }
            else {
                sortedArr[word.key.length]++
                count++
            }
        }
        val startsArr = IntArray(maxLen)
        startsArr[0] = 0
        for (i in 1 until maxLen)
            startsArr[i] = sortedArr[i - 1] + startsArr[i - 1]
        val sortedWords = arrayOfNulls<String>(count)
        for (word in wordCount)
            if (word.key.length < maxLen)
                sortedWords[startsArr[word.key.length]++] = word.key
        sortWords = sortedWords
        BigWordArr = bigWordArr.sortedBy { it.length }
    }

    fun nextWord() : String{
        if (countBig != 0)
            return BigWordArr[--countBig]
        return sortWords[--count]!!
    }

    fun nextMinWord() : String{
        if (countMin >= count)
            return BigWordArr[countMin++ - count]
        return sortWords[countMin++]!!
    }

    fun size() : Int{
        return count + countBig
    }

    fun canNextMin() : Boolean{
        return (count + countBig) - countMin > 0
    }

    fun count_in_text(s : String) : Int{
        return wordCount[s]!!
    }

    fun startMin(){
        countMin = 0
    }
}