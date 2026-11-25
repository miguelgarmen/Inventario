package com.example.inventario.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Destination(val route: String, val label: String, val icon: ImageVector) {
    object Inicio : Destination("inicio", "Inicio", Icons.Filled.Home)
    object Vender : Destination("vender", "Vender", Icons.Filled.ShoppingCart)
    object Productos : Destination("productos", "Productos", Icons.Filled.ShoppingBag)
    object Insumos : Destination("insumos", "Insumos", Icons.Filled.Restaurant)
    object Historial : Destination("historial", "Historial", Icons.Filled.DateRange)
}