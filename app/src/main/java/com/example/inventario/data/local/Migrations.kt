package com.example.inventario.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v4 → v5:
 *  - Se crea tabla `categorias`
 *  - Se recrea `productos` con 2 columnas nuevas (`descripcion`, `categoriaId`)
 *    y una nueva FK hacia categorias
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {

        // 1. Tabla de categorías
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `categorias` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `nombre` TEXT NOT NULL,
                `orden` INTEGER NOT NULL,
                `activo` INTEGER NOT NULL
            )
        """.trimIndent())

        // 2. Recrear productos con las nuevas columnas y FKs
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `productos_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `nombre` TEXT NOT NULL,
                `descripcion` TEXT,
                `precioVenta` REAL NOT NULL,
                `recetaId` INTEGER,
                `categoriaId` INTEGER,
                `stockActual` INTEGER NOT NULL,
                `activo` INTEGER NOT NULL,
                `color` INTEGER NOT NULL,
                FOREIGN KEY(`recetaId`) REFERENCES `recetas`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL,
                FOREIGN KEY(`categoriaId`) REFERENCES `categorias`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
            )
        """.trimIndent())

        // 3. Copiar los datos existentes (los nuevos campos van en NULL)
        db.execSQL("""
            INSERT INTO `productos_new`
                (`id`, `nombre`, `descripcion`, `precioVenta`, `recetaId`, `categoriaId`, `stockActual`, `activo`, `color`)
            SELECT `id`, `nombre`, NULL, `precioVenta`, `recetaId`, NULL, `stockActual`, `activo`, `color`
            FROM `productos`
        """.trimIndent())

        // 4. Eliminar la tabla vieja y renombrar la nueva
        db.execSQL("DROP TABLE `productos`")
        db.execSQL("ALTER TABLE `productos_new` RENAME TO `productos`")

        // 5. Índices
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_productos_recetaId` ON `productos` (`recetaId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_productos_categoriaId` ON `productos` (`categoriaId`)")
    }
}

/**
 * v5 → v6:
 *  - Se crea tabla `ajustes_caja` (ingresos extra y gastos)
 *  - Se crea tabla `caja_dia` (caja chica del día)
 */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `ajustes_caja` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `fecha` INTEGER NOT NULL,
                `tipo` TEXT NOT NULL,
                `descripcion` TEXT NOT NULL,
                `monto` REAL NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ajustes_caja_fecha` ON `ajustes_caja` (`fecha`)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `caja_dia` (
                `fechaDia` INTEGER NOT NULL,
                `montoInicial` REAL NOT NULL,
                PRIMARY KEY(`fechaDia`)
            )
        """.trimIndent())
    }
}