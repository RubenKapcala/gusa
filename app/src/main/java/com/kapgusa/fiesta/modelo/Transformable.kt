package com.kapgusa.fiesta.modelo

import com.google.gson.Gson

interface Transformable {
    //Devuelve un String del objeto en formato JSON
    fun toJson(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
}