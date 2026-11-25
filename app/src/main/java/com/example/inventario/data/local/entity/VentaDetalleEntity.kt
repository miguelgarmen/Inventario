package com.example.inventario.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "venta_detalle",
    foreignKeys = [
        ForeignKey(VentaEntity::class, ["id"], ["ventaId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(ProductoEntity::class, ["id"], ["productoId"])
    ],
    indices = [Index("ventaId"), Index("productoId")]
)
data class VentaDetalleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ventaId: Long,
    val productoId: Long,
    val cantidad: Int,
    val precioUnitario: Double
)