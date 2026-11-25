package com.example.inventario.data.repository

import com.example.inventario.data.local.dao.InsumoDao
import com.example.inventario.data.local.entity.InsumoEntity
import kotlinx.coroutines.flow.Flow

class InsumoRepository(private val dao: InsumoDao) {

    fun observarTodos(): Flow<List<InsumoEntity>> = dao.observarTodos()

    suspend fun insertar(insumo: InsumoEntity): Long = dao.insertar(insumo)

    suspend fun actualizar(insumo: InsumoEntity) = dao.actualizar(insumo)

    suspend fun eliminar(insumo: InsumoEntity) = dao.desactivar(insumo.id)

    suspend fun ajustarStock(id: Long, delta: Double) = dao.ajustarStock(id, delta)
}