package com.example.inventario.ui.ventas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.inventario.domain.model.TipoAjusteCaja

@Composable
fun DialogoAjusteCaja(
    tipo: TipoAjusteCaja,
    onDismiss: () -> Unit,
    onGuardar: (descripcion: String, monto: Double) -> Unit
) {
    var descripcion by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }

    val esIngreso = tipo == TipoAjusteCaja.INGRESO_EXTRA
    val titulo = if (esIngreso) "Ingreso extra" else "Gasto"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = monto,
                    onValueChange = { monto = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Monto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            val montoVal = monto.toDoubleOrNull() ?: 0.0
            TextButton(
                onClick = { onGuardar(descripcion, montoVal) },
                enabled = descripcion.isNotBlank() && montoVal > 0
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}