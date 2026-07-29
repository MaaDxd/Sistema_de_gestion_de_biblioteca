package com.biblioteca.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de Swagger/OpenAPI para documentación automática de la API.
 * Disponible en: http://localhost:8080/swagger-ui.html
 *
 * Punto 6: Documentación de API sin esfuerzo adicional.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI bibliotecaOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("API Sistema de Gestión de Biblioteca")
                .description("""
                    Prototipo funcional para la materia de Estructuras de Datos.
                    
                    **Estructuras implementadas manualmente:**
                    - `ListaEnlazada<T>` → Catálogo de libros y usuarios
                    - `ColaPrestamos<T>` → Lista de espera FIFO
                    - `PilaHistorial<T>` → Historial de acciones LIFO
                    - `TablaHash<K,V>`   → Acceso O(1) a colas por libroId
                    """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("Estructuras de Datos")
                    .email("estudiante@universidad.edu")))
            .tags(List.of(
                new Tag().name("Libros").description("Gestión del catálogo de libros"),
                new Tag().name("Usuarios").description("Gestión de usuarios"),
                new Tag().name("Préstamos").description("Préstamos, devoluciones y cola de espera"),
                new Tag().name("Historial").description("Pila de acciones recientes (LIFO)")
            ));
    }
}
