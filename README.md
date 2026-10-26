# AcuiMuestra

Aplicación Android para **registrar, revisar y dar trazabilidad a muestras de choritos** (mejillones) tomadas en terreno.
Proyecto académico de la asignatura **DSY1105 Desarrollo de Aplicaciones Móviles** (Duoc UC, sede Puerto Montt), basado en el caso de **ALDEMAR SpA**.

> ⚠️ MVP académico. Todos los datos (centros, trenes, líneas, usuarios y muestras) son **ficticios**. No es un sistema oficial de ALDEMAR SpA.

## Integrantes

| Integrante | GitHub | Aportes principales |
|---|---|---|
| Valentina Santana Guajardo | [@valeFSG](https://github.com/valeFSG) | Base del proyecto, capa de datos (Room), login y roles, historial con búsqueda y filtros, detalle y revisión del supervisor, registro de muestras en 4 pasos, exportación CSV, diseño de pantallas |
| Nicol González Cobi | [@Nicol416](https://github.com/Nicol416) | Paleta y tema Material 3, imágenes del inicio, selector de fecha y hora, selector de fotografía (cámara y galería), logo e ícono de la app |

## Funcionalidades

| Requerimiento | Funcionalidad |
|---|---|
| RF01 | Login con usuarios ficticios y opciones distintas según rol (operador / supervisor) |
| RF02 | Registro de muestra: centro, tren y línea en cascada, fecha y hora, tramo muestreado |
| RF03 | Conteo manual de individuos y observaciones |
| RF04 | Fotografía de la muestra con cámara o galería |
| RF05 | Validaciones: campos obligatorios, números válidos, rangos máximos y fecha no futura |
| RF06 | Datos opcionales: calibre promedio y peso |
| RF07 | Historial de muestras y detalle con foto y métricas |
| RF08 | Búsqueda por texto y filtros por centro y estado de revisión |
| RF09 | Revisión del supervisor (observar, validar o descartar) y corrección por el operador (conteo, tramo, calibre, peso, observaciones y foto) |
| RF10 | Exportación a CSV con filtros por centro, estado y período, selección de muestras y compartir el resumen de una muestra |

### Estados de revisión
`Pendiente` → `Observado` → `Corregido` → `Validado` o `Descartado`

Las muestras validadas o descartadas quedan cerradas. Las muestras **no se eliminan**: se descartan con un comentario, para mantener la trazabilidad.

## Usuarios de prueba

| Usuario | Contraseña | Rol |
|---|---|---|
| `operador` | `1234` | Operador de muestreo |
| `supervisor` | `1234` | Supervisor técnico |

## Tecnologías

- **Kotlin** + **Jetpack Compose** + **Material 3**
- Arquitectura **MVVM** (Model, View, ViewModel, Repository)
- **StateFlow** para el estado de las pantallas
- **Room (SQLite)** para la persistencia local
- **Navigation Compose**, con un subgrafo que comparte el ViewModel del registro en 4 pasos
- Recursos nativos: **DatePicker / TimePicker**, **cámara** (`TakePicture`), **galería** (`PickVisualMedia`), **FileProvider** y **menú Compartir** de Android

## Estructura del proyecto

```
app/src/main/java/com/acuimuestra/app
├── data
│   ├── local        # Base de datos Room, DAOs y datos de prueba
│   ├── model        # Entidades y modelos (Muestra, Centro, Tren, Linea, Usuario...)
│   └── repository   # Repositorios (acceso a datos para los ViewModel)
├── navigation       # Rutas y grafo de navegación
├── ui
│   ├── components   # Selector de fecha/hora y selector de foto
│   ├── screens      # Pantallas de la app
│   └── theme        # Colores, tema y colores por estado
├── viewmodel        # ViewModels con la lógica y las validaciones
├── AcuiMuestraApp.kt  # Contenedor de dependencias
└── MainActivity.kt
```

### Base de datos (`acuimuestra.db`)

| Tabla | Contenido |
|---|---|
| `usuarios` | Usuarios ficticios y su rol |
| `centros` / `trenes` / `lineas` | Jerarquía de ubicación de cultivo |
| `muestras` | Datos de cada muestra, ruta de la foto, estado y comentario del supervisor |

Las fotos se guardan como archivo en la carpeta interna de la app; la base guarda solo su ruta.

## Cómo ejecutar

1. Clonar el repositorio:
   ```
   git clone https://github.com/valeFSG/AcuiMuestra-App.git
   ```
2. Abrir la carpeta en **Android Studio**.
3. Si la sincronización falla por la versión de Java: **Settings → Build Tools → Gradle → Gradle JDK = 21**.
4. En redes con proxy (por ejemplo, el laboratorio): **Settings → HTTP Proxy → Auto-detect proxy settings**.
5. Ejecutar con ▶ en un emulador o dispositivo con **Android 7.0 (API 24)** o superior.

Versiones: AGP 8.12, Gradle 8.13, Kotlin 2.2, Java 21.

## Flujo de trabajo en equipo

- `main` contiene siempre la versión estable.
- Cada funcionalidad se desarrolla en una rama `feature/...`.
- Cada rama se integra con un **Pull Request** revisado

icación

Las tareas del equipo están organizadas en Trello, con quién hizo cada una y su Pull Request asociado:
[Tablero AcuiMuestra – DSY1105](https://trello.com/b/wBnok8gn/acuimuestra-dsy1105)

## Créditos de imágenes

Fotografías de las tarjetas del inicio, obtenidas de [Unsplash](https://unsplash.com) (licencia libre):

- `inicio_nueva_muestra.jpg`: foto de Romain Water
- `inicio_historial.jpg`: foto de Torkil Torgard
- `inicio_revisar.jpg`: foto de Julia Taubitz

Logo e ícono de AcuiMuestra elaborados para el proyecto.