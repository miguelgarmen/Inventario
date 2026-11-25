package com.example.inventario.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "producto_empaque",
    primaryKeys = ["productoId", "insumoId"],
    foreignKeys = [
        ForeignKey(ProductoEntity::class, ["id"], ["productoId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(InsumoEntity::class, ["id"], ["insumoId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("insumoId")]
)
data class ProductoEmpaqueEntity(
    val productoId: Long,
    val insumoId: Long,
    val cantidad: Double
)