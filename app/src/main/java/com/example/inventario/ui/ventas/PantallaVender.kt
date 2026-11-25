package com.example.inventario.ui.ventas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items as listItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventario.domain.model.MetodoPago
import com.example.inventario.utils.FeedbackHelper
import com.example.inventario.utils.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaVender(viewModel: VenderViewModel, sessionManager: SessionManager) {
    val productos by viewModel.productosUI.collectAsState()
    val carrito by viewModel.carrito.collectAsState()
    val metodoPago by viewModel.metodoPago.collectAsState()
    val categorias by viewModel.categorias.collectAsState()

    val context = LocalContext.current
    val fecha by viewModel.fechaSeleccionada.collectAsState()
    val caja by viewModel.cajaAbierta.collectAsState()
    var abrirCaja by remember { mutableStateOf(false) }
    var confirmarCobro by remember { mutableStateOf(false) }
    if (!viewModel.esFechaActual(fecha) || caja == null) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (!viewModel.esFechaActual(fecha)) "Ventas bloqueadas: estás consultando un día pasado." else "Abre el turno con tu caja chica antes de vender.")
            Button(onClick = { if (!viewModel.esFechaActual(fecha)) viewModel.setFechaSeleccionada(System.currentTimeMillis()) else abrirCaja = true }) { Text(if (!viewModel.esFechaActual(fecha)) "Volver a hoy" else "Abrir turno") }
        }
        if (abrirCaja) DialogoCajaChica(0.0, { abrirCaja = false }, { viewModel.setCajaChica(it); abrirCaja = false })
        return
    }

    var tabSeleccion by remember { mutableStateOf("TODAS") }
    var textoBusqueda by remember { mutableStateOf("") }
    var verComoLista by remember { mutableStateOf(sessionManager.getVistaVenderLista()) }

    val tabs: List<Pair<String, String>> = remember(categorias) {
        buildList {
            add("TODAS" to "Todas")
            categorias.forEach { add("CAT_${it.id}" to it.nombre) }
        }
    }

    val productosFiltrados = when {
        tabSeleccion == "TODAS" -> productos
        tabSeleccion.startsWith("CAT_") -> {
            val id = tabSeleccion.removePrefix("CAT_").toLongOrNull()
            productos.filter { it.producto.categoriaId == id }
        }
        else -> productos
    }.filter { it.producto.nombre.contains(textoBusqueda, ignoreCase = true) }

    val totalCarrito = carrito.entries.sumOf { (id, cant) ->
        (productos.firstOrNull { it.producto.id == id }?.producto?.precioVenta ?: 0.0) * cant
    }
    val totalItems = carrito.values.sum()

    if (confirmarCobro) AlertDialog(onDismissRequest = { confirmarCobro = false }, title = { Text("Confirmar venta") }, text = { Text("Total: $${"%.2f".format(totalCarrito)} MXN") }, confirmButton = { TextButton(onClick = { confirmarCobro = false; viewModel.confirmarVenta() }) { Text("Cobrar") } }, dismissButton = { TextButton(onClick = { confirmarCobro = false }) { Text("Cancelar") } })
    Scaffold(
        bottomBar = {
            if (totalItems > 0) {
                Surface(shadowElevation = 8.dp) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MetodoPago.values().forEach { m ->
                                FilterChip(
                                    selected = metodoPago == m,
                                    onClick = { viewModel.setMetodoPago(m) },
                                    label = { Text(m.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("$totalItems artículo(s)", style = MaterialTheme.typography.bodySmall)
                                Text("$${"%.2f".format(totalCarrito)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            }
                            TextButton(onClick = { viewModel.limpiarCarrito() }) {
                                Icon(Icons.Filled.Close, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Limpiar")
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = {
                                confirmarCobro = true
                            }) {
                                Icon(Icons.Filled.Check, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Cobrar")
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {

            // Barra de búsqueda + Botón de Lista/Cuadrícula
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textoBusqueda,
                    onValueChange = { textoBusqueda = it },
                    modifier = Modifier.weight(1f),
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

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        verComoLista = !verComoLista
                        sessionManager.setVistaVenderLista(verComoLista)
                    }
                ) {
                    Icon(
                        imageVector = if (verComoLista) Icons.Filled.GridView else Icons.Filled.ViewList,
                        contentDescription = "Cambiar vista",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // CONTENIDO PRINCIPAL
            if (productosFiltrados.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No se encontraron productos.") }
            } else {
                if (verComoLista) {
                    LazyColumn(
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listItems(productosFiltrados, key = { it.producto.id }) { ui ->
                            val enCarrito = carrito[ui.producto.id] ?: 0
                            val colorProducto = Color(ui.producto.color.toInt())
                            Card(
                                Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = colorProducto.copy(alpha = 0.18f))
                            ) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(14.dp).background(colorProducto, CircleShape))
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(ui.producto.nombre, fontWeight = FontWeight.Bold)
                                        Text("$${"%.2f".format(ui.producto.precioVenta)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                    }
                                    if (enCarrito == 0) {
                                        Button(onClick = { FeedbackHelper.vibrarTap(context); viewModel.agregarAlCarrito(ui.producto.id) }) {
                                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Agregar")
                                        }
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            FilledIconButton(onClick = { FeedbackHelper.vibrarTap(context); viewModel.quitarDelCarrito(ui.producto.id) }, modifier = Modifier.size(36.dp)) {
                                                Text("−", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                            }
                                            Text("$enCarrito", modifier = Modifier.padding(horizontal = 12.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                            FilledIconButton(onClick = { FeedbackHelper.vibrarTap(context); viewModel.agregarAlCarrito(ui.producto.id) }, modifier = Modifier.size(36.dp)) {
                                                Icon(Icons.Filled.Add, contentDescription = "Agregar")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        gridItems(productosFiltrados, key = { it.producto.id }) { ui ->
                            val enCarrito = carrito[ui.producto.id] ?: 0
                            val colorProducto = Color(ui.producto.color.toInt())
                            Card(
                                Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = colorProducto.copy(alpha = 0.18f))
                            ) {
                                Column(Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(10.dp).background(colorProducto, CircleShape))
                                        Spacer(Modifier.width(6.dp))
                                        Text(ui.producto.nombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                                    }
                                    Text("$${"%.2f".format(ui.producto.precioVenta)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(6.dp))
                                    if (enCarrito == 0) {
                                        Button(onClick = { FeedbackHelper.vibrarTap(context); viewModel.agregarAlCarrito(ui.producto.id) }, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(4.dp)) {
                                            Icon(Icons.Filled.Add, contentDescription = null)
                                            Spacer(Modifier.width(4.dp))
                                            Text("Agregar", style = MaterialTheme.typography.bodySmall)
                                        }
                                    } else {
                                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                            FilledIconButton(onClick = { FeedbackHelper.vibrarTap(context); viewModel.quitarDelCarrito(ui.producto.id) }, modifier = Modifier.size(36.dp)) { Text("−", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                                            Text("$enCarrito", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                            FilledIconButton(onClick = { FeedbackHelper.vibrarTap(context); viewModel.agregarAlCarrito(ui.producto.id) }, modifier = Modifier.size(36.dp)) { Icon(Icons.Filled.Add, contentDescription = "Agregar") }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}