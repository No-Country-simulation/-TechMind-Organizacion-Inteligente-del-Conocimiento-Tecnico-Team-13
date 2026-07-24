package com.hackaton.logicore.repository;

import com.hackaton.logicore.entity.Resource;
import com.hackaton.logicore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    // Buscar todos los recursos que sean públicos
    List<Resource> findByPublicoTrue();

    // Buscar los recursos pertenecientes a un usuario específico
    List<Resource> findByUser(User user);

    // Buscar recursos privados de un usuario específico
    List<Resource> findByUserAndPublicoFalse(User user);
}