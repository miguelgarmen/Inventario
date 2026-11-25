package com.example.inventario.data.repository

import androidx.room.withTransaction
import com.example.inventario.data.local.InventarioDatabase
import com.example.inventario.data.local.entity.AjusteCajaEntity
import com.example.inventario.data.local.entity.CajaDiaEntity
import com.example.inventario.domain.model.TipoAjusteCaja
import kotlinx.coroutines.flow.Flow

class AjusteCajaRepository(private val db: InventarioDatabase) {

    private val ajusteDao = db.ajusteCajaDao()
    private val cajaDao = db.cajaDiaDao()

    fun observarAjustes(inicio: Long, fin: Long): Flow<List<AjusteCajaEntity>> =
        ajusteDao.observarEntre(inicio, fin)

    fun observarTotalIngresosExtra(inicio: Long, fin: Long): Flow<Double> =
        ajusteDao.observarTotalIngresosExtra(inicio, fin)

    fun observarTotalGastos(inicio: Long, fin: Long): Flow<Double> =
        ajusteDao.observarTotalGastos(inicio, fin)

    fun observarCajaDia(fechaDia: Long): Flow<CajaDiaEntity?> =
        cajaDao.observarPorDia(fechaDia)

    suspend fun agregarAjuste(tipo: TipoAjusteCaja, descripcion: String, monto: Double) {
        ajusteDao.insertar(
            AjusteCajaEntity(
                fecha = System.currentTimeMillis(),
                tipo = tipo,
                descripcion = descripcion.trim(),
                monto = monto
            )
        )
    }

    suspend fun setCajaChica(fechaDia: Long, montoInicial: Double) {
        cajaDao.guardar(CajaDiaEntity(fechaDia, montoInicial))
    }

    suspend fun borrarDelDia(inicio: Long, fin: Long, fechaDia: Long) = db.withTransaction {
        ajusteDao.borrarEntre(inicio, fin)
        cajaDao.borrarDia(fechaDia)
    }
}