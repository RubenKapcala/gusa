package com.kapgusa.fiesta.modelo

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.graphics.*
import androidx.core.content.ContextCompat
import com.kapgusa.fiesta.R
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


data class Mapa(
        val nombre: String,
        val descripcion: String,
        val casillas: List<Int>,
        val picante: Boolean,
        val personalizado: Boolean,
        val id: Int = 0

){

    companion object {
        class Coordenada(val x: Int, val y: Int)

        val coordenadasCasillas = listOf(
                Coordenada(14, 16), Coordenada(16, 16), Coordenada(18, 16), Coordenada(20, 16), Coordenada(22, 14),
                Coordenada(22, 12), Coordenada(22, 10), Coordenada(22, 8), Coordenada(22, 6), Coordenada(22, 4),
                Coordenada(20, 2), Coordenada(18, 2), Coordenada(16, 2), Coordenada(14, 2), Coordenada(12, 2),
                Coordenada(10, 2), Coordenada(8, 2), Coordenada(6, 2), Coordenada(4, 2), Coordenada(2, 4),
                Coordenada(2, 6), Coordenada(2, 8), Coordenada(2, 10), Coordenada(2, 12), Coordenada(2, 14),
                Coordenada(4, 16), Coordenada(6, 16), Coordenada(8, 16), Coordenada(10, 16),

                Coordenada(14, 12), Coordenada(16, 12), Coordenada(18, 12), Coordenada(18, 10), Coordenada(18, 8),
                Coordenada(16, 6), Coordenada(14, 6), Coordenada(12, 6), Coordenada(10, 6), Coordenada(8, 6),
                Coordenada(6, 6), Coordenada(6, 8), Coordenada(6, 10), Coordenada(8, 12), Coordenada(10, 12))


        fun crearImagenMapa(mapa: Mapa, context: Context): Bitmap {

            //Creamos el bitmap
            var bitmapPrincipal = Bitmap.createBitmap(1650, 900, Bitmap.Config.ARGB_8888)
            bitmapPrincipal = bitmapPrincipal.copy(bitmapPrincipal.config, true)
            val canvas = Canvas(bitmapPrincipal)
            var dest: Rect
            var bitmapSecundario: Bitmap

            //Pintamos las casillas
            for ((i, casilla) in mapa.casillas.withIndex()) {
                bitmapSecundario = BitmapFactory.decodeResource(context.resources, Reto.imagenes[casilla])
                dest = Rect(50 * coordenadasCasillas[i].x - 25, 50 * coordenadasCasillas[i].y - 25, 50 * coordenadasCasillas[i].x + 25, 50 * coordenadasCasillas[i].y + 25)
                canvas.drawBitmap(bitmapSecundario, null, dest, null)
            }

            bitmapPrincipal = bitmapPrincipal.copy(Bitmap.Config.ARGB_8888, false)

            return bitmapPrincipal
        }


        fun guardarImagen(nombre: String, imagen: Bitmap, context: Context): String {
            val cw = ContextWrapper(context)
            val dirImages = cw.getDir("Imagenes", Context.MODE_PRIVATE)
            val myPath = File(dirImages, "$nombre.png")
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

        fun cargarImagen(ruta: String): Bitmap {
            return BitmapFactory.decodeFile(ruta)
        }

        fun borrarImagen(ruta: String) {
            val file = File(ruta)
            file.delete()
        }

        fun cambiarCasilla(context: Context, bitmap: Bitmap, casilla: Int, tipoReto: Reto.TipoReto): Bitmap{

            val bitmapPrincipal = bitmap.copy(bitmap.config, true)
            val canvas = Canvas(bitmapPrincipal)
            val dest = Rect(
                    50 * coordenadasCasillas[casilla].x - 25,
                    50 * coordenadasCasillas[casilla].y - 25,
                    50 * coordenadasCasillas[casilla].x + 25,
                    50 * coordenadasCasillas[casilla].y + 25
            )
            val clearPaint = Paint()
            clearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            canvas.drawRect(dest, clearPaint)

            val bitmapSecundario = BitmapFactory.decodeResource(context.resources, Reto.imagenes[tipoReto.ordinal])
            canvas.drawBitmap(bitmapSecundario, null, dest, null)

            return bitmapPrincipal
        }
    }

}
