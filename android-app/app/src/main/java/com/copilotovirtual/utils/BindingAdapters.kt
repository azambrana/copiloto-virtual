package com.copilotovirtual.utils

import android.widget.ImageView
import android.view.View
import androidx.databinding.BindingAdapter
import com.copilotovirtual.data.model.TrafficSign
import com.copilotovirtual.R

@BindingAdapter("trafficSignImage")
fun ImageView.setTrafficSignImage(trafficSign: TrafficSign?) {
    if (trafficSign == null) {
        visibility = View.GONE // Hide ImageView if no sign is detected
        setImageDrawable(null) // Clear image when no sign is detected
        return
    }

    val imageRes = when (trafficSign.type) {
        "pare" -> R.drawable.pare
        "pare-horizontal" -> R.drawable.pare
        "zona-escolar" -> R.drawable.zona_escolar
        "paso-peatonal" -> R.drawable.paso_peatonal
        "ceda-el-paso" -> R.drawable.ceda_el_paso
        "limite-velocidad-10" -> R.drawable.limite_velocidad_10
        "limite-velocidad-20" -> R.drawable.limite_velocidad_20
        "limite-velocidad-30" -> R.drawable.limite_velocidad_30
        "limite-velocidad-35" -> R.drawable.limite_velocidad_35
        "limite-velocidad-40" -> R.drawable.limite_velocidad_40
        "prohibido-girar-izquierda" -> R.drawable.prohibido_girar_izquierda
        "prohibido-girar-derecha" -> R.drawable.prohibido_girar_derecha
        "prohibido-girar-u" -> R.drawable.prohibido_girar_u
        else -> {
            visibility = View.GONE
            setImageDrawable(null)
            return
        }
    }

    setImageResource(imageRes)
    visibility = View.VISIBLE
}


@BindingAdapter("trafficSignVisibility")
fun View.setTrafficSignVisibility(trafficSign: TrafficSign?) {
    visibility = if (trafficSign != null) View.VISIBLE else View.GONE
}