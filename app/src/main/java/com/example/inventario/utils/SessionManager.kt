package com.example.inventario.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("inventario_sesion", Context.MODE_PRIVATE)

    fun getNotas(): String = prefs.getString("notas_${getUsuarioActual()}", "") ?: ""
    fun setNotas(notas: String) { prefs.edit().putString("notas_${getUsuarioActual()}", notas).apply() }
    fun getUsuarioActual(): String? {
        return prefs.getString("usuario_actual", null)
    }

    fun cerrarSesion() {
        prefs.edit().remove("usuario_actual").apply()
    }

    fun usuarioExiste(usuario: String): Boolean {
        val usuarios = prefs.getStringSet("lista_usuarios", emptySet()) ?: emptySet()
        return usuarios.contains(usuario.trim().lowercase())
    }

    fun registrarUsuario(usuario: String, password: String) {
        val userLower = usuario.trim().lowercase()
        val usuarios = prefs.getStringSet("lista_usuarios", emptySet())?.toMutableSet() ?: mutableSetOf()
        usuarios.add(userLower)

        prefs.edit()
            .putStringSet("lista_usuarios", usuarios)
            .putString("pass_$userLower", password)
            .putString("usuario_actual", userLower)
            .apply()
    }

    fun validarLogin(usuario: String, password: String): Boolean {
        val userLower = usuario.trim().lowercase()
        val passGuardada = prefs.getString("pass_$userLower", null)

        return if (passGuardada != null && passGuardada == password) {
            prefs.edit().putString("usuario_actual", userLower).apply()
            true
        } else {
            false
        }
    }

    fun cambiarPassword(usuario: String, nuevaPassword: String) {
        val userLower = usuario.trim().lowercase()
        prefs.edit().putString("pass_$userLower", nuevaPassword).apply()
    }

    fun getNombreUsuario(): String {
        val user = getUsuarioActual() ?: "Usuario"
        return user.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    fun isTipVisible(): Boolean {
        return prefs.getBoolean("tip_visible", true)
    }

    fun ocultarTip() {
        prefs.edit().putBoolean("tip_visible", false).apply()
    }

    // --- PREFERENCIAS DE INTERFAZ ---
    fun getUsarChips(): Boolean {
        return prefs.getBoolean("usar_chips_categorias", true)
    }

    fun setUsarChips(usar: Boolean) {
        prefs.edit().putBoolean("usar_chips_categorias", usar).apply()
    }

    // --- PREFERENCIA DE VISTA (LISTA O CUADRÍCULA) ---
    fun getVerComoLista(): Boolean {
        return prefs.getBoolean("vista_lista_productos", false) // Por defecto: cuadrícula
    }

    fun setVerComoLista(esLista: Boolean) {
        prefs.edit().putBoolean("vista_lista_productos", esLista).apply()
    }

    // --- PREFERENCIA DE VISTA EN VENDER (LISTA O CUADRÍCULA) ---
    fun getVistaVenderLista(): Boolean {
        return prefs.getBoolean("vista_vender_lista", false) // Por defecto: cuadrícula
    }

    fun setVistaVenderLista(esLista: Boolean) {
        prefs.edit().putBoolean("vista_vender_lista", esLista).apply()
    }
    // --- TUTORIAL ---
    fun isTutorialVisto(): Boolean {
        return prefs.getBoolean("tutorial_visto", false)
    }

    fun setTutorialVisto(visto: Boolean) {
        prefs.edit().putBoolean("tutorial_visto", visto).apply()
    }
    // --- DATOS BANCARIOS ---
    fun guardarDatosBancarios(titular: String, banco: String, cuenta: String, clabe: String) {
        prefs.edit()
            .putString("banco_titular", titular)
            .putString("banco_nombre", banco)
            .putString("banco_cuenta", cuenta)
            .putString("banco_clabe", clabe)
            .apply()
    }

    fun getTitular(): String = prefs.getString("banco_titular", "") ?: ""
    fun getBanco(): String = prefs.getString("banco_nombre", "") ?: ""
    fun getCuenta(): String = prefs.getString("banco_cuenta", "") ?: ""
    fun getClabe(): String = prefs.getString("banco_clabe", "") ?: ""
}