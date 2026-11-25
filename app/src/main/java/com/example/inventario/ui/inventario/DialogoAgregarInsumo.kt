package com.example.inventario.ui.inventario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.inventario.data.local.entity.InsumoEntity
import com.example.inventario.domain.model.TipoInsumo
import com.example.inventario.domain.model.UnidadBase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoAgregarInsumo(
    insumoAEditar: InsumoEntity? = null,
    onDismiss: () -> Unit,
    onGuardar: (String, TipoInsumo, UnidadBase, Double, Double, Double) -> Unit
) {
    var nombre by remember { mutableStateOf(insumoAEditar?.nombre ?: "") }
    var stock by remember { mutableStateOf(insumoAEditar?.stockActual?.toString() ?: "") }
    var minimo by remember { mutableStateOf(insumoAEditar?.stockMinimo?.toString() ?: "") }
    var costo by remember { mutableStateOf(insumoAEditar?.costoUnitario?.toString() ?: "") }
    var tipo by remember { mutableStateOf(insumoAEditar?.tipo ?: TipoInsumo.INGREDIENTE) }
    var unidad by remember { mutableStateOf(insumoAEditar?.unidadBase ?: UnidadBase.GRAMO) }
    var tipoMenu by remember { mutableStateOf(false) }
    var unidadMenu by remember { mutableStateOf(false) }

    var confirmar by remember { mutableStateOf(false) }
    var kilos by remember { mutableStateOf(false) }
    val factor = if (kilos && unidad == UnidadBase.GRAMO) 1000.0 else 1.0
    val etiqueta = if (factor == 1000.0) "kg" else unidad.name.lowercase()
    fun guardar() { onGuardar(nombre.trim(), tipo, unidad, stock.toDouble() * factor, minimo.toDouble() * factor, costo.toDouble() / factor) }
    val esEdicion = insumoAEditar != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (esEdicion) "Editar insumo" else "Nuevo insumo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(expanded = tipoMenu, onExpandedChange = { tipoMenu = !tipoMenu }) {
                    OutlinedTextField(
                        value = tipo.name, onValueChange = {}, readOnly = true,
                        label = { Text("Tipo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(tipoMenu) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = tipoMenu, onDismissRequest = { tipoMenu = false }) {
                        TipoInsumo.values().forEach {
                            DropdownMenuItem(text = { Text(it.name) }, onClick = { tipo = it; tipoMenu = false })
                        }
                    }
                }
                ExposedDropdownMenuBox(expanded = unidadMenu, onExpandedChange = { unidadMenu = !unidadMenu }) {
                    OutlinedTextField(
                        value = unidad.name, onValueChange = {}, readOnly = true,
                        label = { Text("Unidad base") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(unidadMenu) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = unidadMenu, onDismissRequest = { unidadMenu = false }) {
                        UnidadBase.values().forEach {
                            DropdownMenuItem(text = { Text(it.name) }, onClick = { unidad = it; unidadMenu = false })
                        }
                    }
                }
                if (unidad == UnidadBase.GRAMO) Row { FilterChip(selected = !kilos, onClick = { if (kilos) { stock = ((stock.toDoubleOrNull() ?: 0.0)*1000).toString(); minimo = ((minimo.toDoubleOrNull() ?: 0.0)*1000).toString(); costo = ((costo.toDoubleOrNull() ?: 0.0)/1000).toString(); kilos = false } }, label = { Text("Gramos") }); Spacer(Modifier.width(8.dp)); FilterChip(selected = kilos, onClick = { if (!kilos) { stock = ((stock.toDoubleOrNull() ?: 0.0)/1000).toString(); minimo = ((minimo.toDoubleOrNull() ?: 0.0)/1000).toString(); costo = ((costo.toDoubleOrNull() ?: 0.0)*1000).toString(); kilos = true } }, label = { Text("Kilos") }) }
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Existencias ($etiqueta)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = minimo,
                    onValueChange = { minimo = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Stock mínimo ($etiqueta)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = costo,
                    onValueChange = { costo = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Costo MXN por $etiqueta") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (esEdicion) confirmar = true else guardar() },
                enabled = nombre.isNotBlank() && listOf(stock, minimo, costo).all { it.toDoubleOrNull()?.let { n -> n.isFinite() && n >= 0 } == true }
            ) { Text(if (esEdicion) "Actualizar" else "Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
    if (confirmar) AlertDialog(onDismissRequest = { confirmar = false }, title = { Text("¿Actualizar insumo?") }, text = { Text("Se guardarán los cambios de existencias y costo.") }, confirmButton = { TextButton(onClick = { confirmar = false; guardar() }) { Text("Confirmar") } }, dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } })
}