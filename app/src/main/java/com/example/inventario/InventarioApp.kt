package com.example.inventario

import android.app.Application
import com.example.inventario.data.local.InventarioDatabase
import com.example.inventario.utils.SessionManager
import java.io.File

class InventarioApp : Application() {
    val sessionManager by lazy { SessionManager(this) }

    val database: InventarioDatabase
        get() {
            val user = sessionManager.getUsuarioActual() ?: throw IllegalStateException("No hay usuario logueado")
            return InventarioDatabase.get(this, user)
        }

    override fun onCreate() {
        super.onCreate()
        migrarDatosAntiguosABana()
    }

    /**
     * Esta función se ejecuta al abrir la app por primera vez después de la actualización.
     * Busca si existe la BD antigua (inventario.db). Si existe, la renombra a inventario_bana.db
     * y crea el usuario "BANA" automáticamente.
     */
    private fun migrarDatosAntiguosABana() {
        val oldDbFile = getDatabasePath("inventario.db")
        val userBana = "bana"

        // Si existe la base de datos antigua y aún no hemos creado al usuario BANA
        if (oldDbFile.exists() && !sessionManager.usuarioExiste(userBana)) {

            // 1. Creamos el usuario BANA con la contraseña "123456" y lo dejamos logueado
            sessionManager.registrarUsuario(userBana, "BANA")

            // 2. Renombramos el archivo antiguo para que BANA lo herede
            val newDbFile = getDatabasePath("inventario_${userBana}.db")
            if (!newDbFile.exists()) {
                oldDbFile.renameTo(newDbFile)
            }

            // 3. Limpiamos archivos temporales de la BD antigua por seguridad
            File(oldDbFile.path + "-wal").delete()
            File(oldDbFile.path + "-shm").delete()
        }
    }
}