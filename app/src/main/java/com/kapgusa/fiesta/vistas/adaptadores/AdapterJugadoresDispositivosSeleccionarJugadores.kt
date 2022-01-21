package com.kapgusa.fiesta.vistas.adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kapgusa.fiesta.R
import com.kapgusa.fiesta.controlador.MiBluetooth
import com.kapgusa.fiesta.modelo.Gustos
import com.kapgusa.fiesta.modelo.Jugador
import com.kapgusa.fiesta.vistas.SeleccionarJugadoresActivity

class AdapterJugadoresDispositivosSeleccionarJugadores (private val dataSet: List<Jugador>, private val funciones: SeleccionarJugadoresActivity.BotonesRv) : RecyclerView.Adapter<AdapterJugadoresDispositivosSeleccionarJugadores.ViewHolder>() {


    //Crea un objeto con los parámetros de cada vista
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.ad_nombreJugador_jugadorDispositivo)
        val sexo: ImageView = view.findViewById(R.id.ad_sexoJugador_jugadorDispositivo)
        val gustos: ImageView = view.findViewById(R.id.ad_gustosJugador_jugadorDispositivo)
        val btnBorrar: ImageView = view.findViewById(R.id.ad_btnBorrar_jugadorDispositivo)
    }

    //Crea la vista para el ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.jugador_dispositivo_seleccionar_jugadores, parent, false)
        )
    }

    //Devuelve el tamaño de la lista
    override fun getItemCount() = dataSet.size

    //Da valores a los atributos del ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.nombre.text = dataSet[position].nombre

        when(dataSet[position].isChico){
            true ->  holder.sexo.setImageResource(R.drawable.chico)
            false -> holder.sexo.setImageResource(R.drawable.chica)
        }
        when(dataSet[position].gustos){
            Gustos.CHICOS ->  holder.gustos.setImageResource(R.drawable.chico)
            Gustos.CHICAS ->  holder.gustos.setImageResource(R.drawable.chica)
            Gustos.AMBOS ->  holder.gustos.setImageResource(R.drawable.chico_chica)
        }

        if (MiBluetooth.eresServidor){
            holder.btnBorrar.setOnClickListener { funciones.btnSacarJugador(dataSet[position]) }
        }else{
            holder.btnBorrar.visibility = View.GONE
        }

    }

}