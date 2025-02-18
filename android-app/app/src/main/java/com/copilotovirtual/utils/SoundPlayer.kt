package com.copilotovirtual.utils

import android.content.Context
import android.media.MediaPlayer
import android.util.Log

/**
 * Clase que se encarga de reproducir sonidos.
 */
class SoundPlayer(private val context: Context) {

    private val mediaPlayer = MediaPlayer()

    /**
     * Reproduce un sonido en base al parámetro clsName.
     * @param clsName Nombre o identificador que se utilizará para buscar el archivo de sonido con el mismo nombre
     */
    fun playSound(clsName: String) {
        val fileName = "${clsName}.wav"

        if (mediaPlayer.isPlaying) {
            return
        }

        try {
            mediaPlayer.reset()
            val afd = context.assets.openFd(fileName)
            mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (e: Exception) {
            Log.e("SoundPlayer", "Error playing sound: $fileName", e)
        }
    }

    fun release() {
        mediaPlayer.release()
    }
}
