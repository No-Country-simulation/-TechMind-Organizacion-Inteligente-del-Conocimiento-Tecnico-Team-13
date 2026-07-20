package com.hackaton.logicore.controller;

import com.hackaton.logicore.entity.User;
import com.hackaton.logicore.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // --- NUEVO ENDPOINT: Obtener los datos del usuario autenticado ---
    @GetMapping("/me")
    public User getCurrentUser() {
        // Extrae el username de la sesión/token actual
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        
        return userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Usuario actual no encontrado"));
    }

    // --- ENDPOINT ACTUALIZADO: Actualizar los datos del usuario autenticado ---
    @PutMapping("/me")
    public User updateCurrentUser(@RequestBody User userDetails) {
        // Extrae el username del token para asegurar que solo se edite a sí mismo
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Actualizamos los datos permitidos (no dejamos cambiar la contraseña aquí por seguridad)
        user.setNombre(userDetails.getNombre());
        user.setApellido(userDetails.getApellido());
        user.setCorreo(userDetails.getCorreo());
        
        // Opcional: Permitir cambiar el username si lo necesitas
        user.setUsername(userDetails.getUsername());
        
        return userRepository.save(user);
    }

    // Mantenemos el resto del CRUD para administración si lo requieres
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "Usuario eliminado";
    }
}