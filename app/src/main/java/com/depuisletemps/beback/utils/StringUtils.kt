package com.depuisletemps.beback.utils

object StringUtils {

    fun capitalizeWord(s: String): String {
        val splits = s.toLowerCase().split(" ").toTypedArray()
        val sb = StringBuilder()
        for (i in splits.indices) {
            val eachWord = splits[i]
            if (i > 0 && eachWord.isNotEmpty()) {
                sb.append(" ")
            }
            val cap = (eachWord.substring(0, 1).toUpperCase()
                    + eachWord.substring(1))
            sb.append(cap)
        }
        return sb.toString()
    }

}