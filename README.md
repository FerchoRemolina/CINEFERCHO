# CINEFERCHO

Sistema multi-teatro para cartelera, boletería, confitería y membresías.

Hay dos aplicaciones:

- **API** en Spring Boot (`8080`)
- **Web** en React + Vite (`5173`)

## Requisitos

- JDK 21
- Node.js 20 o superior
- Maven Wrapper incluido (`mvnw` / `mvnw.cmd`)
- PostgreSQL 16 o superior (ya puedes usar el servicio local)

La API usa PostgreSQL (`cineferchodb` en `localhost:5432`). Los datos se conservan al reiniciar. El seed (admin, cines, películas) solo corre si la base está vacía.

## Cómo correrlo

### 1. Base de datos

Crea usuario y base **una sola vez** (pide la clave del superusuario `postgres`):

```powershell
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -f scripts\init-db.sql
```

Si PostgreSQL está en otra ruta o versión, abre pgAdmin y ejecuta el mismo SQL de `scripts/init-db.sql`.

Por defecto la API conecta a `localhost:5432` con usuario y clave `cinefercho`. Si el usuario o la base ya existen, ignora el error. Con Docker: `docker compose up -d`.

Si usas otras credenciales:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/cineferchodb"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:SPRING_DATASOURCE_PASSWORD = "tu_clave"
```

### 2. Backend

En la raíz del proyecto:

```bash
# Windows (PowerShell)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
.\mvnw.cmd -DskipTests spring-boot:run
```

```bash
# Linux / macOS
./mvnw -DskipTests spring-boot:run
```

La API queda en `http://localhost:8080/api/v1`.

### 3. Frontend

```bash
cd cinefercho-web
npm install
npm run dev
```

Abre `http://localhost:5173`.

## Cuentas de prueba

Al arrancar se carga un administrador:

| Rol    | Correo                 | Contraseña |
|--------|------------------------|------------|
| Admin  | `admin@cinefercho.com` | `admin123` |

Los clientes se registran desde la web (cédula única). Su sesión de compra dura **2 minutos**.

## Cómo está organizado

```
CINEFERCHO/
├── src/main/java/com/cinefercho/   # API (entidades, servicios, seguridad JWT)
├── src/main/resources/             # application.yml
└── cinefercho-web/                 # Interfaz (React)
```

### Roles

- **Invitado:** ve cartelera, preventa, próximos y confitería.
- **Cliente:** reserva asientos, compra boletos/confitería y puede adquirir membresía.
- **Admin:** gestiona películas, funciones e inventario. Al entrar va al dashboard.

### Cartelera

Se arma **por teatro**, no es global:

- **En cartelera:** ya se proyectó en ese cine.
- **Preventa:** estreno en 15 días o menos; solo se venden funciones del día de estreno.
- **Próximos:** estreno a más de 15 días; ficha técnica, sin compra.

Si una función existe solo en Cúcuta, no aparece en Atlantis.

### Membresías

Se compran en el checkout (opcional):

| Plan | Duración | Descuento |
|------|----------|-----------|
| GOLD | 1 año    | 10%       |
| PRO  | 1 mes    | 20%       |

Con un plan vigente no se puede comprar otro hasta que expire.

### Modelo

```
City ──< Theater ──< CinemaHall ──< Seat
                 └──< Screening >── Movie
User ──< Invoice ──< TicketItem >── Screening, Seat
              └──< ConcessionItem >── Product
User >── MembershipType (NONE | GOLD | PRO)
```

El admin puede crear **una función** o un **rango de días** a la misma hora y sala.

## API (resumen)

| Área        | Ruta                         |
|-------------|------------------------------|
| Público     | `/api/v1/public/...`         |
| Auth        | `/api/v1/auth/login`, `/register` |
| Cliente     | `/api/v1/client/...`         |
| Admin       | `/api/v1/admin/...`          |
