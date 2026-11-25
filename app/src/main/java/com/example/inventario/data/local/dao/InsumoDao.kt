package com.example.inventario.data.local.dao

import androidx.room.*
import com.example.inventario.data.local.entity.InsumoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InsumoDao {

    @Query("SELECT * FROM insumos WHERE activo = 1 ORDER BY nombre")
    fun observarTodos(): Flow<List<InsumoEntity>>

    @Query("SELECT * FROM insumos WHERE id = :id")
    suspend fun porId(id: Long): InsumoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(insumo: InsumoEntity): Long

    @Update
    suspend fun actualizar(insumo: InsumoEntity)

    @Delete
    suspend fun eliminar(insumo: InsumoEntity)

    @Query("UPDATE insumos SET activo = 0 WHERE id = :id")
    suspend fun desactivar(id: Long)

    @Query("UPDATE insumos SET stockActual = stockActual + :delta WHERE id = :id")
    suspend fun ajustarStock(id: Long, delta: Double)
}