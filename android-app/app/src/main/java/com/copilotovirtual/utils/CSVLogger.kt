package com.copilotovirtual.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * CSVLogger es una clase que permite registrar datos en un archivo CSV.
 * @param context Contexto de la aplicación
 * @param firstTimestamp Marca de tiempo del primer registro
 * @param headers Encabezados de las columnas del archivo CSV
 * @param suffix Sufijo del archivo CSV
 * @constructor Crea un archivo CSV y escribe los encabezados
 * @author Alvaro Zambrana Sejas
 * @version 0.2
 */
class CSVLogger(
    context: Context,
    private val firstTimestamp: Long,
    private val headers: String,
    private val suffix: String,
) {

    private val csvFile: File

    init {
        csvFile = getCSVFile(context)
        createCSVFileIfNeeded()
    }

    /**
     * Obtiene el archivo CSV en el directorio de documentos de la aplicación
     * @param context Contexto de la aplicación
     */
    private fun getCSVFile(context: Context): File {
        val folder = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val timestampString = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date(firstTimestamp))
        return File(folder, "${timestampString}_${suffix}.csv")
    }

    /**
     * Crea el archivo CSV si no existe y escribe los encabezados
     */
    private fun createCSVFileIfNeeded() {
        try {
            if (!csvFile.exists()) {
                csvFile.createNewFile()
                writeHeaders()
            }
        } catch (e: IOException) {
            Log.e("CSVLogger", "Error creating CSV file", e)
        }
    }

    /**
     * Escribe los encabezados en el archivo CSV
     */
    private fun writeHeaders() {
        try {
            FileWriter(csvFile, true).use { writer ->
                writer.append(headers).append("\n")
                writer.flush()
            }
        } catch (e: IOException) {
            Log.e("CSVLogger", "Error writing headers to CSV file", e)
        }
    }

    /**
     * Registra una fila de datos en el archivo CSV
     * @param data Datos a registrar
     */
    fun logRowData(vararg data: Any) {
        try {
            FileWriter(csvFile, true).use { writer ->
                writer.append(data.joinToString(",")).append("\n")
                writer.flush()
            }
            Log.d("CSVLogger", "Logged Data: ${data.joinToString(",")}")
        } catch (e: IOException) {
            Log.e("CSVLogger", "Error writing to CSV file", e)
        }
    }
}
