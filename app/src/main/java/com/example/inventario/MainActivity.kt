package com.example.inventario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.inventario.data.local.InventarioDatabase
import com.example.inventario.data.repository.*
import com.example.inventario.navigation.Destination
import com.example.inventario.ui.components.PantallaDatosBancarios
import com.example.inventario.ui.components.TutorialOverlay
import com.example.inventario.ui.components.TutorialStep
import com.example.inventario.ui.inicio.InicioViewModel
import com.example.inventario.ui.inicio.PantallaInicio
import com.example.inventario.ui.inventario.InventarioViewModel
import com.example.inventario.ui.inventario.PantallaInventario
import com.example.inventario.ui.login.PantallaLogin
import com.example.inventario.ui.productos.PantallaGestionProductos
import com.example.inventario.ui.theme.InventarioTheme
import com.example.inventario.ui.ventas.PantallaHistorialVentas
import com.example.inventario.ui.ventas.PantallaVender
import com.example.inventario.ui.ventas.VenderViewModel
import com.example.inventario.utils.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val sessionManager = remember { (application as InventarioApp).sessionManager }

            var logueado by remember { mutableStateOf(sessionManager.getUsuarioActual() != null) }
            var modoOscuro by remember { mutableStateOf(false) }

            InventarioTheme(darkTheme = modoOscuro) {
                if (!logueado) {
                    PantallaLogin(
                        sessionManager = sessionManager,
                        onLoginExitoso = { logueado = true }
                    )
                } else {
                    val username = sessionManager.getUsuarioActual()!!
                    val db = remember(username) { InventarioDatabase.get(context, username) }

                    val insumoRepo = remember { InsumoRepository(db.insumoDao()) }
                    val productoRepo = remember { ProductoRepository(db) }
                    val ventaRepo = remember { VentaRepository(db) }
                    val categoriaRepo = remember { CategoriaRepository(db.categoriaDao()) }
                    val ajusteRepo = remember { AjusteCajaRepository(db) }

                    MainScreen(
                        insumoRepo, productoRepo, ventaRepo, categoriaRepo, ajusteRepo,
                        sessionManager, username, modoOscuro,
                        onToggleModoOscuro = { modoOscuro = !modoOscuro },
                        onCerrarSesion = {
                            sessionManager.cerrarSesion()
                            logueado = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    insumoRepo: InsumoRepository, productoRepo: ProductoRepository, ventaRepo: VentaRepository,
    categoriaRepo: CategoriaRepository, ajusteRepo: AjusteCajaRepository,
    sessionManager: SessionManager, username: String, modoOscuro: Boolean,
    onToggleModoOscuro: () -> Unit, onCerrarSesion: () -> Unit
) {
    val navController = rememberNavController()
    val ventasVm: VenderViewModel = viewModel(factory = VenderViewModel.factory(productoRepo, ventaRepo, categoriaRepo, ajusteRepo))
    val items = listOf(
        Destination.Inicio, Destination.Vender, Destination.Productos,
        Destination.Insumos, Destination.Historial
    )

    var menuExpandido by remember { mutableStateOf(false) }
    var mostrarCambiarPass by remember { mutableStateOf(false) }
    var mostrarDatosBancarios by remember { mutableStateOf(false) }

    // --- ESTADOS PARA EL TUTORIAL ---
    var mostrarTutorial by remember { mutableStateOf(!sessionManager.isTutorialVisto()) }
    var tutorialStep by remember { mutableStateOf(0) }
    var rectBarraAbajo by remember { mutableStateOf<Rect?>(null) }
    var rectMenuArriba by remember { mutableStateOf<Rect?>(null) }

    val tutorialSteps = listOf(
        TutorialStep("¡Bienvenido a tu negocio!", "Te guiaremos paso a paso para que aprendas a usar la aplicación desde cero. Toca 'Siguiente'.", null, actionRoute = Destination.Inicio.route),
        TutorialStep("Paso 1: Vender", "En esta sección verás tus postres. Abre turno con caja chica. Toca 'Agregar' para armar el carrito y presiona 'Cobrar' para registrar la venta.", rectBarraAbajo, actionRoute = Destination.Vender.route),
        TutorialStep("Paso 2: Productos", "Aquí creas tus postres. Usa el botón '+' para ponerle nombre, precio, color y los insumos que lleva.", rectBarraAbajo, actionRoute = Destination.Productos.route),
        TutorialStep("Paso 3: Insumos", "Registra aquí tus ingredientes (azúcar, leche) y empaques (vasos, cucharas) para saber cuánto te queda en stock.", rectBarraAbajo, actionRoute = Destination.Insumos.route),
        TutorialStep("Paso 4: Historial y Corte", "Tu centro de control. Revisa las ventas del día, registra gastos y consulta días pasados.", rectBarraAbajo, actionRoute = Destination.Historial.route),
        TutorialStep("Opciones de Usuario", "Desde el menú de 3 puntitos puedes guardar tus Datos de Cobro, cambiar contraseña, activar el Modo Oscuro o Cerrar Sesión.", rectMenuArriba, alignBottom = true, actionRoute = null)
    )

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Inventario App", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    actions = {
                        IconButton(onClick = {
                            tutorialStep = 0
                            mostrarTutorial = true
                            navController.navigate(Destination.Inicio.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }) {
                            Icon(Icons.Filled.Help, contentDescription = "Ayuda")
                        }

                        IconButton(
                            onClick = { menuExpandido = true },
                            modifier = Modifier.onGloballyPositioned { rectMenuArriba = it.boundsInRoot() }
                        ) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Opciones")
                        }
                        DropdownMenu(expanded = menuExpandido, onDismissRequest = { menuExpandido = false }) {
                            DropdownMenuItem(text = { Text("Datos de Cobro") }, onClick = { mostrarDatosBancarios = true; menuExpandido = false }, leadingIcon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null) })
                            DropdownMenuItem(text = { Text("Cambiar Contraseña") }, onClick = { mostrarCambiarPass = true; menuExpandido = false }, leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) })
                            DropdownMenuItem(text = { Text(if (modoOscuro) "Modo Claro" else "Modo Oscuro") }, onClick = { onToggleModoOscuro(); menuExpandido = false }, leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null) })
                            DropdownMenuItem(text = { Text("Cerrar Sesión") }, onClick = { menuExpandido = false; onCerrarSesion() }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) })
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.onGloballyPositioned { rectBarraAbajo = it.boundsInRoot() }
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label, style = MaterialTheme.typography.labelSmall, maxLines = 1) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                // NAVEGACIÓN CORREGIDA: Siempre limpia la pila hacia el inicio
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Destination.Inicio.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Destination.Inicio.route) {
                    val inicioVm: InicioViewModel = viewModel(factory = InicioViewModel.factory(insumoRepo, ventaRepo))
                    PantallaInicio(
                        sessionManager = sessionManager,
                        viewModel = inicioVm,
                        ventasVm = ventasVm,
                        // NAVEGACIÓN DE LAS ALERTAS CORREGIDA (Usa la misma lógica que la barra)
                        onIrAInsumos = {
                            navController.navigate(Destination.Insumos.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onIrAHistorial = {
                            navController.navigate(Destination.Historial.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable(Destination.Vender.route) {
                    val vm = ventasVm
                    PantallaVender(vm, sessionManager)
                }
                composable(Destination.Productos.route) { PantallaGestionProductos(productoRepo, insumoRepo, categoriaRepo, sessionManager) }
                composable(Destination.Insumos.route) {
                    val vm: InventarioViewModel = viewModel(factory = InventarioViewModel.factory(insumoRepo))
                    PantallaInventario(vm)
                }
                composable(Destination.Historial.route) {
                    val vm = ventasVm
                    PantallaHistorialVentas(vm)
                }
            }
        }

        if (mostrarTutorial) {
            TutorialOverlay(
                steps = tutorialSteps,
                currentStep = tutorialStep,
                onNext = {
                    if (tutorialStep < tutorialSteps.size - 1) {
                        tutorialStep++
                        tutorialSteps[tutorialStep].actionRoute?.let { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    } else {
                        mostrarTutorial = false
                        sessionManager.setTutorialVisto(true)
                    }
                },
                onSkip = { mostrarTutorial = false; sessionManager.setTutorialVisto(true) }
            )
        }
    }

    // Diálogo de Datos Bancarios
    if (mostrarDatosBancarios) {
        PantallaDatosBancarios(sessionManager = sessionManager, onDismiss = { mostrarDatosBancarios = false })
    }

    // Diálogo de Cambiar Contraseña
    if (mostrarCambiarPass) {
        var passNueva by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { mostrarCambiarPass = false },
            title = { Text("Cambiar Contraseña") },
            text = {
                Column {
                    Text("Usuario: $username", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = passNueva, onValueChange = { passNueva = it; error = null },
                        label = { Text("Nueva contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (passNueva.length < 6) { error = "Mínimo 6 caracteres."; return@TextButton }
                    sessionManager.cambiarPassword(username, passNueva)
                    mostrarCambiarPass = false
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { mostrarCambiarPass = false }) { Text("Cancelar") } }
        )
    }
}