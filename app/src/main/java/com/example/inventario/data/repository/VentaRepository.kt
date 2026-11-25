package com.example.inventario.data.repository

import androidx.room.withTransaction
import com.example.inventario.data.local.InventarioDatabase
import com.example.inventario.data.local.entity.MovimientoInsumoEntity
import com.example.inventario.data.local.entity.VentaDetalleEntity
import com.example.inventario.data.local.entity.VentaEntity
import com.example.inventario.domain.model.MetodoPago
import com.example.inventario.domain.model.TipoMovimiento
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

data class ItemVenta(
    val productoId: Long,
    val nombre: String,
    val precioUnitario: Double,
    val cantidad: Int
)

class VentaRepository(private val db: InventarioDatabase) {

    private val ventaDao = db.ventaDao()
    private val productoDao = db.productoDao()
    private val insumoDao = db.insumoDao()
    private val movDao = db.movimientoDao()

    fun observarResumenDia(inicio: Long, fin: Long) = ventaDao.observarResumenDia(inicio, fin)

    fun observarCostoInsumosVendidos(inicio: Long, fin: Long): Flow<Double> =
        movDao.observarCostoInsumosVendidosEntre(inicio, fin)

    fun observarDetallesVentasEntre(inicio: Long, fin: Long) =
        ventaDao.observarDetallesEntre(inicio, fin)

    fun observarCantidadVendidaPorProducto(inicio: Long, fin: Long) =
        ventaDao.observarCantidadVendidaPorProducto(inicio, fin)

    suspend fun registrarVenta(
        items: List<ItemVenta>,
        metodoPago: MetodoPago
    ): Long = db.withTransaction {
        if (items.isEmpty()) return@withTransaction -1L

        val ahora = System.currentTimeMillis()
        val total = items.sumOf { it.precioUnitario * it.cantidad }

        // Calcular el número de venta del día (reinicia cuando el historial se borra)
        val (inicio, fin) = rangoDelDia(ahora)
        val siguienteNumero = ventaDao.ultimoNumeroDelDia(inicio, fin) + 1

        val ventaId = ventaDao.insertarVenta(
            VentaEntity(
                fecha = ahora,
                total = total,
                metodoPago = metodoPago,
                numeroDia = siguienteNumero
            )
        )

        val detalles = items.map {
            VentaDetalleEntity(
                ventaId = ventaId,
                productoId = it.productoId,
                cantidad = it.cantidad,
                precioUnitario = it.precioUnitario
            )
        }
        ventaDao.insertarDetalles(detalles)

        val movimientos = mutableListOf<MovimientoInsumoEntity>()
        items.forEach { item ->
            val insumosProducto = productoDao.obtenerInsumosDeProducto(item.productoId)
            insumosProducto.forEach { rel ->
                val cantidadTotal = rel.cantidad * item.cantidad
                insumoDao.ajustarStock(rel.insumoId, -cantidadTotal)
                movimientos += MovimientoInsumoEntity(
                    insumoId = rel.insumoId,
                    fecha = ahora,
                    tipo = TipoMovimiento.SALIDA_VENTA,
                    cantidad = cantidadTotal,
                    referenciaTipo = "venta",
                    referenciaId = ventaId,
                    notas = "${item.cantidad}x ${item.nombre}"
                )
            }
            productoDao.ajustarStockProducto(item.productoId, -item.cantidad)
        }
        if (movimientos.isNotEmpty()) movDao.insertarVarios(movimientos)

        ventaId
    }

    suspend fun borrarVentasDelDia(inicio: Long, fin: Long) = db.withTransaction {
        ventaDao.borrarVentasEntre(inicio, fin)
        movDao.borrarSalidasVentaEntre(inicio, fin)
    }

    private fun rangoDelDia(millis: Long): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val inicio = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val fin = cal.timeInMillis - 1
        return inicio to fin
    }

    suspend fun cambiarMetodoPago(ventaId: Long, nuevoMetodo: MetodoPago) = db.withTransaction {
        val ventaActual = ventaDao.obtenerVentaPorId(ventaId) ?: return@withTransaction
        ventaDao.actualizarVenta(ventaActual.copy(metodoPago = nuevoMetodo))
    }

    suspend fun borrarVentaIndividual(ventaId: Long) = db.withTransaction {
        // 1. Obtener los insumos que se gastaron en esta venta
        val movimientos = movDao.obtenerMovimientosPorVenta(ventaId)
        movimientos.forEach { mov ->
            // Se los devolvemos al inventario (sumamos)
            insumoDao.ajustarStock(mov.insumoId, mov.cantidad)
        }

        // 2. Obtener los productos que se vendieron
        val detalles = ventaDao.obtenerDetallesDeVenta(ventaId)
        detalles.forEach { det ->
            // Se los devolvemos al stock del producto (sumamos)
            productoDao.ajustarStockProducto(det.productoId, det.cantidad)
        }

        // 3. Borrar los movimientos de insumo asociados
        movDao.borrarMovimientosPorVenta(ventaId)

        // 4. Borrar la venta y sus detalles (cascade)
        ventaDao.borrarVentaPorId(ventaId)
    }

    fun observarDiasConVentas(): Flow<List<Long>> = ventaDao.observarTodasLasFechas()
}