package com.application.Views.Library;

import com.application.model.Contenido;
import com.application.model.User;
import com.application.service.ContenidoService;
import com.application.service.UserSession;
import com.application.Views.Layout.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEnterEvent;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@PageTitle("Library - KnowBase")
@Route(value = "library", layout = MainLayout.class)
public class LibraryView extends VerticalLayout implements BeforeEnterObserver {

    private final ContenidoService contenidoService;
    private final UserSession userSession;
    private Div cardsGrid; // Made a field to allow dynamic updates

    public LibraryView(ContenidoService contenidoService, UserSession userSession) {
        this.contenidoService = contenidoService;
        this.userSession = userSession;

        setWidthFull();
        setMinHeight("100vh");
        setPadding(true);
        setSpacing(true);
        getStyle()
                .set("background-color", "#f4f6fa")
                .set("font-family", "'Inter', sans-serif")
                .set("color", "#1e293b");

        // 1. Header principal (Knowledge Repository + Search/Icons)
        add(createHeader());

        // 2. Barra de acciones (Filtrar, AI Summary All, Toggle Grid/List)
        add(createActionBar());

        // Initialize cardsGrid here, but populate it in beforeEnter
        cardsGrid = new Div();
        cardsGrid.setWidthFull();
        cardsGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fill, minmax(480px, 1fr))")
                .set("gap", "16px");
        add(cardsGrid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        loadDataForAuthenticatedUser();
    }

    private void loadDataForAuthenticatedUser() {
        Optional<User> maybeUser = Optional.ofNullable(userSession.getAuthenticatedUser());

        if (maybeUser.isEmpty()) {
            cardsGrid.removeAll();
            cardsGrid.add(new Span("No hay una sesión activa. Inicia sesión para ver tus contenidos."));
            addExampleCards();
            return;
        }

        UUID userId = maybeUser.get().getId();

        getUI().ifPresent(ui -> ui.access(() -> {
            List<Contenido> contents = contenidoService.listarPorUsuario(userId);
            cardsGrid.removeAll();

            if (contents == null || contents.isEmpty()) {
                cardsGrid.add(new Span("No se encontraron registros o contenidos para este usuario."));
                addExampleCards();
                return;
            }

            contents.forEach(content -> {
                String timeAgo = formatTimeAgo(content.getFechaCreacion());
                String description = content.getTexto() != null && !content.getTexto().isBlank()
                        ? content.getTexto()
                        : "Archivo adjunto: " + content.getStoragePath();

                // La categoría real la pone el clasificador de IA; si aún no está disponible,
                // caemos al tipo de contenido (texto_plano, pdf, etc.) como antes.
                String category = content.getCategoria() != null ? content.getCategoria()
                        : (content.getTipoContenido() != null ? content.getTipoContenido() : "General");
                String color = getColorForCategory(category);

                boolean isVerified = false;
                boolean aiReady = content.getCategoria() != null;

                List<String> tags = content.getPalabrasClave() != null && !content.getPalabrasClave().isEmpty()
                        ? content.getPalabrasClave()
                        : Collections.singletonList(category);

                cardsGrid.add(createCard(category, color, isVerified, timeAgo,
                        content.getTitulo() != null ? content.getTitulo() : "Sin título",
                        description,
                        tags,
                        content.getTipoContenido(),
                        aiReady));
            });
        }));
    }

    private void addExampleCards() {
        // Mocks según la imagen
        cardsGrid.add(createCard("DevOps", "#0284c7", true, "Dec 12, 2024",
                "Kubernetes Best Practices",
                "Comprehensive guide on scaling production clusters, managing persistent volumes, and optimizing node affinity for high-availability workloads.",
                List.of("Kubernetes", "Scaling", "Tuning"), "Article", true));

        cardsGrid.add(createCard("Backend", "#8a2be2", false, "Dec 8, 2024",
                "Deep Learning with Python",
                "Advanced architectures for neural networks using PyTorch. Covers transformation layers, attention mechanisms, and fine-tuning LLMs.",
                List.of("Python", "PyTorch"), "Course", true));

        cardsGrid.add(createCard("Cloud", "#d97706", true, "Dec 5, 2024",
                "OCI Architecture Guide",
                "Mastering Oracle Cloud Infrastructure components: networking, Compartments, and identity access management for enterprise scale.",
                List.of("OCI", "Security"), "Documentation", true));

        cardsGrid.add(createCard("Frontend", "#059669", false, "Dec 1, 2024",
                "Advanced React Patterns",
                "Compound components, Render Props, and Custom Hooks. Sharing state with memo/selector and concurrent inside features.",
                List.of("React", "Hooks"), "Documentation", true));

        cardsGrid.add(createCard("Backend", "#8a2be2", false, "Nov 28, 2024",
                "Kafka Streams Real-time",
                "Architecture patterns for event-driven systems. Windowing, stateful processing, and fault tolerance in streaming applications.",
                List.of("Kafka", "Streaming", "Java"), "Article", true));

        cardsGrid.add(createCard("DevOps", "#0284c7", true, "Nov 25, 2024",
                "Terraform Modules Library",
                "Reusable infrastructure-as-code patterns: best practices for module composition, provider versions, and state security.",
                List.of("Terraform", "IaC"), "Article", true));
    }

    private String formatTimeAgo(OffsetDateTime createdAt) {
        if (createdAt == null) return "Fecha desconocida";
        Duration duration = Duration.between(createdAt.toInstant(), OffsetDateTime.now(ZoneId.systemDefault()).toInstant());
        long seconds = duration.getSeconds();
        if (seconds < 60) return "justo ahora";
        long minutes = seconds / 60;
        if (minutes < 60) return "hace " + minutes + "m";
        long hours = minutes / 60;
        if (hours < 24) return "hace " + hours + "h";
        long days = hours / 24;
        return "hace " + days + "d";
    }

    // Helper to get color based on category (can be expanded)
    private String getColorForCategory(String category) {
        return switch (category.toLowerCase()) {
            case "devops" -> "#0284c7";
            case "backend" -> "#8a2be2";
            case "cloud" -> "#d97706";
            case "frontend" -> "#059669";
            case "article" -> "#8b5cf6"; // Assuming article for general content
            case "markdown" -> "#8a2be2";
            case "word" -> "#059669";
            case "pdf" -> "#dc2626";
            case "texto_plano" -> "#8b5cf6";
            default -> "#64748b"; // Default grey
        };
    }

    // ==========================================
    // 1. CABECERA
    // ==========================================
    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);
        header.getStyle().set("margin-bottom", "5px");

        // Título y Subtítulo
        VerticalLayout titleLayout = new VerticalLayout();
        titleLayout.setPadding(false);
        titleLayout.setSpacing(false);

        H2 title = new H2("Knowledge Repository");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "22px")
                .set("font-weight", "700");

        Span subtitle = new Span("Biblioteca estructurada de conocimiento técnico");
        subtitle.getStyle()
                .set("color", "#64748b")
                .set("font-size", "13px");

        titleLayout.add(title, subtitle);

        // Lado Derecho: Search input + Iconos
        HorizontalLayout controls = new HorizontalLayout();
        controls.setAlignItems(Alignment.CENTER);
        controls.setSpacing(true);

        TextField searchField = new TextField();
        searchField.setPlaceholder("Search across technical rep");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setSuffixComponent(new Span("⌘K"));
        searchField.setWidth("300px");
        searchField.getStyle().set("border-radius", "20px");

        Icon bellIcon = VaadinIcon.BELL_O.create();
        bellIcon.getStyle()
                .set("color", "#64748b")
                .set("cursor", "pointer")
                .set("padding", "6px");

        Icon cogIcon = VaadinIcon.COG_O.create();
        cogIcon.getStyle()
                .set("color", "#64748b")
                .set("cursor", "pointer")
                .set("padding", "6px");

        controls.add(searchField, bellIcon, cogIcon);
        header.add(titleLayout, controls);

        return header;
    }

    // ==========================================
    // 2. BARRA DE ACCIONES (Filtros y Vistas)
    // ==========================================
    private HorizontalLayout createActionBar() {
        HorizontalLayout actionBar = new HorizontalLayout();
        actionBar.setWidthFull();
        actionBar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        actionBar.setAlignItems(Alignment.CENTER);
        actionBar.getStyle().set("margin-bottom", "15px");

        // Izquierda: Filtrar + AI Summary All
        HorizontalLayout leftActions = new HorizontalLayout();
        leftActions.setAlignItems(Alignment.CENTER);

       Button filterBtn = new Button("Filtrar", VaadinIcon.FILTER.create());
// En lugar de LUMO_OUTLINED, aplicamos el borde directamente vía inline styles:
filterBtn.getStyle()
        .set("border", "1px solid #cbd5e1")
        .set("border-radius", "20px")
        .set("color", "#334155")
        .set("background-color", "#ffffff");

        Button aiAllBtn = new Button("AI Summary All", VaadinIcon.MAGIC.create());
        aiAllBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        aiAllBtn.getStyle()
                .set("background-color", "#00b894")
                .set("color", "#ffffff")
                .set("border-radius", "20px")
                .set("font-weight", "600");

        leftActions.add(filterBtn, aiAllBtn);

        // Derecha: Toggle Grid/List
        HorizontalLayout rightActions = new HorizontalLayout();
        rightActions.setSpacing(false);

        Button gridBtn = new Button(VaadinIcon.GRID.create());
        gridBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ICON);
        gridBtn.getStyle()
                .set("border-radius", "8px 0 0 8px")
                .set("background-color", "#00b894");

        Button listBtn = new Button(VaadinIcon.LINES.create());
        listBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        listBtn.getStyle()
                .set("border-radius", "0 8px 8px 0")
                .set("background-color", "#e2e8f0")
                .set("color", "#64748b");

        rightActions.add(gridBtn, listBtn);
        actionBar.add(leftActions, rightActions);

        return actionBar;
    }

    // ==========================================
    // 3. GRID DE TARJETAS DE CONOCIMIENTO
    // ==========================================
    private Component createCardsGrid() {
        // This method now initializes the cardsGrid field. Data population is in beforeEnter.
        return cardsGrid;
    }

    // ==========================================
    // FABRICA DE TARJETAS
    // ==========================================
    private VerticalLayout createCard(String category, String color, boolean isVerified, String date,
                                      String title, String description, List<String> tags,
                                      String contentType, boolean aiReady) {
        VerticalLayout card = new VerticalLayout();
        card.setWidthFull();
        card.setPadding(true);
        card.setSpacing(true);
        card.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        card.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "16px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.03)")
                .set("border", "1px solid #f1f5f9");

        // --- Card Header ---
        HorizontalLayout top = new HorizontalLayout();
        top.setWidthFull();
        top.setJustifyContentMode(JustifyContentMode.BETWEEN);
        top.setAlignItems(Alignment.CENTER);

        // Category Tag
        Span catPill = new Span("• " + category);
        catPill.getStyle()
                .set("background-color", color + "1A") // 10% opacity
                .set("color", color)
                .set("font-size", "11px")
                .set("font-weight", "700")
                .set("padding", "3px 10px")
                .set("border-radius", "12px");

        HorizontalLayout metaRight = new HorizontalLayout();
        metaRight.setAlignItems(Alignment.CENTER);
        metaRight.setSpacing(true);

        if (isVerified) {
            Span verified = new Span("✓ Verified");
            verified.getStyle()
                    .set("background-color", "#d1fae5")
                    .set("color", "#059669")
                    .set("font-size", "10px")
                    .set("font-weight", "700")
                    .set("padding", "2px 8px")
                    .set("border-radius", "10px");
            metaRight.add(verified);
        }

        Span dateSpan = new Span(date);
        dateSpan.getStyle().set("color", "#94a3b8").set("font-size", "11px");
        metaRight.add(dateSpan);

        top.add(catPill, metaRight);

        // --- Card Body ---
        VerticalLayout body = new VerticalLayout();
        body.setPadding(false);
        body.setSpacing(false);

        H4 titleHeader = new H4(title);
        titleHeader.getStyle()
                .set("margin", "8px 0 4px 0")
                .set("font-size", "15px")
                .set("font-weight", "700");

        Paragraph descP = new Paragraph(description);
        descP.getStyle()
                .set("color", "#64748b")
                .set("font-size", "12px")
                .set("margin", "0")
                .set("line-height", "1.4");

        body.add(titleHeader, descP);

        // --- Card Tags ---
        FlexLayout tagsLayout = new FlexLayout();
        tagsLayout.getStyle().set("gap", "6px").set("flex-wrap", "wrap").set("margin-top", "8px");

        tags.forEach(tag -> {
            Span tagSpan = new Span(tag);
            tagSpan.getStyle()
                    .set("background-color", "#f1f5f9")
                    .set("color", "#475569")
                    .set("font-size", "11px")
                    .set("padding", "2px 8px")
                    .set("border-radius", "6px");
            tagsLayout.add(tagSpan);
        });

        // --- Card Footer ---
        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setJustifyContentMode(JustifyContentMode.BETWEEN);
        footer.setAlignItems(Alignment.CENTER);
        footer.getStyle().set("margin-top", "10px");

        // AI Status
        HorizontalLayout aiStatus = new HorizontalLayout();
        aiStatus.setAlignItems(Alignment.CENTER);
        aiStatus.setSpacing(true);

        if (aiReady) {
            Icon magicIcon = VaadinIcon.MAGIC.create();
            magicIcon.setSize("12px");
            magicIcon.setColor("#00b894");

            Span aiText = new Span("AI Summary Ready");
            aiText.getStyle()
                    .set("color", "#00b894")
                    .set("font-size", "11px")
                    .set("font-weight", "600");

            aiStatus.add(magicIcon, aiText);
        }

        // Type
        HorizontalLayout typeLayout = new HorizontalLayout();
        typeLayout.setAlignItems(Alignment.CENTER);

        Icon typeIcon = switch (contentType.toLowerCase()) {
            case "course" -> VaadinIcon.ACADEMY_CAP.create();
            case "documentation" -> VaadinIcon.BOOK.create();
            default -> VaadinIcon.FILE_TEXT.create();
        };
        typeIcon.setSize("12px");
        typeIcon.setColor("#94a3b8");

        Span typeText = new Span(contentType);
        typeText.getStyle().set("color", "#94a3b8").set("font-size", "11px");

        typeLayout.add(typeIcon, typeText);

        footer.add(aiStatus, typeLayout);

        card.add(top, body, tagsLayout, footer);
        return card;
    }
}