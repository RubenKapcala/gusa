package com.kapgusa.fiesta.vistas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kapgusa.fiesta.R
import com.kapgusa.fiesta.modelo.Gustos
import com.kapgusa.fiesta.modelo.Jugador
import com.kapgusa.fiesta.modelo.Mapa

class AdapterJugadoresSeleccionarJugadores (private val dataSet: List<Jugador>, private val funciones: SeleccionarJugadoresActivity.BotonesRv) : RecyclerView.Adapter<AdapterJugadoresSeleccionarJugadores.ViewHolder>() {

    //Crea un objeto con los parámetros de cada vista
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.ad_nombreJugador_seleccionarJugador)
        val sexo: ImageView = view.findViewById(R.id.ad_sexoJugador_seleccionarJugador)
        val gustos: ImageView = view.findViewById(R.id.ad_gustosJugador_seleccionarJugador)
        val btnAniadir: ImageView = view.findViewById(R.id.ad_aniadirJugador_seleccionarJugador)
        val btnModificar: ImageView = view.findViewById(R.id.ad_modificarJugador_seleccionarJugador)
    }

    //Crea la vista para el ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterJugadoresSeleccionarJugadores.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.jugador_seleccionar_jugadores, parent, false)
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

        if (dataSet[position].preparado){
            holder.btnAniadir.setImageResource(R.drawable.elemento_nulo)
            holder.btnModificar.visibility = View.INVISIBLE
            holder.btnAniadir.setOnClickListener { funciones.btnSacarJugador(dataSet[position]) }

        }else{
            holder.btnAniadir.setOnClickListener { funciones.btnAniadir(dataSet[position]) }
            holder.btnModificar.setOnClickListener { funciones.btnModificar(dataSet[position]) }

        }
    }

}