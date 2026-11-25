package com.example.inventario.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.inventario.utils.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLogin(
    sessionManager: SessionManager,
    onLoginExitoso: () -> Unit
) {
    var tabSeleccionada by remember { mutableIntStateOf(0) } // 0 = Login, 1 = Registro
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passVisible by remember { mutableStateOf(false) }
    var confirmPassVisible by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Inventario App",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Pestañas de Login y Registro
        TabRow(selectedTabIndex = tabSeleccionada, modifier = Modifier.padding(bottom = 16.dp)) {
            Tab(
                selected = tabSeleccionada == 0,
                onClick = { tabSeleccionada = 0; mensajeError = null },
                text = { Text("Iniciar Sesión") }
            )
            Tab(
                selected = tabSeleccionada == 1,
                onClick = { tabSeleccionada = 1; mensajeError = null },
                text = { Text("Crear Cuenta") }
            )
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                Text(if (tabSeleccionada == 0) "Escribe tu usuario y contraseña para entrar. Si no tienes cuenta, selecciona Crear Cuenta." else "Elige un usuario y una contraseña de al menos 6 caracteres.", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = usuario,
                    onValueChange = { usuario = it; mensajeError = null },
                    label = { Text("Nombre de usuario") },
                    isError = mensajeError != null && usuario.isBlank(),
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; mensajeError = null },
                    label = { Text("Contraseña") },
                    isError = mensajeError != null,
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passVisible = !passVisible }) {
                            Icon(if (passVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, "Ver")
                        }
                    },
                    visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Campo extra si estamos en la pestaña de Registro
                if (tabSeleccionada == 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; mensajeError = null },
                        label = { Text("Confirmar contraseña") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { confirmPassVisible = !confirmPassVisible }) {
                                Icon(if (confirmPassVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, "Ver")
                            }
                        },
                        visualTransformation = if (confirmPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                if (mensajeError != null) {
                    Text(
                        text = mensajeError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (usuario.isBlank() || password.isBlank()) {
                            mensajeError = if (usuario.isBlank()) "Escribe tu nombre de usuario." else "Escribe tu contraseña."
                            return@Button
                        }

                        if (tabSeleccionada == 0) {
                            // LÓGICA DE LOGIN
                            if (sessionManager.validarLogin(usuario, password)) {
                                onLoginExitoso()
                            } else {
                                mensajeError = "Usuario o contraseña incorrectos."
                            }
                        } else {
                            // LÓGICA DE REGISTRO
                            if (sessionManager.usuarioExiste(usuario)) {
                                mensajeError = "Ese nombre de usuario ya está en uso."
                                return@Button
                            }
                            if (password.length < 6) {
                                mensajeError = "La contraseña debe tener al menos 6 caracteres."
                                return@Button
                            }
                            if (password != confirmPassword) {
                                mensajeError = "Las contraseñas no coinciden."
                                return@Button
                            }

                            // Si todo está bien, se registra y entra
                            sessionManager.registrarUsuario(usuario, password)
                            onLoginExitoso()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(if (tabSeleccionada == 0) "Entrar" else "Registrar y Entrar")
                }
            }
        }
    }
}