package com.example.inventario.ui.productos

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventario.data.repository.CategoriaRepository
import com.example.inventario.data.repository.InsumoRepository
import com.example.inventario.data.repository.ProductoRepository
import com.example.inventario.ui.ventas.CategoriasViewModel
import com.example.inventario.ui.ventas.PantallaCategorias
import com.example.inventario.ui.ventas.PantallaProductos
import com.example.inventario.ui.ventas.ProductosViewModel
import com.example.inventario.utils.SessionManager

@Composable
fun PantallaGestionProductos(
    productoRepo: ProductoRepository,
    insumoRepo: InsumoRepository,
    categoriaRepo: CategoriaRepository,
    sessionManager: SessionManager // <-- Necesitamos pasarlo desde el MainActivity
) {
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Productos")

    // Estado para saber si estamos en vista de lista o cuadrícula
    var verComoLista by remember { mutableStateOf(sessionManager.getVerComoLista()) }

    Column(Modifier.fillMaxSize()) {
        // Barra superior con las pestañas y el botón de cambiar vista
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabRow(
                selectedTabIndex = tab,
                modifier = Modifier.weight(1f)
            ) {
                tabs.forEachIndexed { i, t ->
                    Tab(
                        selected = tab == i,
                        onClick = { tab = i },
                        text = { Text(t) }
                    )
                }
            }

            // Botón de cambiar vista (solo aparece en la pestaña de Productos)
            if (tab == 0) {
                IconButton(onClick = {
                    verComoLista = !verComoLista
                    sessionManager.setVerComoLista(verComoLista)
                }) {
                    Icon(
                        imageVector = if (verComoLista) Icons.Filled.GridView else Icons.Filled.ViewList,
                        contentDescription = "Cambiar vista",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp)) // Mantiene el espacio alineado
            }
        }

        Box(Modifier.fillMaxSize()) {
            when (tab) {
                0 -> {
                    val vm: ProductosViewModel = viewModel(
                        factory = ProductosViewModel.factory(productoRepo, insumoRepo, categoriaRepo)
                    )
                    // Le pasamos el parámetro para que sepa cómo dibujarse
                    PantallaProductos(vm, verComoLista = verComoLista)
                }
                1 -> {
                    val vm: CategoriasViewModel = viewModel(
                        factory = CategoriasViewModel.factory(categoriaRepo)
                    )
                    PantallaCategorias(vm)
                }
            }
        }
    }
}