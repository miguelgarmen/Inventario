package com.example.inventario.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.inventario.data.local.entity.ProductoEmpaqueEntity
import com.example.inventario.data.local.entity.ProductoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {

    @Query("SELECT * FROM productos WHERE activo = 1 ORDER BY nombre")
    fun observarTodos(): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM productos WHERE id = :id")
    suspend fun porId(id: Long): ProductoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProducto(producto: ProductoEntity): Long

    @Update
    suspend fun actualizarProducto(producto: ProductoEntity)

    @Query("UPDATE productos SET activo = 0 WHERE id = :id")
    suspend fun desactivar(id: Long)

    // Insumos (ingredientes o empaques) asociados a los productos
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarInsumosProducto(items: List<ProductoEmpaqueEntity>)

    @Query("DELETE FROM producto_empaque WHERE productoId = :productoId")
    suspend fun limpiarInsumosDeProducto(productoId: Long)

    @Query("""
        SELECT pe.productoId AS productoId,
               pe.insumoId AS insumoId,
               i.nombre AS insumoNombre,
               i.unidadBase AS unidadBase,
               pe.cantidad AS cantidad,
               i.costoUnitario AS costoUnitario
        FROM producto_empaque pe
        INNER JOIN insumos i ON i.id = pe.insumoId
    """)
    fun observarTodosLosInsumosDeProductos(): Flow<List<ProductoInsumoDetalle>>

    @Query("""
        SELECT pe.productoId AS productoId,
               pe.insumoId AS insumoId,
               i.nombre AS insumoNombre,
               i.unidadBase AS unidadBase,
               pe.cantidad AS cantidad,
               i.costoUnitario AS costoUnitario
        FROM producto_empaque pe
        INNER JOIN insumos i ON i.id = pe.insumoId
        WHERE pe.productoId = :productoId
    """)
    suspend fun obtenerInsumosDeProducto(productoId: Long): List<ProductoInsumoDetalle>

    @Query("UPDATE productos SET stockActual = stockActual + :delta WHERE id = :id")
    suspend fun ajustarStockProducto(id: Long, delta: Int)
}