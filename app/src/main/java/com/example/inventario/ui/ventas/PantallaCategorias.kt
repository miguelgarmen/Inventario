package com.example.inventario.ui.ventas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.inventario.data.local.entity.CategoriaEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCategorias(viewModel: CategoriasViewModel) {
    val categorias by viewModel.categorias.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }
    var categoriaAEditar by remember { mutableStateOf<CategoriaEntity?>(null) }
    var nombreTemp by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                categoriaAEditar = null
                nombreTemp = ""
                mostrarDialogo = true
            }) { Icon(Icons.Filled.Add, contentDescription = "Nueva categoría") }
        }
    ) { padding ->
        if (categorias.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No hay categorías.\nToca + para crear la primera.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categorias, key = { it.id }) { cat ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                cat.nombre,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                categoriaAEditar = cat
                                nombreTemp = cat.nombre
                                mostrarDialogo = true
                            }) { Icon(Icons.Filled.Edit, contentDescription = "Editar") }
                            IconButton(onClick = { viewModel.eliminar(cat) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text(if (categoriaAEditar == null) "Nueva categoría" else "Editar categoría") },
            text = {
                OutlinedTextField(
                    value = nombreTemp,
                    onValueChange = { nombreTemp = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (categoriaAEditar == null) viewModel.crear(nombreTemp)
                        else viewModel.editar(categoriaAEditar!!, nombreTemp)
                        mostrarDialogo = false
                    },
                    enabled = nombreTemp.isNotBlank()
                ) { Text(if (categoriaAEditar == null) "Crear" else "Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) { Text("Cancelar") }
            }
        )
    }
}