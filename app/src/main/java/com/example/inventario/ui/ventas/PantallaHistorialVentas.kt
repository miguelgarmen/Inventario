package com.example.inventario.ui.ventas

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventario.domain.model.MetodoPago
import com.example.inventario.domain.model.TipoAjusteCaja
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHistorialVentas(viewModel: VenderViewModel) {
    val periodo by viewModel.periodo.collectAsState()
    val estadisticas by viewModel.estadisticas.collectAsState()
    val ventas by viewModel.historialDia.collectAsState()
    val resumen by viewModel.resumenDia.collectAsState()
    val ajustes by viewModel.ajustesDia.collectAsState()
    val diasConVentas by viewModel.diasConVentas.collectAsState()
    val fechaSeleccionada by viewModel.fechaSeleccionada.collectAsState()

    val context = LocalContext.current

    var tabSeleccionada by remember { mutableStateOf(0) } // 0 = Corte, 1 = Caja
    var confirmarBorrado by remember { mutableStateOf(false) }
    var expandidoCorte by remember { mutableStateOf(true) }
    var mostrarCalendario by remember { mutableStateOf(false) }

    var ventaAEditar by remember { mutableStateOf<VentaAgrupada?>(null) }
    var ventaABorrar by remember { mutableStateOf<VentaAgrupada?>(null) }

    var mostrarGasto by remember { mutableStateOf(false) }
    var mostrarCajaChica by remember { mutableStateOf(false) }

    val fmtFecha = remember { SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES")) }
    val fmtHora = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val fmtDiaCorto = remember { SimpleDateFormat("d MMM", Locale("es", "ES")) }

    val cal = Calendar.getInstance().apply { timeInMillis = fechaSeleccionada }
    val esHoy = run {
        val hoy = Calendar.getInstance()
        hoy.get(Calendar.YEAR) == cal.get(Calendar.YEAR) && hoy.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)
    }
    val hoyMillis = remember { Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.timeInMillis }

    Column(Modifier.fillMaxSize()) {

        // SELECTOR DE FECHA PRINCIPAL
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.cambiarDia(-1) }) { Text("<", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { mostrarCalendario = true }.padding(8.dp)) {
                    Icon(Icons.Filled.DateRange, contentDescription = "Calendario", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(fmtFecha.format(cal.time).replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        if (!esHoy) Text("Toca para buscar fecha", style = MaterialTheme.typography.labelSmall)
                    }
                }
                IconButton(onClick = { if (!esHoy) viewModel.cambiarDia(1) }, enabled = !esHoy) {
                    Text(">", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (esHoy) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        // ACCESOS RÁPIDOS (Hoy + Días con ventas)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                FilterChip(
                    selected = esHoy,
                    onClick = { viewModel.setFechaSeleccionada(System.currentTimeMillis()) },
                    label = { Text("Hoy", fontWeight = FontWeight.Bold) }
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            items(diasConVentas) { diaMillis ->
                val esElSeleccionado = {
                    val c1 = Calendar.getInstance().apply { timeInMillis = diaMillis }
                    val c2 = Calendar.getInstance().apply { timeInMillis = fechaSeleccionada }
                    c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
                }()
                FilterChip(
                    selected = esElSeleccionado,
                    onClick = { viewModel.setFechaSeleccionada(diaMillis) },
                    label = { Text("● " + fmtDiaCorto.format(Date(diaMillis))) }
                )
            }
        }

        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1 to "Día", 7 to "7 días", 30 to "1 mes").forEach { (dias, etiqueta) -> FilterChip(selected = periodo == dias, onClick = { viewModel.setPeriodo(dias) }, label = { Text(etiqueta) }) }
        }
        Text("Ganancia: $${"%.2f".format(estadisticas.ganancia)} MXN • Precio promedio: $${"%.2f".format(estadisticas.promedio)} MXN", modifier = Modifier.padding(horizontal = 12.dp), style = MaterialTheme.typography.bodySmall)
        Text("Más vendido: ${estadisticas.masVendido}", modifier = Modifier.padding(horizontal = 12.dp), style = MaterialTheme.typography.bodySmall)
        // PESTAÑAS
        TabRow(selectedTabIndex = tabSeleccionada) {
            Tab(selected = tabSeleccionada == 0, onClick = { tabSeleccionada = 0 }, text = { Text("Corte y Ventas") })
            Tab(selected = tabSeleccionada == 1, onClick = { tabSeleccionada = 1 }, text = { Text("Caja (Movimientos)") })
        }

        if (tabSeleccionada == 0) {
            // ==========================================
            // PESTAÑA 1: CORTE Y VENTAS
            // ==========================================
            LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { expandidoCorte = !expandidoCorte }) {
                                Column(Modifier.weight(1f)) {
                                    Text("Corte del día", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("${resumen.cantidadVentas} venta(s) registradas", style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { expandidoCorte = !expandidoCorte }) { Icon(if (expandidoCorte) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown, "Expandir") }
                                IconButton(onClick = {
                                    val texto = buildString {
                                        appendLine("📊 CORTE DEL DÍA")
                                        appendLine("Fecha: ${fmtFecha.format(cal.time)}")
                                        appendLine("──────────────────")
                                        appendLine("Caja inicial: $${"%.2f".format(resumen.cajaChicaInicial)}")
                                        appendLine("Ventas: ${resumen.cantidadVentas}")
                                        appendLine("Total vendido: $${"%.2f".format(resumen.totalVendido)}")
                                        appendLine("  • Efectivo: $${"%.2f".format(resumen.totalEfectivo)}")
                                        appendLine("  • Transferencia: $${"%.2f".format(resumen.totalTransferencia)}")
                                        appendLine("Ingresos extra: $${"%.2f".format(resumen.ingresosExtra)}")
                                        appendLine("Gastos: -$${"%.2f".format(resumen.gastos)}")
                                        appendLine("Costo insumos: $${"%.2f".format(resumen.costoInsumos)}")
                                        appendLine("──────────────────")
                                        appendLine("GANANCIA NETA: $${"%.2f".format(resumen.ganancia)}")
                                        appendLine("💵 EFECTIVO ESPERADO: $${"%.2f".format(resumen.efectivoEsperado)}")
                                    }
                                    val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, texto) }
                                    context.startActivity(Intent.createChooser(intent, "Compartir corte"))
                                }) { Icon(Icons.Filled.Share, contentDescription = "Compartir corte") }
                                if (esHoy && (resumen.cantidadVentas > 0 || ajustes.isNotEmpty() || resumen.cajaChicaInicial > 0)) {
                                    IconButton(onClick = { confirmarBorrado = true }) { Icon(Icons.Filled.Delete, contentDescription = "Borrar todo", tint = MaterialTheme.colorScheme.error) }
                                }
                            }
                            if (expandidoCorte) {
                                Spacer(Modifier.height(10.dp))
                                HorizontalDivider()
                                Spacer(Modifier.height(10.dp))
                                FilaCorte("Caja inicial", resumen.cajaChicaInicial)
                                FilaCorte("Total vendido", resumen.totalVendido)
                                FilaCorte("  • Efectivo", resumen.totalEfectivo)
                                FilaCorte("  • Transferencia", resumen.totalTransferencia)
                                if (resumen.ingresosExtra > 0) FilaCorte("Ingresos anteriores", resumen.ingresosExtra)
                                if (resumen.gastos > 0) FilaCorte("Gastos", -resumen.gastos)
                                FilaCorte("Costo insumos", resumen.costoInsumos)
                                Spacer(Modifier.height(6.dp))
                                HorizontalDivider()
                                Spacer(Modifier.height(6.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Ganancia neta", fontWeight = FontWeight.Bold)
                                    Text("$${"%.2f".format(resumen.ganancia)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
                if (ventas.isNotEmpty()) {
                    item { Text("Ventas registradas", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp)) }
                    items(ventas, key = { it.ventaId }) { venta ->
                        TarjetaVenta(venta, fmtHora, esHoy, { ventaAEditar = venta }, { ventaABorrar = venta })
                    }
                } else {
                    item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("No hay ventas este día.") } }
                }
            }
        } else {
            // ==========================================
            // PESTAÑA 2: CAJA / MOVIMIENTOS
            // ==========================================
            Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

                // TARJETA: RESUMEN DE CAJA CHICA
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Fondo de Caja (Caja Chica)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text("Dinero con el que iniciaste el día", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                        }
                        Text(
                            "$${"%.2f".format(resumen.cajaChicaInicial)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                // Botones de acción rápida
                if (esHoy) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { mostrarGasto = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.RemoveCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Gasto", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { mostrarCajaChica = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Caja", fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            "🔒 Día pasado (Solo lectura)",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (ajustes.isEmpty()) {
                    // ==========================================
                    // ESTADO VACÍO MEJORADO
                    // ==========================================
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Card(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp).fillMaxWidth()
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.size(80.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.ReceiptLong,
                                        contentDescription = null,
                                        modifier = Modifier.padding(20.dp).fillMaxSize(),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(Modifier.height(24.dp))
                                Text(
                                    "Sin movimientos",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "No has registrado movimientos para este día.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(24.dp))
                                if (esHoy) {
                                    Button(
                                        onClick = { mostrarGasto = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("+ Registrar primer movimiento")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // LISTA DE MOVIMIENTOS
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Pequeño resumen del día
                        item {
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Ingresos anteriores: +$${"%.2f".format(resumen.ingresosExtra)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                                Text("Total Gastos: -$${"%.2f".format(resumen.gastos)}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                            }
                            HorizontalDivider()
                            Spacer(Modifier.height(8.dp))
                        }

                        items(ajustes, key = { "ajuste_${it.id}" }) { aj ->
                            TarjetaAjuste(aj, fmtHora)
                        }
                    }
                }
            }
        }
    }

    // --- CALENDARIO Y DIÁLOGOS ---
    if (mostrarCalendario) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = fechaSeleccionada + java.util.TimeZone.getDefault().getOffset(fechaSeleccionada),
            selectableDates = object : SelectableDates { override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= hoyMillis }
        )
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { viewModel.setFechaSeleccionada(it - java.util.TimeZone.getDefault().getOffset(it)) }; mostrarCalendario = false }) { Text("Seleccionar") } },
            dismissButton = { TextButton(onClick = { mostrarCalendario = false }) { Text("Cancelar") } }
        ) { Column { Text("● Días con ventas", modifier = Modifier.padding(12.dp)); LazyRow { items(diasConVentas) { dia -> TextButton(onClick = { viewModel.setFechaSeleccionada(dia); mostrarCalendario = false }) { Text("● " + fmtDiaCorto.format(Date(dia))) } } }; DatePicker(state = datePickerState) } }
    }

    if (mostrarGasto) {
        DialogoAjusteCaja(tipo = TipoAjusteCaja.GASTO, onDismiss = { mostrarGasto = false }, onGuardar = { desc, monto -> viewModel.agregarGasto(desc, monto); mostrarGasto = false })
    }
    if (mostrarCajaChica) {
        DialogoCajaChica(montoActual = resumen.cajaChicaInicial, onDismiss = { mostrarCajaChica = false }, onGuardar = { monto -> viewModel.setCajaChica(monto); mostrarCajaChica = false })
    }

    if (confirmarBorrado) {
        AlertDialog(
            onDismissRequest = { confirmarBorrado = false },
            title = { Text("¿Borrar historial?") },
            text = { Text("Se eliminarán ventas, ingresos extra, gastos y la caja chica de este día. El consumo de insumos se mantiene.") },
            confirmButton = { TextButton(onClick = { viewModel.borrarVentasDelDia(); confirmarBorrado = false }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Sí, borrar todo") } },
            dismissButton = { TextButton(onClick = { confirmarBorrado = false }) { Text("Cancelar") } }
        )
    }

    if (ventaABorrar != null) {
        AlertDialog(
            onDismissRequest = { ventaABorrar = null },
            title = { Text("Eliminar venta") },
            text = { Text("¿Seguro que quieres eliminar la venta #${ventaABorrar!!.numeroDia}? Los insumos y productos vendidos volverán al inventario.") },
            confirmButton = { TextButton(onClick = { viewModel.borrarVenta(ventaABorrar!!.ventaId); ventaABorrar = null }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { ventaABorrar = null }) { Text("Cancelar") } }
        )
    }

    if (ventaAEditar != null) {
        var nuevoMetodo by remember { mutableStateOf(ventaAEditar!!.metodoPago) }
        AlertDialog(
            onDismissRequest = { ventaAEditar = null },
            title = { Text("Cambiar método de pago") },
            text = {
                Column {
                    MetodoPago.values().forEach { m ->
                        Row(Modifier.fillMaxWidth().clickable { nuevoMetodo = m }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = nuevoMetodo == m, onClick = { nuevoMetodo = m })
                            Text(m.name)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.cambiarMetodoPagoVenta(ventaAEditar!!.ventaId, nuevoMetodo); ventaAEditar = null }) { Text("Guardar") } },
            dismissButton = { TextButton(onClick = { ventaAEditar = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun FilaCorte(label: String, valor: Double) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text("$${"%.2f".format(valor)}", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun TarjetaAjuste(ajuste: com.example.inventario.data.local.entity.AjusteCajaEntity, fmt: SimpleDateFormat) {
    val esIngreso = ajuste.tipo == TipoAjusteCaja.INGRESO_EXTRA
    val color = if (esIngreso) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val colorFondo = if (esIngreso) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
    val signo = if (esIngreso) "+" else "−"

    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(if (esIngreso) "Ingreso extra" else "Gasto", fontWeight = FontWeight.Bold, color = color)
                Text(ajuste.descripcion, style = MaterialTheme.typography.bodyMedium)
                Text(fmt.format(Date(ajuste.fecha)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("$signo$${"%.2f".format(ajuste.monto)}", fontWeight = FontWeight.Bold, color = color, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun TarjetaVenta(venta: VentaAgrupada, fmtHora: SimpleDateFormat, esHoy: Boolean, onEditarMetodo: () -> Unit, onEliminar: () -> Unit) {
    var expandido by remember { mutableStateOf(false) }
    var mostrarMenu by remember { mutableStateOf(false) }

    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Venta #${venta.numeroDia}", fontWeight = FontWeight.Bold)
                    Text(fmtHora.format(Date(venta.fecha)) + "  •  " + venta.metodoPago.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall)
                }
                if (esHoy) {
                    Box {
                        IconButton(onClick = { mostrarMenu = true }) { Text("⋮", style = MaterialTheme.typography.titleLarge) }
                        DropdownMenu(expanded = mostrarMenu, onDismissRequest = { mostrarMenu = false }) {
                            DropdownMenuItem(text = { Text("Cambiar método de pago") }, onClick = { mostrarMenu = false; onEditarMetodo() })
                            DropdownMenuItem(text = { Text("Eliminar venta", color = MaterialTheme.colorScheme.error) }, onClick = { mostrarMenu = false; onEliminar() })
                        }
                    }
                }
                Text("$${"%.2f".format(venta.total)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            TextButton(onClick = { expandido = !expandido }, contentPadding = PaddingValues(0.dp)) {
                Text(if (expandido) "Ocultar detalle" else "Ver detalle (${venta.items.size})", fontSize = 12.sp)
            }
            if (expandido) {
                venta.items.forEach { item ->
                    Row(Modifier.fillMaxWidth().padding(start = 8.dp, top = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("• ${item.cantidad}x ${item.productoNombre}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Text("$${"%.2f".format(item.precioUnitario * item.cantidad)}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}