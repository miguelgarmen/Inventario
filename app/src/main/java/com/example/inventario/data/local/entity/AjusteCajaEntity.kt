package com.example.inventario.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.inventario.domain.model.TipoAjusteCaja

@Entity(tableName = "ajustes_caja", indices = [Index("fecha")])
data class AjusteCajaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fecha: Long,
    val tipo: TipoAjusteCaja,
    val descripcion: String,
    val monto: Double
)