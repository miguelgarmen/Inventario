package com.example.inventario.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object FeedbackHelper {

    /**
     * Vibración larga tipo "confirmación" — para acciones importantes
     * como el botón Cobrar.
     */
    fun vibrarExito(context: Context) {
        vibrar(context, longArrayOf(0, 80, 50, 150), intArrayOf(0, 255, 0, 255))
    }

    fun vibrarTap(context: Context) {
        vibrar(context, longArrayOf(0, 30, 30, 60), intArrayOf(0, 255, 0, 255))
    }

    private fun vibrar(context: Context, tiempos: LongArray, amplitudes: IntArray) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // En Android 8+ usamos amplitudes (0-255) para garantizar intensidad
            vibrator.vibrate(
                VibrationEffect.createWaveform(tiempos, amplitudes, -1)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(tiempos, -1)
        }
    }
}