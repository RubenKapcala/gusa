package com.kapgusa.fiesta.modelo.bbdd

import android.annotation.SuppressLint
import android.bluetooth.BluetoothSocket
import android.content.ContentValues
import android.content.Context
import android.content.res.Resources
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.kapgusa.fiesta.R
import com.kapgusa.fiesta.modelo.Gustos
import com.kapgusa.fiesta.modelo.Jugador
import com.kapgusa.fiesta.modelo.Mapa
import com.kapgusa.fiesta.modelo.Reto
import org.greenrobot.eventbus.EventBus
import java.io.IOException

class DbHelper(private var context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val db: SQLiteDatabase = this.writableDatabase//Representa la BBDD

    //Declaración de constantes
    companion object{
        private const val DATABASE_NAME = "miBBDD" //Nombre de la BBDD
        private const val DATABASE_VERSION = 1 //Versión de la BBDD
    }

    //Introduce los datos del jugador en la BBDD por primera vez
    fun crearUsuario(nombre: String, gustos: Gustos){
        val values = ContentValues() //Agrupa los valores a insertar
        values.put(Tablas.Jugadores.COLUMN_nombre, nombre) //Introduce el nombre
        values.put(Tablas.Jugadores.COLUMN_gustos, gustos.name) //Introduce los gustos
        values.put(Tablas.Jugadores.COLUMN_tragos, 0) //Introduce los tragos a 0
        values.put(Tablas.Jugadores.COLUMN_ganadas, 0) //Introduce las partidas ganadas a 0
        values.put(Tablas.Jugadores.COLUMN_perdidas, 0) //Introduce las partidas perdidas a 0

        db.insert(Tablas.Jugadores.TABLE_NAME, null, values) //Realiza el insert
    }

    //Modifica los datos del jugador en la BBDD
    fun modificarUsuario(nombre: String, gustos: Gustos){
        val values = ContentValues() //Agrupa los valores a insertar
        values.put(Tablas.Jugadores.COLUMN_nombre, nombre) //Introduce el nombre
        values.put(Tablas.Jugadores.COLUMN_gustos, gustos.name) //Introduce los gustos

        db.update(Tablas.Jugadores.TABLE_NAME, values, Tablas.Jugadores.COLUMN_id + " = 1", null)

    }

    //Recupera los datos del jugador
    @SuppressLint("Range") //El valor siempre será positivo
    fun obtenerUsuario(): Jugador?{

        //Realiza la query y guarda el resultado en un cursor
        val cursor = db.query(Tablas.Jugadores.TABLE_NAME,
                arrayOf(
                    Tablas.Jugadores.COLUMN_nombre,
                    Tablas.Jugadores.COLUMN_gustos,
                    Tablas.Jugadores.COLUMN_tragos,
                    Tablas.Jugadores.COLUMN_ganadas,
                    Tablas.Jugadores.COLUMN_perdidas,
                ),
                Tablas.Jugadores.COLUMN_id + " = 1", null, null, null, null)

        //Saca del cursor la información y con los datos crea un objeto Jugador
        if (cursor.moveToFirst()){
            val nombre = cursor.getString(cursor.getColumnIndex(Tablas.Jugadores.COLUMN_nombre)) //Obtiene el nombre
            val gustos = cursor.getString(cursor.getColumnIndex(Tablas.Jugadores.COLUMN_gustos)) //Obtiene los gustos
            val tragos = cursor.getInt(cursor.getColumnIndex(Tablas.Jugadores.COLUMN_tragos)) //Obtiene las partidas
            val ganadas = cursor.getInt(cursor.getColumnIndex(Tablas.Jugadores.COLUMN_ganadas)) //Obtiene ganadas
            val perdidas = cursor.getInt(cursor.getColumnIndex(Tablas.Jugadores.COLUMN_perdidas)) //Obtiene perdidas

            cursor.close() //Cierra el cursor
            //Devuelve el jugador
            return Jugador(nombre, Gustos.valueOf(gustos), 0, 0, 0, tragos, ganadas, perdidas)
        }
        cursor.close()//Cierra el cursor
        return null //En caso de que no se pueda realizar la query devuelve null
    }

    //Devuelve una lista con todos los retos dependiendo de la partida
    @SuppressLint("Range") //El valor siempre será positivo
    fun obtenerRetos(partidaPresencial: Boolean, partidaPicante: Boolean, retosPersonalizados: Boolean): List<List<Reto>>{

        val listaRetos = mutableListOf<MutableList<Reto>>()

        //Añade una lista por cada tipo
        for (tipo in Reto.TipoReto.values()){
            listaRetos.add(mutableListOf())
        }

        //Creamos la cláusula WHERE para filtrar los retos
        var clausula = " WHERE 1 = 1"
        if (!partidaPresencial){
            clausula += " AND " + Tablas.Retos.COLUMN_presencial + " = 0"
        }
        if (!partidaPicante){
            clausula += " AND " + Tablas.Retos.COLUMN_nivelPicante + " = 0"
        }
        if (!retosPersonalizados){
            clausula += " AND " + Tablas.Retos.COLUMN_personalizado + " = 0"
        }


        //Realiza la query y guarda el resultado en un cursor
        val cursor = db.rawQuery("select * from " + Tablas.Retos.TABLE_NAME + clausula, null)

        //Recorre el cursor y guarda la información en un objeto Juego para añadirlo a la lista
        while (cursor.moveToNext()) {

            val textos = obtenerTextosReto(cursor.getInt(cursor.getColumnIndex(Tablas.Retos.COLUMN_id)))
            val tipo = Reto.TipoReto.valueOf(cursor.getString(cursor.getColumnIndex(Tablas.Retos.COLUMN_tipo)))
            val nivelPicante = cursor.getInt(cursor.getColumnIndex(Tablas.Retos.COLUMN_nivelPicante))
            val nivelBeber = cursor.getInt(cursor.getColumnIndex(Tablas.Retos.COLUMN_nivelBeber))
            val monedasSiempre = cursor.getInt(cursor.getColumnIndex(Tablas.Retos.COLUMN_monedas))
            val monedasReto = cursor.getInt(cursor.getColumnIndex(Tablas.Retos.COLUMN_monedasReto))
            val presencial = cursor.getInt(cursor.getColumnIndex(Tablas.Retos.COLUMN_nivelPicante)) > 0
            val personalizado = cursor.getInt(cursor.getColumnIndex(Tablas.Retos.COLUMN_nivelPicante)) > 0

            val reto = Reto(textos, tipo, nivelPicante, nivelBeber, monedasSiempre, monedasReto, presencial, personalizado)

            listaRetos[tipo.ordinal].add(reto)
        }
        cursor.close() //Cierra el cursor

        return listaRetos
    }


    @SuppressLint("Range") //El valor siempre será positivo
    private fun obtenerTextosReto(id: Int): MutableList<String>{

        val lista = mutableListOf<String>()

        //Realiza la query y guarda el resultado en un cursor
        val cursor = db.rawQuery("select * from " + Tablas.TextosRetos.TABLE_NAME +
                " WHERE " + Tablas.TextosRetos.COLUMN_idReto + " = " + id, null)

        //Recorre el cursor y guarda el texto del reto para añadirlo a la lista
        while (cursor.moveToNext()) {

            val texto = cursor.getString(cursor.getColumnIndex(Tablas.TextosRetos.COLUMN_texto))
            lista.add(texto)
        }
        cursor.close() //Cierra el cursor

        return lista
    }


    //Devuelve una lista con todos los retos dependiendo de la partida
    @SuppressLint("Range") //El valor siempre será positivo
    fun obtenerMapas(): List<Mapa>{

        val listaMapas = mutableListOf<Mapa>()

        //Realiza la query y guarda el resultado en un cursor
        val cursor = db.rawQuery("select * from " + Tablas.Mapas.TABLE_NAME, null)

        //Recorre el cursor y guarda la información en un objeto Mapa para añadirlo a la lista
        while (cursor.moveToNext()) {

            val casillas = obtenerCasillasMapa(cursor.getInt(cursor.getColumnIndex(Tablas.Mapas.COLUMN_id)))
            val nombre = cursor.getString(cursor.getColumnIndex(Tablas.Mapas.COLUMN_nombre))
            val descripcion = cursor.getString(cursor.getColumnIndex(Tablas.Mapas.COLUMN_descripcion))
            val picante = cursor.getInt(cursor.getColumnIndex(Tablas.Mapas.COLUMN_picante)) > 0
            val rutaImagen = cursor.getString(cursor.getColumnIndex(Tablas.Mapas.COLUMN_imagen))

            val mapa = Mapa(nombre, descripcion, casillas, picante, rutaImagen)

            listaMapas.add(mapa)
        }
        cursor.close() //Cierra el cursor

        return listaMapas
    }


    @SuppressLint("Range") //El valor siempre será positivo
    private fun obtenerCasillasMapa(id: Int): MutableList<Int>{

        val lista = mutableListOf<Int>()

        //Realiza la query y guarda el resultado en un cursor
        val cursor = db.rawQuery("select * from " + Tablas.CasillasMapas.TABLE_NAME +
                " WHERE " + Tablas.CasillasMapas.COLUMN_id_mapa + " = " + id +
                " ORDER BY " + Tablas.CasillasMapas.COLUMN_posicion + " ASC", null)

        //Recorre el cursor y guarda el tipo del reto para añadirlo a la lista
        while (cursor.moveToNext()) {

            val tipo = cursor.getInt(cursor.getColumnIndex(Tablas.CasillasMapas.COLUMN_tipo))
            val posicion = cursor.getInt(cursor.getColumnIndex(Tablas.CasillasMapas.COLUMN_posicion))
            lista.add(posicion, tipo)
        }
        cursor.close() //Cierra el cursor

        return lista
    }



    //Si la BBDD no existe llama a esta función para crearla
    override fun onCreate(db: SQLiteDatabase?) {
        version1(db)
        version2(db)
    }

    //Se llama a esta función cuando hay un cambio en la versión de la BBDD
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        version2(db)
    }


    private fun version1(db: SQLiteDatabase?){
        //Crea la tabla jugadores
        db!!.execSQL("CREATE TABLE IF NOT EXISTS ${Tablas.Jugadores.TABLE_NAME} (" +
                "${Tablas.Jugadores.COLUMN_id} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "${Tablas.Jugadores.COLUMN_nombre} TEXT NOT NULL, " +
                "${Tablas.Jugadores.COLUMN_gustos} TEXT NOT NULL, " +
                "${Tablas.Jugadores.COLUMN_tragos} INTEGER NOT NULL, " +
                "${Tablas.Jugadores.COLUMN_ganadas} INTEGER NOT NULL, " +
                "${Tablas.Jugadores.COLUMN_perdidas} INTEGER NOT NULL)")


        //Crea la tabla retos
        db.execSQL("CREATE TABLE IF NOT EXISTS ${Tablas.Retos.TABLE_NAME} (" +
                "${Tablas.Retos.COLUMN_id} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "${Tablas.Retos.COLUMN_tipo} TEXT NOT NULL, " +
                "${Tablas.Retos.COLUMN_nivelPicante} INTEGER NOT NULL, " +
                "${Tablas.Retos.COLUMN_nivelBeber} INTEGER NOT NULL, " +
                "${Tablas.Retos.COLUMN_monedas} INTEGER NOT NULL, " +
                "${Tablas.Retos.COLUMN_monedasReto} INTEGER NOT NULL, " +
                "${Tablas.Retos.COLUMN_presencial} INTEGER NOT NULL, " +
                "${Tablas.Retos.COLUMN_personalizado} INTEGER NOT NULL)")


        //Crea la tabla retos_textos
        db.execSQL("CREATE TABLE IF NOT EXISTS ${Tablas.TextosRetos.TABLE_NAME} (" +
                "${Tablas.TextosRetos.COLUMN_idReto} INTEGER NOT NULL, " +
                "${Tablas.TextosRetos.COLUMN_texto} TEXT NOT NULL, " +
                "FOREIGN KEY(${Tablas.TextosRetos.COLUMN_idReto}) REFERENCES ${Tablas.Retos.TABLE_NAME}(${Tablas.Retos.COLUMN_id}))")



        //Introduce los retos predefinidos en la BBDD
        for (reto in crearListaDeRetos()){
            val values = ContentValues() //Agrupa los valores a insertar
            values.put(Tablas.Retos.COLUMN_tipo, reto.tipo.name) //Introduce el tipo de reto
            values.put(Tablas.Retos.COLUMN_nivelPicante, reto.nivelPicante) //Introduce el nivel de picante
            values.put(Tablas.Retos.COLUMN_nivelBeber, reto.nivelBeber) //Introduce el niver de beber
            values.put(Tablas.Retos.COLUMN_monedas, reto.monedasSiempre) //Introduce las monedas ganadas
            values.put(Tablas.Retos.COLUMN_monedasReto, reto.monedasReto) //Introduce las monedas ganadas tras el reto
            values.put(Tablas.Retos.COLUMN_presencial, reto.presencial) //Introduce si es presencial
            values.put(Tablas.Retos.COLUMN_personalizado, reto.personalizado) //Introduce si es personalizado

            val id = db.insert(Tablas.Retos.TABLE_NAME, null, values).toInt() //Realiza el insert

            for (texto in reto.texto){
                val valores = ContentValues() //Agrupa los valores a insertar
                valores.put(Tablas.TextosRetos.COLUMN_idReto, id) //Introduce el id del reto
                valores.put(Tablas.TextosRetos.COLUMN_texto, texto) //Introduce el nivel de picante

                db.insert(Tablas.TextosRetos.TABLE_NAME, null, valores) //Realiza el insert
            }
        }
    }

    private fun version2(db: SQLiteDatabase?){

        //Crea la tabla mapas
        db!!.execSQL("CREATE TABLE IF NOT EXISTS ${Tablas.Mapas.TABLE_NAME} (" +
                "${Tablas.Mapas.COLUMN_id} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "${Tablas.Mapas.COLUMN_nombre} TEXT NOT NULL, " +
                "${Tablas.Mapas.COLUMN_descripcion} TEXT NOT NULL, " +
                "${Tablas.Mapas.COLUMN_picante} INTEGER NOT NULL, " +
                "${Tablas.Mapas.COLUMN_imagen} TEXT NOT NULL)" )


        //Crea la tabla casillas_mapas
        db.execSQL("CREATE TABLE IF NOT EXISTS ${Tablas.CasillasMapas.TABLE_NAME} (" +
                "${Tablas.CasillasMapas.COLUMN_id_mapa} INTEGER NOT NULL, " +
                "${Tablas.CasillasMapas.COLUMN_posicion} INTEGER NOT NULL, " +
                "${Tablas.CasillasMapas.COLUMN_tipo} INTEGER NOT NULL, " +
                "FOREIGN KEY(${Tablas.CasillasMapas.COLUMN_id_mapa}) REFERENCES ${Tablas.Mapas.TABLE_NAME}(${Tablas.Mapas.COLUMN_id}))")


        //Introduce los mapas predefinidos en la BBDD
        for (mapa in crearListaDeMapas()){
            val values = ContentValues() //Agrupa los valores a insertar
            values.put(Tablas.Mapas.COLUMN_nombre, mapa.nombre) //Introduce el nombre
            values.put(Tablas.Mapas.COLUMN_descripcion, mapa.descripcion) //Introduce la descripcion
            values.put(Tablas.Mapas.COLUMN_picante, mapa.picante) //Introduce si es picante
            values.put(Tablas.Mapas.COLUMN_imagen, mapa.direccionImagen) //Introduce la ruta de la imagen

            val id = db.insert(Tablas.Mapas.TABLE_NAME, null, values).toInt() //Realiza el insert

            for ((i, casilla) in mapa.casillas.withIndex()){
                val valores = ContentValues() //Agrupa los valores a insertar
                valores.put(Tablas.CasillasMapas.COLUMN_id_mapa, id) //Introduce el id del mapa
                valores.put(Tablas.CasillasMapas.COLUMN_posicion, i) //Introduce la posición
                valores.put(Tablas.CasillasMapas.COLUMN_tipo, casilla) //Introduce el tipo de casilla

                db.insert(Tablas.CasillasMapas.TABLE_NAME, null, valores) //Realiza el insert
            }
        }
    }

    //Crea una lista con todos los retos predefinidos
    private fun crearListaDeRetos(): List<Reto>{

        val listaRetos = mutableListOf<Reto>()

        //-----------------------
        //Prenda
        //-----------------------

        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPrenda)), Reto.TipoReto.PRENDA, 1, 1, 0,0, false, false))


        //-----------------------
        //Retos de beber (tipo 1)
        //-----------------------

        //Muy flojos
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo1_1)), Reto.TipoReto.BEBER, 0, 1, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo1_1), context.getString(R.string.RetoBeberMuyBajo1_2)), Reto.TipoReto.BEBER, 0, 1, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo1_1), context.getString(R.string.RetoBeberMuyBajo1_2), context.getString(R.string.RetoBeberMuyBajo1_3)), Reto.TipoReto.BEBER, 0, 1, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo2)), Reto.TipoReto.BEBER, 0, 1, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo3_1)), Reto.TipoReto.BEBER, 0, 1, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo3_1), context.getString(R.string.RetoBeberMuyBajo3_2_1)), Reto.TipoReto.BEBER, 0, 1, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo3_1), context.getString(R.string.RetoBeberMuyBajo3_2_2)), Reto.TipoReto.BEBER, 0, 1, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo4)), Reto.TipoReto.BEBER, 0, 1, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo5)), Reto.TipoReto.BEBER, 0, 1, -1,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo6_1)), Reto.TipoReto.BEBER, 0, 1, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo6_1), context.getString(R.string.RetoBeberMuyBajo6_2_1)), Reto.TipoReto.BEBER, 0, 1, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo6_1), context.getString(R.string.RetoBeberMuyBajo6_2_2)), Reto.TipoReto.BEBER, 0, 1, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo7)), Reto.TipoReto.BEBER, 0, 1, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo8)), Reto.TipoReto.BEBER, 0, 1, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo9)), Reto.TipoReto.BEBER, 0, 1, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo10)), Reto.TipoReto.BEBER, 0, 1, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo11)), Reto.TipoReto.BEBER, 0, 1, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo13)), Reto.TipoReto.BEBER, 0, 1, -2,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo14)), Reto.TipoReto.BEBER, 0, 1, -3,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo16)), Reto.TipoReto.BEBER, 0, 1, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo17)), Reto.TipoReto.BEBER, 0, 1, 0,-3, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo18)), Reto.TipoReto.BEBER, 0, 1, -3,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo19)), Reto.TipoReto.BEBER, 0, 1, -2,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo22)), Reto.TipoReto.BEBER, 0, 1, 4,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo26)), Reto.TipoReto.BEBER, 0, 1, -2,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo27)), Reto.TipoReto.BEBER, 0, 1, -2,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo28)), Reto.TipoReto.BEBER, 0, 1, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo28), context.getString(R.string.tambienBebes)), Reto.TipoReto.BEBER, 0, 1, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo29)), Reto.TipoReto.BEBER, 0, 1, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo30)), Reto.TipoReto.BEBER, 0, 1, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo31)), Reto.TipoReto.BEBER, 0, 1, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo32)), Reto.TipoReto.BEBER, 0, 1, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo33)), Reto.TipoReto.BEBER, 0, 1, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo34)), Reto.TipoReto.BEBER, 0, 1, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyBajo40)), Reto.TipoReto.BEBER, 0, 1, 0,0, false, false))

        //flojos
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo6_1)), Reto.TipoReto.BEBER, 0, 2, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo6_1), context.getString(R.string.RetoBeberBajo6_2_1)), Reto.TipoReto.BEBER, 0, 2, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo6_1), context.getString(R.string.RetoBeberBajo6_2_2)), Reto.TipoReto.BEBER, 0, 2, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo9)), Reto.TipoReto.BEBER, 0, 2, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo10)), Reto.TipoReto.BEBER, 0, 2, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo11)), Reto.TipoReto.BEBER, 0, 2, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo12)), Reto.TipoReto.BEBER, 0, 2, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo13)), Reto.TipoReto.BEBER, 0, 2, -2,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo15)), Reto.TipoReto.BEBER, 0, 2, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo17)), Reto.TipoReto.BEBER, 0, 2, 0,-3, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo21)), Reto.TipoReto.BEBER, 0, 2, 2,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo22)), Reto.TipoReto.BEBER, 0, 2, 4,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo23)), Reto.TipoReto.BEBER, 0, 2, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo24)), Reto.TipoReto.BEBER, 0, 2, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo24), context.getString(R.string.chicosTambien)), Reto.TipoReto.BEBER, 0, 2, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo25)), Reto.TipoReto.BEBER, 0, 2, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo25), context.getString(R.string.chicasTambien)), Reto.TipoReto.BEBER, 0, 2, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo26)), Reto.TipoReto.BEBER, 0, 2, -2,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo27)), Reto.TipoReto.BEBER, 0, 2, -2,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo31)), Reto.TipoReto.BEBER, 0, 2, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo32)), Reto.TipoReto.BEBER, 0, 2, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo33)), Reto.TipoReto.BEBER, 0, 2, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo34)), Reto.TipoReto.BEBER, 0, 2, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo35)), Reto.TipoReto.BEBER, 0, 2, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo36)), Reto.TipoReto.BEBER, 0, 2, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo37)), Reto.TipoReto.BEBER, 0, 2, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo38)), Reto.TipoReto.BEBER, 0, 2, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo39)), Reto.TipoReto.BEBER, 0, 2, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo40_1)), Reto.TipoReto.BEBER, 0, 2, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo40_1), context.getString(R.string.RetoBeberBajo40_2)), Reto.TipoReto.BEBER, 0, 2, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo41)), Reto.TipoReto.BEBER, 0, 2, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo42)), Reto.TipoReto.BEBER, 0, 2, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberBajo43_1), context.getString(R.string.RetoBeberBajo43_2)), Reto.TipoReto.BEBER, 0, 2, 0,0, false, false))

        //Medio
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio1)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio1), context.getString(R.string.chicosTambien)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio2)), Reto.TipoReto.BEBER, 0, 3, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio3)), Reto.TipoReto.BEBER, 0, 3, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio5_1)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio5_1), context.getString(R.string.RetoBeberMedio5_2)), Reto.TipoReto.BEBER, 0, 3, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio6)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio7)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio8)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio9)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio10)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio11_1)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio11_1), context.getString(R.string.RetoBeberMedio11_2_1)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio11_1), context.getString(R.string.RetoBeberMedio11_2_2)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio11_1), context.getString(R.string.RetoBeberMedio11_2_3)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio11_1), context.getString(R.string.RetoBeberMedio11_2_4)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio11_1), context.getString(R.string.RetoBeberMedio11_2_5)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio12)), Reto.TipoReto.BEBER, 0, 3, 0,10, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio13)), Reto.TipoReto.BEBER, 0, 3, -3,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio14)), Reto.TipoReto.BEBER, 1, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio15)), Reto.TipoReto.BEBER, 0, 3, -3,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio16_1), context.getString(R.string.RetoBeberMedio16_2)), Reto.TipoReto.BEBER, 0, 3, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio17)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio18)), Reto.TipoReto.BEBER, 0, 3, -5,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio19)), Reto.TipoReto.BEBER, 0, 3, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio20)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio21)), Reto.TipoReto.BEBER, 0, 3, -2,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio22)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio23)), Reto.TipoReto.BEBER, 0, 3, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio24)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio25)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio26)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio27)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio28)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio29)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio30)), Reto.TipoReto.BEBER, 0, 3, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio31)), Reto.TipoReto.BEBER, 0, 3, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio32)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio33)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio33)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio34), context.getString(R.string.chicasTambien)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio35)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio36)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio37)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio38)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio39)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio40)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio41)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio42)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio43)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio43), context.getString(R.string.tambienTodos)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio44)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio45)), Reto.TipoReto.BEBER, 0, 3, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio46_1)), Reto.TipoReto.BEBER, 0, 3, -6,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio46_1), context.getString(R.string.RetoBeberMedio46_2)), Reto.TipoReto.BEBER, 0, 3, -6,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio47)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio48)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio49)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio50)), Reto.TipoReto.BEBER, 0, 3, 0,-10, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio51)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio52)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio53)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio54)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio55)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio56)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio57)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio58_1), context.getString(R.string.RetoBeberMedio58_2_1)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio58_1), context.getString(R.string.RetoBeberMedio58_2_2)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio58_1), context.getString(R.string.RetoBeberMedio58_2_3)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio58_1), context.getString(R.string.RetoBeberMedio58_2_4)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio58_1), context.getString(R.string.RetoBeberMedio58_2_5)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio58_1), context.getString(R.string.RetoBeberMedio58_2_6)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio58_1), context.getString(R.string.RetoBeberMedio58_2_7)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio58_1), context.getString(R.string.RetoBeberMedio58_2_8)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio58_1), context.getString(R.string.RetoBeberMedio58_2_9)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio58_1), context.getString(R.string.RetoBeberMedio58_2_10)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio59)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio60)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio61)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio62)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio63)), Reto.TipoReto.BEBER, 0, 3, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio64)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio65)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio67)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio68)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio69)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMedio70)), Reto.TipoReto.BEBER, 0, 3, 0,0, false, false))

        //fuertes
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto1)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto1), context.getString(R.string.soloTu)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto2)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto2), context.getString(R.string.RetoBeberAlto3)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto3)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto5)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto6)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto7)), Reto.TipoReto.BEBER, 0, 4, -2,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto8)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto9)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto10)), Reto.TipoReto.BEBER, 0, 4, -1,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto11)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto12)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto13)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto14)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto15)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto16)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto17)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto18)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto19)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto20)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto21)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto22)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto23)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto24)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto25)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto26)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto27)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto28)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto33_1), context.getString(R.string.RetoBeberAlto33_2_1)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto33_1), context.getString(R.string.RetoBeberAlto33_2_2)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto33_1), context.getString(R.string.RetoBeberAlto33_2_3)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto34)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto35_1)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto35_1), context.getString(R.string.RetoBeberAlto35_2_1)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto35_1), context.getString(R.string.RetoBeberAlto35_2_2)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberAlto36)), Reto.TipoReto.BEBER, 0, 4, 0,0, false, false))

        //Muy fuertes
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyAlto1)), Reto.TipoReto.BEBER, 0, 5, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyAlto2)), Reto.TipoReto.BEBER, 0, 5, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyAlto3)), Reto.TipoReto.BEBER, 0, 5, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyAlto4)), Reto.TipoReto.BEBER, 0, 5, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyAlto5)), Reto.TipoReto.BEBER, 0, 5, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyAlto6)), Reto.TipoReto.BEBER, 0, 5, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyAlto7)), Reto.TipoReto.BEBER, 0, 5, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyAlto8)), Reto.TipoReto.BEBER, 0, 5, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyAlto9)), Reto.TipoReto.BEBER, 0, 5, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyAlto10)), Reto.TipoReto.BEBER, 0, 5, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyAlto11)), Reto.TipoReto.BEBER, 0, 5, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyAlto12)), Reto.TipoReto.BEBER, 0, 5, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyAlto13)), Reto.TipoReto.BEBER, 0, 5, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyAlto14)), Reto.TipoReto.BEBER, 0, 5, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyAlto15)), Reto.TipoReto.BEBER, 0, 5, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyAlto16)), Reto.TipoReto.BEBER, 0, 5, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyAlto17)), Reto.TipoReto.BEBER, 0, 5, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyAlto18_1), context.getString(R.string.RetoBeberMuyAlto18_2_1)), Reto.TipoReto.BEBER, 0, 5, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyAlto18_1), context.getString(R.string.RetoBeberMuyAlto18_2_2)), Reto.TipoReto.BEBER, 0, 5, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBeberMuyAlto18_1), context.getString(R.string.RetoBeberMuyAlto18_2_3)), Reto.TipoReto.BEBER, 0, 5, 0,0, false, false))

        //-----------------------
        //Retos aleatorios (tipo 2)
        //-----------------------

        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio1)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio1)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio2)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio2)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio3)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio3)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio4)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio4)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio5)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio5)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio6)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio6)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio7)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio8), context.getString(R.string.etiquetarGusa)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio9)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio10)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio11)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio12)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio13_1)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio13_1), context.getString(R.string.RetoAleatorio13_2)), Reto.TipoReto.ALEATORIO, 1, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio14)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio14)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio15)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio15)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio16)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio17)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio18)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio19)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio20)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio20), context.getString(R.string.soloTu)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio21)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio21), context.getString(R.string.soloTu)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio22_1)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio22_1), context.getString(R.string.RetoAleatorio22_2)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio23)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio24)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio25)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio25), context.getString(R.string.tambienTodos)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio26)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio27), context.getString(R.string.etiquetarGusa)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoAleatorio28)), Reto.TipoReto.ALEATORIO, 0, 0, 0,0, false, false))

        //-----------------------
        //Retos de la casilla estrella (tipo 3)
        //-----------------------

        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella1_1)), Reto.TipoReto.ESTRELLA, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella1_1), context.getString(R.string.RetoEstrella1_2)), Reto.TipoReto.ESTRELLA, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella2_1)), Reto.TipoReto.ESTRELLA, 0, 0, 1,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella2_1), context.getString(R.string.RetoEstrella2_2)), Reto.TipoReto.ESTRELLA, 0, 0, 10,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella3)), Reto.TipoReto.ESTRELLA, 0, 0, 2,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella4)), Reto.TipoReto.ESTRELLA, 0, 0, 4,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella5)), Reto.TipoReto.ESTRELLA, 0, 0, 6,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella6)), Reto.TipoReto.ESTRELLA, 0, 0, 8,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella7)), Reto.TipoReto.ESTRELLA, 0, 0, 0,8, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella8)), Reto.TipoReto.ESTRELLA, 0, 0, 9,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella8)), Reto.TipoReto.ESTRELLA, 0, 0, 9,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella9)), Reto.TipoReto.ESTRELLA, 0, 0, 0,15, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella10)), Reto.TipoReto.ESTRELLA, 0, 0, 6,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella10)), Reto.TipoReto.ESTRELLA, 0, 0, 6,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella11)), Reto.TipoReto.ESTRELLA, 0, 0, 0,10, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella11), context.getString(R.string.loBebesIgualmente)), Reto.TipoReto.ESTRELLA, 0, 0, 0,10, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella12)), Reto.TipoReto.ESTRELLA, 0, 0, 0,15, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella12), context.getString(R.string.loBebesIgualmente)), Reto.TipoReto.ESTRELLA, 0, 0, 0,10, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella13)), Reto.TipoReto.ESTRELLA, 0, 0, 0,20, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella13), context.getString(R.string.loBebesIgualmente)), Reto.TipoReto.ESTRELLA, 0, 0, 0,10, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella14)), Reto.TipoReto.ESTRELLA, 0, 0, 0,25, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella14), context.getString(R.string.loBebesIgualmente)), Reto.TipoReto.ESTRELLA, 0, 0, 0,10, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella15)), Reto.TipoReto.ESTRELLA, 0, 0, 0,30, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella15), context.getString(R.string.loBebesIgualmente)), Reto.TipoReto.ESTRELLA, 0, 0, 0,10, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella16)), Reto.TipoReto.ESTRELLA, 0, 0, 10,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella17)), Reto.TipoReto.ESTRELLA, 0, 0, 30,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella18)), Reto.TipoReto.ESTRELLA, 0, 0, 0,30, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella19)), Reto.TipoReto.ESTRELLA, 1, 0, 0,30, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella20)), Reto.TipoReto.ESTRELLA, 1, 0, 0,12, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella21)), Reto.TipoReto.ESTRELLA, 1, 0, 0,15, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella22)), Reto.TipoReto.ESTRELLA, 1, 0, 0,9, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoEstrella23)), Reto.TipoReto.ESTRELLA, 1, 0, 0,15, false, false))

        //-----------------------
        //Retos del baul (tipo 4)
        //-----------------------

        //1 Punto
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta1), context.getString(R.string.RetoBaulVacio)), Reto.TipoReto.BAUL, 0, 0, 0,-1, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta1), context.getString(R.string.RetoBaul5A1)), Reto.TipoReto.BAUL, 0, 4, 0,-1, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta1), context.getString(R.string.RetoBaulCamioAbajo)), Reto.TipoReto.BAUL, 0, 4, 0,-1, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta1), context.getString(R.string.RetoBaulCaminoArriba)), Reto.TipoReto.BAUL, 0, 3, 0,-1, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta1), context.getString(R.string.RetoBaulTodos2Tragos)), Reto.TipoReto.BAUL, 0, 0, 0,-1, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta1), context.getString(R.string.RetoBaulPregunta)), Reto.TipoReto.BAUL, 0, 4, 0,-1, false, false))

        //5 puntos
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta5), context.getString(R.string.RetoBaul5A1)), Reto.TipoReto.BAUL, 0, 4, 0,-5, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta5), context.getString(R.string.RetoBaulCamioAbajo)), Reto.TipoReto.BAUL, 0, 4, 0,-5, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta5), context.getString(R.string.RetoBaulCaminoArriba)), Reto.TipoReto.BAUL, 0, 3, 0,-5, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta5), context.getString(R.string.RetoBaulVacio)), Reto.TipoReto.BAUL, 0, 0, 0,-5, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta5), context.getString(R.string.RetoBaulTodos2Tragos)), Reto.TipoReto.BAUL, 0, 0, 0,-5, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta5), context.getString(R.string.RetoBaulComodin)), Reto.TipoReto.BAUL, 0, 1, 0,-5, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta5), context.getString(R.string.RetoBaulTodos5)), Reto.TipoReto.BAUL, 0, 0, 0,-5, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta5), context.getString(R.string.RetoBaulPregunta)), Reto.TipoReto.BAUL, 0, 4, 0,-5, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta5), context.getString(R.string.RetoBaulPiquito)), Reto.TipoReto.BAUL, 1, 4, 0,-5, true, false))

        //10 puntos
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta10), context.getString(R.string.RetoBaul5A1)), Reto.TipoReto.BAUL, 0, 4, 0,-10, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta10), context.getString(R.string.RetoBaulCamioAbajo)), Reto.TipoReto.BAUL, 0, 4, 0,-10, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta10), context.getString(R.string.RetoBaulCaminoArriba)), Reto.TipoReto.BAUL, 0, 3, 0,-10, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta10), context.getString(R.string.RetoBaulTodos2Tragos)), Reto.TipoReto.BAUL, 0, 0, 0,-10, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta10), context.getString(R.string.RetoBaulComodin)), Reto.TipoReto.BAUL, 0, 1, 0,-10, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta10), context.getString(R.string.RetoBaulCamarero)), Reto.TipoReto.BAUL, 0, 0, 0,-10, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta10), context.getString(R.string.RetoBaulTodosChupito)), Reto.TipoReto.BAUL, 0, 0, 0,-10, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta10), context.getString(R.string.RetoBaulTodos5)), Reto.TipoReto.BAUL, 0, 0, 0,-10, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta10), context.getString(R.string.RetoBaulPiquito)), Reto.TipoReto.BAUL, 1, 4, 0,-10, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta10), context.getString(R.string.RetoBaulBeso)), Reto.TipoReto.BAUL, 1, 4, 0,-10, true, false))

        //15 puntos
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta15), context.getString(R.string.RetoBaulComodin)), Reto.TipoReto.BAUL, 0, 1, 0,-15, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta15), context.getString(R.string.RetoBaulCamarero)), Reto.TipoReto.BAUL, 0, 0, 0,-15, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta15), context.getString(R.string.RetoBaulTodosChupito)), Reto.TipoReto.BAUL, 0, 0, 0,-15, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta15), context.getString(R.string.RetoBaulTodos5)), Reto.TipoReto.BAUL, 0, 0, 0,-15, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta15), context.getString(R.string.RetoBaulPiquito)), Reto.TipoReto.BAUL, 1, 4, 0,-15, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta15), context.getString(R.string.RetoBaulBeso)), Reto.TipoReto.BAUL, 1, 4, 0,-15, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta15), context.getString(R.string.RetoBaulBesoOtros)), Reto.TipoReto.BAUL, 1, 4, 0,-15, true, false))

        // 20 puntos
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta20), context.getString(R.string.RetoBaulComodin)), Reto.TipoReto.BAUL, 0, 1, 0,-20, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta20), context.getString(R.string.RetoBaulSuperComodin)), Reto.TipoReto.BAUL, 0, 2, 0,-20, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta20), context.getString(R.string.RetoBaulNorma)), Reto.TipoReto.BAUL, 0, 0, 0,-20, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta20), context.getString(R.string.RetoBaulPonerReto)), Reto.TipoReto.BAUL, 0, 0, 0,-20, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta20), context.getString(R.string.RetoBaulAcabarVaso)), Reto.TipoReto.BAUL, 0, 0, 0,-20, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta20), context.getString(R.string.RetoBaulBeso)), Reto.TipoReto.BAUL, 1, 4, 0,-20, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoBaulCuesta20), context.getString(R.string.RetoBaulBesoOtros)), Reto.TipoReto.BAUL, 1, 4, 0,-20, true, false))


        //-----------------------
        //Retos picantes (tipo 5)
        //-----------------------

        //Muy flojos
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyBajo1_1)), Reto.TipoReto.PICANTE, 1, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyBajo1_1), context.getString(R.string.RetoPicanteMuyBajo1_2)), Reto.TipoReto.PICANTE, 1, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyBajo2)), Reto.TipoReto.PICANTE, 1, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyBajo3)), Reto.TipoReto.PICANTE, 1, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyBajo4)), Reto.TipoReto.PICANTE, 1, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyBajo5)), Reto.TipoReto.PICANTE, 1, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyBajo6)), Reto.TipoReto.PICANTE, 1, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyBajo7)), Reto.TipoReto.PICANTE, 1, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyBajo8)), Reto.TipoReto.PICANTE, 1, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyBajo9_1), context.getString(R.string.RetoPicanteMuyBajo9_2)), Reto.TipoReto.PICANTE, 1, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyBajo10)), Reto.TipoReto.PICANTE, 1, 0, 0,-5, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyBajo11)), Reto.TipoReto.PICANTE, 1, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyBajo12)), Reto.TipoReto.PICANTE, 1, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyBajo13)), Reto.TipoReto.PICANTE, 1, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyBajo14)), Reto.TipoReto.PICANTE, 1, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyBajo15)), Reto.TipoReto.PICANTE, 1, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyBajo16)), Reto.TipoReto.PICANTE, 1, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyBajo17)), Reto.TipoReto.PICANTE, 1, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyBajo18)), Reto.TipoReto.PICANTE, 1, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyBajo19)), Reto.TipoReto.PICANTE, 1, 0, 0,0, false, false))


        //flojos
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteBajo1)), Reto.TipoReto.PICANTE, 2, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteBajo2)), Reto.TipoReto.PICANTE, 2, 0, 0,3, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteBajo3)), Reto.TipoReto.PICANTE, 2, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteBajo4)), Reto.TipoReto.PICANTE, 2, 0, 0,9, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteBajo5)), Reto.TipoReto.PICANTE, 2, 0, 0,-3, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteBajo5), context.getString(R.string.tambienTodos)), Reto.TipoReto.PICANTE, 2, 0, 0,-3, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteBajo6)), Reto.TipoReto.PICANTE, 2, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteBajo7)), Reto.TipoReto.PICANTE, 2, 0, 0,25, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteBajo8)), Reto.TipoReto.PICANTE, 2, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteBajo9)), Reto.TipoReto.PICANTE, 2, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteBajo10)), Reto.TipoReto.PICANTE, 2, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteBajo11)), Reto.TipoReto.PICANTE, 2, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteBajo12)), Reto.TipoReto.PICANTE, 2, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteBajo13)), Reto.TipoReto.PICANTE, 2, 0, 0,-12, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteBajo14)), Reto.TipoReto.PICANTE, 2, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteBajo15)), Reto.TipoReto.PICANTE, 2, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteBajo16)), Reto.TipoReto.PICANTE, 2, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteBajo17)), Reto.TipoReto.PICANTE, 2, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteBajo18)), Reto.TipoReto.PICANTE, 2, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteBajo19_1)), Reto.TipoReto.PICANTE, 2, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteBajo19_1), context.getString(R.string.RetoPicanteBajo19_2)), Reto.TipoReto.PICANTE, 2, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteBajo20)), Reto.TipoReto.PICANTE, 3, 0, 0,0, true, false))


        //Medio
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio1)), Reto.TipoReto.PICANTE, 3, 0, 0,25, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio2)), Reto.TipoReto.PICANTE, 3, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio3)), Reto.TipoReto.PICANTE, 3, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio4)), Reto.TipoReto.PICANTE, 3, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio5)), Reto.TipoReto.PICANTE, 3, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio8)), Reto.TipoReto.PICANTE, 3, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio9)), Reto.TipoReto.PICANTE, 3, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio11)), Reto.TipoReto.PICANTE, 3, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio12)), Reto.TipoReto.PICANTE, 3, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio13)), Reto.TipoReto.PICANTE, 3, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio13), context.getString(R.string.tambienTodos)), Reto.TipoReto.PICANTE, 3, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio14)), Reto.TipoReto.PICANTE, 3, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio17)), Reto.TipoReto.PICANTE, 3, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio19)), Reto.TipoReto.PICANTE, 3, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio20)), Reto.TipoReto.PICANTE, 3, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio21)), Reto.TipoReto.PICANTE, 3, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio22)), Reto.TipoReto.PICANTE, 3, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio23)), Reto.TipoReto.PICANTE, 3, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio24)), Reto.TipoReto.PICANTE, 3, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio25)), Reto.TipoReto.PICANTE, 3, 0, 0,-10, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio25), context.getString(R.string.tambienTodos)), Reto.TipoReto.PICANTE, 3, 0, 0,-10, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio26)), Reto.TipoReto.PICANTE, 3, 0, 0,-5, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio26), context.getString(R.string.tambienTodos)), Reto.TipoReto.PICANTE, 3, 0, 0,-5, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio27)), Reto.TipoReto.PICANTE, 3, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio28)), Reto.TipoReto.PICANTE, 3, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio29)), Reto.TipoReto.PICANTE, 3, 0, 20,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio31)), Reto.TipoReto.PICANTE, 3, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio32)), Reto.TipoReto.PICANTE, 3, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio32), context.getString(R.string.chicasTambien)), Reto.TipoReto.PICANTE, 3, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio32), context.getString(R.string.chicosTambien)), Reto.TipoReto.PICANTE, 3, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio33)), Reto.TipoReto.PICANTE, 3, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio35)), Reto.TipoReto.PICANTE, 3, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio36)), Reto.TipoReto.PICANTE, 3, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio38)), Reto.TipoReto.PICANTE, 3, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio39)), Reto.TipoReto.PICANTE, 3, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio42)), Reto.TipoReto.PICANTE, 3, 0, 5,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio43)), Reto.TipoReto.PICANTE, 3, 0, 5,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio44)), Reto.TipoReto.PICANTE, 3, 0, 5,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio45)), Reto.TipoReto.PICANTE, 3, 0, 5,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio45)), Reto.TipoReto.PICANTE, 3, 0, 5,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio46)), Reto.TipoReto.PICANTE, 3, 0, 5,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio49)), Reto.TipoReto.PICANTE, 3, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio50)), Reto.TipoReto.PICANTE, 3, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio51)), Reto.TipoReto.PICANTE, 3, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMedio53)), Reto.TipoReto.PICANTE, 3, 0, 0,0, true, false))


        //Fuertes
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto1)), Reto.TipoReto.PICANTE, 4, 0, 25,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto2)), Reto.TipoReto.PICANTE, 4, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto3)), Reto.TipoReto.PICANTE, 4, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto3), context.getString(R.string.tambienTu)), Reto.TipoReto.PICANTE, 4, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto4)), Reto.TipoReto.PICANTE, 4, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto5)), Reto.TipoReto.PICANTE, 4, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto6)), Reto.TipoReto.PICANTE, 4, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto7)), Reto.TipoReto.PICANTE, 4, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto8)), Reto.TipoReto.PICANTE, 4, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto9)), Reto.TipoReto.PICANTE, 4, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto10)), Reto.TipoReto.PICANTE, 4, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto11)), Reto.TipoReto.PICANTE, 4, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto12)), Reto.TipoReto.PICANTE, 4, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto13_1)), Reto.TipoReto.PICANTE, 4, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto13_1), context.getString(R.string.RetoPicanteAlto13_2)), Reto.TipoReto.PICANTE, 4, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto14)), Reto.TipoReto.PICANTE, 4, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto15)), Reto.TipoReto.PICANTE, 4, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto15), context.getString(R.string.tambienTodos)), Reto.TipoReto.PICANTE, 4, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto16)), Reto.TipoReto.PICANTE, 4, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto17)), Reto.TipoReto.PICANTE, 4, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto18)), Reto.TipoReto.PICANTE, 4, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto19)), Reto.TipoReto.PICANTE, 4, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto20)), Reto.TipoReto.PICANTE, 4, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto21)), Reto.TipoReto.PICANTE, 4, 0, 5,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto22)), Reto.TipoReto.PICANTE, 4, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto23)), Reto.TipoReto.PICANTE, 4, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto24)), Reto.TipoReto.PICANTE, 4, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto25)), Reto.TipoReto.PICANTE, 4, 0, 0,-7, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto26)), Reto.TipoReto.PICANTE, 4, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteAlto27)), Reto.TipoReto.PICANTE, 4, 0, 0,0, true, false))


        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyAlto2)), Reto.TipoReto.PICANTE, 5, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyAlto3)), Reto.TipoReto.PICANTE, 5, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyAlto4)), Reto.TipoReto.PICANTE, 5, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyAlto5)), Reto.TipoReto.PICANTE, 5, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyAlto6)), Reto.TipoReto.PICANTE, 5, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyAlto7)), Reto.TipoReto.PICANTE, 5, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyAlto8)), Reto.TipoReto.PICANTE, 5, 0, 0,-10, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyAlto9)), Reto.TipoReto.PICANTE, 5, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyAlto10)), Reto.TipoReto.PICANTE, 5, 0, 0,0, true, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyAlto11)), Reto.TipoReto.PICANTE, 5, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyAlto12)), Reto.TipoReto.PICANTE, 5, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyAlto13)), Reto.TipoReto.PICANTE, 5, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyAlto14)), Reto.TipoReto.PICANTE, 5, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyAlto15)), Reto.TipoReto.PICANTE, 5, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoPicanteMuyAlto16)), Reto.TipoReto.PICANTE, 5, 0, 0,0, true, false))



        //-----------------------
        //Retos iniciales (tipo 12)
        //-----------------------

        listaRetos.add(Reto(listOf(context.getString(R.string.RetoInicial1)), Reto.TipoReto.EVENTO_INICIAL, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoInicial2)), Reto.TipoReto.EVENTO_INICIAL, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoInicial3)), Reto.TipoReto.EVENTO_INICIAL, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoInicial4)), Reto.TipoReto.EVENTO_INICIAL, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoInicial5)), Reto.TipoReto.EVENTO_INICIAL, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoInicial6)), Reto.TipoReto.EVENTO_INICIAL, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoInicial7)), Reto.TipoReto.EVENTO_INICIAL, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoInicial8)), Reto.TipoReto.EVENTO_INICIAL, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoInicial9)), Reto.TipoReto.EVENTO_INICIAL, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoInicial10)), Reto.TipoReto.EVENTO_INICIAL, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoInicial11)), Reto.TipoReto.EVENTO_INICIAL, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoInicial12)), Reto.TipoReto.EVENTO_INICIAL, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoInicial13)), Reto.TipoReto.EVENTO_INICIAL, 0, 0, 0,0, false, false))
        listaRetos.add(Reto(listOf(context.getString(R.string.RetoInicial14)), Reto.TipoReto.EVENTO_INICIAL, 0, 0, 0,0, false, false))

        return listaRetos
    }

    private fun crearListaDeMapas(): List<Mapa>{

        val lista = mutableListOf<Mapa>()

        //Mapa casa de gusa picante
        var casillas = listOf(
                Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.ESTRELLA.ordinal, Reto.TipoReto.ALEATORIO.ordinal, Reto.TipoReto.BAUL.ordinal, Reto.TipoReto.PICANTE.ordinal,
                Reto.TipoReto.PRENDA.ordinal, Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal,
                Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal,
                Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal,
                Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal,
                Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal,

                Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal,
                Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal,
                Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.PICANTE.ordinal
        )

        var rutaImagen = Mapa.crearImagenMapa(context.getString(R.string.nombreMapa1), casillas, context.resources, context)
        lista.add(Mapa(context.getString(R.string.nombreMapa1), context.getString(R.string.descripcion1), casillas, true, rutaImagen))


        //Mapa casa de gusa
        casillas = listOf(
                Reto.TipoReto.PICANTE.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal,
                Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal,
                Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal,
                Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal,
                Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal,
                Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal,

                Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal,
                Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal,
                Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal
        )

        rutaImagen = Mapa.crearImagenMapa(context.getString(R.string.nombreMapa2), casillas, context.resources, context)
        lista.add(Mapa(context.getString(R.string.nombreMapa2), context.getString(R.string.descripcion2), casillas, true, rutaImagen))


        //Mapa sin tonterias
        casillas = listOf(
                Reto.TipoReto.PRENDA.ordinal, Reto.TipoReto.PRENDA.ordinal, Reto.TipoReto.PRENDA.ordinal, Reto.TipoReto.PRENDA.ordinal, Reto.TipoReto.PRENDA.ordinal,
                Reto.TipoReto.PRENDA.ordinal, Reto.TipoReto.PRENDA.ordinal, Reto.TipoReto.PRENDA.ordinal, Reto.TipoReto.PRENDA.ordinal, Reto.TipoReto.PRENDA.ordinal,
                Reto.TipoReto.PRENDA.ordinal, Reto.TipoReto.PRENDA.ordinal, Reto.TipoReto.PRENDA.ordinal, Reto.TipoReto.PRENDA.ordinal, Reto.TipoReto.PRENDA.ordinal,
                Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal,
                Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal,
                Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal,

                Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal,
                Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal,
                Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal, Reto.TipoReto.BEBER.ordinal
        )

        rutaImagen = Mapa.crearImagenMapa(context.getString(R.string.nombreMapa3), casillas, context.resources, context)
        lista.add(Mapa(context.getString(R.string.nombreMapa3), context.getString(R.string.descripcion3), casillas, true, rutaImagen))

        return lista
    }



}