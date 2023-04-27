package com.nailorsh.morfi.stemmers

import kotlin.text.Regex

class SStem(lang: String) {
    var lang: String
    init {
        this.lang = lang
    }

    val rvre = Regex("^(.*?[аеиоуыэюя])(.*)$")
    val r1re = Regex("^.*?[аеиоуыэюя][^аеиоуыэюя](.*)$")
    val vovel = "аеиоуыэюя"
    val perfectiveground = Regex("(ав|авши|авшись|яв|явши|явшись|ив|ивши|ившись|ыв|ывши|ывшись)$")
    val reflexive = Regex("(с[яь])$")
    val adjective = Regex("(ее|ие|ые|ое|ими|ыми|ей|ий|ый|ой|ем|им|ым|ом|его|ого|ему|ому|их|ых|ую|юю|ая|яя|ою|ею)$")
    val verb = Regex("(ила|ыла|ена|ейте|уйте|ите|или|ыли|ей|уй|ил|ыл|им|ым|ен|ило|ыло|ено|ят|ует|уют|ит|ыт|ены|ить|ыть|ишь|ую|ю|ала|ана|аете|айте|али|ай|ал|аем|ан|ало|ано|ает|ают|аны|ать|аешь|анно|яла|яна|яете|яйте|яли|яй|ял|яем|ян|яло|яно|яет|яют|яны|ять|яешь|янно)$")
    val noun = Regex("(а|ев|ов|ие|ье|е|иями|ями|ами|еи|ии|и|ией|ей|ой|ий|й|иям|ям|ием|ем|ам|ом|о|у|ах|иях|ях|ы|ь|ию|ью|ю|ия|ья|я)$")
    val i = Regex("и$")
    val derivational = Regex("(ост|ость)$")
    val superlative = Regex("(ейш|ейше)$")
    val nn = Regex("н(н)$")
    val n = Regex("(н)$")
    val p = Regex("ь$")

    fun rv(word: String): String = rvre.find(word)?.groupValues?.lastOrNull() ?: ""

    fun r1(word: String): String = r1re.find(word)?.groupValues?.lastOrNull() ?: ""

    fun r2(word: String): String? {
        val r1part = r1(word)
        return if (r1part.isNotEmpty()) r1(r1part) else null
    }

    fun del(word: String, regexp: Regex): String = regexp.replace(word, "")

    fun step1(word: String): String {
        return perfectiveground.find(word)?.let { word.replace(it.value, "") }
            ?: reflexive.find(word)?.let { word.replace(it.value, "") }
            ?: adjective.find(word)?.let { word.replace(it.value, "") }
            ?: verb.find(word)?.let { word.replace(it.value, "") }
            ?: noun.find(word)?.let { word.replace(it.value, "") }
            ?: word
    }

    fun step2(word: String): String = i.replace(word, "")

    fun step3(word: String): String {
        val r2part = r2(word)
        return if (r2part != null) {
            if (derivational.containsMatchIn(r2part)) {
                del(word, derivational)
            } else {
                word
            }
        } else {
            word
        }
    }

    fun step4a(word: String): String {
        return if (nn.find(word) != null) {
            nn.replace(word, "")
        } else {
            word
        }
    }

    fun step4b(word: String): String {
        return if (superlative.find(word) != null) {
            step4a(superlative.replace(word, ""))
        } else {
            word
        }
    }

    fun step4c(word: String): String {
        return if (p.find(word) != null) {
            p.replace(word, "")
        } else {
            word
        }
    }

    fun stem(word: String): String {
        return if (rv(word).isEmpty())
            word
        else step4c(step4b(step4a(step3(step2(step1(word))))))
    }

}