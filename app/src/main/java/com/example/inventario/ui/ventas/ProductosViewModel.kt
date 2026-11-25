package com.example.inventario.ui.ventas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.inventario.data.local.entity.CategoriaEntity
import com.example.inventario.data.local.entity.InsumoEntity
import com.example.inventario.data.local.entity.ProductoEntity
import com.example.inventario.data.repository.CategoriaRepository
import com.example.inventario.data.repository.InsumoRepository
import com.example.inventario.data.repository.ProductoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductosViewModel(
    private val productoRepo: ProductoRepository,
    private val insumoRepo: InsumoRepository,
    private val categoriaRepo: CategoriaRepository
) : ViewModel() {

    val insumosDisponibles = insumoRepo.observarTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<InsumoEntity>())

    val categorias = categoriaRepo.observarTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<CategoriaEntity>())

    val productosUI = combine(
        productoRepo.observarTodos(),
        productoRepo.observarInsumosDeProductos(),
        categoriaRepo.observarTodas()
    ) { productos, relaciones, cats ->
        productos.map { p ->
            ProductoUI(
                producto = p,
                insumos = relaciones.filter { it.productoId == p.id },
                categoriaNombre = cats.firstOrNull { it.id == p.categoriaId }?.nombre
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun crearInsumo(n: String, t: com.example.inventario.domain.model.TipoInsumo, u: com.example.inventario.domain.model.UnidadBase, s: Double, m: Double, c: Double) {
        viewModelScope.launch { insumoRepo.insertar(InsumoEntity(nombre=n, tipo=t, unidadBase=u, stockActual=s, stockMinimo=m, costoUnitario=c)) }
    }
    fun agregarProducto(
        nombre: String,
        descripcion: String?,
        precio: Double,
        insumos: List<Pair<Long, Double>>,
        color: Long,
        categoriaId: Long?
    ) {
        if (nombre.isBlank() || precio <= 0) return
        viewModelScope.launch {
            productoRepo.crearProducto(nombre, descripcion, precio, insumos, color, categoriaId)
        }
    }

    fun editarProducto(
        productoId: Long,
        nombre: String,
        descripcion: String?,
        precio: Double,
        insumos: List<Pair<Long, Double>>,
        color: Long,
        categoriaId: Long?
    ) {
        if (nombre.isBlank() || precio <= 0) return
        viewModelScope.launch {
            productoRepo.editarProducto(productoId, nombre, descripcion, precio, insumos, color, categoriaId)
        }
    }

    fun eliminar(producto: ProductoEntity) {
        viewModelScope.launch { productoRepo.desactivar(producto) }
    }

    companion object {
        fun factory(
            productoRepo: ProductoRepository,
            insumoRepo: InsumoRepository,
            categoriaRepo: CategoriaRepository
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ProductosViewModel(productoRepo, insumoRepo, categoriaRepo) as T
        }
    }
}