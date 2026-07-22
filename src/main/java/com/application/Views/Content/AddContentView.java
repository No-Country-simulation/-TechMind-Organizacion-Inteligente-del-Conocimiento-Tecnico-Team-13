package com.application.Views.Content;

import com.application.Views.Layout.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.Route;

@PageTitle("Add Content - KnowBase")
@Route(value = "add-content", layout = MainLayout.class)
public class AddContentView extends VerticalLayout {

    public AddContentView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background-color", "#f4f6fa").set("font-family", "'Inter', sans-serif");

        // Construcción modular de la UI
        add(createHeader());
        add(createTabsSection());
        add(createMainWorkspace());
    }

    // ==========================================
    // 1. CABECERA GLOBAL
    // ==========================================
    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        VerticalLayout titleLayout = new VerticalLayout();
        titleLayout.setPadding(false);
        titleLayout.setSpacing(false);

        H2 title = new H2("Añadir Contenido");
        title.getStyle().set("margin", "0").set("font-size", "22px").set("font-weight", "700").set("color", "#0f172a");

        Span subtitle = new Span("Pega, arrastra o enlaza contenido técnico para procesarlo con IA");
        subtitle.getStyle().set("color", "#64748b").set("font-size", "13px");

        titleLayout.add(title, subtitle);

        // Controles derechos (Buscador y notificaciones)
        HorizontalLayout controls = new HorizontalLayout();
        controls.setAlignItems(Alignment.CENTER);
        
        TextField searchField = new TextField();
        searchField.setPlaceholder("Buscar en repositorio... ⌘K");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("280px");

        Icon bellIcon = VaadinIcon.BELL_O.create();
        bellIcon.getStyle().set("color", "#64748b").set("cursor", "pointer").set("padding", "0 10px");
        Icon cogIcon = VaadinIcon.COG_O.create();
        cogIcon.getStyle().set("color", "#64748b").set("cursor", "pointer");

        controls.add(searchField, bellIcon, cogIcon);
        header.add(titleLayout, controls);

        return header;
    }

    // ==========================================
    // 2. NAVEGACIÓN (TABS)
    // ==========================================
    private HorizontalLayout createTabsSection() {
        HorizontalLayout tabs = new HorizontalLayout();
        tabs.setSpacing(true);
        tabs.getStyle().set("margin-top", "10px").set("margin-bottom", "5px");

        tabs.add(createTab("Texto / Artículo", VaadinIcon.FILE_TEXT, true));
        tabs.add(createTab("URL / Enlace", VaadinIcon.LINK, false));
        tabs.add(createTab("PDF / Documentación", VaadinIcon.FILE_O, false));
        tabs.add(createTab("Video / Curso", VaadinIcon.PLAY, false));

        return tabs;
    }

    private Button createTab(String text, VaadinIcon icon, boolean isActive) {
        Button btn = new Button(text, icon.create());
        if (isActive) {
            btn.getStyle().set("background-color", "#00b894").set("color", "white").set("border-radius", "20px");
        } else {
            btn.getStyle().set("background-color", "white").set("color", "#64748b").set("border-radius", "20px");
            btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        }
        return btn;
    }

    // ==========================================
    // 3. ESPACIO DE TRABAJO (SPLIT LAYOUT)
    // ==========================================
    private HorizontalLayout createMainWorkspace() {
        HorizontalLayout workspace = new HorizontalLayout();
        workspace.setSizeFull();
        workspace.setSpacing(true);

        VerticalLayout leftPanel = createLeftPanel();
        VerticalLayout rightPanel = createRightPanel();

        // Asignamos 50% de ancho a cada panel usando FlexGrow
        workspace.add(leftPanel, rightPanel);
        workspace.setFlexGrow(1, leftPanel);
        workspace.setFlexGrow(1, rightPanel);

        return workspace;
    }

    // --- PANEL IZQUIERDO: ENTRADA DE DATOS ---
    private VerticalLayout createLeftPanel() {
        VerticalLayout panel = new VerticalLayout();
        panel.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.03)")
                .set("border", "1px solid #f1f5f9");
        panel.setSizeFull(); // Ocupar todo el alto disponible

        // Header del panel
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        Span title = new Span("Contenido a Procesar");
        title.getStyle().set("font-weight", "600").set("font-size", "14px");
        Span meta = new Span("833 caracteres • ~1 min de lectura");
        meta.getStyle().set("color", "#94a3b8").set("font-size", "12px");
        header.add(title, meta);

        // Área de texto principal
        TextArea textArea = new TextArea();
        textArea.setSizeFull(); // Para que el textarea se expanda verticalmente
        textArea.setValue("Spring Boot 3 introduce mejoras significativas en la integración con Spring Security 6 para la gestión de autenticación y autorización mediante OAuth2 y JWT.\n\nEn este contexto, la configuración del Security Filter Chain se realiza de forma declarativa...");
        textArea.getStyle()
                .set("font-family", "'Consolas', 'Courier New', monospace")
                .set("font-size", "13px")
                .set("border", "none")
                .set("background", "transparent");

        // Fila de metadatos (Upload, Título, URL)
        HorizontalLayout inputsRow = new HorizontalLayout();
        inputsRow.setWidthFull();
        
        Button uploadBtn = new Button("O arrastra un archivo PDF, .txt o .md", VaadinIcon.UPLOAD.create());
        uploadBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        uploadBtn.getStyle().set("background-color", "#f8fafc").set("color", "#64748b").set("font-size", "12px").set("flex-grow", "1");

        TextField titleField = new TextField();
        titleField.setPlaceholder("Título del contenido (opcional)");
        titleField.getStyle().set("flex-grow", "1");

        TextField urlField = new TextField();
        urlField.setPlaceholder("URL de origen (opcional)");
        urlField.getStyle().set("flex-grow", "1");

        inputsRow.add(uploadBtn, titleField, urlField);

        // Botón de acción principal
        Button analyzeBtn = new Button("Analizar con IA", VaadinIcon.MAGIC.create());
        analyzeBtn.setWidthFull();
        analyzeBtn.getStyle()
                .set("background-color", "#00b894")
                .set("color", "white")
                .set("font-weight", "600")
                .set("border-radius", "8px")
                .set("padding", "20px 0");

        panel.add(header, textArea, inputsRow, analyzeBtn);
        return panel;
    }

    // --- PANEL DERECHO: RESULTADOS DE LA IA ---
    private VerticalLayout createRightPanel() {
        VerticalLayout panel = new VerticalLayout();
        panel.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.03)")
                .set("border", "1px solid #f1f5f9");
        panel.setSizeFull();
        panel.getStyle().set("overflow-y", "auto"); // Scroll si el contenido es largo

        // Header del resultado
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        
        HorizontalLayout titleGroup = new HorizontalLayout();
        Icon spark = VaadinIcon.MAGIC.create();
        spark.setColor("#00b894");
        Span title = new Span("Resultado del Análisis");
        title.getStyle().set("font-weight", "700").set("font-size", "14px");
        titleGroup.add(spark, title);

        Span badge = new Span("COMPLETADO");
        badge.getStyle()
                .set("background-color", "#d1fae5")
                .set("color", "#059669")
                .set("font-size", "10px")
                .set("font-weight", "800")
                .set("padding", "4px 8px")
                .set("border-radius", "12px");
        
        header.add(titleGroup, badge);
        panel.add(header);

        // Barras de progreso de categorías
        panel.add(createCategoryBar("Backend", "89%", 0.89, "#8b5cf6"));
        panel.add(createCategoryBar("Spring Security", "76%", 0.76, "#a855f7"));
        panel.add(createCategoryBar("Seguridad", "68%", 0.68, "#ef4444"));
        panel.add(createCategoryBar("Data Patterns", "51%", 0.51, "#0ea5e9"));
        panel.add(createCategoryBar("Infraestructura", "32%", 0.32, "#eab308"));

        // Alerta de Categoría Principal
        Div highlightAlert = new Div();
        highlightAlert.getStyle()
                .set("background-color", "#ecfdf5") // Verde muy claro
                .set("border", "1px solid #a7f3d0")
                .set("border-radius", "8px")
                .set("padding", "15px")
                .set("margin-top", "15px")
                .set("display", "flex")
                .set("align-items", "center");
        
        Icon check = VaadinIcon.CHECK_CIRCLE_O.create();
        check.setColor("#059669");
        check.getStyle().set("margin-right", "10px");

        VerticalLayout alertTexts = new VerticalLayout();
        alertTexts.setPadding(false); alertTexts.setSpacing(false);
        Span aTop = new Span("Categoría principal asignada");
        aTop.getStyle().set("font-size", "11px").set("color", "#059669");
        Span aBot = new Span("Backend · 89% confianza");
        aBot.getStyle().set("font-size", "13px").set("font-weight", "700").set("color", "#064e3b");
        alertTexts.add(aTop, aBot);
        
        highlightAlert.add(check, alertTexts);
        panel.add(highlightAlert);

        // Etiquetas generadas por IA
        Span tagsTitle = new Span("ETIQUETAS GENERADAS POR IA");
        tagsTitle.getStyle().set("font-size", "11px").set("font-weight", "700").set("color", "#94a3b8").set("margin-top", "20px");
        
        FlexLayout tagsLayout = new FlexLayout();
        tagsLayout.getStyle().set("gap", "8px").set("flex-wrap", "wrap").set("margin-top", "10px");
        tagsLayout.add(
            createPill("Spring Boot"), createPill("JWT"), createPill("OAuth2"), 
            createPill("Backend"), createPill("Spring Security"), createPill("Microservices"), createPill("REST API")
        );
        panel.add(tagsTitle, tagsLayout);

        // Conceptos Clave
        Span conceptTitle = new Span("CONCEPTOS CLAVE IDENTIFICADOS");
        conceptTitle.getStyle().set("font-size", "11px").set("font-weight", "700").set("color", "#94a3b8").set("margin-top", "20px");
        panel.add(conceptTitle);

        panel.add(createNumberedItem("1", "Spring Security Filter Chain"));
        panel.add(createNumberedItem("2", "Bearer Token Validation"));
        panel.add(createNumberedItem("3", "Refresh Token Rotation"));
        panel.add(createNumberedItem("4", "OAuth2 Authorization Server"));
        panel.add(createNumberedItem("5", "JWT Claims & Scope"));
        panel.add(createNumberedItem("6", "Keycloak Integration"));

        // Resumen IA
        Span resTitle = new Span("RESUMEN IA");
        resTitle.getStyle().set("font-size", "11px").set("font-weight", "700").set("color", "#94a3b8").set("margin-top", "20px");
        
        Div resBox = new Div();
        resBox.setText("Este contenido es ideal para el archivo en la categoría de Arquitectura de Autenticación en entornos distribuidos con Spring Framework.");
        resBox.getStyle()
                .set("background-color", "#f8fafc")
                .set("padding", "15px")
                .set("border-radius", "8px")
                .set("font-size", "13px")
                .set("color", "#334155")
                .set("margin-top", "10px");
        panel.add(resTitle, resBox);

        // Botón Final
        Button saveBtn = new Button("Guardar en Biblioteca", VaadinIcon.CHECK.create());
        saveBtn.setWidthFull();
        saveBtn.getStyle()
                .set("background-color", "#0f172a") // Negro/Azul muy oscuro
                .set("color", "white")
                .set("font-weight", "600")
                .set("border-radius", "8px")
                .set("padding", "20px 0")
                .set("margin-top", "auto"); // Empuja el botón al fondo si hay espacio
        panel.add(saveBtn);

        return panel;
    }

    // --- MÉTODOS AUXILIARES (REUTILIZABLES) ---

    private VerticalLayout createCategoryBar(String name, String pctText, double pctValue, String color) {
        VerticalLayout vl = new VerticalLayout();
        vl.setPadding(false); vl.setSpacing(false); vl.getStyle().set("margin-top", "10px");
        
        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidthFull(); hl.setJustifyContentMode(JustifyContentMode.BETWEEN);
        Span n = new Span(name); n.getStyle().set("font-size", "12px").set("font-weight", "600");
        Span p = new Span(pctText); p.getStyle().set("font-size", "12px").set("font-weight", "700");
        hl.add(n, p);

        ProgressBar bar = new ProgressBar();
        bar.setValue(pctValue);
        bar.setWidthFull();
        bar.getStyle().set("height", "6px").set("border-radius", "3px").set("--lumo-primary-color", color);
        
        vl.add(hl, bar);
        return vl;
    }

    private Span createPill(String text) {
        Span badge = new Span(text);
        badge.getStyle()
                .set("background-color", "#f3e8ff") // Fondo morado claro
                .set("color", "#8b5cf6")           // Texto morado
                .set("padding", "4px 12px")
                .set("border-radius", "16px")
                .set("font-size", "12px")
                .set("font-weight", "600");
        return badge;
    }

    private HorizontalLayout createNumberedItem(String number, String text) {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setAlignItems(Alignment.CENTER);
        hl.getStyle().set("margin-top", "6px");

        Span num = new Span(number);
        num.getStyle()
                .set("background-color", "#d1fae5")
                .set("color", "#059669")
                .set("font-weight", "800")
                .set("font-size", "10px")
                .set("width", "18px")
                .set("height", "18px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("border-radius", "4px");
        
        Span txt = new Span(text);
        txt.getStyle().set("font-size", "13px").set("color", "#334155");

        hl.add(num, txt);
        return hl;
    }
}