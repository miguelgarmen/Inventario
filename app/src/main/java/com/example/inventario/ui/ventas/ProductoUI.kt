package com.example.inventario.ui.ventas

import com.example.inventario.data.local.dao.ProductoInsumoDetalle
import com.example.inventario.data.local.entity.ProductoEntity

data class ProductoUI(
    val producto: ProductoEntity,
    val insumos: List<ProductoInsumoDetalle>,
    val categoriaNombre: String? = null
) {
    val costoTotal: Double get() = insumos.sumOf { it.cantidad * it.costoUnitario }
    val margen: Double get() = producto.precioVenta - costoTotal
    val margenPorcentaje: Double
        get() = if (producto.precioVenta > 0) (margen / producto.precioVenta) * 100 else 0.0
}