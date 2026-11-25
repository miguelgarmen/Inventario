package com.example.inventario.data.local

import androidx.room.TypeConverter
import com.example.inventario.domain.model.*

class Converters {
    @TypeConverter fun tipoInsumoToString(v: TipoInsumo) = v.name
    @TypeConverter fun stringToTipoInsumo(v: String) = TipoInsumo.valueOf(v)

    @TypeConverter fun unidadBaseToString(v: UnidadBase) = v.name
    @TypeConverter fun stringToUnidadBase(v: String) = UnidadBase.valueOf(v)

    @TypeConverter fun tipoMovToString(v: TipoMovimiento) = v.name
    @TypeConverter fun stringToTipoMov(v: String) = TipoMovimiento.valueOf(v)

    @TypeConverter fun metodoPagoToString(v: MetodoPago) = v.name
    @TypeConverter fun stringToMetodoPago(v: String) = MetodoPago.valueOf(v)

    @TypeConverter fun tipoAjusteToString(v: TipoAjusteCaja) = v.name
    @TypeConverter fun stringToTipoAjuste(v: String) = TipoAjusteCaja.valueOf(v)
}