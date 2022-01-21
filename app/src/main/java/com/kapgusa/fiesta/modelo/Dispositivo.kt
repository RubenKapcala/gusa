package com.kapgusa.fiesta.modelo

import java.io.Serializable


data class Dispositivo(
        val jugadores: MutableList<Jugador> = mutableListOf()
):Serializable, Transformable
