package com.example.inventario.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.inventario.data.local.dao.*
import com.example.inventario.data.local.entity.*

@Database(
    entities = [
        InsumoEntity::class, RecetaEntity::class, RecetaInsumoEntity::class,
        ProductoEntity::class, ProductoEmpaqueEntity::class, ProduccionEntity::class,
        VentaEntity::class, VentaDetalleEntity::class, MovimientoInsumoEntity::class,
        CategoriaEntity::class, AjusteCajaEntity::class, CajaDiaEntity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class InventarioDatabase : RoomDatabase() {
    abstract fun insumoDao(): InsumoDao
    abstract fun productoDao(): ProductoDao
    abstract fun ventaDao(): VentaDao
    abstract fun movimientoDao(): MovimientoDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun ajusteCajaDao(): AjusteCajaDao
    abstract fun cajaDiaDao(): CajaDiaDao

    companion object {
        // Guardamos una instancia por cada usuario en memoria
        private val instancias = mutableMapOf<String, InventarioDatabase>()

        fun get(context: Context, username: String): InventarioDatabase {
            val nombreDB = "inventario_$username.db"

            return instancias.getOrPut(nombreDB) {
                Room.databaseBuilder(
                    context.applicationContext,
                    InventarioDatabase::class.java,
                    nombreDB
                )
                    .fallbackToDestructiveMigration() // Si cambias la BD, se borra solo la de ese usuario
                    .build()
            }
        }
    }
}