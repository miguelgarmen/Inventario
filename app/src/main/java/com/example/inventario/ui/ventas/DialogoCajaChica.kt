package com.example.inventario.ui.ventas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun DialogoCajaChica(
    montoActual: Double,
    onDismiss: () -> Unit,
    onGuardar: (Double) -> Unit
) {
    var monto by remember {
        mutableStateOf(if (montoActual > 0) montoActual.toString() else "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Caja chica del día") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Indica cuánto dinero tienes en caja al iniciar el día. " +
                            "Se sumará a las ventas en efectivo para calcular el total esperado al final.",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = monto,
                    onValueChange = { monto = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Fondo inicial en efectivo (MXN)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onGuardar(monto.toDouble()) },
                enabled = monto.toDoubleOrNull()?.let { it.isFinite() && it >= 0 } == true
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}