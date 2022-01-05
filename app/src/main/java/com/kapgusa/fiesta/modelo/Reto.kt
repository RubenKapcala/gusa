package com.kapgusa.fiesta.modelo

import com.kapgusa.fiesta.R

data class Reto(
    val texto: List<String?>,
    val tipo: TipoReto,
    val nivelPicante: Int,
    val nivelBeber: Int,
    val monedasSiempre: Int,
    val monedasReto: Int,
    val presencial: Boolean,
    val personalizado: Boolean,
    val id: Int = 0
){
    //Diferentes retos
    enum class TipoReto{BEBER, ALEATORIO, ESTRELLA, BAUL, PICANTE, PRENDA, EVENTO_INICIAL}

    companion object{

        //Imágenes de los diferentes retos
        val imagenes = listOf(
                R.drawable.casilla_beber, R.drawable.casilla_aleatoria, R.drawable.casilla_estrella,
                R.drawable.casilla_baul, R.drawable.casilla_picante, R.drawable.casilla_prenda
        )
    }
}
