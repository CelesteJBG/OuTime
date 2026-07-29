# MEMORY BANK — OuTime (TFC DAM Dual)

---

## Descripción del proyecto

- **OuTime** es una aplicación Android desarrollada en **Kotlin + Jetpack Compose** siguiendo **Clean Architecture** y patrón **MVVM**.
- El proyecto forma parte del **Trabajo Final de Curso (TFC) de 2º DAM Dual**.
- El objetivo **NO** es desarrollar una aplicación comercial completa, sino una **demo funcional** que represente una plataforma de gestión de citas entre clientes y negocios.
- La prioridad es una **experiencia fluida durante la demostración del TFC**.

---

## Arquitectura

Se mantiene estrictamente la arquitectura:

```
presentation/
    screens/
    navigation/
    components/
    viewmodel/

domain/
    model/
    repository/

data/
    repository/

Firebase
```

### Patrones utilizados

- MVVM
- Repository Pattern
- StateFlow
- Clean Architecture
- Firebase Authentication
- Cloud Firestore

> ⚠️ **NO utilizar Hilt.**  
> La inyección de dependencias continúa siendo **manual** mediante `ViewModelFactory`.

---

## Filosofía del proyecto

- El proyecto debe avanzar mediante **pequeños sprints**.
- Cada sprint debe:
  - ✅ compilar
  - ✅ funcionar
  - ✅ terminar con un commit
- **Nunca** implementar varios módulos grandes en un mismo commit.

---

## Flujo definitivo de la aplicación

### 1. Splash

```
Splash
  └── Comprueba: sesión iniciada / usuario Firestore / rol
        └── Si no hay sesión → Login
```

### 2. Login

- Permite iniciar sesión.
- Debe incluir acceso a **Crear cuenta**.

### 3. Registro

- El usuario elige **UNA SOLA VEZ**:
  - ○ Cliente
  - ○ Negocio
- Ese rol se guarda en Firestore.
- **Nunca vuelve a preguntarse.**

---

## Flujo BUSINESS

```
Splash
  └── Role == BUSINESS
        └── Buscar Business asociado al ownerId
              ├── Si NO existe → CreateBusinessScreen
              │     └── Crear: nombre / descripción / categoría
              │           └── BusinessHome
              └── Si existe → BusinessHome (directamente)
```

### Business Home

Debe mostrar:

- **Datos del negocio:** Nombre, Descripción, Categoría
- **Botones:** Crear servicio / Gestionar disponibilidad / Mis citas / Cerrar sesión
- **Lista:** Servicios publicados

### Gestión de Servicios

```
Business Home → Crear servicio
  └── Formulario: nombre / descripción / duración / precio
        └── Guardar → Firestore → Actualizar lista
```

### Gestión de disponibilidad (Business)

```
Business Home → Gestionar disponibilidad
  └── Horario semanal: activar/desactivar días
        └── Configurar turnos (mañana y/o tarde)
  └── Fechas bloqueadas: añadir / eliminar
  └── Guardar → Firestore (business_schedules + blocked_dates)
```

### Gestión de citas (Business)

```
Business Home → Mis citas → BusinessAppointmentsScreen
  └── Lista de citas agrupadas por fecha (Hoy / Mañana / fecha)
  └── Filtros: Todas / Confirmadas / Completadas / Canceladas
  └── Acciones sobre citas Confirmadas:
        ├── Marcar como Completada
        └── Cancelar
  └── Completadas y Canceladas: solo lectura
```

> **Nota:** No existe aprobación manual. Las citas se crean directamente como `CONFIRMED`.

---

## Flujo CLIENT

```
Splash
  └── Role == CLIENT
        └── Client Home
```

### Client Home *(objetivo final)*

- Debe **parecer una aplicación comercial**.
- **NO** debe mostrar pantallas técnicas.
- Debe incluir:
  - **Carrusel superior** (Promociones / Destacados / Nuevos negocios)
  - **Categorías** (Peluquería, Barbería, Estética, Masajes, Uñas, Dentistas…)
  - **Buscador**
  - **Lista de negocios** — cada Card mostrará: Nombre / Categoría / Descripción corta

### Detalle del negocio

```
Lista de negocios → BusinessDetailScreen
  └── Muestra: Nombre / Descripción / Categoría / Lista de servicios
```

### Reserva de cita (flujo basado en disponibilidad)

```
Cliente
  └── Selecciona servicio (con duración)
        └── Calendario mensual (días no disponibles en gris)
              └── Selecciona día válido
                    └── Cuadrícula de franjas 🟢/🔴 (generadas automáticamente)
                          └── Pulsa franja verde → Cita CONFIRMED
                                └── La franja deja de estar disponible
```

**Generación automática de franjas:**
- Horario del negocio (1-2 turnos/día)
- Duración del servicio seleccionado
- Citas ya existentes (marcadas como ocupadas)
- Fechas bloqueadas (día no disponible)

### Gestión de citas del cliente

```
Cliente → Mis citas → Confirmada / Cancelada / Completada → Firestore
```

---

## Firestore

### Colecciones actuales

| Colección            | Estado     | Descripción |
|----------------------|------------|-------------|
| `users`              | ✅ activa  | Usuarios con rol |
| `businesses`         | ✅ activa  | Negocios |
| `services`           | ✅ activa  | Servicios (con `durationMinutes`) |
| `appointments`       | ✅ activa  | Citas (estado `CONFIRMED` por defecto) |
| `business_schedules` | ✅ activa  | Horario semanal del negocio (1-2 turnos/día) |
| `blocked_dates`      | ✅ activa  | Fechas bloqueadas por el negocio |

---

## Estado actual del proyecto

### Completado

- ✅ Firebase
- ✅ Login
- ✅ Registro
- ✅ Persistencia de sesión
- ✅ Navegación por roles
- ✅ Business Management
- ✅ Service Management
- ✅ Appointment Architecture
- ✅ Catálogo de negocios (ClientHome rediseñado)
- ✅ Búsqueda y filtro por categoría
- ✅ Detalle de negocio (BusinessDetailScreen)
- ✅ Reserva de cita basada en disponibilidad (rediseñada)
- ✅ Gestión de disponibilidad del negocio (ScheduleManagementScreen)
- ✅ Generación automática de franjas horarias
- ✅ Calendario mensual con días no disponibles deshabilitados
- ✅ Cuadrícula visual de franjas 🟢/🔴
- ✅ Citas confirmadas automáticamente (sin aprobación manual)
- ✅ Gestión de citas del negocio (BusinessAppointmentsScreen)

### Pendiente

- ⬜ Historial de citas del cliente
- ⬜ Pulido UI

---

## Roadmap oficial

### Sprint 1 — Infrastructure
> ✅ terminado

### Sprint 2 — Authentication
> ✅ terminado

### Sprint 3 — Business Management
> ✅ terminado

### Sprint 4 — Client Business Catalog
> ✅ terminado

**Objetivo:** Construir la experiencia principal del cliente.

**Incluye:**
- `BusinessRepository` (listado público)
- `ClientHome` rediseñado
- Cards
- Categorías
- Buscador
- `BusinessDetailScreen`

**Commit:** `Implement client business catalog`

---

### Sprint 5 — Appointment Booking
> ✅ terminado

**Objetivo:** Reservar citas.

**Incluye:**
- Seleccionar servicio
- Fecha
- Hora
- Guardar `Appointment`

**Commit:** `Implement appointment booking`

---

### Sprint 6 — Business Appointment Management
> ✅ terminado

**Objetivo:** Gestionar reservas.

**Incluye:**
- Lista de citas agrupadas por fecha
- Filtros por estado (Todas / Confirmadas / Completadas / Canceladas)
- Marcar como Completada
- Cancelar
- Estados vacíos y loading
- Recarga automática tras acciones

**Commit:** `Implement business appointment management`

---

### Sprint 7 — Client Appointment History

**Objetivo:** Mostrar historial del cliente.

**Commit:** `Implement client appointment history`

---

### Sprint 8 — UI Polish

**Objetivo:** Convertir la demo en una aplicación visualmente atractiva.

**Incluye:**
- Material 3
- Cards
- Iconos
- Colores
- Carrusel
- Animaciones ligeras
- Loading
- Estados vacíos

**Commit:** `UI polishing`

---

## Reglas para futuras implementaciones

1. Mantener siempre la arquitectura **Clean Architecture** existente.
2. **No** introducir Hilt ni otras librerías de inyección de dependencias.
3. Mantener **MVVM** con `ViewModelFactory` manuales.
4. Cada sprint debe **compilar completamente** antes de continuar.
5. Un **único objetivo funcional** por sprint.
6. Mantener nombres de commits **descriptivos y alineados con el roadmap**.
7. Priorizar funcionalidades **visibles para la demo del TFC** antes que características avanzadas.