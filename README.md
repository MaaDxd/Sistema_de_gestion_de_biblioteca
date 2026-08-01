# 📚 Sistema de Gestión de Biblioteca

Prototipo funcional desarrollado para la materia de **Estructuras de Datos**.  
Backend en **Java + Spring Boot**, frontend en **React + Vite**, comunicación via **API REST JSON**.

---

## Estructuras de datos implementadas manualmente

| Estructura | Clase | Justificación de uso |
|---|---|---|
| **Lista enlazada** | `ListaEnlazada<T>` | Catálogo de libros y registro de usuarios. Inserción O(1) al final, búsqueda O(n) por recorrido. |
| **Cola (FIFO)** | `ColaPrestamos<T>` | Lista de espera cuando no hay copias disponibles. El primero en solicitar es el primero en ser atendido. |
| **Pila (LIFO)** | `PilaHistorial<T>` | Historial de acciones recientes y funcionalidad "deshacer". La última acción registrada es la primera en consultarse. |
| **Tabla Hash** | `TablaHash<K,V>` | Acceso O(1) amortizado a la cola de espera de cada libro por su ID, en lugar de búsqueda O(n). |

Cada estructura implementa sus operaciones básicas desde cero: inserción, eliminación, búsqueda y recorrido.  
No se usan colecciones nativas de alto nivel (`ArrayList`, `HashMap`, `LinkedList`, etc.).

---

## Requisitos del sistema

| Herramienta | Versión mínima | Verificar con |
|---|---|---|
| Java (JDK) | 17+ (probado con 25) | `java -version` |
| Maven | 3.6.3+ (probado con 3.9.6 y 3.9.16) | `mvn -version` |
| Node.js | 18+ (probado con 24) | `node --version` |

> **Si Maven no está instalado**, descárgalo de [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi).  
> Cualquier versión 3.9.x funciona (3.9.6, 3.9.16, etc.).  
> Extrae el zip en `C:\maven\` y agrega `C:\maven\apache-maven-X.X.X\bin` al PATH del sistema.

---

## Cómo levantar el proyecto

### Terminal 1 — Backend (Spring Boot)

```powershell
cd backend
mvn spring-boot:run
```cd ..

Espera hasta ver:
```
Started BibliotecaApplication in X.XX seconds
```

La API queda disponible en: `http://localhost:8080/api`  
Documentación Swagger UI: `http://localhost:8080/swagger-ui.html`

### Terminal 2 — Frontend (React + Vite)

```powershell
cd frontend
npm install      # solo la primera vez
npm run dev
```

La app queda disponible en: `http://localhost:5173`

---

## Ejecutar los tests

```powershell
cd backend
mvn test
```

Incluye **35 tests** divididos en dos suites:

| Suite | Tests | Qué cubre |
|---|---|---|
| `EstructurasDatosTest` | 24 | `ListaEnlazada`, `ColaPrestamos`, `PilaHistorial`, `TablaHash` — todas las operaciones básicas |
| `PrestamoServicioTest` | 11 | Lógica de negocio: préstamo activo, cola FIFO, devolución, activación automática desde cola, casos de error |

---

## Arquitectura del proyecto

```
backend/
├── datastructures/
│   ├── Nodo.java                  ← Nodo genérico base
│   ├── ListaEnlazada.java         ← Catálogo de libros y usuarios
│   ├── ColaPrestamos.java         ← Lista de espera FIFO
│   ├── PilaHistorial.java         ← Historial LIFO / deshacer
│   └── TablaHash.java             ← Acceso O(1) a colas por libroId
├── modelo/
│   ├── Libro.java                 ← Con Bean Validation (@NotBlank, @Min, etc.)
│   ├── Usuario.java               ← Con Bean Validation (@Email, @Size, etc.)
│   ├── Prestamo.java
│   └── AccionHistorial.java
├── servicio/
│   ├── LibroServicio.java         ← synchronized en todos los métodos
│   ├── UsuarioServicio.java       ← synchronized en todos los métodos
│   ├── PrestamoServicio.java      ← synchronized, usa TablaHash para colas
│   └── HistorialServicio.java     ← Expone la PilaHistorial a los controladores
├── controlador/
│   ├── LibroControlador.java
│   ├── UsuarioControlador.java
│   ├── PrestamoControlador.java
│   └── HistorialControlador.java
├── excepcion/
│   ├── RecursoNoEncontradoException.java   ← HTTP 404
│   ├── OperacionInvalidaException.java     ← HTTP 409
│   └── ManejadorGlobalExcepciones.java     ← Convierte excepciones a JSON
├── persistencia/
│   └── PersistenciaServicio.java  ← Serializa/deserializa a JSON en disco (./data/)
└── config/
    ├── PilaHistorialBean.java     ← Bean singleton de la pila (evita ciclos)
    ├── InicializadorServicio.java ← @PostConstruct (carga) / @PreDestroy (guarda)
    └── SwaggerConfig.java         ← Documentación OpenAPI

frontend/
├── src/
│   ├── api/
│   │   ├── axios.js               ← Instancia Axios centralizada
│   │   ├── libros.js
│   │   ├── usuarios.js
│   │   └── prestamos.js
│   ├── hooks/
│   │   └── useApi.js              ← Hook con manejo de errores 400/404/409/500
│   ├── components/
│   │   ├── Alerta.jsx             ← Componente de alerta reutilizable
│   │   ├── FormularioLibro.jsx
│   │   ├── FormularioUsuario.jsx
│   │   └── TablaLibros.jsx
│   └── pages/
│       ├── PaginaLibros.jsx
│       ├── PaginaUsuarios.jsx
│       ├── PaginaPrestamos.jsx
│       └── PaginaHistorial.jsx
```

---

## Endpoints REST

### Libros — `/api/libros`

| Método | Ruta | Descripción | Respuesta |
|--------|------|-------------|-----------|
| GET | `/api/libros` | Listar catálogo completo | 200 |
| GET | `/api/libros/buscar?termino=` | Buscar por título, autor o ISBN | 200 |
| GET | `/api/libros/{id}` | Obtener libro por ID | 200 / 404 |
| POST | `/api/libros` | Registrar libro | 201 / 400 / 409 |
| PUT | `/api/libros/{id}` | Editar libro | 200 / 404 |
| DELETE | `/api/libros/{id}` | Eliminar libro | 204 / 404 / 409 |

### Usuarios — `/api/usuarios`

| Método | Ruta | Descripción | Respuesta |
|--------|------|-------------|-----------|
| GET | `/api/usuarios` | Listar usuarios | 200 |
| GET | `/api/usuarios/buscar?termino=` | Buscar por nombre, ID o correo | 200 |
| GET | `/api/usuarios/{id}` | Obtener usuario por ID | 200 / 404 |
| POST | `/api/usuarios` | Registrar usuario | 201 / 400 / 409 |
| PUT | `/api/usuarios/{id}` | Editar usuario | 200 / 404 |

### Préstamos — `/api/prestamos`

| Método | Ruta | Descripción | Respuesta |
|--------|------|-------------|-----------|
| GET | `/api/prestamos` | Historial completo | 200 |
| GET | `/api/prestamos/{id}` | Obtener préstamo por ID | 200 / 404 |
| POST | `/api/prestamos` | Registrar préstamo (activo o en cola si no hay copias) | 201 / 202 / 400 / 409 |
| PUT | `/api/prestamos/{id}/devolver` | Registrar devolución | 200 / 404 |
| GET | `/api/prestamos/cola/{libroId}` | Ver cola de espera FIFO del libro | 200 |
| GET | `/api/prestamos/usuario/{usuarioId}` | Historial de préstamos por usuario | 200 |

> `201` = préstamo activo. `202` = sin copias, usuario encolado en lista de espera.

### Historial (Pila) — `/api/historial`

| Método | Ruta | Descripción | Respuesta |
|--------|------|-------------|-----------|
| GET | `/api/historial` | Ver todas las acciones en orden LIFO | 200 |
| GET | `/api/historial/cima` | Ver la última acción sin eliminarla (peek) | 200 |
| GET | `/api/historial/count` | Cantidad de acciones en la pila | 200 |
| DELETE | `/api/historial/deshacer` | Desapilar la última acción | 200 |

---

## Persistencia de datos

Los datos se guardan automáticamente en la carpeta `./data/` al apagar el servidor:

```
backend/data/
├── libros.json
├── usuarios.json
└── prestamos.json
```

Al reiniciar el servidor, los datos se recargan desde estos archivos.  
Si la carpeta no existe (primera ejecución), se crea automáticamente.

---

## Documentación interactiva (Swagger)

Con el backend corriendo, abre:

```
http://localhost:8080/swagger-ui.html
```

Desde ahí puedes probar todos los endpoints directamente en el navegador sin necesidad de Postman.
