package com.example.inventario.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.inventario.utils.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDatosBancarios(
    sessionManager: SessionManager,
    onDismiss: () -> Unit
) {
    var enModoEdicion by remember { mutableStateOf(sessionManager.getCuenta().isEmpty()) }

    var titular by remember { mutableStateOf(sessionManager.getTitular()) }
    var banco by remember { mutableStateOf(sessionManager.getBanco()) }
    var cuenta by remember { mutableStateOf(sessionManager.getCuenta()) }
    var clabe by remember { mutableStateOf(sessionManager.getClabe()) }

    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().navigationBarsPadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = { Text("Datos de Cobro", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            TextButton(onClick = onDismiss) { Text("Cerrar") }
                        },
                        actions = {
                            if (!enModoEdicion && cuenta.isNotBlank()) {
                                // BOTÓN DE COMPARTIR
                                IconButton(onClick = {
                                    val textoCompartir = buildString {
                                        appendLine("🏦 DATOS PARA TRANSFERENCIA")
                                        appendLine("──────────────────")
                                        appendLine("👤 Titular: $titular")
                                        appendLine("🏛️ Banco: $banco")
                                        appendLine("💳 Cuenta/Tarjeta: $cuenta")
                                        if (clabe.isNotBlank()) {
                                            appendLine("🔢 CLABE: $clabe")
                                        }
                                    }
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, textoCompartir)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Compartir datos bancarios"))
                                }) {
                                    Icon(Icons.Filled.Share, contentDescription = "Compartir datos", tint = MaterialTheme.colorScheme.primary)
                                }

                                // BOTÓN DE EDITAR
                                IconButton(onClick = { enModoEdicion = true }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    if (enModoEdicion) {
                        Text(
                            "Ingresa tus datos bancarios para tenerlos listos cuando te hagan una transferencia.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = titular,
                            onValueChange = { titular = it },
                            label = { Text("Titular de la cuenta") },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = banco,
                            onValueChange = { banco = it },
                            label = { Text("Banco (ej. BBVA, Banorte)") },
                            leadingIcon = { Icon(Icons.Filled.AccountBalance, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = cuenta,
                            onValueChange = { cuenta = it },
                            label = { Text("Número de cuenta / Tarjeta") },
                            leadingIcon = { Icon(Icons.Filled.CreditCard, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = clabe,
                            onValueChange = { clabe = it },
                            label = { Text("CLABE Interbancaria (opcional)") },
                            leadingIcon = { Icon(Icons.Filled.Pin, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                sessionManager.guardarDatosBancarios(titular, banco, cuenta, clabe)
                                enModoEdicion = false
                                scope.launch { snackbarHostState.showSnackbar("Datos guardados con éxito") }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Guardar Datos", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            "Toca el icono de copiar para enviar un dato específico, o usa el botón de compartir arriba para enviar todos los datos juntos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        TarjetaDato(
                            titulo = "Titular",
                            valor = titular,
                            icono = Icons.Filled.Person,
                            colorIcono = MaterialTheme.colorScheme.primary
                        ) { clipboardManager.setText(AnnotatedString(titular)); scope.launch { snackbarHostState.showSnackbar("Copiado") } }

                        TarjetaDato(
                            titulo = "Banco",
                            valor = banco,
                            icono = Icons.Filled.AccountBalance,
                            colorIcono = MaterialTheme.colorScheme.secondary
                        ) { clipboardManager.setText(AnnotatedString(banco)); scope.launch { snackbarHostState.showSnackbar("Copiado") } }

                        TarjetaDato(
                            titulo = "Cuenta / Tarjeta",
                            valor = cuenta,
                            icono = Icons.Filled.CreditCard,
                            colorIcono = MaterialTheme.colorScheme.tertiary
                        ) { clipboardManager.setText(AnnotatedString(cuenta)); scope.launch { snackbarHostState.showSnackbar("Copiado") } }

                        TarjetaDato(
                            titulo = "CLABE",
                            valor = clabe,
                            icono = Icons.Filled.Pin,
                            colorIcono = MaterialTheme.colorScheme.primary
                        ) { clipboardManager.setText(AnnotatedString(clabe)); scope.launch { snackbarHostState.showSnackbar("Copiado") } }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaDato(
    titulo: String,
    valor: String,
    icono: ImageVector,
    colorIcono: androidx.compose.ui.graphics.Color,
    onCopiar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, contentDescription = null, tint = colorIcono)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = if (valor.isBlank()) "No registrado" else valor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (valor.isBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                )
            }

            if (valor.isNotBlank()) {
                FilledTonalIconButton(
                    onClick = onCopiar,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copiar", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}