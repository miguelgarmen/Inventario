package com.example.inventario.data.local.dao

import androidx.room.*
import com.example.inventario.data.local.entity.CategoriaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {

    @Query("SELECT * FROM categorias WHERE activo = 1 ORDER BY orden, nombre")
    fun observarTodas(): Flow<List<CategoriaEntity>>

    @Query("SELECT * FROM categorias WHERE id = :id")
    suspend fun porId(id: Long): CategoriaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(categoria: CategoriaEntity): Long

    @Update
    suspend fun actualizar(categoria: CategoriaEntity)

    @Query("UPDATE categorias SET activo = 0 WHERE id = :id")
    suspend fun desactivar(id: Long)
}