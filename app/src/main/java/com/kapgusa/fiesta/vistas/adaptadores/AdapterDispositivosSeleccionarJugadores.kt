package com.kapgusa.fiesta.vistas.adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kapgusa.fiesta.R
import com.kapgusa.fiesta.modelo.Dispositivo
import com.kapgusa.fiesta.vistas.SeleccionarJugadoresActivity

class AdapterDispositivosSeleccionarJugadores (private val context: Context, private val dataSet: List<Dispositivo>, private val funciones: SeleccionarJugadoresActivity.BotonesRv) : RecyclerView.Adapter<AdapterDispositivosSeleccionarJugadores.ViewHolder>() {


    //Crea un objeto con los parámetros de cada vista
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rvJugadores: RecyclerView = view.findViewById(R.id.ad_rv_dispositivo)
    }

    //Crea la vista para el ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.dispositivo_seleccionar_jugadores, parent, false)
        )
    }

    //Devuelve el tamaño de la lista
    override fun getItemCount() = dataSet.size

    //Da valores a los atributos del ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.rvJugadores.setHasFixedSize(true)
        holder.rvJugadores.layoutManager = LinearLayoutManager(context)
        holder.rvJugadores.adapter = AdapterJugadoresDispositivosSeleccionarJugadores(dataSet[position].jugadores, funciones)

    }

}