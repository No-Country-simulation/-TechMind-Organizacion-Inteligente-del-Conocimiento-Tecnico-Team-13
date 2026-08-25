package com.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.AppShellSettings;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@StyleSheet("styles.css")
@Push
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void configurePage(AppShellSettings settings) {
        // 1. Icono SVG principal Logicore
        Map<String, String> svgAttr = new HashMap<>();
        svgAttr.put("type", "image/svg+xml");
        svgAttr.put("href", "Logo-Logicore.svg");
        settings.addLink("icon", svgAttr);

        // 2. Icono estándar .ico para pestañas de navegador
        Map<String, String> icoAttr = new HashMap<>();
        icoAttr.put("type", "image/x-icon");
        icoAttr.put("href", "favicon.ico");
        settings.addLink("shortcut icon", icoAttr);
        settings.addLink("icon", icoAttr);

        // 3. Icono específico Logicore-logo.ico como respaldo
        Map<String, String> logicoreIcoAttr = new HashMap<>();
        logicoreIcoAttr.put("type", "image/x-icon");
        logicoreIcoAttr.put("href", "Logicore-logo.ico");
        settings.addLink("icon", logicoreIcoAttr);

        // 4. Icono PNG de alta resolución para navegadores modernos y móviles
        Map<String, String> pngAttr = new HashMap<>();
        pngAttr.put("type", "image/png");
        pngAttr.put("href", "images/Logicore-logo.png");
        settings.addLink("icon", pngAttr);
        settings.addLink("apple-touch-icon", pngAttr);
    }

}
