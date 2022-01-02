package com.kapgusa.fiesta.controlador

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.media.SoundPool
import com.kapgusa.fiesta.R
import com.kapgusa.fiesta.modelo.bbdd.DbHelper

@SuppressLint("StaticFieldLeak")
object Musica {

    private var context: Context = MainApplication.applicationContext()

    const val VOLUMEN_MUSICA_INICIAL = 10f // Volumen de música cuando se instala el juego
    const val VOLUMEN_EFECTOS_INICIAL = 25f // Volumen de efectos cuando se instala el juego
    const val MUSICA_ELEGIDA_INICIAL = 4 // Música elegida cuando se instala el juego

    private lateinit var mediaPlayer: MediaPlayer // Permite controlar la música del juego
    private var volumenEfectos = VOLUMEN_EFECTOS_INICIAL // Volumen de los efectos de sonido
    private  var volumenMusica = VOLUMEN_MUSICA_INICIAL // Volumen de la música
    private var musicaElegida = MUSICA_ELEGIDA_INICIAL // Elección de la música
    private var cancionPorDefecto = 0 // Canción que sonará si esta en automático

    //Sonidos para el SoundPool
    private val sonidos = SoundPool.Builder().setMaxStreams(5).build() // Reproduce sonidos cortos
    private var sonidoBoton = 0
    private var sonidoBotonMal = 0
    private var sonidoDado = 0
    private var sonidoGong = 0
    private var sonidoMovimiento = 0
    private var sonidoAleatorio: Int = 0


    //Array con las distintas melodias para el juego
    private val cancion = intArrayOf(R.raw.melodia0, R.raw.melodia1, R.raw.melodia2, R.raw.melodia3)

    init {
        ajustarPreferencias()
        iniciarMediaPlayer()
        cargarSonidos()
    }

    private fun cargarSonidos() {
        //Sonidos para el SoundPool
        sonidoBoton = sonidos.load(context, R.raw.sonido_boton, 1)
        sonidoBotonMal = sonidos.load(context, R.raw.sonido_boton_cancel, 1)
        sonidoDado = sonidos.load(context, R.raw.dado, 1)
        sonidoGong = sonidos.load(context, R.raw.gong, 1)
        sonidoMovimiento = sonidos.load(context, R.raw.movimiento_gusa, 1)
        sonidoAleatorio = sonidos.load(context, R.raw.aleatorio, 1)    }

    //Ajusta las preferencias guardadas en la BBDD
    private fun ajustarPreferencias(){
        val db = DbHelper(context)

        //Ajusta el volumen
        val volumen = db.getVolumenMusica()
        if (volumen != null){ // Comprueba que existan datos en la BBDD sobre las preferencias
            volumenMusica = volumen // Pone el volumen de la música
            volumenEfectos = db.getVolumeEfectos()!! // Pone el volumen de los efectos
            musicaElegida = db.getMusicaElegida()!! // Selecciona la musica elegida
        }
    }

    private fun iniciarMediaPlayer(){
        mediaPlayer = if (musicaElegida != cancion.size) { // Si no esta puesto en automático
            MediaPlayer.create(context, cancion[musicaElegida]) // Pone la melidía elegida
        } else {
            MediaPlayer.create(context, cancion[cancionPorDefecto]) // Pone la melidía por defecto
        }
        mediaPlayer.isLooping = true
        cambiarVolumenMusica(volumenMusica)
    }

    // Inicia la melodía
    fun ponerMelidia() {
        iniciarMediaPlayer()
        mediaPlayer.start()
    }

    fun siguienteCancion(): Int {
        mediaPlayer.release() // Reinicia el mediaPlayer

        // Cambia a la siguiente canción
        if (musicaElegida == cancion.size) {
            musicaElegida = 0
        } else {
            musicaElegida++
        }
        // Guarda la música en la BBDD
        val db = DbHelper(context)
        db.setMusicaElegida(musicaElegida)
        ponerMelidia() // Inicia la música
        return musicaElegida
    }

    fun anteriorCancion(): Int {
        mediaPlayer.release() // Reinicia el mediaPlayer

        // Cambia a la anterior canción
        if (musicaElegida == 0) {
            musicaElegida = cancion.size
        } else {
            musicaElegida--
        }
        // Guarda la música en la BBDD
        val db = DbHelper(context)
        db.setMusicaElegida(musicaElegida)
        ponerMelidia() // Inicia la música
        return musicaElegida
    }

    //Cuando esta por defecto la melodía la cambia a la que se pasa por parámetro
    fun cambiarCancion(nuevaCancion: Int) {
        if (musicaElegida == cancion.size) { // Comprueba que esta la música por defecto
            if (cancionPorDefecto != nuevaCancion) { // Comprueba que la música es diferente a la que ya suena
                cancionPorDefecto = nuevaCancion
                mediaPlayer.release() // Reinicia el mediaPlayer
                ponerMelidia() // Inicia la música
            }
        }
    }

    //Cambia el volumen de la música
    fun cambiarVolumenMusica(vol: Float) {
        mediaPlayer.setVolume(vol / 50, vol / 50)
        volumenMusica = vol
        // Guarda el volumen de la música en la BBDD
        val db = DbHelper(context)
        db.setVolumenMusica(volumenMusica)
    }

    fun cambiarVolumenEfectos(vol: Float) {
        volumenEfectos = vol
        // Guarda el volumen de la música en la BBDD
        val db = DbHelper(context)
        db.setVolumenEfectos(volumenEfectos)
    }

    fun pausarMusica() {
        mediaPlayer.pause()
    }

    fun playMusica() {
        mediaPlayer.start()
    }

    fun sonidoBoton() {
        sonidos.play(sonidoBoton, volumenEfectos / 50, volumenEfectos / 50, 0, 0, 1f)
    }

    fun sonidoBotonMal() {
        sonidos.play(sonidoBotonMal, volumenEfectos / 50, volumenEfectos / 50, 0, 0, 1f)
    }

    fun sonidoDado() {
        sonidos.play(sonidoDado, volumenEfectos / 50, volumenEfectos / 50, 0, 0, 1f)
    }

    fun sonidoMovimiento() {
        sonidos.play(sonidoMovimiento, volumenEfectos / 50, volumenEfectos / 50, 0, 0, 1f)
    }

    fun sonidoGong() {
        sonidos.play(sonidoGong, volumenEfectos / 50, volumenEfectos / 50, 0, 0, 1f)
    }

    fun sonidoAleatorio() {
        sonidos.play(sonidoAleatorio, volumenEfectos / 400, volumenEfectos / 400, 0, 0, 1f)
    }

}