package com.kapgusa.fiesta.vistas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kapgusa.fiesta.R
import com.kapgusa.fiesta.modelo.Mapa
import com.kapgusa.fiesta.modelo.Reto

class JugadoresJuegoAdapter (private val context: Context, private val dataSet: List<Mapa>) : RecyclerView.Adapter<JugadoresJuegoAdapter.ViewHolder>() {


    //Crea un objeto con los parámetros de cada vista
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.tv_nombreJuego)
        val alias: TextView = view.findViewById(R.id.tv_aliasJuego)
        val puntos: TextView = view.findViewById(R.id.tv_puntosJuego)
    }

    //Crea la vista para el ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JugadoresJuegoAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.jugador_con_puntos_item_list, parent, false)
        )
    }

    //Devuelve el tamaño de la lista
    override fun getItemCount() = dataSet.size

    //Da valores a los atributos del ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.nombre.text = dataSet[position].nombre
        holder.alias.text = dataSet[position].casillas.toString()
        holder.puntos.text = dataSet[position].direccionImagen
    }

}