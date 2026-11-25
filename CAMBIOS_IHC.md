# Inventario — revisión IHC
Proyecto original Android Studio, Kotlin / Jetpack Compose / Room. Se conservan las tablas originales para evitar una migración destructiva.

- Inicio: instrucciones, notas/deudas persistentes por usuario, aviso de fecha histórica y caja no abierta.
- Ventas: fecha compartida con historial, bloqueo de fechas pasadas, apertura de caja incluso con $0 y confirmación de cobro.
- Productos: formulario sin descripción ni selector de categoría, lista plana, alta de insumos dentro del formulario, costo estimado por receta, confirmación de edición y baja.
- Insumos: captura en gramos o kilos (almacenamiento en gramos), unidades explícitas, validación numérica y confirmaciones. Baja lógica para conservar relaciones históricas.
- Historial: indicadores de ganancia, precio medio por unidad vendida y postre más vendido para día, 7 días o mes móvil. El detalle y corte siguen siendo diarios para evitar acciones destructivas sobre periodos completos. Accesos a fechas con ventas marcados con punto, también dentro del diálogo de calendario (no se colorean las celdas del calendario nativo). Se retira el alta de ingresos extra, se conservan registros anteriores.

## Pruebas manuales pendientes
1. Abrir el proyecto en Android Studio, sincronizar Gradle y ejecutar en emulador/API 35.
2. Registrar usuario, intentar campos vacíos y contraseña incorrecta.
3. Crear insumo de 1 kg a $40; comprobar 1000 g y $0.04/g. Crear producto con 100 g; costo $4. Crear otro insumo sin salir del formulario.
4. Cancelar y confirmar cambios y bajas.
5. Intentar vender sin caja, abrir con $0, registrar venta y consultar ayer: verificar bloqueo en Vender e Inicio. Volver a hoy.
6. Consultar indicadores de periodos y fechas con ventas. Escribir notas, cerrar y reabrir aplicación.

No se ha compilado ni ejecutado el proyecto en este entorno; estas pruebas son pendientes. No incluye APK ni cachés de compilación. Los ingresos históricos se conservan; el precio promedio mostrado es de unidades vendidas, no un cálculo de promedio ponderado de compras de insumos.
