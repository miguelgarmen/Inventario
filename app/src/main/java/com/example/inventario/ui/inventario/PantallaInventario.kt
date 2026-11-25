package com.example.inventario.ui.inventario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalCafe // <-- Icono para empaques (vaso)
import androidx.compose.material.icons.filled.Restaurant // <-- Icono para ingredientes
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.inventario.data.local.entity.InsumoEntity
import com.example.inventario.domain.model.TipoInsumo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInventario(viewModel: InventarioViewModel) {
    var borrar by remember { mutableStateOf<InsumoEntity?>(null) }
    val insumos by viewModel.insumos.collectAsState()
    var insumoAEditar by remember { mutableStateOf<InsumoEntity?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }

    var textoBusqueda by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    insumoAEditar = null
                    mostrarDialogo = true
                },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nuevo") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {

            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                placeholder = { Text("Buscar insumo...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Buscar", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    if (textoBusqueda.isNotEmpty()) {
                        IconButton(onClick = { textoBusqueda = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(50.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            val insumosFiltrados = insumos.filter {
                it.nombre.contains(textoBusqueda, ignoreCase = true)
            }

            if (insumosFiltrados.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // NUEVO ICONO DE ESTADO VACÍO
                        Icon(
                            Icons.Filled.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (textoBusqueda.isEmpty()) "No hay insumos registrados" else "No se encontraron resultados",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(insumosFiltrados, key = { it.id }) { insumo ->
                        TarjetaInsumo(
                            insumo = insumo,
                            onEditar = { insumoAEditar = insumo; mostrarDialogo = true },
                            onEliminar = { borrar = insumo }
                        )
                    }
                }
            }
        }
    }

    borrar?.let { i -> AlertDialog(onDismissRequest = { borrar = null }, title = { Text("¿Eliminar insumo?") }, text = { Text(i.nombre) }, confirmButton = { TextButton(onClick = { viewModel.eliminar(i); borrar = null }) { Text("Eliminar") } }, dismissButton = { TextButton(onClick = { borrar = null }) { Text("Cancelar") } }) }
    if (mostrarDialogo) {
        DialogoAgregarInsumo(
            insumoAEditar = insumoAEditar,
            onDismiss = { mostrarDialogo = false },
            onGuardar = { nombre, tipo, unidad, stock, min, costo ->
                viewModel.guardarInsumo(insumoAEditar, nombre, tipo, unidad, stock, min, costo)
                mostrarDialogo = false
            }
        )
    }
}

@Composable
private fun TarjetaInsumo(insumo: InsumoEntity, onEditar: () -> Unit, onEliminar: () -> Unit) {
    val esStockBajo = insumo.stockActual <= insumo.stockMinimo

    val colorFondo = if (esStockBajo) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
    val colorTexto = if (esStockBajo) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface

    val colorStockBadge = if (esStockBajo) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer
    val colorStockText = if (esStockBajo) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimaryContainer

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    // ICONO DINÁMICO: Cuchara/Tenedor para ingredientes, Taza para empaques
                    Icon(
                        imageVector = if (insumo.tipo == TipoInsumo.INGREDIENTE) Icons.Filled.Restaurant else Icons.Filled.LocalCafe,
                        contentDescription = null,
                        tint = if (esStockBajo) colorTexto else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = insumo.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorTexto,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    color = colorStockBadge,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (insumo.unidadBase.name == "GRAMO") "${insumo.stockActual} g (${insumo.stockActual / 1000} kg)" else "${insumo.stockActual} ${insumo.unidadBase.name.lowercase()}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorStockText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${insumo.tipo.name.lowercase().replaceFirstChar { it.uppercase()}}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorTexto.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Costo: $${"%.3f".format(insumo.costoUnitario)} / ${insumo.unidadBase.name.lowercase()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorTexto.copy(alpha = 0.7f)
                    )
                    if (esStockBajo) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Stock bajo (Mín: ${insumo.stockMinimo})",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEditar) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = colorTexto)
                    }
                    IconButton(onClick = onEliminar) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = colorTexto)
                    }
                }
            }
        }
    }
}