package com.kapgusa.fiesta.modelo.bbdd

//Descripción de las tablas
abstract class Tablas {
    abstract class Retos{
        companion object{
            const val TABLE_NAME = "retos"
            const val COLUMN_id = "id"
            const val COLUMN_tipo = "tipo"
            const val COLUMN_nivelPicante = "nivel_picante"
            const val COLUMN_nivelBeber = "nivel_beber"
            const val COLUMN_monedas = "monedas"
            const val COLUMN_monedasReto = "monedas_reto"
            const val COLUMN_presencial = "presencial"
            const val COLUMN_personalizado = "personalizado"
        }
    }

    abstract class TextosRetos{
        companion object{
            const val TABLE_NAME = "textos_retos"
            const val COLUMN_idReto = "id_reto"
            const val COLUMN_texto = "texto"
        }
    }

    abstract class Jugadores{
        companion object{
            const val TABLE_NAME = "jugadores"
            const val COLUMN_id = "id"
            const val COLUMN_nombre = "nombre"
            const val COLUMN_gustos = "gustos"
            const val COLUMN_tragos = "tragos"
            const val COLUMN_ganadas = "ganadas"
            const val COLUMN_perdidas = "partidas"
        }
    }

    abstract class Mapas{
        companion object{
            const val TABLE_NAME = "mapas"
            const val COLUMN_id = "id"
            const val COLUMN_nombre = "nombre"
            const val COLUMN_descripcion = "descripcion"
            const val COLUMN_picante = "picante"
            const val COLUMN_imagen = "imagen"
            const val COLUMN_personalizado = "personalizado"
        }
    }

    abstract class CasillasMapas{
        companion object{
            const val TABLE_NAME = "casillas_mapas"
            const val COLUMN_id_mapa = "id_mapa"
            const val COLUMN_posicion = "posicion"
            const val COLUMN_tipo = "tipo"
        }
    }

    abstract class Preferencias{
        companion object{
            const val TABLE_NAME = "preferencias"
            const val COLUMN_volumen_audio = "volumen_audio"
            const val COLUMN_volumen_efectos = "volumen_efectos"
            const val COLUMN_eleccion_musica = "eleccion_musica"
            const val COLUMN_nivel_beber = "nivel_beber"
            const val COLUMN_nivel_picante = "nivel_picante"
            const val COLUMN_vasallo = "vasallo"
        }
    }

}