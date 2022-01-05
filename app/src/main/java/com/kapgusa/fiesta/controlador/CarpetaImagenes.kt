package com.kapgusa.fiesta.controlador

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

object CarpetaImagenes {

    //Guarda la imagen en la ruta especificada
    fun guardarImagen(nombre: String, imagen: Bitmap, context: Context?): String {
        val cw = ContextWrapper(context)
        val dirImagenes = cw.getDir("Imagenes", Context.MODE_PRIVATE)
        val myPath = File(dirImagenes, "$nombre.png")
        val fos: FileOutputStream?
        try {
            fos = FileOutputStream(myPath)
            imagen.compress(Bitmap.CompressFormat.PNG, 10, fos)
            fos.flush()
        } catch (ex: FileNotFoundException) {
            ex.printStackTrace()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return myPath.absolutePath
    }

    //Carga la imagen de la ruta especificada
    fun cargarImagen(ruta: String?): Bitmap? {
        return BitmapFactory.decodeFile(ruta)
    }

    //Borra la imagen en la ruta especificada
    fun borrarImagen(ruta: String) {
        val file = File(ruta)
        file.delete()
    }
}