package com.example.inventario.domain.model

/** Colores predefinidos para identificar productos (24 opciones). */
object ColoresProducto {
    val opciones: List<Pair<String, Long>> = listOf(
        "Rojo"         to 0xFFEF5350,
        "Rosa"         to 0xFFEC407A,
        "Fucsia"       to 0xFFD81B60,
        "Morado"       to 0xFFAB47BC,
        "Violeta"      to 0xFF7E57C2,
        "Índigo"       to 0xFF5C6BC0,
        "Azul"         to 0xFF42A5F5,
        "Azul claro"   to 0xFF29B6F6,
        "Cian"         to 0xFF26C6DA,
        "Turquesa"     to 0xFF00ACC1,
        "Verde"        to 0xFF66BB6A,
        "Verde oscuro" to 0xFF2E7D32,
        "Lima"         to 0xFF9CCC65,
        "Oliva"        to 0xFF827717,
        "Amarillo"     to 0xFFFFEE58,
        "Mostaza"      to 0xFFFBC02D,
        "Ámbar"        to 0xFFFFB300,
        "Naranja"      to 0xFFFFA726,
        "Mandarina"    to 0xFFF4511E,
        "Café"         to 0xFF8D6E63,
        "Chocolate"    to 0xFF5D4037,
        "Gris"         to 0xFF78909C,
        "Gris claro"   to 0xFFB0BEC5,
        "Negro"        to 0xFF37474F,
    )

    val porDefecto: Long = 0xFF5C6BC0
}