package com.example.inventario.ui.ventas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.inventario.data.local.entity.CategoriaEntity
import com.example.inventario.data.repository.CategoriaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriasViewModel(private val repo: CategoriaRepository) : ViewModel() {

    val categorias = repo.observarTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<CategoriaEntity>())

    fun crear(nombre: String) {
        if (nombre.isBlank()) return
        viewModelScope.launch { repo.crear(nombre) }
    }

    fun editar(categoria: CategoriaEntity, nuevoNombre: String) {
        if (nuevoNombre.isBlank()) return
        viewModelScope.launch { repo.editar(categoria, nuevoNombre) }
    }

    fun eliminar(categoria: CategoriaEntity) {
        viewModelScope.launch { repo.eliminar(categoria) }
    }

    companion object {
        fun factory(repo: CategoriaRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                CategoriasViewModel(repo) as T
        }
    }
}