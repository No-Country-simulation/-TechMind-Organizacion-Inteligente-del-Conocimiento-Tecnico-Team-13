package com.application.Views.Annotations;

import com.application.Views.Layout.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.application.model.Content;
import com.application.model.User;
import com.application.service.SupabaseService;
import com.application.service.UserSession;

@PageTitle("Anotaciones y Workflows - KnowBase")
@Route(value = "anotaciones", layout = MainLayout.class)

public class AnnotationsView extends HorizontalLayout {

    // Color Palette
    private static final String COLOR_PRIMARY_GREEN = "#10B981";
    private static final String COLOR_BACKGROUND_ACTIVE_GREEN = "#ECFDF5";
    private static final String COLOR_BORDER_ACTIVE_GREEN = "#A7F3D0";
    private static final String COLOR_TEXT_DARK = "#0F172A";
    private static final String COLOR_TEXT_SECONDARY = "#64748B";

    private boolean showWorkflows = true;

    private VerticalLayout leftSidebar;
    private VerticalLayout centerPanel;
    private VerticalLayout rightSidebar;
    
    private Button btnWorkflows;
    private Button btnHilos;

    // Injected services
    private final SupabaseService supabaseService;
    private final UserSession userSession;
    private java.util.List<Content> userContents = new java.util.ArrayList<>();
    private Content selectedContent = null;
    // Temporary in-memory notes map (not persisted) keyed by content ID; null key used for unsaved/general notes
    private java.util.Map<java.util.UUID, String> tempNotes = new java.util.HashMap<>();

    public AnnotationsView(SupabaseService supabaseService, UserSession userSession) {
        this.supabaseService = supabaseService;
        this.userSession = userSession;

        setSizeFull();
        setSpacing(false);
        setPadding(false);
        getStyle().set("background-color", "#F8FAFC");

        loadUserContents();
        if (!userContents.isEmpty()) selectedContent = userContents.get(0);
        refreshView();
    }

    private void refreshView() {
        removeAll();
        leftSidebar = createLeftSidebar();
        centerPanel = createCenterPanel();
        rightSidebar = createRightSidebar();
        add(leftSidebar, centerPanel, rightSidebar);
        expand(centerPanel);
        updateToggleStyles();
    }

    private VerticalLayout createLeftSidebar() {
        VerticalLayout sidebar = new VerticalLayout();
        sidebar.setWidth("280px");
        sidebar.setHeightFull();
        sidebar.setPadding(false);
        sidebar.setSpacing(false);
        sidebar.getStyle()
            .set("border-right", "1px solid #E2E8F0")
            .set("background-color", "#FFFFFF");

        H3 headerTitle = new H3("Anotaciones");
        headerTitle.getStyle().set("padding", "var(--lumo-space-m)").set("margin", "0");
        
        HorizontalLayout toggleGroup = createToggleGroup();
        
        VerticalLayout contentList = new VerticalLayout();
        contentList.setSizeFull();
        contentList.setPadding(true);
        contentList.setSpacing(true);
        contentList.getStyle().set("overflow-y", "auto");

        if (showWorkflows) {
            contentList.add(buildWorkflowListItems());
        } else {
            contentList.add(buildThreadListItems());
        }
        
        sidebar.add(headerTitle, toggleGroup, contentList);
        sidebar.expand(contentList);
        return sidebar;
    }
    
    private HorizontalLayout createToggleGroup() {
        btnWorkflows = new Button("Workflows (3)");
        btnHilos = new Button("Hilos IA (3)");

        btnWorkflows.addClickListener(e -> {
            if (!showWorkflows) {
                showWorkflows = true;
                refreshView();
            }
        });

        btnHilos.addClickListener(e -> {
            if (showWorkflows) {
                showWorkflows = false;
                refreshView();
            }
        });
        
        HorizontalLayout toggleContainer = new HorizontalLayout(btnWorkflows, btnHilos);
        toggleContainer.setSpacing(true);
        toggleContainer.getStyle()
            .set("background-color", "#F1F5F9")
            .set("border-radius", "20px")
            .set("padding", "4px");
        
        HorizontalLayout wrapper = new HorizontalLayout(toggleContainer);
        wrapper.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        wrapper.setWidthFull();
        wrapper.getStyle().set("padding", "0 var(--lumo-space-s)");
            
        return wrapper;
    }
    
    private void updateToggleStyles() {
        btnWorkflows.getStyle().clear();
        btnHilos.getStyle().clear();

        if (showWorkflows) {
            btnWorkflows.getStyle()
                .set("background-color", COLOR_TEXT_DARK)
                .set("color", "white")
                .set("border-radius", "16px");
            btnHilos.getStyle()
                .set("background", "transparent")
                .set("color", COLOR_TEXT_SECONDARY);
        } else {
            btnHilos.getStyle()
                .set("background-color", COLOR_TEXT_DARK)
                .set("color", "white")
                .set("border-radius", "16px");
            btnWorkflows.getStyle()
                .set("background", "transparent")
                .set("color", COLOR_TEXT_SECONDARY);
        }
    }


    private VerticalLayout createCenterPanel() {
        centerPanel = new VerticalLayout();
        centerPanel.setSizeFull();
        centerPanel.setPadding(true);
        centerPanel.setSpacing(true);
        centerPanel.getStyle().set("overflow-y", "auto");

        if (showWorkflows) {
            centerPanel.add(buildWorkflowDetailView());
        } else {
            centerPanel.add(buildThreadDetailView());
        }

        return centerPanel;
    }

    private VerticalLayout createRightSidebar() {
        rightSidebar = new VerticalLayout();
        rightSidebar.setWidth("300px");
        rightSidebar.setHeightFull();
        rightSidebar.setPadding(true);
        rightSidebar.setSpacing(true);
        rightSidebar.getStyle()
            .set("border-left", "1px solid #E2E8F0")
            .set("background-color", "#FFFFFF");

        if (showWorkflows) {
            rightSidebar.add(buildWorkflowSummary());
        } else {
            rightSidebar.add(buildThreadSummary());
        }
        return rightSidebar;
    }
    
    private VerticalLayout buildWorkflowListItems() {
        VerticalLayout list = new VerticalLayout();
        list.setSpacing(true);
        list.setPadding(false);

        if (userContents.isEmpty()) {
            // Fallback sample items
            list.add(createWorkflowListItem("Setup Spring Boot + OAuth2 en OCI", 66, "Backend", 5, true));
            list.add(createWorkflowListItem("Crear Pipeline de CI/CD para Vaadin", 33, "DevOps", 8, false));
            list.add(createWorkflowListItem("Guía de Estilos para UI", 100, "Frontend", 4, false));
        } else {
            for (Content c : userContents) {
                int progress = computeProgressForContent(c);
                String category = c.getTipoContenido() != null ? c.getTipoContenido() : "General";
                int steps = 4;
                if (c.getTextoPlano() != null && !c.getTextoPlano().isBlank()) {
                    String[] sents = c.getTextoPlano().split("(?<=[.!?])\\s+");
                    steps = Math.min(8, Math.max(1, sents.length));
                }
                boolean active = selectedContent != null && c.getId() != null && selectedContent.getId() != null && selectedContent.getId().equals(c.getId());
                if (selectedContent == null) active = userContents.indexOf(c) == 0;
                list.add(createWorkflowListItem(c, progress, category, steps, active));
            }
        }

        return list;
    }
    
    private Component createWorkflowListItem(Content content, int progress, String category, int steps, boolean isActive) {
        // clickable version that selects the workflow
        Div card = new Div();
        card.setWidthFull();
        card.getStyle()
            .set("background-color", isActive ? COLOR_BACKGROUND_ACTIVE_GREEN : "#FFFFFF")
            .set("border-radius", "12px")
            .set("padding", "12px 16px")
            .set("border", "1px solid " + (isActive ? COLOR_BORDER_ACTIVE_GREEN : "#E2E8F0"))
                .set("width", "auto")
            .set("box-shadow", "0 1px 3px rgba(0,0,0,0.05)");

        H5 titleLabel = new H5(content != null && content.getTitulo() != null && !content.getTitulo().isBlank() ? content.getTitulo() : "Sin título");
        titleLabel.getStyle().set("margin", "0 0 4px 0");

        ProgressBar progressBar = new ProgressBar(0, 100, progress);
        progressBar.getStyle().set("height", "4px");

        Span progressText = new Span(progress + "%");
        if (progress == 100) progressText.getElement().getThemeList().add("badge success");
        else if (progress > 50) progressText.getElement().getThemeList().add("badge");
        else progressText.getElement().getThemeList().add("badge error");

        HorizontalLayout header = new HorizontalLayout(titleLabel, progressText);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        Span categoryBadge = new Span(category);
        categoryBadge.getElement().getThemeList().add("badge contrast");
        Span stepsBadge = new Span(steps + " pasos");
        stepsBadge.getElement().getThemeList().add("badge contrast");

        HorizontalLayout footer = new HorizontalLayout(categoryBadge, stepsBadge);
        footer.setSpacing(true);

        card.add(header, progressBar, footer);

        card.getElement().addEventListener("click", ev -> {
            this.selectedContent = content;
            // refresh center and right side
            centerPanel.removeAll();
            rightSidebar.removeAll();
            centerPanel.add(buildWorkflowDetailView());
            rightSidebar.add(buildWorkflowSummary());
            refreshView();
        });

        return card;
    }

    // Overload used by example/fallback items
    private Component createWorkflowListItem(String title, int progress, String category, int steps, boolean isActive) {
        Div card = new Div();
        card.setWidthFull();
        card.getStyle()
            .set("background-color", isActive ? COLOR_BACKGROUND_ACTIVE_GREEN : "#FFFFFF")
            .set("border-radius", "12px")
            .set("padding", "12px 16px")
            .set("border", "1px solid " + (isActive ? COLOR_BORDER_ACTIVE_GREEN : "#E2E8F0"))
            .set("box-shadow", "0 1px 3px rgba(0,0,0,0.05)");

        H5 titleLabel = new H5(title);
        titleLabel.getStyle().set("margin", "0 0 4px 0");

        ProgressBar progressBar = new ProgressBar(0, 100, progress);
        progressBar.getStyle().set("height", "4px");

        Span progressText = new Span(progress + "%");
        if (progress == 100) progressText.getElement().getThemeList().add("badge success");
        else if (progress > 50) progressText.getElement().getThemeList().add("badge");
        else progressText.getElement().getThemeList().add("badge error");

        HorizontalLayout header = new HorizontalLayout(titleLabel, progressText);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        Span categoryBadge = new Span(category);
        categoryBadge.getElement().getThemeList().add("badge contrast");
        Span stepsBadge = new Span(steps + " pasos");
        stepsBadge.getElement().getThemeList().add("badge contrast");

        HorizontalLayout footer = new HorizontalLayout(categoryBadge, stepsBadge);
        footer.setSpacing(true);

        card.add(header, progressBar, footer);

        card.getElement().addEventListener("click", ev -> {
            this.selectedContent = null;
            if (centerPanel != null) {
                centerPanel.removeAll();
                centerPanel.add(buildWorkflowDetailView());
            }
            if (rightSidebar != null) {
                rightSidebar.removeAll();
                rightSidebar.add(buildWorkflowSummary());
            }
            refreshView();
        });

        return card;
    }

    private VerticalLayout buildThreadListItems() {
        VerticalLayout list = new VerticalLayout();
        list.setSpacing(true);
        list.setPadding(false);

        list.add(createThreadListItem("Diferencias OAuth2 vs JWT", "OAuth2 es un protocolo de autorización...", "Security", 4, true));
        list.add(createThreadListItem("Implementar patrón Saga", "El patrón Saga es una forma de gestionar...", "Architecture", 5, false));
        
        return list;
    }
    
    private Component createThreadListItem(String title, String snippet, String category, int turns, boolean isActive) {
        Div card = new Div();
        card.setWidthFull();
        card.getStyle()
            .set("background-color", isActive ? COLOR_BACKGROUND_ACTIVE_GREEN : "#FFFFFF")
            .set("border-radius", "12px")
            .set("padding", "12px 16px")
            .set("border", "1px solid " + (isActive ? COLOR_BORDER_ACTIVE_GREEN : "#E2E8F0"))
            .set("box-shadow", "0 1px 3px rgba(0,0,0,0.05)");
        
        Icon chatIcon = VaadinIcon.CHAT.create();
        chatIcon.setColor(COLOR_PRIMARY_GREEN);
        
        H5 titleLabel = new H5(title);
        titleLabel.getStyle().set("margin", "0");
        
        HorizontalLayout header = new HorizontalLayout(chatIcon, titleLabel);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        
        Paragraph snippetP = new Paragraph(snippet);
        snippetP.getStyle().set("font-size", "var(--lumo-font-size-s)").set("color", COLOR_TEXT_SECONDARY);
        
        Span categoryBadge = createBadge(category);
        Span turnsBadge = new Span(turns + " turnos");
        turnsBadge.getElement().getThemeList().add("badge contrast");
        
        HorizontalLayout footer = new HorizontalLayout(categoryBadge, turnsBadge);
        
        card.add(header, snippetP, footer);
        return card;
    }

    private Component buildWorkflowDetailView() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        if (selectedContent == null) {
            layout.add(new H2("No hay workflows seleccionados"), new Paragraph("Selecciona un documento en la columna izquierda para ver y editar su workflow asociado."));
            return layout;
        }

        String titleText = selectedContent.getTitulo() != null && !selectedContent.getTitulo().isBlank() ? selectedContent.getTitulo() : "Sin título";
        String descriptionText = selectedContent.getTextoPlano() != null && !selectedContent.getTextoPlano().isBlank() ? excerpt(selectedContent.getTextoPlano(), 400) : "No hay descripción disponible.";
        String category = selectedContent.getTipoContenido() != null ? selectedContent.getTipoContenido() : "General";
        String dateText = selectedContent.getCreatedAt() != null ? selectedContent.getCreatedAt().toString() : "Fecha desconocida";
        int progress = computeProgressForContent(selectedContent);
        int stepsCount = 4; // default

        // Header
        HorizontalLayout badges = new HorizontalLayout(createBadge(category), createBadge(progress == 100 ? "HECHO" : "EN PROGRESO"));

        H2 title = new H2(titleText);
        Paragraph description = new Paragraph(descriptionText);

        Span date = new Span(dateText);
        Span steps = new Span("0/" + stepsCount + " pasos");
        HorizontalLayout metadata = new HorizontalLayout(date, steps);
        metadata.setAlignItems(FlexComponent.Alignment.CENTER);

        Button edit = new Button(new Icon(VaadinIcon.EDIT));
        Button download = new Button(new Icon(VaadinIcon.DOWNLOAD));
        Button menu = new Button(new Icon(VaadinIcon.ELLIPSIS_DOTS_V));
        HorizontalLayout actions = new HorizontalLayout(edit, download, menu);

        HorizontalLayout headerRow = new HorizontalLayout(new VerticalLayout(badges, title, description, metadata), actions);
        headerRow.setWidthFull();
        headerRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        ProgressBar progressBar = new ProgressBar(0, 100, progress);

        layout.add(headerRow, progressBar, buildWorkflowStepsFromContent(selectedContent), createNotesEditor());

        return layout;
    }
    
    private Component createWorkflowSteps() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        
        layout.add(new H3("PASOS DEL WORKFLOW"));
        layout.add(createWorkflowStep("Crear instancia y configurar red en OCI", "Configuración inicial de la VCN y las subredes.", "HECHO", new String[]{"OCI Architecture Guide"}));
        layout.add(createWorkflowStep("Instalar y configurar Keycloak", "Desplegar Keycloak en una VM y crear el realm y los clientes.", "ACTIVO", new String[]{"Keycloak Docs"}));
        layout.add(createWorkflowStep("Crear proyecto Spring Boot 3", "Inicializar con Spring Initializr, añadir dependencias web y de seguridad.", "PENDIENTE", null));
        
        Button addStep = new Button("Añadir paso al workflow", new Icon(VaadinIcon.PLUS));
        addStep.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        addStep.getStyle().set("border-style", "dashed");
        layout.add(addStep);
        
        return layout;
    }
    
    private Component createWorkflowStep(String title, String description, String status, String[] resources) {
        boolean isActive = "ACTIVO".equals(status);
        boolean isDone = "HECHO".equals(status);

        Div card = new Div();
        card.getStyle()
            .set("background-color", isActive ? COLOR_BACKGROUND_ACTIVE_GREEN : "#FFFFFF")
            .set("border-radius", "12px")
            .set("padding", "16px")
            .set("border", "1px solid " + (isActive ? COLOR_PRIMARY_GREEN : "#E2E8F0"));

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon statusIcon;
        if (isDone) {
            statusIcon = VaadinIcon.CHECK_CIRCLE.create();
            statusIcon.setColor("var(--lumo-success-color)");
        } else if (isActive) {
            statusIcon = VaadinIcon.DOT_CIRCLE.create();
            statusIcon.setColor(COLOR_PRIMARY_GREEN);
        } else {
            statusIcon = VaadinIcon.CIRCLE_THIN.create();
            statusIcon.setColor(COLOR_TEXT_SECONDARY);
        }

        Span titleSpan = new Span(title);
        titleSpan.getStyle().set("font-weight", "600");

        header.add(statusIcon, titleSpan, new Span()); // Empty span for spacer
        header.expand(header.getComponentAt(2));
        header.add(createBadge(status));
        
        Paragraph desc = new Paragraph(description);
        desc.getStyle().set("color", COLOR_TEXT_SECONDARY);

        card.add(header, desc);

        if(resources != null && resources.length > 0) {
            HorizontalLayout resourceLayout = new HorizontalLayout();
            for(String resource : resources) {
                resourceLayout.add(createTag(resource));
            }
            card.add(resourceLayout);
        }
        
        return card;
    }
    
    private Component createNotesEditor() {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(false);
        card.setSpacing(false);
        card.getStyle().set("border-radius", "12px").set("border", "1px solid #E2E8F0");

        H3 title = new H3("NOTAS PERSONALES");
        title.getStyle().set("padding", "16px 16px 0 16px");

        TextArea notes = new TextArea();
        notes.setWidthFull();
        notes.setHeight("150px");
        notes.setPlaceholder("Escribe notas sobre este workflow...");
        notes.getStyle().set("background-color", "#FFFFFF").set("border", "none");

        // Load existing temporary note if present
        String existing = tempNotes.getOrDefault(selectedContent != null ? selectedContent.getId() : null, "");
        notes.setValue(existing);

        Span charCount = new Span(String.valueOf(notes.getValue().length()) + "/1000");

        notes.addValueChangeListener(e -> {
            int len = e.getValue() == null ? 0 : e.getValue().length();
            charCount.setText(len + "/1000");
        });

        Button save = new Button("Guardar nota");
        save.getStyle().set("background-color", COLOR_PRIMARY_GREEN).set("color", "white");
        save.addClickListener(ev -> {
            java.util.UUID key = selectedContent != null ? selectedContent.getId() : null;
            tempNotes.put(key, notes.getValue());
            com.vaadin.flow.component.notification.Notification.show("Nota guardada temporalmente.", 2000, com.vaadin.flow.component.notification.Notification.Position.BOTTOM_END);
            // Refresh detail and summary panels to show the note as a workflow step
            if (centerPanel != null) {
                centerPanel.removeAll();
                centerPanel.add(buildWorkflowDetailView());
            }
            if (rightSidebar != null) {
                rightSidebar.removeAll();
                rightSidebar.add(buildWorkflowSummary());
            }
        });

        Button clear = new Button("Borrar");
        clear.addClickListener(ev -> {
            java.util.UUID key = selectedContent != null ? selectedContent.getId() : null;
            tempNotes.remove(key);
            notes.clear();
            charCount.setText("0/1000");
            com.vaadin.flow.component.notification.Notification.show("Nota borrada.", 1500, com.vaadin.flow.component.notification.Notification.Position.BOTTOM_END);
            // Refresh views
            if (centerPanel != null) {
                centerPanel.removeAll();
                centerPanel.add(buildWorkflowDetailView());
            }
            if (rightSidebar != null) {
                rightSidebar.removeAll();
                rightSidebar.add(buildWorkflowSummary());
            }
        });

        HorizontalLayout footer = new HorizontalLayout(charCount, clear, save);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footer.getStyle()
            .set("background-color", "#F8FAFC")
            .set("padding", "8px 16px")
            .set("border-top", "1px solid #E2E8F0");

        card.add(title, notes, footer);
        return card;
    }

    private Component buildThreadDetailView() { /* Omitted for brevity */ return new Span("Thread Detail View");}
    private Component createMessageBubble(boolean isUser, String text) { /* Omitted for brevity */ return new Span(text);}
    private Component createLinkedResource(String title) { /* Omitted for brevity */ return new Span(title);}

    // Build workflow steps from the content text (simple heuristic)
    private Component buildWorkflowStepsFromContent(Content content) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.add(new H3("PASOS DEL WORKFLOW"));

        String text = content != null && content.getTextoPlano() != null ? content.getTextoPlano() : "";
        String[] sentences = text.split("(?<=[.!?])\\s+");
        int maxSteps = Math.min(5, Math.max(1, sentences.length));
        for (int i = 0; i < maxSteps; i++) {
            String s = sentences[i].trim();
            if (s.length() > 140) s = s.substring(0, 140) + "...";
            layout.add(createWorkflowStep("Paso " + (i + 1), s, i == 0 ? "ACTIVO" : "PENDIENTE", null));
        }

        // If there is a temporary note for this content, include it as the final step
        String note = tempNotes.get(content != null ? content.getId() : null);
        if (note != null && !note.isBlank()) {
            String noteText = note.trim();
            if (noteText.length() > 240) noteText = noteText.substring(0, 240) + "...";
            layout.add(createWorkflowStep("Nota del usuario", noteText, "ACTIVO", null));
        }

        Button addStep = new Button("Añadir paso al workflow", new Icon(VaadinIcon.PLUS));
        addStep.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        addStep.getStyle().set("border-style", "dashed");
        layout.add(addStep);

        return layout;
    }

    private void loadUserContents() {
        try {
            java.util.Optional<User> maybeUser = java.util.Optional.ofNullable(userSession.getAuthenticatedUser());
            if (maybeUser.isPresent()) {
                userContents = supabaseService.getContentsForUser(maybeUser.get().getId());
            } else {
                userContents = new java.util.ArrayList<>();
            }
        } catch (Exception e) {
            userContents = new java.util.ArrayList<>();
        }
    }

    private int computeProgressForContent(Content content) {
        if (content == null) return 0;
        String estado = content.getEstadoProcesamiento();
        if (estado != null && (estado.equalsIgnoreCase("processed") || estado.toLowerCase().contains("proces"))) {
            return 100;
        }
        String text = content.getTextoPlano() != null ? content.getTextoPlano() : "";
        int len = Math.min(2000, text.length());
        return Math.min(95, Math.max(5, (int) ((len / 2000.0) * 100)));
    }

    private String excerpt(String text, int maxChars) {
        if (text == null || text.isBlank()) return "";
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxChars) return normalized;
        int idx = normalized.lastIndexOf('.', maxChars);
        if (idx == -1 || idx < maxChars / 2) {
            idx = normalized.lastIndexOf(' ', maxChars);
            if (idx == -1) idx = maxChars;
        }
        return normalized.substring(0, idx).trim() + "...";
    }

    private Component buildWorkflowSummary() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        layout.add(new H4("Resumen"));
        layout.add(createMetric("Total workflows", "3", VaadinIcon.ARCHIVES));
        layout.add(createMetric("Completados", "1", VaadinIcon.CHECK_CIRCLE_O));
        layout.add(createMetric("En progreso", "2", VaadinIcon.CLOCK));
        
        layout.add(new H4("Categorías"));
        layout.add(new Span("Backend: 2"));
        layout.add(new Span("DevOps: 1"));
        
        Button askAI = new Button("Preguntar a la IA", new Icon(VaadinIcon.QUESTION));
        askAI.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        askAI.getStyle().set("background-color", COLOR_PRIMARY_GREEN);
        
        Button viewResources = new Button("Ver recursos vinculados", new Icon(VaadinIcon.LIST));
        viewResources.getStyle().set("background-color", "#F1F5F9").set("color", COLOR_TEXT_DARK);
        
        Button export = new Button("Exportar como Markdown", new Icon(VaadinIcon.DOWNLOAD));
        export.getStyle().set("background-color", "#F1F5F9").set("color", COLOR_TEXT_DARK);
        
        layout.add(new Hr(), askAI, viewResources, export);
        return layout;
    }

    private Component createMetric(String label, String value, VaadinIcon icon) {
        Icon i = icon.create();
        i.getStyle().set("color", COLOR_TEXT_SECONDARY);
        Div iconWrapper = new Div(i);
        iconWrapper.getStyle()
            .set("background-color", "#F1F5F9")
            .set("border-radius", "50%")
            .set("width", "32px")
            .set("height", "32px")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center");

        Span labelSpan = new Span(label);
        Span valueSpan = new Span(value);
        valueSpan.getStyle().set("font-weight", "600");
        VerticalLayout text = new VerticalLayout(labelSpan, valueSpan);
        text.setSpacing(false);
        text.setPadding(false);

        HorizontalLayout metric = new HorizontalLayout(iconWrapper, text);
        metric.setAlignItems(FlexComponent.Alignment.CENTER);
        return metric;
    }

    private Component buildThreadSummary() { /* Omitted for brevity */ return new Span("Thread Summary View"); }

    private Span createBadge(String text) {
        Span badge = new Span(text);
        badge.getElement().getThemeList().add("badge");
        
        switch (text.toUpperCase()) {
            case "BACKEND":
                badge.getStyle().set("background-color", "#F3E8FF").set("color", "#7E22CE");
                break;
            case "DEVOPS":
                badge.getStyle().set("background-color", "#E0F2FE").set("color", "#0369A1");
                break;
            case "SECURITY":
                 badge.getStyle().set("background-color", "#FEE2E2").set("color", "#B91C1C");
                 break;
            case "EN PROGRESO":
            case "ACTIVO":
                badge.getStyle().set("background-color", "#FEF3C7").set("color", "#D97706");
                break;
            case "HECHO":
                badge.getStyle().set("background-color", "#D1FAE5").set("color", "#047857");
                break;
            default:
                badge.getElement().getThemeList().add("badge contrast");
        }
        return badge;
    }

    private Span createTag(String text) {
        Span tag = new Span(text);
        tag.getElement().getThemeList().add("badge contrast");
        return tag;
    }
}
