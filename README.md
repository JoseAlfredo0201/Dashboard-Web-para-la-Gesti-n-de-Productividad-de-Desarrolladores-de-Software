# Sprint5Web

Proyecto full stack para visualizar productividad de desarrolladores mediante métricas.

## 1. Vista general

El workspace contiene dos aplicaciones:

1. Backend en Spring Boot (API REST) que consulta métricas desde Firestore.
2. Frontend en React + Vite que consume la API y muestra tarjetas y gráfica de evolución.

Estructura principal:

- `backFinalProgramacionWeb-master/backFinalProgramacionWeb-master/demo`: backend Java.
- `gitFinalFrontProgramacionWeb-master/gitFinalFrontProgramacionWeb-master/productivity-dashboard`: frontend React.

## 2. Arquitectura funcional

Flujo de datos:

1. El frontend solicita al backend el catálogo de métricas (`GET /metrics`).
2. El usuario selecciona una métrica en pantalla.
3. El frontend consulta la serie temporal de esa métrica (`GET /metrics/{metric}`).
4. El backend valida la métrica solicitada, lee documentos de Firestore y devuelve una lista normalizada con `label` y `value`.
5. El frontend calcula total/promedio/máximo y renderiza la línea histórica con Chart.js.

## 3. Backend (Spring Boot)

### 3.1 Stack y dependencias

Archivo clave: `pom.xml`

- `spring-boot-starter-web`: endpoints REST.
- `spring-boot-starter-security`: configuración de seguridad y CORS.
- `firebase-admin`: conexión con Firestore.
- `lombok`: reduce boilerplate en DTOs/controlador.
- `spring-boot-starter-test`: pruebas JUnit/Mockito.

Configuración importante:

- Java: 21.
- Parent: Spring Boot `4.0.6`.

### 3.2 Punto de entrada

Archivo: `src/main/java/com/exampleback/demo/DemoApplication.java`

- Clase principal con `@SpringBootApplication`.
- Arranca el contexto y el servidor embebido con `SpringApplication.run(...)`.

### 3.3 Configuración

#### `config/CorsConfig.java`

- Registra un `CorsConfigurationSource`.
- Permite origen `http://localhost:5173` (frontend local).
- Métodos permitidos: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`.
- Headers permitidos: todos (`*`).
- Credenciales: habilitadas.

#### `config/SecurityConfig.java`

- Define un `SecurityFilterChain`.
- Desactiva CSRF para facilitar consumo REST en entorno local.
- Habilita CORS con la configuración anterior.
- Permite todas las requests (`anyRequest().permitAll()`).

### 3.4 Propiedades de aplicación

Archivo: `src/main/resources/application.properties`

- `spring.application.name=demo`.
- Variables Firebase (`project-id`, ubicación de credenciales y datos web de proyecto).
- `server.error.include-message=always` para propagar mensajes de error útiles.

### 3.5 Capa de dominio/modelo

#### `model/DeveloperMetric.java`

Representa un registro de productividad con:

- `id`
- `developerName`
- `metricDate`
- `commits`
- `bugsFixed`
- `tasksCompleted`
- `storyPoints`

Contiene constructor vacío, constructor completo (sin `id`) y getters/setters.

#### `model/MetricType.java`

Enum que centraliza métricas soportadas y su mapeo:

- `COMMITS` -> `metric.getCommits()`
- `BUGS` -> `metric.getBugsFixed()`
- `TASKS` -> `metric.getTasksCompleted()`
- `STORY_POINTS` -> `metric.getStoryPoints()`

Responsabilidades:

- Exponer `key` y `label` de cada métrica.
- Resolver métrica por clave (`fromKey`).
- Generar catálogo para UI (`definitions`).
- Extraer valor numérico de un `DeveloperMetric`.
- Tratar nulos como `0` (`safeValue`).

### 3.6 DTOs

#### `dto/MetricDefinitionDTO.java`

- `record` con `key` y `label`.
- Se usa para poblar el selector de métricas en frontend.

#### `dto/MetricResponseDTO.java`

- Objeto de respuesta para series.
- Campos: `label` (fecha) y `value` (valor de la métrica).
- Usa `@Data` de Lombok.

#### `dto/MetricRequestDTO.java`

- Clase con campo `metric`.
- Actualmente no participa en el flujo REST expuesto.

### 3.7 Repositorio (acceso a Firestore)

Archivo: `repository/DeveloperMetricRepository.java`

Responsabilidades:

- Inicializar Firebase Admin si no existe instancia (`FirebaseApp.getApps().isEmpty()`).
- Cargar credenciales desde:
  - `classpath:`
  - `file:`
  - ruta directa
- Consultar colección `developer_metrics` ordenada por `metricDate`.
- Transformar cada `QueryDocumentSnapshot` a `DeveloperMetric`.
- Convertir valores numéricos nulos a `0`.
- Ordenar nuevamente por fecha ascendente y retornar lista.

Manejo de errores:

- Cualquier fallo de lectura termina en `IllegalStateException` con mensaje claro.

### 3.8 Servicio de negocio

Archivo: `service/MetricsService.java`

Métodos principales:

1. `getAvailableMetrics()`:
- Devuelve la lista de definiciones desde `MetricType.definitions()`.

2. `getMetricData(String metricKey)`:
- Valida que la clave exista (`MetricType.fromKey`).
- Si no existe, lanza `ResponseStatusException(BAD_REQUEST, "Métrica no soportada: ...")`.
- Consulta registros en repositorio.
- Mapea cada registro a `MetricResponseDTO` con:
  - `label = metricDate`
  - `value = valor extraído por el MetricType`

### 3.9 Controlador REST

Archivo: `controller/MetricsController.java`

Base path: `/metrics`.

Endpoints:

1. `GET /metrics`
- Devuelve catálogo de métricas disponibles (`List<MetricDefinitionDTO>`).

2. `GET /metrics/{metric}`
- Devuelve serie temporal para la métrica solicitada (`List<MetricResponseDTO>`).

### 3.10 Pruebas backend

#### `src/test/java/com/exampleback/demo/DemoApplicationTests.java`

- Smoke test de contexto Spring (`contextLoads`).

#### `src/test/java/com/exampleback/demo/service/MetricsServiceTest.java`

Pruebas unitarias de servicio con Mockito:

- `shouldMapCommitMetricSeries`: valida mapeo correcto de fechas y valores para `commits`.
- `shouldRejectUnknownMetric`: valida rechazo con `400 BAD_REQUEST` para métrica inválida.

## 4. Frontend (React + Vite)

### 4.1 Stack y dependencias

Archivo: `package.json`

Dependencias principales:

- `react`, `react-dom`
- `chart.js`
- `react-chartjs-2`

Scripts:

- `npm run dev`: servidor de desarrollo.
- `npm run build`: build de producción.
- `npm run lint`: linting.
- `npm run preview`: vista de build.

### 4.2 Entrada de la aplicación

#### `src/main.jsx`

- Crea el root de React.
- Renderiza `App` dentro de `StrictMode`.

#### `src/App.jsx`

- Componente raíz minimalista.
- Renderiza únicamente `Dashboard`.

### 4.3 Servicio HTTP

Archivo: `src/services/metricsService.js`

- Define `API_URL` desde `VITE_API_URL` o por defecto `http://localhost:8080/metrics`.
- Función interna `requestJson(url)`:
  - Ejecuta `fetch`.
  - Si falla, intenta leer mensaje del backend y lanza `Error`.
  - Si responde OK, retorna `response.json()`.
- Expone:
  - `getAvailableMetrics()` -> `GET /metrics`
  - `getMetricData(metric)` -> `GET /metrics/{metric}`

### 4.4 Componentes

#### `src/component/Dashboard.jsx`

Componente principal de la UI.

Estado interno:

- `availableMetrics`: catálogo de métricas.
- `selectedMetric`: métrica activa.
- `data`: serie de puntos para gráfica.
- `loading`: estado de carga.
- `error`: mensaje de error.

Efectos:

1. Al montar:
- Carga métricas disponibles.
- Selecciona por defecto la primera o `commits`.

2. Cuando cambia `selectedMetric`:
- Carga serie temporal asociada.

Cálculos derivados:

- `total`: suma de la serie.
- `promedio`: promedio diario con un decimal.
- `maximo`: valor máximo.
- `metricLabel`: etiqueta de la métrica activa.
- `chartColor`: color según clave de métrica.

Renderizado:

- Hero descriptivo.
- Selector de métricas (`MetricSelector`).
- Tarjetas resumen (`MetricCard`).
- Gráfico lineal (`Line` de `react-chartjs-2`).
- Estados de error/carga.

#### `src/component/MetricCard.jsx`

- Componente presentacional.
- Muestra `title`, `value` y `description`.

#### `src/component/MetricSelector.jsx`

- Renderiza botones por cada métrica disponible.
- Aplica estilo activo según selección.
- Al click, cambia métrica vía callback `onChange`.

### 4.5 Estilos

#### `src/index.css`

Define el sistema visual principal:

- Tipografía (`Manrope` desde Google Fonts).
- Fondos con gradientes/radiales.
- Layout responsivo del dashboard.
- Estilos para paneles, tarjetas, selector, badge y chart.
- Media query para móvil/tablet.

#### `src/App.css`

- Contiene estilos heredados de plantilla Vite (bloques `.counter`, `.hero`, etc.).
- No es usado por `App.jsx` actual.

## 5. Contrato API resumido

### `GET /metrics`

Respuesta de ejemplo:

```json
[
  { "key": "commits", "label": "Commits" },
  { "key": "bugs", "label": "Incidencias resueltas" },
  { "key": "tasks", "label": "Tareas completadas" },
  { "key": "storyPoints", "label": "Story points" }
]
```

### `GET /metrics/{metric}`

`metric` soportadas: `commits`, `bugs`, `tasks`, `storyPoints`.

Respuesta de ejemplo:

```json
[
  { "label": "2026-05-01", "value": 4 },
  { "label": "2026-05-02", "value": 7 }
]
```

Error esperado para clave inválida:

- HTTP `400 BAD_REQUEST`
- Mensaje: `Métrica no soportada: <clave>`

## 6. Ejecución local

### Backend

```bash
cd backFinalProgramacionWeb-master/backFinalProgramacionWeb-master/demo
./mvnw spring-boot:run
```

En Windows PowerShell también funciona:

```powershell
.\mvnw.cmd spring-boot:run
```

### Frontend

```bash
cd gitFinalFrontProgramacionWeb-master/gitFinalFrontProgramacionWeb-master/productivity-dashboard
npm install
npm run dev
```

El frontend queda en `http://localhost:5173` y consume backend en `http://localhost:8080` por defecto.

## 7. Pruebas

### Backend

```bash
cd backFinalProgramacionWeb-master/backFinalProgramacionWeb-master/demo
./mvnw test
```

### Frontend

No hay suite de tests automatizados configurada actualmente.

Validaciones disponibles:

```bash
cd gitFinalFrontProgramacionWeb-master/gitFinalFrontProgramacionWeb-master/productivity-dashboard
npm run lint
npm run build
```

## 8. Observaciones técnicas

- El backend está preparado para entorno local con credenciales Firebase leídas desde archivo.
- El CORS está abierto explícitamente para el origen del frontend de Vite.
- `MetricRequestDTO` existe pero no se usa en endpoints actuales.
- `App.css` conserva estilos de plantilla y no participa en el render final.
