# OYL Frutos Secos - Aplicación Móvil Android

**Asignatura:** DSY1105 - Desarrollo de Aplicaciones Móviles  
**Evaluación:** Evaluación Parcial 2 (Encargo)  
**Institución:** Duoc UC - Escuela de Informática y Telecomunicaciones  
**Arquitectura:** MVVM (Model - View - ViewModel) + Flujo Unidireccional de Datos (UDF)  
**Tecnologías:** Kotlin, Jetpack Compose, Material Design 3, SQLite (Persistencia Local), OpenStreetMap (osmdroid), ML Kit Code Scanner, Notificaciones Nativas Android.

---

## 📱 Descripción del Proyecto

**OYL Frutos Secos** es una solución móvil nativa diseñada para resolver la problemática de desincronización de existencias, sobreventa y atención manual no estructurada entre la tienda física y los canales de venta digital (WhatsApp Business y Mercado Libre).

La aplicación cuenta con dos perfiles de usuario completamente operativos:
1. **Perfil Cliente:** Exploración de catálogo categorizado (Frutos secos, Semillas, Deshidratados), buscador en vivo, detalle de productos, carrito de compras independiente persistido localmente, confirmación de pedidos con descuento de existencias transaccional, visualización de historial de pedidos, escáner QR de empaques y mapa interactivo con la tienda central y puntos de retiro.
2. **Perfil Administrador:** Dashboard operativo con indicadores de gestión (pedidos pendientes, alertas de stock bajo y total de catálogo), formulario para dar de alta o editar productos integrando la **cámara del celular**, control de inventario con registro de entradas y salidas de stock, y actualización del estado de las solicitudes recibidas.

---

## 📋 Cumplimiento de Rúbrica e Indicadores de Logro (Evaluación Parcial 2)

| Indicador | Criterio Evaluado | Implementación en el Proyecto |
| :--- | :--- | :--- |
| **IE 2.1.1** | Interfaz visual estructurada y navegación fluida (15%) | Diseño con Material 3, `Scaffold`, `TopAppBar`, barra inferior adaptable (`NavigationBar`), diseño adaptable para pantallas compactas y flujo entre las 15 pantallas. |
| **IE 2.1.2** | Formularios completos y validaciones visuales por campo (15%) | `OutlinedTextField` con `leadingIcon`, `trailingIcon`, `isError`, y `supportingText` con mensajes dinámicos de error en Registro, Login, Producto y Perfil. |
| **IE 2.2.1** | Lógica de validación desacoplada y gestión de estado (10%) | Validadores centralizados en `Validadores.kt`, patrón Backing Property (`MutableStateFlow` privado y `StateFlow` público) en `ViewModel`, inmutabilidad con `.copy()`. |
| **IE 2.2.2** | Animaciones visuales funcionales (10%) | Transiciones de entrada (`AnimatedVisibility`, fade/slide) en Bienvenida, animación de éxito con rebote y escala en Confirmación de Pedido, indicadores Loader (`CircularProgressIndicator`) en botones y badges reactivos. |
| **IE 2.3.1** | Estructura modular y persistencia local de datos (15%) | Organización en paquetes `data`, `domain`, `navigation`, `ui` y `utils`. Base de datos SQLite (`OylDbHelper.kt`) para usuarios, catálogo, carrito aislado y pedidos. |
| **IE 2.4.1** | Acceso a al menos dos recursos nativos (15%) | **1. Cámara y Galería:** Captura de fotos para productos y avatar mediante `ActivityResultContracts.TakePicture()` y FileProvider.<br>**2. Escáner QR:** Reconocimiento de códigos de barras/QR mediante Google Play Services Code Scanner.<br>**3. Mapas y GPS:** Mapa interactivo con `osmdroid` y geolocalización GPS.<br>**4. Notificaciones:** Canal nativo y alertas con `NotificationCompat`. |

---

## 🗺️ Mapa de las 15 Pantallas Implementadas

1. **Pantalla de Bienvenida / Inicio (`BienvenidaScreen.kt`):** Presentación de marca OYL con animación de entrada y accesos a catálogo, mapa y login.
2. **Pantalla de Registro de Cliente (`RegistroScreen.kt`):** Formulario con validación visual por campo para nombre, alias, correo, teléfono, dirección y contraseña.
3. **Pantalla de Inicio de Sesión (`LoginScreen.kt`):** Selector de roles (Cliente / Administrador) y precarga de credenciales de demostración.
4. **Pantalla Principal / Dashboard (`DashboardScreen.kt`):** Vista personalizada según rol. Tarjetas métricas y alertas de stock bajo para admin; banner y destacados para cliente.
5. **Pantalla de Mapa de Tiendas y Retiro (`MapaTiendasScreen.kt`):** Mapa interactivo OpenStreetMap con marcadores de tienda central, puntos de retiro, datos de contacto y botón de centrado GPS.
6. **Pantalla de Catálogo de Productos (`CatalogoScreen.kt`):** Catálogo organizado en cuadrícula con chips de categorías, buscador en vivo y botón rápido para agregar al carrito.
7. **Pantalla de Detalle del Producto (`DetalleProductoScreen.kt`):** Fotografía ampliada, ficha técnica, badge de stock, selector de cantidades (+/-) y botón de compra.
8. **Pantalla de Escáner QR de Productos (`EscanearQrScreen.kt`):** Lector nativo QR de Google con cámara y simulador integrado para pruebas en emuladores.
9. **Pantalla de Carrito de Compras (`CarritoScreen.kt`):** Gestión de cantidades, selección de modalidad (Retiro o Despacho a domicilio), cálculo de totales y persistencia por cliente.
10. **Pantalla de Confirmación del Pedido (`ConfirmacionPedidoScreen.kt`):** Animación de éxito, número de solicitud generado transaccionalmente y desglose de datos.
11. **Pantalla de Gestión de Productos (`GestionProductosScreen.kt`):** Formulario administrativo con captura de foto mediante la cámara del móvil.
12. **Pantalla de Gestión de Inventario (`GestionInventarioScreen.kt`):** Consulta de existencias, filtro de stock bajo (<= 10 unidades) y botones rápidos de Entrada (+) y Salida (-).
13. **Pantalla de Historial y Gestión de Pedidos (`HistorialPedidosScreen.kt`):** Consulta filtrada de solicitudes y selector de actualización de estado para el administrador.
14. **Pantalla de Notificaciones (`NotificacionesScreen.kt`):** Centro de avisos sobre nuevos pedidos, stock bajo y promociones, con soporte de permisos de Android 13+.
15. **Pantalla de Perfil y Configuración (`PerfilScreen.kt`):** Edición de datos personales, cambio de foto con la cámara, interruptores de permisos nativos y cierre de sesión.

---

## 🔑 Credenciales de Demostración para Evaluación

* **Perfil Administrador:**
  * **Usuario / Correo:** `admin` o `admin@admin.cl`
  * **Contraseña:** `admin`
  * *Datos precargados:* Administrador OYL, +56 9 1234 1234, Santiago.
* **Perfil Cliente:**
  * **Usuario / Correo:** `ana` o `ana@correo.cl`
  * **Contraseña:** `1234`
  * *Datos precargados:* Ana Torres, +56 9 1234 5678, Santiago, RM.

*(También es posible registrar nuevos clientes mediante la pantalla de registro, cuyos datos y carritos quedarán separados de forma autónoma en la base de datos).*

---

## 🚀 Instrucciones para Abrir y Ejecutar el Proyecto

### 1. Abrir en Android Studio
1. Inicia **Android Studio**.
2. Selecciona **File > Open...** (o *Open Project*).
3. Navega y selecciona la carpeta:  
   `D:\Descargas\Proyecto AppsMoviles\OYLFrutosSecos`
4. Espera a que Gradle sincronice las dependencias del proyecto.

> [!NOTE]
> Si tu unidad `C:` tiene poco espacio libre en disco, puedes configurar la variable de entorno de Gradle en tu equipo o en la configuración de Android Studio apuntando a `D:\.gradle` para asegurar una compilación sin interrupciones.

### 2. Ejecutar en tu Celular Físico
1. En tu teléfono Android, activa las **Opciones de desarrollador** y la **Depuración por USB**.
2. Conecta el teléfono al computador mediante cable USB y acepta el mensaje de autorización.
3. En la barra superior de Android Studio, selecciona tu dispositivo físico en la lista desplegable de dispositivos.
4. Presiona el botón verde **Run 'app'** (o `Shift + F10`).
5. La aplicación se instalará directamente en tu celular para la demostración en vivo.

---

## 🗂️ Estructura del Código Fuente

```
OYLFrutosSecos/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/cl/oylfrutossecos/app/
│   │   │   ├── data/
│   │   │   │   ├── local/OylDbHelper.kt          # Base de datos SQLite
│   │   │   │   └── repository/OylRepository.kt   # Repositorio de datos
│   │   │   ├── domain/model/Models.kt            # Entidades y modelos de formulario
│   │   │   ├── navigation/AppDestinations.kt     # Rutas y barra de navegación
│   │   │   ├── ui/
│   │   │   │   ├── components/ComponentesComunes.kt # Inputs, Badges, Botones
│   │   │   │   ├── screens/                      # Las 15 pantallas requeridas
│   │   │   │   ├── theme/                        # Paleta, Tipografía y Tema M3
│   │   │   │   ├── viewmodels/                   # ViewModels MVVM
│   │   │   │   └── OYLFrutosSecosApp.kt          # Coordinador principal
│   │   │   ├── utils/
│   │   │   │   ├── NotificacionHelper.kt         # Canal y envío de notificaciones
│   │   │   │   └── Validadores.kt                # Reglas lógicas de validación
│   │   │   └── MainActivity.kt                   # Punto de entrada de la aplicación
│   │   └── res/
│   │       ├── values/
│   │       └── xml/file_paths.xml                # Rutas para FileProvider de Cámara
│   └── build.gradle.kts
├── gradle/libs.versions.toml
├── build.gradle.kts
└── settings.gradle.kts
```
