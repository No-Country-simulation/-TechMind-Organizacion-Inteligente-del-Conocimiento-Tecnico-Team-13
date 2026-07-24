package com.hackaton.logicore.controller;

import com.hackaton.logicore.entity.Resource;
import com.hackaton.logicore.entity.User;
import com.hackaton.logicore.enums.ResourceType;
import com.hackaton.logicore.repository.ResourceRepository;
import com.hackaton.logicore.repository.UserRepository;
import com.hackaton.logicore.service.StorageService;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final StorageService storageService; // Inyectamos el servicio de MinIO

    public ResourceController(ResourceRepository resourceRepository, 
                                UserRepository userRepository, 
                                StorageService storageService) {
            this.resourceRepository = resourceRepository;
            this.userRepository = userRepository;
            this.storageService = storageService;
        }

    // Método auxiliar para obtener el usuario autenticado a partir del JWT
    private User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    // --- CREAR RECURSO (Se asigna automáticamente al usuario del JWT) ---
    // --- OPCIÓN A: CREAR RECURSO Y SUBIR ARCHIVO EN EL MISMO ENDPOINT ---
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Resource createResource(
            @RequestParam("titulo") String titulo,
            @RequestParam(value = "url", required = false) String url,
            @RequestParam(value = "texto", required = false) String texto,
            @RequestParam(value = "categoria", required = false) String categoria,
            @RequestParam(value = "tecnologia", required = false) String tecnologia,
            @RequestParam("tipo") ResourceType tipo,
            @RequestParam(value = "publico", defaultValue = "false") Boolean publico,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        User currentUser = getAuthenticatedUser();
        Resource resource = new Resource();

    // 1. Manejo según el tipo de recurso seleccionado
        if (tipo == ResourceType.TEXTO) {
            if (texto == null || texto.isBlank()) {
                throw new RuntimeException("Para un recurso de tipo TEXTO, el campo 'texto' no puede estar vacío.");
            }
            // Asignamos una URL por defecto o vacía para cumplir con la columna si no envían nada
            resource.setUrl(url != null && !url.isBlank() ? url : "N/A");

        } else if (file != null && !file.isEmpty()) {
            // 2. Si es de otro tipo (ej. ARCHIVO) y adjuntaron archivo físico
            String uploadedUrl = storageService.uploadFile(file);
            resource.setUrl(uploadedUrl);

        } else if (url != null && !url.isBlank()) {
            // 3. Si es VIDEO u otro tipo sin archivo adjunto, requiere una URL válida
            resource.setUrl(url);

        } else {
            throw new RuntimeException("Para el tipo " + tipo + ", debe adjuntar un archivo o proporcionar una URL válida.");
        }

        resource.setTitulo(titulo);
        resource.setTexto(texto);
        resource.setCategoria(categoria);
        resource.setTecnologia(tecnologia);
        resource.setTipo(tipo);
        resource.setPublico(publico);
        resource.setUser(currentUser);

        return resourceRepository.save(resource);
    }

    // --- ENDPOINT: Obtener TODOS los recursos PÚBLICOS ---
    @GetMapping("/public")
    public List<Resource> getPublicResources() {
        return resourceRepository.findByPublicoTrue();
    }

    // --- ENDPOINT: Obtener TODOS los recursos del usuario autenticado (públicos y privados) ---
    @GetMapping("/me")
    public List<Resource> getMyResources() {
        User currentUser = getAuthenticatedUser();
        return resourceRepository.findByUser(currentUser);
    }

    // --- ENDPOINT: Obtener SOLO los recursos PRIVADOS del usuario autenticado ---
    @GetMapping("/me/private")
    public List<Resource> getMyPrivateResources() {
        User currentUser = getAuthenticatedUser();
        return resourceRepository.findByUserAndPublicoFalse(currentUser);
    }

    // --- Actualizar recurso (solo si pertenece al usuario) ---
    @PutMapping("/{id}")
    public Resource updateResource(@PathVariable Long id, @RequestBody Resource resourceDetails) {
        User currentUser = getAuthenticatedUser();
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recurso no encontrado"));

        // Validar que el recurso le pertenezca al usuario que intenta editarlo
        if (!resource.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("No tienes permiso para modificar este recurso");
        }

        resource.setTitulo(resourceDetails.getTitulo());
        resource.setUrl(resourceDetails.getUrl());
        resource.setTexto(resourceDetails.getTexto());
        resource.setCategoria(resourceDetails.getCategoria());
        resource.setTecnologia(resourceDetails.getTecnologia());
        resource.setPublico(resourceDetails.getPublico());

        resource.setTipo(resourceDetails.getTipo());

        return resourceRepository.save(resource);
    }

    // --- Eliminar recurso (solo si pertenece al usuario) ---
    @DeleteMapping("/{id}")
    public String deleteResource(@PathVariable Long id) {
        User currentUser = getAuthenticatedUser();
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recurso no encontrado"));

        if (!resource.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("No tienes permiso para eliminar este recurso");
        }

        resourceRepository.delete(resource);
        return "Recurso eliminado con éxito";
    }
}