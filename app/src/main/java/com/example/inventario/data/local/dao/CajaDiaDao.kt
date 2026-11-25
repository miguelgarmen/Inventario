package com.example.inventario.data.local.dao

import androidx.room.*
import com.example.inventario.data.local.entity.CajaDiaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CajaDiaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(caja: CajaDiaEntity)

    @Query("SELECT * FROM caja_dia WHERE fechaDia = :fechaDia")
    fun observarPorDia(fechaDia: Long): Flow<CajaDiaEntity?>

    @Query("DELETE FROM caja_dia WHERE fechaDia = :fechaDia")
    suspend fun borrarDia(fechaDia: Long)
}