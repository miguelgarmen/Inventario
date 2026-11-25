package com.example.inventario.data.local.dao

import androidx.room.*
import com.example.inventario.data.local.entity.VentaDetalleEntity
import com.example.inventario.data.local.entity.VentaEntity
import com.example.inventario.domain.model.MetodoPago
import kotlinx.coroutines.flow.Flow

data class VentaResumenDia(
    val totalVendido: Double,
    val cantidadVentas: Int,
    val totalEfectivo: Double,
    val totalTransferencia: Double
)

data class VentaDetalleConProducto(
    val detalleId: Long,
    val ventaId: Long,
    val numeroDia: Int,
    val productoId: Long,
    val productoNombre: String,
    val cantidad: Int,
    val precioUnitario: Double,
    val ventaFecha: Long,
    val metodoPago: MetodoPago
)

data class ProductoVendido(
    val productoId: Long,
    val cantidadVendida: Int
)

@Dao
interface VentaDao {

    @Insert
    suspend fun insertarVenta(venta: VentaEntity): Long

    @Insert
    suspend fun insertarDetalles(detalles: List<VentaDetalleEntity>)

    @Query("SELECT * FROM ventas WHERE fecha BETWEEN :inicio AND :fin ORDER BY fecha DESC")
    fun observarVentasEntre(inicio: Long, fin: Long): Flow<List<VentaEntity>>

    @Query("""
        SELECT IFNULL(SUM(total), 0.0) AS totalVendido,
               COUNT(*) AS cantidadVentas,
               IFNULL(SUM(CASE WHEN metodoPago = 'EFECTIVO' THEN total ELSE 0 END), 0.0) AS totalEfectivo,
               IFNULL(SUM(CASE WHEN metodoPago = 'TRANSFERENCIA' THEN total ELSE 0 END), 0.0) AS totalTransferencia
        FROM ventas WHERE fecha BETWEEN :inicio AND :fin
    """)
    fun observarResumenDia(inicio: Long, fin: Long): Flow<VentaResumenDia>

    @Query("""
        SELECT vd.id AS detalleId,
               vd.ventaId AS ventaId,
               v.numeroDia AS numeroDia,
               vd.productoId AS productoId,
               p.nombre AS productoNombre,
               vd.cantidad AS cantidad,
               vd.precioUnitario AS precioUnitario,
               v.fecha AS ventaFecha,
               v.metodoPago AS metodoPago
        FROM venta_detalle vd
        INNER JOIN productos p ON p.id = vd.productoId
        INNER JOIN ventas v ON v.id = vd.ventaId
        WHERE v.fecha BETWEEN :inicio AND :fin
        ORDER BY v.fecha DESC
    """)
    fun observarDetallesEntre(inicio: Long, fin: Long): Flow<List<VentaDetalleConProducto>>

    @Query("""
        SELECT vd.productoId AS productoId,
               SUM(vd.cantidad) AS cantidadVendida
        FROM venta_detalle vd
        INNER JOIN ventas v ON v.id = vd.ventaId
        WHERE v.fecha BETWEEN :inicio AND :fin
        GROUP BY vd.productoId
    """)
    fun observarCantidadVendidaPorProducto(inicio: Long, fin: Long): Flow<List<ProductoVendido>>

    @Query("SELECT IFNULL(MAX(numeroDia), 0) FROM ventas WHERE fecha BETWEEN :inicio AND :fin")
    suspend fun ultimoNumeroDelDia(inicio: Long, fin: Long): Int

    @Query("SELECT * FROM venta_detalle WHERE ventaId IN (SELECT id FROM ventas WHERE fecha BETWEEN :inicio AND :fin)")
    suspend fun obtenerDetallesEntreList(inicio: Long, fin: Long): List<VentaDetalleEntity>

    @Query("DELETE FROM ventas WHERE fecha BETWEEN :inicio AND :fin")
    suspend fun borrarVentasEntre(inicio: Long, fin: Long)

    @Update
    suspend fun actualizarVenta(venta: VentaEntity)

    @Query("DELETE FROM ventas WHERE id = :id")
    suspend fun borrarVentaPorId(id: Long)

    @Query("SELECT * FROM ventas WHERE id = :id") suspend fun obtenerVentaPorId(id: Long): VentaEntity?

    @Query("SELECT * FROM venta_detalle WHERE ventaId = :ventaId")
    suspend fun obtenerDetallesDeVenta(ventaId: Long): List<VentaDetalleEntity>

    @Query("SELECT DISTINCT fecha FROM ventas ORDER BY fecha DESC")
    fun observarTodasLasFechas(): Flow<List<Long>>
}