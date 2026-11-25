package com.example.inventario.data.local.dao

import androidx.room.*
import com.example.inventario.data.local.entity.AjusteCajaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AjusteCajaDao {

    @Insert
    suspend fun insertar(ajuste: AjusteCajaEntity): Long

    @Query("SELECT * FROM ajustes_caja WHERE fecha BETWEEN :inicio AND :fin ORDER BY fecha DESC")
    fun observarEntre(inicio: Long, fin: Long): Flow<List<AjusteCajaEntity>>

    @Query("""
        SELECT IFNULL(SUM(monto), 0.0) FROM ajustes_caja
        WHERE tipo = 'INGRESO_EXTRA' AND fecha BETWEEN :inicio AND :fin
    """)
    fun observarTotalIngresosExtra(inicio: Long, fin: Long): Flow<Double>

    @Query("""
        SELECT IFNULL(SUM(monto), 0.0) FROM ajustes_caja
        WHERE tipo = 'GASTO' AND fecha BETWEEN :inicio AND :fin
    """)
    fun observarTotalGastos(inicio: Long, fin: Long): Flow<Double>

    @Query("DELETE FROM ajustes_caja WHERE fecha BETWEEN :inicio AND :fin")
    suspend fun borrarEntre(inicio: Long, fin: Long)
}