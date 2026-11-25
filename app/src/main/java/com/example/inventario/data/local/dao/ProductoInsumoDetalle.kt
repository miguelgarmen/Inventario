package com.example.inventario.data.local.dao

import com.example.inventario.domain.model.UnidadBase

data class ProductoInsumoDetalle(
    val productoId: Long,
    val insumoId: Long,
    val insumoNombre: String,
    val unidadBase: UnidadBase,
    val cantidad: Double,
    val costoUnitario: Double
)