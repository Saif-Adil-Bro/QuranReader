package com.example.data

object HafeziQuranData {
    fun getParaStartPage(para: Int, fatihaPage: Int): Int {
        if (para < 1 || para > 30) return fatihaPage
        
        var startPage = fatihaPage
        for (i in 1 until para) {
            startPage += getParaLength(i)
        }
        return startPage
    }

    fun getParaLength(para: Int): Int {
        return when (para) {
            1 -> 21
            29 -> 24
            30 -> 25
            else -> 20
        }
    }
}
