class EnglishStemmer() : Stem{
    override fun getStem(str: String): String {
        val s = Stemmer()
        s.add(str.toCharArray(), str.length)
        return s.stem()
    }
}

private const val INC = 25
private class Stemmer {
    var resultBuffer: CharArray
        private set
    private var i = 0
    var resultLength = 0
        private set

    private var j = 0
    private var k = 0

    init {
        resultBuffer = CharArray(INC)
    }

    fun add(w: CharArray, wLen: Int) {
        if (i + wLen >= resultBuffer.size) {
            val new_b = CharArray(i + wLen + INC)
            for (c in 0 until i) new_b[c] = resultBuffer[c]
            resultBuffer = new_b
        }
        for (c in 0 until wLen) resultBuffer[i++] = w[c]
    }

    override fun toString(): String {
        return String(resultBuffer, 0, resultLength)
    }

    private fun cons(i: Int): Boolean {
        return when (resultBuffer[i]) {
            'a', 'e', 'i', 'o', 'u' -> false
            'y' -> if (i == 0) true else !cons(i - 1)
            else -> true
        }
    }

    private fun m(): Int {
        var n = 0
        var i = 0
        while (true) {
            if (i > j) return n
            if (!cons(i)) break
            i++
        }
        i++
        while (true) {
            while (true) {
                if (i > j) return n
                if (cons(i)) break
                i++
            }
            i++
            n++
            while (true) {
                if (i > j) return n
                if (!cons(i)) break
                i++
            }
            i++
        }
    }

    private fun vowelinstem(): Boolean {
        var i: Int
        i = 0
        while (i <= j) {
            if (!cons(i)) return true
            i++
        }
        return false
    }

    private fun doublec(j: Int): Boolean {
        if (j < 1) return false
        return if (resultBuffer[j] != resultBuffer[j - 1]) false else cons(j)
    }

    private fun cvc(i: Int): Boolean {
        if (i < 2 || !cons(i) || cons(i - 1) || !cons(i - 2)) return false
        run {
            val ch = resultBuffer[i].code
            if (ch == 'w'.code || ch == 'x'.code || ch == 'y'.code) return false
        }
        return true
    }

    private fun ends(s: String): Boolean {
        val l = s.length
        val o = k - l + 1
        if (o < 0) return false
        for (i in 0 until l) if (resultBuffer[o + i] != s[i]) return false
        j = k - l
        return true
    }

    private fun setto(s: String) {
        val l = s.length
        val o = j + 1
        for (i in 0 until l) resultBuffer[o + i] = s[i]
        k = j + l
    }

    private fun r(s: String, n: Int = 0) {
        if (m() > n) setto(s)
    }

    private fun step1() {
        if (resultBuffer[k] == 's') {
            if (ends("sses")) k -= 2 else if (ends("ies")) setto("i") else if (resultBuffer[k - 1] != 's') k--
        }
        if (ends("eed")) {
            if (m() > 0) k--
        } else if ((ends("ed") || ends("ing")) && vowelinstem()) {
            k = j
            if (ends("at")) setto("ate") else if (ends("bl")) setto("ble") else if (ends("iz")) setto("ize") else if (doublec(
                    k
                )
            ) {
                k--
                run {
                    val ch = resultBuffer[k].code
                    if (ch == 'l'.code || ch == 's'.code || ch == 'z'.code) k++
                }
            } else if (m() == 1 && cvc(k)) setto("e")
        }
    }

    private fun step2() {
        if (ends("y") && vowelinstem()) resultBuffer[k] = 'i'
    }

    private fun step3() {
        if (k == 0) return
        when (resultBuffer[k - 1]) {
            'a' -> {
                if (ends("ational")) {
                    r("ate")

                }
                if (ends("tional")) {
                    r("tion")

                }
            }

            'c' -> {
                if (ends("enci")) {
                    r("ence")
                }
                if (ends("anci")) {
                    r("ance")
                }
            }

            'e' -> if (ends("izer")) {
                r("ize")
            }

            'l' -> {
                if (ends("bli")) {
                    r("ble")
                }
                if (ends("alli")) {
                    r("al")
                }
                if (ends("entli")) {
                    r("ent")
                }
                if (ends("eli")) {
                    r("e")
                }
                if (ends("ousli")) {
                    r("ous")
                }
            }

            'o' -> {
                if (ends("ization")) {
                    r("ize")
                }
                if (ends("ation")) {
                    r("ate")
                }
                if (ends("ator")) {
                    r("ate")
                }
            }

            's' -> {
                if (ends("alism")) {
                    r("al")
                }
                if (ends("iveness")) {
                    r("ive")
                }
                if (ends("fulness")) {
                    r("ful")
                }
                if (ends("ousness")) {
                    r("ous")
                }
            }

            't' -> {
                if (ends("aliti")) {
                    r("al")
                }
                if (ends("iviti")) {
                    r("ive")
                }
                if (ends("biliti")) {
                    r("ble")
                }
            }

            'g' -> if (ends("logi")) {
                r("log")
            }
        }
    }

    private fun step4() {
        when (resultBuffer[k]) {
            'e' -> {
                if (ends("icate")) {
                    r("ic")
                }
                if (ends("ative")) {
                    r("")
                }
                if (ends("alize")) {
                    r("al")
                }
            }

            'i' -> if (ends("iciti")) {
                r("ic")
            }

            'l' -> {
                if (ends("ical")) {
                    r("ic")
                }
                if (ends("ful")) {
                    r("")
                }
            }

            's' -> if (ends("ness")) {
                r("")
            }
        }
    }

    private fun step5() {
        if (k == 0) return
        when (resultBuffer[k - 1]) {
            'a' -> {
                if (ends("al")) if (m() > 1) k = j
                return
            }

            'c' -> {
                if (ends("ance")) {
                    if (m() > 1) k = j
                    return
                }
                if (ends("ence")) if (m() > 1) k = j
                return
            }

            'e' -> {
                if (ends("er")) if (m() > 1) k = j
                return
            }

            'i' -> {
                if (ends("ic")) if (m() > 1) k = j
                return
            }

            'l' -> {
                if (ends("able")) {
                    if (m() > 1) k = j
                    return
                }
                if (ends("ible")) if (m() > 1) k = j
                return
            }

            'n' -> {
                if (ends("ant")) {
                    if (m() > 1) k = j
                    return
                }
                if (ends("ement")) {
                    if (m() > 1) k = j
                    return
                }
                if (ends("ment")) {
                    if (m() > 1) k = j
                    return
                }
                if (ends("ent")) if (m() > 1) k = j
                return
            }

            'o' -> {
                if (ends("ion") && j >= 0 && ((resultBuffer[j]) == 's' || resultBuffer[j] == 't')) {
                    if (m() > 1) k = j
                    return
                }
                if (ends("ou")) if (m() > 1) k = j
                return
            }

            's' -> {
                if (ends("ism")) if (m() > 1) k = j
                return

            }

            't' -> {
                if (ends("ate")) {
                    if (m() > 1) k = j
                    return
                }
                if (ends("iti")) if (m() > 1) k = j
                return
            }

            'u' -> {
                if (ends("ous")) if (m() > 1) k = j
                return
            }

            'v' -> {
                if (ends("ive")) if (m() > 1) k = j
                return
            }

            'z' -> {
                if (ends("ize")) if (m() > 1) k = j
                return
            }

            else -> return
        }
    }

    private fun step6() {
        j = k
        if (resultBuffer[k] == 'e') {
            val a = m()
            if (a > 1 || a == 1 && !cvc(k - 1)) k--
        }
        if (resultBuffer[k] == 'l' && doublec(k) && m() > 1) k--
    }

    fun stem(): String {
        k = i - 1
        if (k > 1) {
            step1()
            step2()
            step3()
            step4()
            step5()
            step6()
        }
        resultLength = k + 1
        i = 0
        return String(resultBuffer, 0, resultLength)
    }
}
