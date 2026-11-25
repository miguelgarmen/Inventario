package com.example.inventario.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "caja_dia")
data class CajaDiaEntity(
    @PrimaryKey val fechaDia: Long,   // timestamp de las 00:00 del día
    val montoInicial: Double
)