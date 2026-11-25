package com.example.inventario.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "producciones")
data class ProduccionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recetaId: Long,
    val cantidadProducida: Double,
    val fecha: Long,
    val notas: String? = null
)