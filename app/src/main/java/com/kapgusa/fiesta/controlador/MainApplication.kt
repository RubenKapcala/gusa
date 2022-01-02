package com.kapgusa.fiesta.controlador

import android.content.Context

object MainApplication {

    private lateinit var applicationContext: Context

    fun setApplicationContext(context: Context){
        applicationContext = context
    }

    fun applicationContext() : Context {
        return applicationContext
    }
}