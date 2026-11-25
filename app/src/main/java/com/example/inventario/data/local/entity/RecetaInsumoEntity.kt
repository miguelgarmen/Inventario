package com.example.inventario.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "receta_insumo",
    primaryKeys = ["recetaId", "insumoId"],
    foreignKeys = [
        ForeignKey(entity = RecetaEntity::class, parentColumns = ["id"], childColumns = ["recetaId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = InsumoEntity::class, parentColumns = ["id"], childColumns = ["insumoId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("insumoId")]
)
data class RecetaInsumoEntity(
    val recetaId: Long,
    val insumoId: Long,
    val cantidad: Double
)