package com.example.inventario.ui.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.inventario.data.local.entity.InsumoEntity
import com.example.inventario.data.repository.InsumoRepository
import com.example.inventario.domain.model.TipoInsumo
import com.example.inventario.domain.model.UnidadBase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventarioViewModel(private val repo: InsumoRepository) : ViewModel() {

    val insumos = repo.observarTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun agregarInsumo(
        nombre: String,
        tipo: TipoInsumo,
        unidad: UnidadBase,
        stock: Double,
        stockMinimo: Double,
        costo: Double
    ) {
        if (nombre.isBlank()) return
        viewModelScope.launch {
            repo.insertar(
                InsumoEntity(
                    nombre = nombre.trim(),
                    tipo = tipo,
                    unidadBase = unidad,
                    stockActual = stock,
                    stockMinimo = stockMinimo,
                    costoUnitario = costo
                )
            )
        }
    }

    fun ajustarStock(id: Long, delta: Double) {
        viewModelScope.launch { repo.ajustarStock(id, delta) }
    }

    fun eliminar(insumo: InsumoEntity) {
        viewModelScope.launch { repo.eliminar(insumo) }
    }
    fun guardarInsumo(
        insumoExistente: InsumoEntity?,
        nombre: String,
        tipo: TipoInsumo,
        unidad: UnidadBase,
        stock: Double,
        minimo: Double,
        costo: Double
    ) {
        if (nombre.isBlank()) return
        viewModelScope.launch {
            if (insumoExistente == null) {
                repo.insertar(
                    InsumoEntity(
                        nombre = nombre.trim(), tipo = tipo, unidadBase = unidad,
                        stockActual = stock, stockMinimo = minimo, costoUnitario = costo
                    )
                )
            } else {
                repo.actualizar(
                    insumoExistente.copy(
                        nombre = nombre.trim(), tipo = tipo, unidadBase = unidad,
                        stockActual = stock, stockMinimo = minimo, costoUnitario = costo
                    )
                )
            }
        }
    }

    // Factory simple para no usar Hilt todavía
    companion object {
        fun factory(repo: InsumoRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return InventarioViewModel(repo) as T
            }
        }
    }
}