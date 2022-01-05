package com.kapgusa.fiesta.vistas

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.kapgusa.fiesta.R
import com.kapgusa.fiesta.controlador.CarpetaImagenes
import com.kapgusa.fiesta.controlador.Musica
import com.kapgusa.fiesta.databinding.ActivityCrearMapasBinding
import com.kapgusa.fiesta.modelo.Mapa
import com.kapgusa.fiesta.modelo.Reto
import com.kapgusa.fiesta.modelo.bbdd.DbHelper
import java.io.File
import java.util.*

class CrearMapasActivity : AppCompatActivity(), View.OnTouchListener {

    private lateinit var binding: ActivityCrearMapasBinding

    private var listaMapasPersonalizados: List<Mapa> = ArrayList<Mapa>()
    private var tipo = 1
    private  var posicion = 0
    private var listaRetosCasillas = IntArray(44)
    private val idCasillas = intArrayOf(R.id.crearMapaCasilla0, R.id.crearMapaCasilla1, R.id.crearMapaCasilla2, R.id.crearMapaCasilla3, R.id.crearMapaCasilla4, R.id.crearMapaCasilla5, R.id.crearMapaCasilla6, R.id.crearMapaCasilla7, R.id.crearMapaCasilla8, R.id.crearMapaCasilla9,
            R.id.crearMapaCasilla10, R.id.crearMapaCasilla11, R.id.crearMapaCasilla12, R.id.crearMapaCasilla13, R.id.crearMapaCasilla14, R.id.crearMapaCasilla15, R.id.crearMapaCasilla16, R.id.crearMapaCasilla17, R.id.crearMapaCasilla18, R.id.crearMapaCasilla19,
            R.id.crearMapaCasilla20, R.id.crearMapaCasilla21, R.id.crearMapaCasilla22, R.id.crearMapaCasilla23, R.id.crearMapaCasilla24, R.id.crearMapaCasilla25, R.id.crearMapaCasilla26, R.id.crearMapaCasilla27, R.id.crearMapaCasilla28, R.id.crearMapaCasilla29,
            R.id.crearMapaCasilla30, R.id.crearMapaCasilla31, R.id.crearMapaCasilla32, R.id.crearMapaCasilla33, R.id.crearMapaCasilla34, R.id.crearMapaCasilla35, R.id.crearMapaCasilla36, R.id.crearMapaCasilla37, R.id.crearMapaCasilla38, R.id.crearMapaCasilla39,
            R.id.crearMapaCasilla40, R.id.crearMapaCasilla41, R.id.crearMapaCasilla42, R.id.crearMapaCasilla43)
    private val casillas = arrayOfNulls<ImageView>(idCasillas.size)
    private var modificandoMapa = false
    private lateinit var db: DbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearMapasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DbHelper(this)

        funcionalidadBotones()

        RefrescarBotonesCasillas()

        CargarPantalla()
    }

    private fun funcionalidadBotones() {

        for (i in idCasillas.indices) {
            casillas[i] = findViewById(idCasillas[i])
        }

        binding.btnAtrasCrearMapas.setOnClickListener{ atras() }
        binding.btnAtrasDescripcionCrearMapas.setOnClickListener { atras() }
        binding.btnContinuarCrearMapas.setOnClickListener {
            var casillasRellenas = true
            for (casilla in listaRetosCasillas) {
                if (casilla == -1) {
                    casillasRellenas = false
                    break
                }
            }
            if (casillasRellenas) {
                binding.lytDescripcionCrearMapas.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, getString(R.string.rellenarCampos), Toast.LENGTH_LONG).show()
            }
        }
        binding.btnGuardarCrearMapas.setOnClickListener {
            if (nombreValido()){
                guardarMapa()
            }
        }
        binding.btnBorrarMapaCrearMapas.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.en_serio)
            builder.setMessage(R.string.deseaBorrar)
                    .setPositiveButton(R.string.atras, DialogInterface.OnClickListener { dialog, which -> })
                    .setNegativeButton(R.string.aceptar, DialogInterface.OnClickListener { dialog, which -> //Borramos la imagen
                        val file = File(listaMapasPersonalizados[posicion].direccionImagen)
                        if (file.exists()) {
                            CarpetaImagenes.borrarImagen(file.absolutePath)
                        }

                        //Guardamos la lista sin el mapa
                        db.borrarMapa(listaMapasPersonalizados[posicion].id)
                        posicion = 0
                        CargarPantalla()
                    }).setCancelable(false).show()
        }
        binding.btnNuevoMapaCrearMapas.setOnClickListener {
            comenzarNuevoMapa()
            Musica.sonidoBoton()
        }
        binding.btnIzquierdaMapaCrearMapas.setOnClickListener {
            if (posicion > 0) {
                posicion--
            } else {
                posicion = listaMapasPersonalizados.size - 1
            }
            ajustarVistaMapa()
            Musica.sonidoBoton()
        }
        binding.btnDerechaMapaCrearMapas.setOnClickListener {
            if (listaMapasPersonalizados.size > posicion + 1) {
                posicion++
            } else {
                posicion = 0
            }
            ajustarVistaMapa()
            Musica.sonidoBoton()
        }
        binding.botonCasillaBeberCrearMapa.setOnClickListener {
            tipo = Reto.TipoReto.BEBER.ordinal
            RefrescarBotonesCasillas()
            Musica.sonidoBoton()
        }
        binding.botonCasillaAleatoriaCrearMapa.setOnClickListener {
            tipo = Reto.TipoReto.ALEATORIO.ordinal
            RefrescarBotonesCasillas()
            Musica.sonidoBoton()
        }
        binding.botonCasillaEstrellaCrearMapa.setOnClickListener {
            tipo = Reto.TipoReto.ESTRELLA.ordinal
            RefrescarBotonesCasillas()
            Musica.sonidoBoton()
        }
        binding.botonCasillaBaulCrearMapa.setOnClickListener {
            tipo = Reto.TipoReto.BAUL.ordinal
            RefrescarBotonesCasillas()
            Musica.sonidoBoton()
        }
        binding.botonCasillaPicanteCrearMapa.setOnClickListener {
            tipo = Reto.TipoReto.PICANTE.ordinal
            RefrescarBotonesCasillas()
            Musica.sonidoBoton()
        }
        binding.botonCasillaPrendaCrearMapa.setOnClickListener {
            tipo = Reto.TipoReto.PRENDA.ordinal
            RefrescarBotonesCasillas()
            Musica.sonidoBoton()
        }
        binding.mapaCrearMapas.setOnTouchListener(this)
    }

    private fun comenzarNuevoMapa() {
        binding.textTusMapasCrearMapas.setText(R.string.nuevoMapa)
        binding.btnBorrarMapaCrearMapas.visibility = View.INVISIBLE
        binding.btnNuevoMapaCrearMapas.visibility = View.INVISIBLE
        binding.btnIzquierdaMapaCrearMapas.visibility = View.GONE
        binding.btnDerechaMapaCrearMapas.visibility = View.GONE
        binding.nombreNuevoMapa.setText("")
        binding.descripcionNuevoMapa.setText("")
        binding.btnContinuarCrearMapas.setText(R.string.continuar)
        modificandoMapa = false
        for (indice in casillas.indices) {
            casillas[indice]?.setImageResource(R.drawable.elemento_nulo)
            listaRetosCasillas[indice] = -1
        }
        tipo = Reto.TipoReto.BEBER.ordinal
        RefrescarBotonesCasillas()
    }


    private fun nombreValido(): Boolean {

        //Comprueba que los campos no estén vacíos
        if (binding.nombreNuevoMapa.text.toString().replace(" ", "").compareTo("") == 0 || binding.descripcionNuevoMapa.text.toString().replace(" ", "").compareTo("") == 0) {
            Toast.makeText(this, getString(R.string.rellenarCampos), Toast.LENGTH_LONG).show()
            return false
        } else if (!modificandoMapa){ //Comprueba que no exista un mapa con ese nombre a menos que se esté modificando un mapa
            val listaMapas = db.getMapas()
            for (mapa in listaMapas) {
                if (binding.nombreNuevoMapa.text.toString() == mapa.nombre) {
                    Toast.makeText(this, getString(R.string.nombreRepetido), Toast.LENGTH_LONG).show()
                    return false
                }
            }
        }
        return true
    }

    private fun guardarMapa() {
        //Si estamos modificando un mapa borrar la imagen anterior
        if (modificandoMapa) {
            val file = File(listaMapasPersonalizados[posicion].direccionImagen)
            if (file.exists()) {
                CarpetaImagenes.borrarImagen(file.absolutePath)
            }
        }
        //Metemos la descripción
        val descripcion = binding.descripcionNuevoMapa.text.toString()

        var picante = false
        for (casilla in listaRetosCasillas) {
            if (casilla == Reto.TipoReto.PICANTE.ordinal || casilla == Reto.TipoReto.PRENDA.ordinal) {
                picante = true
                break
            }
        }

        val rutaImagen = Mapa.crearImagenMapa(binding.nombreNuevoMapa.text.toString(), listaRetosCasillas.asList(), this)

        //Guarda el mapa
        if (modificandoMapa) {
            db.modificarMapa(Mapa(binding.nombreNuevoMapa.text.toString(), descripcion, listaRetosCasillas.asList(), picante, rutaImagen, true), listaMapasPersonalizados[posicion].id)
        } else {
            db.insertarMapa(Mapa(binding.nombreNuevoMapa.text.toString(), descripcion, listaRetosCasillas.asList(), picante, rutaImagen, true))
        }

        //Guardamos la lista de mapas
        binding.lytDescripcionCrearMapas.visibility = View.GONE
        modificandoMapa = true
        CargarPantalla()
    }


    private fun MeterPosicion(posicionCasilla: Int) {
        if (listaRetosCasillas[posicionCasilla] != tipo) {
            Musica.sonidoBoton()
            casillas[posicionCasilla]?.setImageResource(Reto.imagenes[tipo])
            listaRetosCasillas[posicionCasilla] = tipo
        }
    }

    @SuppressLint("SetTextI18n")
    private fun CargarPantalla() {
        listaMapasPersonalizados = db.getMapasPersonalizados()
        if (listaMapasPersonalizados.isEmpty()) {
            //Preparamos para añadir mapa
            comenzarNuevoMapa()
        } else {
            //Mostramos las vistas necesarias
            binding.btnBorrarMapaCrearMapas.visibility = View.VISIBLE
            binding.btnNuevoMapaCrearMapas.visibility = View.VISIBLE
            binding.btnIzquierdaMapaCrearMapas.visibility = View.VISIBLE
            binding.btnDerechaMapaCrearMapas.visibility = View.VISIBLE
            binding.btnContinuarCrearMapas.setText(R.string.modificar)

            //Ponemos modo modificar mapas
            modificandoMapa = true

            ajustarVistaMapa()
        }
    }

    private fun ajustarVistaMapa() {
        //Mostramos titulo
        binding.textTusMapasCrearMapas.text = getString(R.string.tusMapas) + "  " + (posicion + 1) + "/" + listaMapasPersonalizados.size

        //Inicializamos variables
        val mapaTemporal = listaMapasPersonalizados[posicion].copy()
        listaRetosCasillas = mapaTemporal.casillas.toIntArray()
        for (i in casillas.indices) {
            casillas[i]?.setImageResource(Reto.imagenes[mapaTemporal.casillas[i]])
        }

        //Cargamos nombre y descripcion del mapa
        binding.nombreNuevoMapa.setText(mapaTemporal.nombre)
        binding.descripcionNuevoMapa.setText(mapaTemporal.descripcion)    }

    private fun RefrescarBotonesCasillas() {
        binding.layoutBeberMapa.visibility = View.VISIBLE
        binding.layoutAleatorioMapa.visibility = View.VISIBLE
        binding.layoutEstrellaMapa.visibility = View.VISIBLE
        binding.layoutBaulMapa.visibility = View.VISIBLE
        binding.layoutPicanteMapa.visibility = View.VISIBLE
        binding.layoutPrendaMapa.visibility = View.VISIBLE
        when (tipo) {
            Reto.TipoReto.BEBER.ordinal -> {
                binding.layoutBeberMapa.visibility = View.GONE
                binding.descripcionCasillaCrearMapas.setText(R.string.casillaBeber)
            }
            Reto.TipoReto.ALEATORIO.ordinal -> {
                binding.layoutAleatorioMapa.visibility = View.GONE
                binding.descripcionCasillaCrearMapas.setText(R.string.casillaAleatoria)
            }
            Reto.TipoReto.ESTRELLA.ordinal -> {
                binding.layoutEstrellaMapa.visibility = View.GONE
                binding.descripcionCasillaCrearMapas.setText(R.string.casillaEstrella)
            }
            Reto.TipoReto.BAUL.ordinal -> {
                binding.layoutBaulMapa.visibility = View.GONE
                binding.descripcionCasillaCrearMapas.setText(R.string.casillaBaul)
            }
            Reto.TipoReto.PICANTE.ordinal -> {
                binding.layoutPicanteMapa.visibility = View.GONE
                binding.descripcionCasillaCrearMapas.setText(R.string.casillaPicante)
            }
            Reto.TipoReto.PRENDA.ordinal -> {
                binding.layoutPrendaMapa.visibility = View.GONE
                binding.descripcionCasillaCrearMapas.setText(R.string.casillaPrenda)
            }
        }
    }


    private fun atras() {
        if (binding.lytDescripcionCrearMapas.visibility == View.VISIBLE) {
            binding.lytDescripcionCrearMapas.visibility = View.GONE
        } else if (!modificandoMapa) {
            CargarPantalla()
            if (listaMapasPersonalizados.isEmpty()) {
                finish()
            }
        } else {
            finish()
        }
    }

    override fun onBackPressed() {
        atras()
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN) {

            val x = ((event.getX(event.actionIndex) - 25) * 33 / binding.mapaCrearMapas.width).toInt()
            val y = ((event.getY(event.actionIndex) - 25) * 18 / binding.mapaCrearMapas.height).toInt()
            when (y) {
                1 -> when (x) {
                    3 -> MeterPosicion(18)
                    5 -> MeterPosicion(17)
                    7 -> MeterPosicion(16)
                    9 -> MeterPosicion(15)
                    11 -> MeterPosicion(14)
                    13 -> MeterPosicion(13)
                    15 -> MeterPosicion(12)
                    17 -> MeterPosicion(11)
                    19 -> MeterPosicion(10)
                }
                3 -> when (x) {
                    1 -> MeterPosicion(19)
                    21 -> MeterPosicion(9)
                }
                5 -> when (x) {
                    1 -> MeterPosicion(20)
                    5 -> MeterPosicion(39)
                    7 -> MeterPosicion(38)
                    9 -> MeterPosicion(37)
                    11 -> MeterPosicion(36)
                    13 -> MeterPosicion(35)
                    15 -> MeterPosicion(34)
                    21 -> MeterPosicion(8)
                }
                7 -> when (x) {
                    1 -> MeterPosicion(21)
                    5 -> MeterPosicion(40)
                    17 -> MeterPosicion(33)
                    21 -> MeterPosicion(7)
                }
                9 -> when (x) {
                    1 -> MeterPosicion(22)
                    5 -> MeterPosicion(41)
                    17 -> MeterPosicion(32)
                    21 -> MeterPosicion(6)
                }
                11 -> when (x) {
                    1 -> MeterPosicion(23)
                    7 -> MeterPosicion(42)
                    9 -> MeterPosicion(43)
                    13 -> MeterPosicion(29)
                    15 -> MeterPosicion(30)
                    17 -> MeterPosicion(31)
                    21 -> MeterPosicion(5)
                }
                13 -> when (x) {
                    1 -> MeterPosicion(24)
                    21 -> MeterPosicion(4)
                }
                15 -> when (x) {
                    3 -> MeterPosicion(25)
                    5 -> MeterPosicion(26)
                    7 -> MeterPosicion(27)
                    9 -> MeterPosicion(28)
                    13 -> MeterPosicion(0)
                    15 -> MeterPosicion(1)
                    17 -> MeterPosicion(2)
                    19 -> MeterPosicion(3)
                }
            }
        }
        return true
    }

}