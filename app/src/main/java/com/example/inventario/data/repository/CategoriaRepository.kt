package com.example.inventario.data.repository

import com.example.inventario.data.local.dao.CategoriaDao
import com.example.inventario.data.local.entity.CategoriaEntity
import kotlinx.coroutines.flow.Flow

class CategoriaRepository(private val dao: CategoriaDao) {

    fun observarTodas(): Flow<List<CategoriaEntity>> = dao.observarTodas()

    suspend fun crear(nombre: String): Long =
        dao.insertar(CategoriaEntity(nombre = nombre.trim()))

    suspend fun editar(categoria: CategoriaEntity, nuevoNombre: String) =
        dao.actualizar(categoria.copy(nombre = nuevoNombre.trim()))

    suspend fun eliminar(categoria: CategoriaEntity) = dao.desactivar(categoria.id)
}