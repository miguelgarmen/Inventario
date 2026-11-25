package com.example.inventario.ui.ventas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.inventario.data.local.dao.ProductoInsumoDetalle
import com.example.inventario.data.local.entity.CategoriaEntity
import com.example.inventario.data.local.entity.InsumoEntity
import com.example.inventario.data.local.entity.ProductoEntity
import com.example.inventario.domain.model.ColoresProducto

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DialogoAgregarProducto(
    insumosDisponibles: List<InsumoEntity>,
    categoriasDisponibles: List<CategoriaEntity>,
    productoAEditar: ProductoEntity? = null,
    insumosActuales: List<ProductoInsumoDetalle> = emptyList(),
    onDismiss: () -> Unit,
    onCrearInsumo: (String, com.example.inventario.domain.model.TipoInsumo, com.example.inventario.domain.model.UnidadBase, Double, Double, Double) -> Unit = { _, _, _, _, _, _ -> },
    onGuardar: (nombre: String, descripcion: String?, precio: Double,
                insumos: List<Pair<Long, Double>>, color: Long, categoriaId: Long?) -> Unit
) {
    val esEdicion = productoAEditar != null
    var nombre by remember { mutableStateOf(productoAEditar?.nombre ?: "") }
    var confirmar by remember { mutableStateOf(false) }
    var nuevoInsumo by remember { mutableStateOf(false) }
    var precio by remember { mutableStateOf(productoAEditar?.precioVenta?.toString() ?: "") }
    var colorSeleccionado by remember { mutableStateOf(productoAEditar?.color ?: 0xFF5C6BC0) }
    var categoriaId by remember { mutableStateOf<Long?>(productoAEditar?.categoriaId) }
    var categoriaMenu by remember { mutableStateOf(false) }
    var mostrarPickerColor by remember { mutableStateOf(false) }

    val cantidades = remember {
        mutableStateMapOf<Long, String>().apply {
            insumosActuales.forEach { put(it.insumoId, it.cantidad.toString()) }
        }
    }

    fun guardar() {
        val seleccionados = cantidades.mapNotNull { (id, txt) -> txt.toDoubleOrNull()?.takeIf { it > 0 }?.let { id to it } }
        onGuardar(nombre.trim(), null, precio.toDouble(), seleccionados, colorSeleccionado, categoriaId)
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            Modifier.fillMaxSize().navigationBarsPadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(if (esEdicion) "Editar producto" else "Nuevo producto") },
                        navigationIcon = {
                            TextButton(onClick = onDismiss) { Text("Cancelar") }
                        }
                    )
                },
                bottomBar = {
                    Surface(shadowElevation = 8.dp) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            val precioVal = precio.toDoubleOrNull() ?: 0.0
                            Button(
                                onClick = { if (esEdicion) confirmar = true else guardar() },
                                enabled = nombre.isNotBlank() && precioVal.isFinite() && precioVal > 0 && cantidades.values.all { it.isBlank() || it.toDoubleOrNull()?.let { n -> n.isFinite() && n >= 0 } == true }
                            ) {
                                Text(if (esEdicion) "Actualizar producto" else "Guardar producto")
                            }
                        }
                    }
                }
            ) { padding ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre del postre") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = precio,
                            onValueChange = { precio = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Precio de venta por pieza (MXN)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    // Paleta de colores + botón para color personalizado
                    item {
                        Text(
                            "Color del producto",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ColoresProducto.opciones.forEach { (_, argb) ->
                                val seleccionado = colorSeleccionado == argb
                                Box(
                                    Modifier
                                        .size(38.dp)
                                        .background(Color(argb.toInt()), CircleShape)
                                        .border(
                                            width = if (seleccionado) 3.dp else 0.dp,
                                            color = if (seleccionado) MaterialTheme.colorScheme.onSurface
                                            else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { colorSeleccionado = argb }
                                )
                            }
                            // Botón "+" para elegir color personalizado
                            Box(
                                Modifier
                                    .size(38.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline,
                                        shape = CircleShape
                                    )
                                    .clickable { mostrarPickerColor = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "Color personalizado",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    item {
                        TextButton(onClick = { nuevoInsumo = true }) { Text("+ Crear insumo") }
                        val costoReceta = insumosDisponibles.sumOf { it.costoUnitario * (cantidades[it.id]?.toDoubleOrNull() ?: 0.0) }
                        Text("Costo estimado por pieza: $${"%.2f".format(costoReceta)} MXN")
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Insumos por unidad vendida",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Deja en 0 o en blanco los que no uses.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (insumosDisponibles.isEmpty()) {
                        item {
                            Text(
                                "No hay insumos en el inventario.",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        items(insumosDisponibles, key = { it.id }) { insumo ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(insumo.nombre, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${insumo.tipo.name} • ${insumo.unidadBase.name} • stock: ${insumo.stockActual}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                OutlinedTextField(
                                    value = cantidades[insumo.id] ?: "",
                                    onValueChange = {
                                        cantidades[insumo.id] = it.filter { c -> c.isDigit() || c == '.' }
                                    },
                                    label = { Text(when (insumo.unidadBase.name) { "GRAMO" -> "g"; "MILILITRO" -> "ml"; else -> "pzas" }) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.width(100.dp),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmar) AlertDialog(onDismissRequest = { confirmar = false }, title = { Text("¿Guardar cambios?") }, text = { Text("Se actualizará el producto.") }, confirmButton = { TextButton(onClick = { confirmar = false; guardar() }) { Text("Confirmar") } }, dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } })
    if (nuevoInsumo) com.example.inventario.ui.inventario.DialogoAgregarInsumo(onDismiss = { nuevoInsumo = false }, onGuardar = { n, t, u, s, m, c -> onCrearInsumo(n,t,u,s,m,c); nuevoInsumo = false })
    // Diálogo del selector de color personalizado
    if (mostrarPickerColor) {
        DialogoColorPersonalizado(
            colorInicial = colorSeleccionado,
            onDismiss = { mostrarPickerColor = false },
            onColorElegido = { argb ->
                colorSeleccionado = argb
                mostrarPickerColor = false
            }
        )
    }
}