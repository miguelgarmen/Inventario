package com.example.inventario.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "productos",
    foreignKeys = [
        ForeignKey(
            entity = RecetaEntity::class,
            parentColumns = ["id"],
            childColumns = ["recetaId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CategoriaEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("recetaId"), Index("categoriaId")]
)
data class ProductoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val descripcion: String? = null,
    val precioVenta: Double,
    val recetaId: Long? = null,
    val categoriaId: Long? = null,
    val stockActual: Int = 0,
    val activo: Boolean = true,
    val color: Long = 0xFF5C6BC0
)