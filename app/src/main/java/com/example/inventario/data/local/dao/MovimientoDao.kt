package com.example.inventario.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.inventario.data.local.entity.MovimientoInsumoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimientoDao {

    @Insert
    suspend fun insertar(movimiento: MovimientoInsumoEntity)

    @Insert
    suspend fun insertarVarios(movimientos: List<MovimientoInsumoEntity>)

    @Query("""
        SELECT IFNULL(SUM(m.cantidad * i.costoUnitario), 0.0)
        FROM movimientos_insumo m
        INNER JOIN insumos i ON i.id = m.insumoId
        WHERE m.tipo = 'SALIDA_VENTA' AND m.fecha BETWEEN :inicio AND :fin
    """)
    fun observarCostoInsumosVendidosEntre(inicio: Long, fin: Long): Flow<Double>

    @Query("""
        SELECT * FROM movimientos_insumo
        WHERE tipo = 'SALIDA_VENTA' AND fecha BETWEEN :inicio AND :fin
    """)
    suspend fun obtenerSalidasVentaEntre(inicio: Long, fin: Long): List<MovimientoInsumoEntity>

    @Query("""
        DELETE FROM movimientos_insumo
        WHERE tipo = 'SALIDA_VENTA' AND fecha BETWEEN :inicio AND :fin
    """)
    suspend fun borrarSalidasVentaEntre(inicio: Long, fin: Long)

    @Query("DELETE FROM movimientos_insumo WHERE referenciaTipo = 'venta' AND referenciaId = :ventaId")
    suspend fun borrarMovimientosPorVenta(ventaId: Long)

    @Query("SELECT * FROM movimientos_insumo WHERE referenciaTipo = 'venta' AND referenciaId = :ventaId")
    suspend fun obtenerMovimientosPorVenta(ventaId: Long): List<MovimientoInsumoEntity>

}