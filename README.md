# AcuiMuestra

Aplicación Android para **registrar, revisar y dar trazabilidad a muestras de choritos** (mejillones) tomadas en terreno, con backend propio, sincronización offline y reportes.
Proyecto académico de la asignatura **DSY1105 Desarrollo de Aplicaciones Móviles** (Duoc UC, sede Puerto Montt), basado en el caso de **ALDEMAR SpA**.

> MVP académico. Todos los datos (centros, trenes, líneas, usuarios y muestras) son **ficticios**. No es un sistema oficial de ALDEMAR SpA.

**Versión:** 2.0 (EP4)

**Backend en la nube:** https://acuimuestra-app.onrender.com/swagger-ui.html

## Integrantes

| Integrante | GitHub | Aportes principales |
|---|---|---|
| Valentina Santana Guajardo | [@valeFSG](https://github.com/valeFSG) | Base del proyecto, Room, login y roles, historial y filtros, revisión del supervisor, registro en 4 pasos, exportación CSV, **backend Spring Boot + Swagger**, **API Retrofit y sincronización offline (WorkManager)**, **despliegue en Render + Neon**, **sincronización de fotos**, **trazabilidad (nombres y revisado por)**, **análisis con IA**, **historial compacto**, **CameraX y conteo asistido**, **densidad**, **pruebas unitarias y de UI (Kover)**, **modo claro/oscuro** |
| Nicol González Cobi | [@Nicol416](https://github.com/Nicol416) | Paleta y tema Material 3, imágenes del inicio, selector de foto, hora de red (NTP), logo e ícono, **API externa Open-Meteo Marine**, **dashboard**, **exportación PDF**, **rol analista**, **íconos Material**, **APK firmada**, **login por perfiles** |

## Funcionalidades

### App móvil

| Funcionalidad | Descripción |
|---|---|
| Login por perfiles | Tarjetas "¿Quién eres?" con cada persona y su rol; solo se escribe la contraseña |
| Roles | Operador, supervisor y analista. Cada uno ve opciones distintas |
| Trazabilidad | Cada muestra registra quién la tomó y quién la revisó ("Revisado por") |
| Registro de muestra | 4 pasos: ubicación (centro, tren y línea en cascada) → fotografía (obligatoria) → conteo → resumen |
| Fecha y hora de red | Se obtiene por NTP al guardar (no editable). Sin conexión se usa la del dispositivo y queda **no verificada** |
| CameraX | Cámara integrada en la app con vista previa y captura; también se puede elegir desde la galería |
| Conteo asistido | Sobre la foto (con zoom de Telephoto) se toca cada individuo para marcarlo; se puede deshacer, limpiar y usar el total como conteo |
| Densidad | Se calcula como `conteo × 100 / tramo (cm)` en **individuos por metro** (ej.: 185 en 30 cm = 617 ind/m) |
| Datos opcionales | Calibre promedio de un individuo (mm) y peso total de la muestra (g) |
| Historial | Búsqueda por texto, filtros por centro y estado, marca de pendientes. El encabezado se desplaza con la lista y muestra el estado de sincronización |
| Análisis con IA | Segunda opinión sobre la foto: individuos estimados, presencia de cholgas y calidad de la imagen, comparado con el conteo del operador |
| Revisión | El supervisor valida, observa o descarta (con comentario); el operador corrige las observadas |
| Dashboard | Panel con totales, individuos y distribución por estado y por centro |
| Condiciones del mar | Altura de ola y temperatura del mar en tiempo real desde **Open-Meteo Marine** |
| Exportación | **CSV** y **PDF** con filtros por centro, estado y período; incluye tramo y densidad |
| Modo sin conexión | Las muestras se guardan localmente y se envían solas al volver la conexión |
| Sincronización de fotos | La foto se sube comprimida (≈60 KB) y los demás dispositivos la descargan |
| Modo claro / oscuro | Botón en el Inicio: automático, claro u oscuro. La preferencia queda guardada |

### Estados de revisión
`Pendiente` → `Observado` → `Corregido` → `Validado` o `Descartado`

Las muestras validadas o descartadas quedan cerradas. Las muestras no se eliminan desde la app: se descartan con un comentario, para mantener la trazabilidad.

### Roles

| Rol | Puede |
|---|---|
| Operador | Registrar muestras y corregir las observadas |
| Supervisor | Validar, observar o descartar muestras |
| Analista | Consultar historial, ver el panel y exportar reportes (solo lectura) |

## Usuarios de prueba

| Persona (ficticia) | Usuario | Contraseña | Rol |
|---|---|---|---|
| Camila Rojas | `operador` | `1234` | Operadora de muestreo |
| Diego Muñoz | `operador2` | `1234` | Operador de muestreo |
| Andrea Soto | `supervisor` | `1234` | Supervisora técnica |
| Felipe Vargas | `analista` | `1234` | Analista de datos |

## Arquitectura

```
Android (Compose + MVVM)                     Backend (Spring Boot)
Screen → ViewModel → Repository ─┬─ Room     Controller → Service → JPA → H2
                                 └─ Retrofit ──── REST /api/... ────┘
                     WorkManager (sincronización)
                     Retrofit → Open-Meteo Marine (API externa)
```

- **Local-first:** Room es la fuente de verdad. Cada muestra tiene un `uuid` y una marca `sincronizada`.
- **Sincronización:** `SincronizacionWorker` corre con restricción de red (`CONNECTED`) y reintentos. Primero envía lo pendiente (POST idempotente por `uuid`, luego PATCH de estado o PUT de corrección) y sube la foto; después recibe los cambios del servidor sin pisar lo local no enviado, y descarga las fotos que el dispositivo no tiene.

## Análisis con IA

En el Detalle de una muestra con foto, la supervisora o el analista toca **Analizar con IA**. El backend envía la foto guardada a un modelo de visión y devuelve:

- individuos estimados,
- si se ven cholgas (especie que ALDEMAR no cultiva),
- calidad de la foto (buena, regular o mala),
- un comentario breve.

Lo compara con el conteo del operador y sugiere **revisar** si la diferencia supera el 20 %, si hay cholgas o si la foto es mala. La IA es una **segunda opinión**: no reemplaza el conteo (humano en el ciclo).

**Proveedores con respaldo automático:** primero **Gemini** (máximo 10 segundos; prueba varios modelos configurables con `GEMINI_MODEL`) y, si no responde, **Groq** con un modelo abierto de visión (`GROQ_MODEL`, por defecto `qwen/qwen3.8-27b`). Las claves (`GEMINI_API_KEY`, `GROQ_API_KEY`) solo existen como variables de entorno del servidor. Se usan los niveles gratuitos, por lo que los datos enviados son ficticios.

**Hoja de ruta:** con un protocolo de foto estándar (1 kg de choritos separados sobre bandeja clara), las fotos marcadas con el conteo asistido sirven como datos etiquetados para entrenar un modelo propio que cuente y distinga chorito de cholga.

## Tecnologías

**App:** Kotlin, Jetpack Compose, Material 3, MVVM, StateFlow y corrutinas, Room, Navigation Compose, Retrofit + Gson + OkHttp, WorkManager, CameraX, Coil, Telephoto (zoom), DataStore, NTP.

**Backend:** Java 21, Spring Boot 4, RestClient (Gemini y Groq), Spring Web, Spring Data JPA, Bean Validation, H2 (local) / PostgreSQL en Neon (nube), springdoc-openapi (Swagger), Docker, Render.

**Pruebas:** JUnit 4, MockK, kotlinx-coroutines-test, Compose UI Test, Kover.

## Backend (API REST)

Los permisos se controlan con el encabezado **`X-Usuario`** (`operador`, `supervisor` o `analista`). Un rol sin permiso recibe **403**.

| Método | Ruta | Rol | Descripción |
|---|---|---|---|
| GET | `/api/muestras` | Todos | Lista muestras (filtros opcionales `centro` y `estado`) |
| GET | `/api/muestras/{id}` | Todos | Detalle de una muestra |
| POST | `/api/muestras` | Operador | Crea una muestra (idempotente por id) |
| PUT | `/api/muestras/{id}` | Operador | Corrige una muestra observada |
| PATCH | `/api/muestras/{id}/estado` | Supervisor | Valida, observa o descarta (comentario de 10+ caracteres al observar o descartar) |
| PUT | `/api/muestras/{id}/foto` | Operador | Sube o reemplaza la foto (JPEG, hasta 3 MB) |
| GET | `/api/muestras/{id}/foto` | Todos | Descarga la foto |
| DELETE | `/api/muestras/{id}` | Supervisor | Elimina una muestra y su foto |
| POST | `/api/muestras/{id}/analisis-ia` | Supervisor, analista | Analiza la foto con IA y la compara con el conteo |
| GET | `/api/reportes/resumen` | Todos | Totales, por estado, por centro y densidad promedio por centro |
| GET | `/api/usuarios` | — | Usuarios y roles |

Documentación interactiva: **http://localhost:8080/swagger-ui.html**

## Cómo ejecutar

### 1. Backend

Requiere Java 21 (sirve el que trae Android Studio). En PowerShell, desde la carpeta `backend`:

```
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\mvnw.cmd spring-boot:run
```

Queda en `http://localhost:8080` con usuarios y muestras de ejemplo. Los datos se guardan en `backend/data/` (no se sube al repositorio).

### Backend en la nube

El backend también está desplegado en **Render** (contenedor Docker definido en `backend/Dockerfile`) con base de datos **PostgreSQL en Neon**:

- API: `https://acuimuestra-app.onrender.com/`
- Swagger: `https://acuimuestra-app.onrender.com/swagger-ui.html`

Se configura con variables de entorno (`PORT`, `DB_URL`, `DB_USER`, `DB_PASSWORD`, `API_KEY`, `GEMINI_API_KEY`, `GROQ_API_KEY`). En la nube, todas las rutas `/api/` exigen el encabezado **`X-Api-Key`** (en Swagger, botón **Authorize**). La clave no está en el repositorio.

> Render gratuito se suspende tras 15 minutos sin uso; la primera petición puede tardar cerca de un minuto.

### 2. App

1. Clonar el repositorio:
   ```
   git clone https://github.com/valeFSG/AcuiMuestra-App.git
   ```
2. Abrir la carpeta en **Android Studio** y sincronizar Gradle (Gradle JDK = 21).
3. Ejecutar con ▶ en un emulador o dispositivo con **Android 7.0 (API 24)** o superior.

En el emulador la app se conecta sola al backend en `http://10.0.2.2:8080/`. En un celular físico, agregar en `local.properties` la IP del PC:

```
api.url=http://192.168.X.X:8080/
```

La **APK release** apunta al backend en la nube. Se configura en `local.properties` (no se sube al repositorio):

```
api.url.release=https://acuimuestra-app.onrender.com/
api.key=...
```

La versión debug (▶ Run en el emulador) sigue usando el backend local.

La app también funciona sin backend: guarda todo localmente y lo marca como pendiente.

> Si se instaló una versión anterior, desinstalarla antes para que la base de datos se cree de nuevo.

Versiones: AGP 8.12, Gradle 8.13, Kotlin 2.2, compileSdk 36, minSdk 24.

## Pruebas

| Comando | Qué hace |
|---|---|
| `.\gradlew testDebugUnitTest` | Pruebas unitarias de ViewModels y repositorios (JUnit + MockK) |
| `.\gradlew koverHtmlReportDebug` | Reporte de cobertura en `app/build/reports/kover/htmlDebug/index.html` |
| `.\gradlew koverVerifyDebug` | Falla si la cobertura baja de **90 %** |
| `.\gradlew connectedDebugAndroidTest` | Prueba de UI con Compose (pantalla de login), requiere emulador abierto |

Cobertura de líneas sobre ViewModels y repositorios: **sobre 98 %**.

## APK firmada

La APK de release se firma con un keystore propio. **El archivo `.jks` y sus contraseñas no están en el repositorio**: se leen desde `local.properties`:

```
release.storeFile=C:/ruta/al/keystore.jks
release.storePassword=...
release.keyAlias=...
release.keyPassword=...
```

Generar: **Build → Generate Signed App Bundle / APK → APK → release**.

## Estructura del proyecto

```
app/src/main/java/com/acuimuestra/app
├── data
│   ├── local        # Room: base de datos, DAOs y datos de prueba
│   ├── model        # Entidades y modelos
│   ├── remote       # Retrofit (backend y Open-Meteo), hora de red, monitor de conexión
│   └── repository   # Repositorios: muestras, sincronización, marina, preferencias...
├── navigation       # Rutas y grafo de navegación
├── sync             # WorkManager: worker y sincronizador
├── ui
│   ├── components   # CameraX, conteo asistido, selector de foto, botón de tema
│   ├── screens      # Pantallas (login, inicio, registro, historial, detalle, panel, exportar)
│   └── theme        # Colores, tema claro/oscuro y colores por estado
├── viewmodel        # ViewModels con la lógica y validaciones
├── AcuiMuestraApp.kt  # Contenedor de dependencias
└── MainActivity.kt

backend/src/main/java/com/acuimuestra/backend
├── config       # Swagger y datos iniciales
├── dto          # Peticiones y respuestas
├── model        # Entidades JPA
├── repository   # Spring Data JPA
├── service      # Reglas de negocio y control de acceso por rol
└── web          # Controladores REST y manejo de errores
```

## Flujo de trabajo en equipo

- `main` contiene siempre la versión estable.
- Cada funcionalidad se desarrolla en una rama `feature/...` y se integra con un **Pull Request**.
- Planificación en Trello: [Tablero AcuiMuestra – DSY1105](https://trello.com/b/wBnok8gn/acuimuestra-dsy1105)

## Créditos

- API marina: [Open-Meteo](https://open-meteo.com) (licencia CC BY 4.0).
- Fotografías del inicio, de [Unsplash](https://unsplash.com) (licencia libre):
  - `inicio_nueva_muestra.jpg`: foto de Romain Water
  - `inicio_historial.jpg`: foto de Torkil Torgard
  - `inicio_revisar.jpg`: foto de Julia Taubitz
- Logo e ícono de AcuiMuestra elaborados para el proyecto.
