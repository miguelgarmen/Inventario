package com.example.inventario.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.inventario.domain.model.TipoInsumo
import com.example.inventario.domain.model.UnidadBase

@Entity(tableName = "insumos")
data class InsumoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val tipo: TipoInsumo,
    val unidadBase: UnidadBase,
    val stockActual: Double = 0.0,
    val stockMinimo: Double = 0.0,
    val costoUnitario: Double = 0.0,
    val activo: Boolean = true
)