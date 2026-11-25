package com.example.inventario.ui.ventas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items as listItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaProductos(viewModel: ProductosViewModel, verComoLista: Boolean) {
    val productos by viewModel.productosUI.collectAsState()
    val insumos by viewModel.insumosDisponibles.collectAsState()
    val categorias by viewModel.categorias.collectAsState()

    var borrar by remember { mutableStateOf<ProductoUI?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var productoAEditar by remember { mutableStateOf<ProductoUI?>(null) }

    // NUEVO ESTADO PARA EL BUSCADOR
    var textoBusqueda by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                productoAEditar = null
                mostrarDialogo = true
            }) { Icon(Icons.Filled.Add, contentDescription = "Nuevo producto") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {

            // BUSCADOR DE PRODUCTOS
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                placeholder = { Text("Buscar producto...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Buscar") },
                trailingIcon = {
                    if (textoBusqueda.isNotEmpty()) {
                        IconButton(onClick = { textoBusqueda = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            // FILTRAMOS LOS PRODUCTOS
            val productosFiltrados = productos.filter {
                it.producto.nombre.contains(textoBusqueda, ignoreCase = true)
            }

            if (productosFiltrados.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontraron productos.")
                }
            } else {
                // Agrupamos por categoría
                val ordenados = listOf("Productos" to productosFiltrados)
                if (verComoLista) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ordenados.forEach { (titulo, lista) ->
                            item(key = "header_$titulo") {
                                Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                            }
                            listItems(lista, key = { it.producto.id }) { ui ->
                                TarjetaProductoLista(
                                    ui = ui,
                                    onEditar = { productoAEditar = ui; mostrarDialogo = true },
                                    onEliminar = { borrar = ui }
                                )
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ordenados.forEach { (titulo, lista) ->
                            item(key = "header_$titulo", span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                                Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                            }
                            gridItems(lista, key = { it.producto.id }) { ui ->
                                TarjetaProductoCudricula(
                                    ui = ui,
                                    onEditar = { productoAEditar = ui; mostrarDialogo = true },
                                    onEliminar = { borrar = ui }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    borrar?.let { ui -> AlertDialog(onDismissRequest = { borrar = null }, title = { Text("¿Eliminar producto?") }, text = { Text(ui.producto.nombre) }, confirmButton = { TextButton(onClick = { viewModel.eliminar(ui.producto); borrar = null }) { Text("Eliminar") } }, dismissButton = { TextButton(onClick = { borrar = null }) { Text("Cancelar") } }) }
    if (mostrarDialogo) {
        DialogoAgregarProducto(
            insumosDisponibles = insumos,
            categoriasDisponibles = categorias,
            productoAEditar = productoAEditar?.producto,
            insumosActuales = productoAEditar?.insumos ?: emptyList(),
            onDismiss = { mostrarDialogo = false },
            onCrearInsumo = viewModel::crearInsumo,
            onGuardar = { nombre, descripcion, precio, seleccion, color, categoriaId ->
                if (productoAEditar == null) {
                    viewModel.agregarProducto(nombre, descripcion, precio, seleccion, color, categoriaId)
                } else {
                    viewModel.editarProducto(productoAEditar!!.producto.id, nombre, descripcion, precio, seleccion, color, categoriaId)
                }
                mostrarDialogo = false
            }
        )
    }
}

// --- COMPONENTES DE TARJETA (Sin cambios) ---
@Composable
private fun TarjetaProductoLista(ui: ProductoUI, onEditar: () -> Unit, onEliminar: () -> Unit) {
    val colorProducto = Color(ui.producto.color.toInt())
    val colorMargen = if (ui.margen > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = colorProducto.copy(alpha = 0.18f))) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(14.dp).background(colorProducto, CircleShape))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(ui.producto.nombre, fontWeight = FontWeight.Bold)
                Text("Precio: $${"%.2f".format(ui.producto.precioVenta)}", style = MaterialTheme.typography.bodyMedium)
                Text("Ganancia: $${"%.2f".format(ui.margen)}", style = MaterialTheme.typography.bodySmall, color = colorMargen)
            }
            IconButton(onClick = onEditar) { Icon(Icons.Filled.Edit, contentDescription = "Editar") }
            IconButton(onClick = onEliminar) { Icon(Icons.Filled.Delete, contentDescription = "Eliminar") }
        }
    }
}

@Composable
private fun TarjetaProductoCudricula(ui: ProductoUI, onEditar: () -> Unit, onEliminar: () -> Unit) {
    val colorProducto = Color(ui.producto.color.toInt())
    val colorMargen = if (ui.margen > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = colorProducto.copy(alpha = 0.18f))) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(12.dp).background(colorProducto, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text(ui.producto.nombre, fontWeight = FontWeight.Bold, maxLines = 1)
            }
            Text("$${"%.2f".format(ui.producto.precioVenta)}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
            Text("Ganancia: $${"%.2f".format(ui.margen)}", style = MaterialTheme.typography.bodySmall, color = colorMargen)

            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onEditar, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Edit, contentDescription = "Editar") }
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = onEliminar, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Delete, contentDescription = "Eliminar") }
            }
        }
    }
}