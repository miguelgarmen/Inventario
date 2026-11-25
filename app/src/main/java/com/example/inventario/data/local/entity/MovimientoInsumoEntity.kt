package com.example.inventario.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.inventario.domain.model.TipoMovimiento

@Entity(
    tableName = "movimientos_insumo",
    indices = [Index("insumoId"), Index("fecha")]
)
data class MovimientoInsumoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val insumoId: Long,
    val fecha: Long,
    val tipo: TipoMovimiento,
    val cantidad: Double,
    val referenciaTipo: String? = null,
    val referenciaId: Long? = null,
    val notas: String? = null
)