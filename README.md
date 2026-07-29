# 📚 Sistema de Gestión de Biblioteca

Prototipo funcional para la materia de **Estructuras de Datos**.

## Estructuras implementadas manualmente

| Estructura | Clase | Uso |
|---|---|---|
| **Lista enlazada** | `ListaEnlazada<T>` | Catálogo de libros y registro de usuarios |
| **Cola (FIFO)** | `ColaPrestamos<T>` | Lista de espera cuando no hay copias disponibles |
| **Pila (LIFO)** | `PilaHistorial<T>` | Historial de acciones / funcionalidad deshacer |

---

## Cómo levantar el proyecto

### Requisitos
- Java 17+
- Maven 3.8+
- Node.js 18+

---

### 1. Backend (Spring Boot)

```bash
cd backend
mvn spring-boot:run
```

La API queda en: `http://localhost:8080/api`

Para correr los tests de estructuras de datos:
```bash
mvn test
```

---

### 2. Frontend (React + Vite)

```bash
cd frontend
npm install
npm run dev
```

La app queda en: `http://localhost:5173`

---

## Endpoints REST

### Libros
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/libros` | Listar catálogo |
| GET | `/api/libros/buscar?termino=` | Buscar por título/autor/ISBN |
| GET | `/api/libros/{id}` | Obtener por ID |
| POST | `/api/libros` | Registrar libro |
| PUT | `/api/libros/{id}` | Editar libro |
| DELETE | `/api/libros/{id}` | Eliminar libro |

### Usuarios
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/usuarios` | Listar usuarios |
| GET | `/api/usuarios/buscar?termino=` | Buscar usuario |
| POST | `/api/usuarios` | Registrar usuario |
| PUT | `/api/usuarios/{id}` | Editar usuario |

### Préstamos
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/prestamos` | Historial completo |
| POST | `/api/prestamos` | Registrar préstamo (o encolar si no hay copias) |
| PUT | `/api/prestamos/{id}/devolver` | Registrar devolución |
| GET | `/api/prestamos/cola/{libroId}` | Ver cola de espera del libro |
| GET | `/api/prestamos/usuario/{usuarioId}` | Historial por usuario |

### Historial (Pila)
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/historial` | Ver todas las acciones (LIFO) |
| GET | `/api/historial/cima` | Ver la última acción (peek) |
| DELETE | `/api/historial/deshacer` | Desapilar la última acción |
