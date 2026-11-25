package com.example.inventario.ui.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.inventario.data.local.entity.InsumoEntity
import com.example.inventario.data.repository.InsumoRepository
import com.example.inventario.data.repository.VentaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class ResumenInicio(
    val totalVendido: Double,
    val cantidadVentas: Int
)

class InicioViewModel(
    insumoRepo: InsumoRepository,
    ventaRepo: VentaRepository
) : ViewModel() {

    // 1. Insumos que están por debajo o igual al stock mínimo
    val insumosBajos = insumoRepo.observarTodos()
        .map { lista -> lista.filter { it.stockActual <= it.stockMinimo } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Resumen de ventas de hoy
    private val rangoHoy = rangoDeHoy()

    val resumenHoy = combine(
        ventaRepo.observarResumenDia(rangoHoy.first, rangoHoy.second),
        ventaRepo.observarCostoInsumosVendidos(rangoHoy.first, rangoHoy.second)
    ) { resumen, _ ->
        ResumenInicio(
            totalVendido = resumen.totalVendido,
            cantidadVentas = resumen.cantidadVentas
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ResumenInicio(0.0, 0))

    private fun rangoDeHoy(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val inicio = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val fin = cal.timeInMillis - 1
        return inicio to fin
    }

    companion object {
        fun factory(i: InsumoRepository, v: VentaRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = InicioViewModel(i, v) as T
        }
    }
}