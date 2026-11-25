package com.example.inventario.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.inventario.domain.model.MetodoPago

@Entity(tableName = "ventas")
data class VentaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fecha: Long,
    val total: Double,
    val metodoPago: MetodoPago = MetodoPago.EFECTIVO,
    val numeroDia: Int = 0
)