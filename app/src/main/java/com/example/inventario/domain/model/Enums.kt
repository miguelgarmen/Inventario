package com.example.inventario.domain.model

enum class TipoInsumo { INGREDIENTE, EMPAQUE }
enum class UnidadBase { GRAMO, MILILITRO, PIEZA }
enum class TipoMovimiento {
    ENTRADA, SALIDA_PRODUCCION, SALIDA_VENTA, AJUSTE, MERMA
}
enum class MetodoPago { EFECTIVO, TRANSFERENCIA }
enum class TipoAjusteCaja { INGRESO_EXTRA, GASTO }