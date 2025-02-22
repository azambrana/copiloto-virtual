package com.copilotovirtual.utils

import android.content.Context
import com.copilotovirtual.Constants

/**
 * Clase para cargar las etiquetas de un archivo de assets.
 *
 * @author Alvaro Zambrana Sejas
 * @email azambrana777@gmail.com
 */
object LabelsLoader {
    private var labels: List<String>? = null

    /**
     * Cargar las etiquetas de un archivo de assets.
     * @param context Contexto de la aplicación.
     * @param labelsPath Ruta del archivo de etiquetas.
     */
    fun loadLabelsFromAssets(context: Context, labelsPath: String): List<String> {
        if (labels == null) {
            labels = try {
                context.assets.open(labelsPath).bufferedReader().readLines()
            } catch (e: Exception) {
                println("Error loading labels: ${e.message}")
                emptyList()
            }
        }
        return labels!!
    }
}
