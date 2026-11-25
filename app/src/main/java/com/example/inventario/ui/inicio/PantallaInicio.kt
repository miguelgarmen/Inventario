package com.example.inventario.ui.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.inventario.utils.SessionManager

@Composable
fun PantallaInicio(
    sessionManager: SessionManager,
    viewModel: InicioViewModel,
    ventasVm: com.example.inventario.ui.ventas.VenderViewModel,
    onIrAInsumos: () -> Unit,
    onIrAHistorial: () -> Unit
) {
    val fecha by ventasVm.fechaSeleccionada.collectAsState()
    val caja by ventasVm.cajaAbierta.collectAsState()
    var notas by remember { mutableStateOf(sessionManager.getNotas()) }
    val nombreUsuario = sessionManager.getNombreUsuario()
    val insumosBajos by viewModel.insumosBajos.collectAsState()
    val resumen by viewModel.resumenHoy.collectAsState()

    var mostrarTip by remember { mutableStateOf(sessionManager.isTipVisible()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "¡Hola, $nombreUsuario!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Este es el resumen de tu negocio hoy.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onIrAHistorial() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Ventas de hoy", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("$ ${"%.2f".format(resumen.totalVendido)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Tickets", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("${resumen.cantidadVentas}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }

        item {
            if (!ventasVm.esFechaActual(fecha)) { Text("Ventas bloqueadas: estás viendo una fecha anterior.", color = MaterialTheme.colorScheme.error); TextButton(onClick = { ventasVm.setFechaSeleccionada(System.currentTimeMillis()) }) { Text("Volver a hoy") } }
            else if (caja == null) Text("Turno sin abrir. Registra caja chica en Vender.")
            OutlinedTextField(value = notas, onValueChange = { notas = it; sessionManager.setNotas(it) }, label = { Text("Deudas y notas") }, supportingText = { Text("Anota cliente, monto MXN y fecha de pago.") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        }
        if (insumosBajos.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onIrAInsumos() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("¡Atención! Stock bajo", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Los siguientes insumos están por agotarse:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)

                        insumosBajos.take(3).forEach { insumo ->
                            Text("• ${insumo.nombre} (Quedan ${insumo.stockActual})", color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.SemiBold)
                        }
                        if (insumosBajos.size > 3) {
                            Text("... y ${insumosBajos.size - 3} más.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }
        }

        if (mostrarTip) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Recordatorio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("No olvides revisar tu Historial al final del día y compartir tu corte de caja por WhatsApp.", style = MaterialTheme.typography.bodyMedium)
                        }
                        IconButton(onClick = { mostrarTip = false; sessionManager.ocultarTip() }) {
                            Icon(Icons.Filled.Close, contentDescription = "Cerrar tip", modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}