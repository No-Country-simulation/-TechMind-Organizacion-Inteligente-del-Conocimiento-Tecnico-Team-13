package com.application.Views.Settings;

import com.application.Views.Layout.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Settings - KnowBase")
@Route(value = "settings", layout = MainLayout.class)
public class SettingsView extends VerticalLayout {

    public SettingsView() {
        setSizeFull();
        setMargin(false);
        setSpacing(true);
        setPadding(false);
        getStyle()
                .set("overflow-y", "auto")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("padding", "20px");

        // Cabecera
        HorizontalLayout header = createHeader();
        header.setHeight("auto");
        add(header);

        // Contenido de configuración
        VerticalLayout settings = createSettings();
        settings.setHeight("auto");
        add(settings);
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);

        VerticalLayout titles = new VerticalLayout();
        titles.setPadding(false);
        titles.setSpacing(false);
        H2 mainTitle = new H2("Configuración");
        mainTitle.getStyle().set("margin", "0").set("font-size", "24px").set("color", "#0f172a");
        Span subtitle = new Span("Personaliza tu experiencia y preferencias");
        subtitle.getStyle().set("color", "#64748b").set("font-size", "14px");
        titles.add(mainTitle, subtitle);

        header.add(titles);
        return header;
    }

    private VerticalLayout createSettings() {
        VerticalLayout container = new VerticalLayout();
        container.getStyle()
                .set("background-color", "#ffffff")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "12px")
                .set("padding", "30px")
                .set("max-width", "600px");

        // Sección de Perfil
        VerticalLayout profileSection = createSection("Perfil de Usuario", createProfileFields());
        container.add(profileSection);

        // Sección de Preferencias
        VerticalLayout preferencesSection = createSection("Preferencias", createPreferencesFields());
        container.add(preferencesSection);

        // Sección de Seguridad
        VerticalLayout securitySection = createSection("Seguridad", createSecurityFields());
        container.add(securitySection);

        // Botones de acción
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setJustifyContentMode(JustifyContentMode.END);
        buttons.getStyle().set("margin-top", "20px");

        Button saveBtn = new Button("Guardar Cambios");
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.getStyle().set("background-color", "#00b894").set("color", "#ffffff");

        Button cancelBtn = new Button("Cancelar");
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        buttons.add(cancelBtn, saveBtn);

        container.add(buttons);
        return container;
    }

    private VerticalLayout createSection(String title, VerticalLayout content) {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);

        H4 sectionTitle = new H4(title);
        sectionTitle.getStyle()
                .set("margin", "0 0 15px 0")
                .set("color", "#0f172a")
                .set("border-bottom", "1px solid #e2e8f0")
                .set("padding-bottom", "10px");

        section.add(sectionTitle, content);
        return section;
    }

    private VerticalLayout createProfileFields() {
        VerticalLayout fields = new VerticalLayout();
        fields.setPadding(false);
        fields.setSpacing(true);

        TextField nameField = new TextField("Nombre");
        nameField.setWidthFull();
        nameField.setValue("Ana Torres");

        TextField emailField = new TextField("Correo");
        emailField.setWidthFull();
        emailField.setValue("ana.torres@example.com");

        fields.add(nameField, emailField);
        return fields;
    }

    private VerticalLayout createPreferencesFields() {
        VerticalLayout fields = new VerticalLayout();
        fields.setPadding(false);
        fields.setSpacing(true);

        TextField languageField = new TextField("Idioma");
        languageField.setWidthFull();
        languageField.setValue("Español");

        TextField themeField = new TextField("Tema");
        themeField.setWidthFull();
        themeField.setValue("Automático");

        fields.add(languageField, themeField);
        return fields;
    }

    private VerticalLayout createSecurityFields() {
        VerticalLayout fields = new VerticalLayout();
        fields.setPadding(false);
        fields.setSpacing(true);

        Button changePasswordBtn = new Button("Cambiar Contraseña");
        changePasswordBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button twoFactorBtn = new Button("Habilitar Autenticación Doble Factor");
        twoFactorBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        fields.add(changePasswordBtn, twoFactorBtn);
        return fields;
    }
}
