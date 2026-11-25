package com.example.inventario.data.repository

import androidx.room.withTransaction
import com.example.inventario.data.local.InventarioDatabase
import com.example.inventario.data.local.dao.ProductoInsumoDetalle
import com.example.inventario.data.local.entity.ProductoEmpaqueEntity
import com.example.inventario.data.local.entity.ProductoEntity
import kotlinx.coroutines.flow.Flow

class ProductoRepository(private val db: InventarioDatabase) {

    private val dao = db.productoDao()

    fun observarTodos(): Flow<List<ProductoEntity>> = dao.observarTodos()

    fun observarInsumosDeProductos(): Flow<List<ProductoInsumoDetalle>> =
        dao.observarTodosLosInsumosDeProductos()

    suspend fun crearProducto(
        nombre: String,
        descripcion: String?,
        precio: Double,
        insumos: List<Pair<Long, Double>>,
        color: Long,
        categoriaId: Long?
    ): Long = db.withTransaction {
        val productoId = dao.insertarProducto(
            ProductoEntity(
                nombre = nombre,
                descripcion = descripcion?.takeIf { it.isNotBlank() },
                precioVenta = precio,
                color = color,
                categoriaId = categoriaId
            )
        )
        val relaciones = insumos
            .filter { it.second > 0 }
            .map { (insumoId, cantidad) ->
                ProductoEmpaqueEntity(productoId, insumoId, cantidad)
            }
        if (relaciones.isNotEmpty()) dao.insertarInsumosProducto(relaciones)
        productoId
    }

    suspend fun editarProducto(
        productoId: Long,
        nombre: String,
        descripcion: String?,
        precio: Double,
        insumos: List<Pair<Long, Double>>,
        color: Long,
        categoriaId: Long?
    ) = db.withTransaction {
        val actual = dao.porId(productoId) ?: return@withTransaction
        dao.actualizarProducto(
            actual.copy(
                nombre = nombre.trim(),
                descripcion = descripcion?.takeIf { it.isNotBlank() },
                precioVenta = precio,
                color = color,
                categoriaId = categoriaId
            )
        )
        dao.limpiarInsumosDeProducto(productoId)
        val relaciones = insumos
            .filter { it.second > 0 }
            .map { (insumoId, cantidad) ->
                ProductoEmpaqueEntity(productoId, insumoId, cantidad)
            }
        if (relaciones.isNotEmpty()) dao.insertarInsumosProducto(relaciones)
    }

    suspend fun desactivar(producto: ProductoEntity) = dao.desactivar(producto.id)
}