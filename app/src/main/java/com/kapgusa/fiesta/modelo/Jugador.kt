package com.kapgusa.fiesta.modelo

data class Jugador(
        val nombre: String,
        val isChico: Boolean,
        var gustos: Gustos,
        var id: Int? = null
){
    var posicion: Int = 0
    var monedas: Int = 0
    var preparado = false
}
