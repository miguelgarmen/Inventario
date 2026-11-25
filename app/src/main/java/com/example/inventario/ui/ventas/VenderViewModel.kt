package com.example.inventario.ui.ventas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.inventario.data.local.dao.VentaDetalleConProducto
import com.example.inventario.data.local.entity.AjusteCajaEntity
import com.example.inventario.data.local.entity.CategoriaEntity
import com.example.inventario.data.repository.AjusteCajaRepository
import com.example.inventario.data.repository.CategoriaRepository
import com.example.inventario.data.repository.ItemVenta
import com.example.inventario.data.repository.ProductoRepository
import com.example.inventario.data.repository.VentaRepository
import com.example.inventario.domain.model.MetodoPago
import com.example.inventario.domain.model.TipoAjusteCaja
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class ResumenDia(
    val totalVendido: Double,
    val costoInsumos: Double,
    val totalEfectivo: Double,
    val totalTransferencia: Double,
    val cantidadVentas: Int,
    val ingresosExtra: Double,
    val gastos: Double,
    val cajaChicaInicial: Double
) {
    val ganancia: Double get() = totalVendido - costoInsumos + ingresosExtra - gastos
    val efectivoEsperado: Double get() = cajaChicaInicial + totalEfectivo + ingresosExtra - gastos
}

data class VentaAgrupada(
    val ventaId: Long,
    val numeroDia: Int,
    val fecha: Long,
    val total: Double,
    val metodoPago: MetodoPago,
    val items: List<VentaDetalleConProducto>
)

data class ProductoVendidoResumen(
    val productoId: Long,
    val nombre: String,
    val cantidad: Int,
    val total: Double
)

@OptIn(ExperimentalCoroutinesApi::class)
class VenderViewModel(
    private val productoRepo: ProductoRepository,
    private val ventaRepo: VentaRepository,
    private val categoriaRepo: CategoriaRepository,
    private val ajusteRepo: AjusteCajaRepository
) : ViewModel() {

    val categorias: StateFlow<List<CategoriaEntity>> = categoriaRepo.observarTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _categoriaFiltro = MutableStateFlow<Long?>(null)
    val categoriaFiltro: StateFlow<Long?> = _categoriaFiltro.asStateFlow()

    fun setCategoriaFiltro(id: Long?) { _categoriaFiltro.value = id }

    val productosUI = combine(
        productoRepo.observarTodos(),
        productoRepo.observarInsumosDeProductos(),
        categoriaRepo.observarTodas()
    ) { productos, relaciones, cats ->
        productos.map { p ->
            ProductoUI(
                producto = p,
                insumos = relaciones.filter { it.productoId == p.id },
                categoriaNombre = cats.firstOrNull { it.id == p.categoriaId }?.nombre
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _carrito = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val carrito: StateFlow<Map<Long, Int>> = _carrito.asStateFlow()

    private val _metodoPago = MutableStateFlow(MetodoPago.EFECTIVO)
    val metodoPago: StateFlow<MetodoPago> = _metodoPago.asStateFlow()

    // --- LÓGICA DE FECHAS PARA EL HISTORIAL ---
    private val _fechaSeleccionada = MutableStateFlow(System.currentTimeMillis())
    val fechaSeleccionada: StateFlow<Long> = _fechaSeleccionada.asStateFlow()

    fun esFechaActual(fecha: Long): Boolean = rangoDelDia(fecha).first == rangoDelDia(System.currentTimeMillis()).first
    val cajaAbierta = _fechaSeleccionada.flatMapLatest { fecha -> ajusteRepo.observarCajaDia(rangoDelDia(fecha).first) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    private val _periodo = MutableStateFlow(1)
    val periodo = _periodo.asStateFlow()
    fun setPeriodo(dias: Int) { _periodo.value = dias }
    private fun rangoPeriodo(fecha: Long, dias: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply { timeInMillis = fecha; if (dias == 30) { add(Calendar.MONTH, -1); add(Calendar.DAY_OF_YEAR, 1) } else add(Calendar.DAY_OF_YEAR, -(dias-1)) }
        return rangoDelDia(cal.timeInMillis).first to rangoDelDia(fecha).second
    }
    data class Estadisticas(val ganancia: Double, val promedio: Double, val masVendido: String)
    val estadisticas = combine(_fechaSeleccionada, _periodo) { fecha, dias -> rangoPeriodo(fecha,dias) }.flatMapLatest { (inicio, fin) ->
        combine(ventaRepo.observarResumenDia(inicio, fin), ventaRepo.observarCostoInsumosVendidos(inicio, fin), ajusteRepo.observarTotalGastos(inicio, fin), ventaRepo.observarDetallesVentasEntre(inicio, fin)) { r, costo, gastos, detalles ->
            val unidades = detalles.sumOf { it.cantidad }
            val top = detalles.groupBy { it.productoNombre }.maxByOrNull { (_, items) -> items.sumOf { it.cantidad } }?.key ?: "Sin ventas"
            Estadisticas(r.totalVendido - costo - gastos, if (unidades > 0) r.totalVendido / unidades else 0.0, top)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Estadisticas(0.0,0.0,"Sin ventas"))

    fun setFechaSeleccionada(millis: Long) {
        _fechaSeleccionada.value = millis
    }

    fun cambiarDia(deltaDias: Int) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = _fechaSeleccionada.value
            add(Calendar.DAY_OF_YEAR, deltaDias)
        }
        _fechaSeleccionada.value = cal.timeInMillis
    }

    private fun rangoDelDia(millis: Long): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val inicio = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val fin = cal.timeInMillis - 1
        return inicio to fin
    }

    // --- FLUJOS REACTIVOS A LA FECHA ---

    val resumenDia: StateFlow<ResumenDia> = _fechaSeleccionada.flatMapLatest { fecha ->
        val (inicio, fin) = rangoDelDia(fecha)
        combine(
            ventaRepo.observarResumenDia(inicio, fin),
            ventaRepo.observarCostoInsumosVendidos(inicio, fin),
            ajusteRepo.observarTotalIngresosExtra(inicio, fin),
            ajusteRepo.observarTotalGastos(inicio, fin),
            ajusteRepo.observarCajaDia(inicio)
        ) { resumen, costo, ingresosExtra, gastos, caja ->
            ResumenDia(
                totalVendido = resumen.totalVendido,
                costoInsumos = costo,
                totalEfectivo = resumen.totalEfectivo,
                totalTransferencia = resumen.totalTransferencia,
                cantidadVentas = resumen.cantidadVentas,
                ingresosExtra = ingresosExtra,
                gastos = gastos,
                cajaChicaInicial = caja?.montoInicial ?: 0.0
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),
        ResumenDia(0.0, 0.0, 0.0, 0.0, 0, 0.0, 0.0, 0.0))

    val historialDia: StateFlow<List<VentaAgrupada>> = _fechaSeleccionada.flatMapLatest { fecha ->
        val (inicio, fin) = rangoDelDia(fecha)
        ventaRepo.observarDetallesVentasEntre(inicio, fin)
            .map { detalles ->
                detalles.groupBy { it.ventaId }.map { (ventaId, items) ->
                    VentaAgrupada(
                        ventaId = ventaId,
                        numeroDia = items.first().numeroDia,
                        fecha = items.first().ventaFecha,
                        total = items.sumOf { it.precioUnitario * it.cantidad },
                        metodoPago = items.first().metodoPago,
                        items = items
                    )
                }.sortedByDescending { it.fecha }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ajustesDia: StateFlow<List<AjusteCajaEntity>> = _fechaSeleccionada.flatMapLatest { fecha ->
        val (inicio, fin) = rangoDelDia(fecha)
        ajusteRepo.observarAjustes(inicio, fin)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val productosVendidosResumen: StateFlow<List<ProductoVendidoResumen>> = historialDia.map { ventas ->
        ventas.flatMap { it.items }
            .groupBy { it.productoId }
            .map { (pid, items) ->
                ProductoVendidoResumen(
                    productoId = pid,
                    nombre = items.first().productoNombre,
                    cantidad = items.sumOf { it.cantidad },
                    total = items.sumOf { it.precioUnitario * it.cantidad }
                )
            }.sortedByDescending { it.cantidad }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // NUEVO: Obtener días únicos (a las 00:00) en los que hubo ventas
    val diasConVentas: StateFlow<List<Long>> = ventaRepo.observarDiasConVentas()
        .map { fechas ->
            fechas.map { millis ->
                val cal = java.util.Calendar.getInstance().apply {
                    timeInMillis = millis
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }
                cal.timeInMillis
            }.distinct().sorted()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- FUNCIONES DE ACCIÓN ---
    fun agregarAlCarrito(productoId: Long) {
        _carrito.value = _carrito.value.toMutableMap().apply {
            this[productoId] = (this[productoId] ?: 0) + 1
        }
    }

    fun quitarDelCarrito(productoId: Long) {
        _carrito.value = _carrito.value.toMutableMap().apply {
            val actual = this[productoId] ?: 0
            if (actual <= 1) remove(productoId) else this[productoId] = actual - 1
        }
    }

    fun setMetodoPago(m: MetodoPago) { _metodoPago.value = m }

    fun limpiarCarrito() { _carrito.value = emptyMap() }

    fun confirmarVenta() {
        val carritoActual = _carrito.value
        val productos = productosUI.value
        if (carritoActual.isEmpty() || !esFechaActual(_fechaSeleccionada.value) || cajaAbierta.value == null) return
        val items = carritoActual.mapNotNull { (id, cant) ->
            val prod = productos.firstOrNull { it.producto.id == id } ?: return@mapNotNull null
            ItemVenta(productoId = id, nombre = prod.producto.nombre, precioUnitario = prod.producto.precioVenta, cantidad = cant)
        }
        viewModelScope.launch {
            ventaRepo.registrarVenta(items, _metodoPago.value)
            _carrito.value = emptyMap()
            // Al terminar de vender, regresamos la vista a "Hoy" por si estaban viendo otro día
            _fechaSeleccionada.value = System.currentTimeMillis()
        }
    }



    fun agregarGasto(descripcion: String, monto: Double) {
        if (descripcion.isBlank() || monto <= 0) return
        viewModelScope.launch { ajusteRepo.agregarAjuste(TipoAjusteCaja.GASTO, descripcion, monto) }
    }

    fun setCajaChica(monto: Double) {
        if (!esFechaActual(_fechaSeleccionada.value) || monto < 0 || !monto.isFinite()) return
        viewModelScope.launch {
            val (inicio, _) = rangoDelDia(_fechaSeleccionada.value)
            ajusteRepo.setCajaChica(inicio, monto)
        }
    }

    fun borrarVentasDelDia() {
        viewModelScope.launch {
            val (inicio, fin) = rangoDelDia(_fechaSeleccionada.value)
            ventaRepo.borrarVentasDelDia(inicio, fin)
            ajusteRepo.borrarDelDia(inicio, fin, inicio)
        }
    }

    fun borrarVenta(ventaId: Long) {
        viewModelScope.launch { ventaRepo.borrarVentaIndividual(ventaId) }
    }

    fun cambiarMetodoPagoVenta(ventaId: Long, nuevoMetodo: MetodoPago) {
        viewModelScope.launch { ventaRepo.cambiarMetodoPago(ventaId, nuevoMetodo) }
    }

    companion object {
        fun factory(p: ProductoRepository, v: VentaRepository, c: CategoriaRepository, a: AjusteCajaRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = VenderViewModel(p, v, c, a) as T
        }
    }
}