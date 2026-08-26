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
        Map<String, String> iconAttr = new HashMap<>();
        iconAttr.put("type", "image/png");
        iconAttr.put("href", "images/LogicoreLogoico.png");
        settings.addLink("icon", iconAttr);
        settings.addLink("shortcut icon", iconAttr);
        settings.addLink("apple-touch-icon", iconAttr);
    }

}
