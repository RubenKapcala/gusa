package com.kapgusa.fiesta.modelo

import java.io.Serializable

data class Jugador(
        val nombre: String,
        val isChico: Boolean,
        var gustos: Gustos,
        var id: Int? = null
): Serializable, Transformable {
    var posicion: Int = 0
    var monedas: Int = 0
    var preparado = false
}
