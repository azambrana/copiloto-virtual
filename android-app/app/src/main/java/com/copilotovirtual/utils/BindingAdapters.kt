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
        "zona-escolar" -> R.drawable.zona_escolar
        "paso-peatonal" -> R.drawable.paso_peatonal
        "ceda-el-paso" -> R.drawable.ceda_el_paso
        "limite-velocidad-10" -> R.drawable.limite_velocidad_10
        "limite-velocidad-20" -> R.drawable.limite_velocidad_20
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