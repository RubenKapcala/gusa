package com.kapgusa.fiesta.modelo

data class Jugador(
        val nombre: String,
        var gustos: Gustos,
        var posicion: Int,
        var monedas: Int,
        var tragos: Int,
        var partidas: Int,
        var ganadas: Int,
        var perdidas: Int
)
