package com.application.Views.Library;

import com.application.model.Content;
import com.application.model.User;
import com.application.service.SupabaseService;
import com.application.service.UserSession;
import com.application.Views.Layout.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.component.textfield.TextField;
import com.application.Views.Library.NativeRangeInput;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEnterEvent;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@PageTitle("Library - KnowBase")
@Route(value = "library", layout = MainLayout.class)
public class LibraryView extends VerticalLayout implements BeforeEnterObserver {

    private final SupabaseService supabaseService;
    private final UserSession userSession;
    private Div cardsGrid;
    private boolean gridView = true;

    private List<Content> allContents = new ArrayList<>();
    private Set<String> activeCategories = new HashSet<>();
    private Set<String> activeContentTypes = new HashSet<>();
    private String activeDateRange = "Siempre";
    private boolean verifiedOnly = false;
    private int minAiRelevance = 0;
    private String searchQuery = "";
    private FlexLayout activeFiltersBar;
    private int currentPage = 1;
    private int pageSize = 12;
    private HorizontalLayout paginationLayout;
    private TextField searchField;



    public LibraryView(SupabaseService supabaseService, UserSession userSession) {
        this.supabaseService = supabaseService;
        this.userSession = userSession;

        setWidthFull();
        // Fill parent so internal scrolling works via the layout chain
        setSizeFull();
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

        activeFiltersBar = new FlexLayout();
        activeFiltersBar.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        activeFiltersBar.getStyle().set("gap", "8px");
        add(activeFiltersBar);
        updateActiveFiltersBar();


        // Initialize cardsGrid here, but populate it in beforeEnter
        cardsGrid = new Div();
        cardsGrid.setWidthFull();
        // Make the grid/list container flexible and scrollable inside the content area
        cardsGrid.getStyle()
                .set("display", "flex")
                .set("flex-direction", "row")
                .set("flex", "1")
                .set("min-height", "0")
                .set("flex-wrap", "wrap")
                .set("gap", "1rem")
                .set("align-content", "flex-start")
                .set("overflow-y", "auto");
        applyCardsLayout(gridView);
        add(cardsGrid);


        paginationLayout = new HorizontalLayout();
        paginationLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        paginationLayout.setWidthFull();
        paginationLayout.setAlignItems(Alignment.CENTER);
        add(paginationLayout);
    }

    private void applyCardsLayout(boolean useGrid) {
        gridView = useGrid;

        if (useGrid) {
            cardsGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(280px, 1fr))")
                .set("gap", "1.25rem")
                .set("width", "100%")
                .set("padding", "1rem 0")
                .set("flex", "1")
                .set("min-height", "0");
        } else {
            cardsGrid.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("grid-template-columns", "1fr")
                .set("gap", "12px")
                .set("width", "100%")
                .set("padding", "0")
                .set("flex", "1")
                .set("min-height", "0")
                .set("overflow-y", "auto");
        }

        cardsGrid.getChildren().forEach(component -> {
            if (component instanceof VerticalLayout card) {
                card.setWidthFull();
                card.getStyle().set("min-width", "0");
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        loadDataForAuthenticatedUser();
    }

    private void loadDataForAuthenticatedUser() {
        Optional<User> maybeUser = Optional.ofNullable(userSession.getAuthenticatedUser());

        if (maybeUser.isEmpty()) {
            allContents.clear();
            applyFilters();
            cardsGrid.add(new Span("No hay una sesión activa. Inicia sesión para ver tus contenidos."));
            addExampleCards();
            return;
        }

        UUID userId = maybeUser.get().getId();
        allContents = supabaseService.getContentsForUser(userId);
        applyFilters();
    }

    private void updateViewToggleStyles(Button gridBtn, Button listBtn, boolean gridActive) {
        gridBtn.getStyle()
                .set("background-color", gridActive ? "#00b894" : "#e2e8f0")
                .set("color", gridActive ? "#ffffff" : "#64748b");

        listBtn.getStyle()
                .set("background-color", !gridActive ? "#00b894" : "#e2e8f0")
                .set("color", !gridActive ? "#ffffff" : "#64748b");
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

        searchField = new TextField();
        searchField.setPlaceholder("Search across technical rep");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setSuffixComponent(new Span("⌘K"));
        searchField.setWidth("300px");
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(event -> {
            searchQuery = event.getValue() != null ? event.getValue().trim() : "";
            currentPage = 1;
            applyFilters();
        });
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

        filterBtn.addClickListener(e -> createFilterDialog().open());

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
                .set("background-color", "#00b894")
                .set("color", "#ffffff");

        Button listBtn = new Button(VaadinIcon.LINES.create());
        listBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        listBtn.getStyle()
                .set("border-radius", "0 8px 8px 0")
                .set("background-color", "#e2e8f0")
                .set("color", "#64748b");

        gridBtn.addClickListener(event -> {
            applyCardsLayout(true);
            updateViewToggleStyles(gridBtn, listBtn, true);
        });

        listBtn.addClickListener(event -> {
            applyCardsLayout(false);
            updateViewToggleStyles(gridBtn, listBtn, false);
        });

        updateViewToggleStyles(gridBtn, listBtn, gridView);
        rightActions.add(gridBtn, listBtn);
        actionBar.add(leftActions, rightActions);

        return actionBar;
    }

    private void updateActiveFiltersBar() {
        activeFiltersBar.removeAll();
        boolean hasFilters = !searchQuery.isBlank() || !activeCategories.isEmpty() || !activeContentTypes.isEmpty()
                || !activeDateRange.equals("Siempre") || verifiedOnly || minAiRelevance > 0;

        if (hasFilters) {
            if (!searchQuery.isBlank()) {
                activeFiltersBar.add(createFilterChip("Search: " + searchQuery, () -> {
                    searchQuery = "";
                    if (searchField != null && !searchField.isEmpty()) {
                        searchField.clear();
                    }
                    currentPage = 1;
                    applyFilters();
                }));
            }

            activeCategories.forEach(category -> {
                activeFiltersBar.add(createFilterChip("Category: " + category, () -> {
                    activeCategories.remove(category);
                    currentPage = 1;
                    applyFilters();
                }));
            });

            activeContentTypes.forEach(type -> {
                activeFiltersBar.add(createFilterChip("Type: " + type, () -> {
                    activeContentTypes.remove(type);
                    currentPage = 1;
                    applyFilters();
                }));
            });

            if (!activeDateRange.equals("Siempre")) {
                activeFiltersBar.add(createFilterChip("Date: " + activeDateRange, () -> {
                    activeDateRange = "Siempre";
                    currentPage = 1;
                    applyFilters();
                }));
            }

            if (verifiedOnly) {
                activeFiltersBar.add(createFilterChip("Verified Only", () -> {
                    verifiedOnly = false;
                    currentPage = 1;
                    applyFilters();
                }));
            }

            if (minAiRelevance > 0) {
                activeFiltersBar.add(createFilterChip("AI >= " + minAiRelevance + "%", () -> {
                    minAiRelevance = 0;
                    currentPage = 1;
                    applyFilters();
                }));
            }

            Button clearAllBtn = new Button("Clear all", VaadinIcon.CLOSE_SMALL.create(), e -> clearFilters());
            clearAllBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            activeFiltersBar.add(clearAllBtn);
        }
    }

    private Component createFilterChip(String label, Runnable onRemove) {
        Span chip = new Span(label);
        chip.getStyle()
                .set("background-color", "#e2e8f0")
                .set("color", "#475569")
                .set("border-radius", "16px")
                .set("padding", "4px 8px")
                .set("font-size", "12px");

        Button removeBtn = new Button(VaadinIcon.CLOSE_SMALL.create(), e -> onRemove.run());
        removeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ICON);
        removeBtn.getStyle().set("margin-left", "4px");

        HorizontalLayout layout = new HorizontalLayout(chip, removeBtn);
        layout.setAlignItems(Alignment.CENTER);
        return layout;
    }

    private void applyFilters() {
        cardsGrid.removeAll();
        List<Content> filteredContents = allContents.stream()
                .filter(this::matchesFilters)
                .toList();

        int totalPages = (int) Math.ceil((double) filteredContents.size() / pageSize);
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }
        if (currentPage < 1) {
            currentPage = 1;
        }

        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, filteredContents.size());
        List<Content> pageContents = filteredContents.subList(start, end);

        if (pageContents.isEmpty()) {
            cardsGrid.add(createEmptyState());
        } else {
            pageContents.forEach(content -> {
                String timeAgo = formatTimeAgo(content.getCreatedAt());
                String description;
                if (content.getTextoPlano() != null && !content.getTextoPlano().isBlank()) {
                    description = excerpt(content.getTextoPlano(), 220);
                } else {
                    description = "Archivo adjunto: " + content.getStoragePath();
                }

                String category = content.getTipoContenido() != null ? content.getTipoContenido() : "General";
                String color = getColorForCategory(category);

                boolean isVerified = false;
                boolean aiReady = true;

                cardsGrid.add(createCard(content, category, color, isVerified, timeAgo,
                        content.getTitulo() != null ? content.getTitulo() : "Sin título",
                        description,
                        Collections.singletonList(category),
                        content.getTipoContenido(),
                        aiReady));
            });
        }

        updateActiveFiltersBar();
        updatePagination(totalPages, filteredContents.size());
    }

    private Component createEmptyState() {
        VerticalLayout emptyState = new VerticalLayout();
        emptyState.setWidthFull();
        emptyState.setAlignItems(Alignment.CENTER);
        emptyState.setJustifyContentMode(JustifyContentMode.CENTER);
        emptyState.getStyle()
                .set("min-height", "240px")
                .set("color", "#64748b")
                .set("grid-column", "1 / -1");

        Icon icon = VaadinIcon.SEARCH.create();
        icon.setSize("32px");
        icon.setColor("#94a3b8");

        Span message = new Span("No se encontró ningún resultado.");
        message.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "600")
                .set("color", "#334155");

        Span hint = new Span(searchQuery.isBlank()
                ? "Prueba ajustando los filtros seleccionados."
                : "Revisa el texto de búsqueda o intenta con otro título.");
        hint.getStyle().set("font-size", "13px");

        emptyState.add(icon, message, hint);
        return emptyState;
    }

    private void updatePagination(int totalPages, int totalItems) {
        paginationLayout.removeAll();

        if (totalPages <= 1) {
            return;
        }

        Button prev = new Button("Previous", e -> {
            if (currentPage > 1) {
                currentPage--;
                applyFilters();
            }
        });
        prev.setEnabled(currentPage > 1);

        Button next = new Button("Next", e -> {
            if (currentPage < totalPages) {
                currentPage++;
                applyFilters();
            }
        });
        next.setEnabled(currentPage < totalPages);

        Span pageInfo = new Span("Page " + currentPage + " of " + totalPages + " (" + totalItems + " items)");
        pageInfo.getStyle().set("margin", "0 1rem");

        paginationLayout.add(prev, pageInfo, next);
    }

    private boolean matchesFilters(Content content) {
        boolean searchMatch = matchesSearch(content);
        boolean categoryMatch = activeCategories.isEmpty() || activeCategories.stream()
                .anyMatch(category -> matchesCategory(content, category));
        boolean contentTypeMatch = activeContentTypes.isEmpty() || activeContentTypes.stream()
                .anyMatch(type -> matchesContentType(content.getTipoContenido(), type));
        boolean dateMatch = isWithinDateRange(content);
        boolean verifiedMatch = !verifiedOnly || isVerified(content);
        boolean relevanceMatch = calculateAiRelevance(content) >= minAiRelevance;
        return searchMatch && categoryMatch && contentTypeMatch && dateMatch && verifiedMatch && relevanceMatch;
    }

    private boolean matchesSearch(Content content) {
        return searchQuery.isBlank() || containsNormalized(content.getTitulo(), searchQuery);
    }

    private boolean matchesCategory(Content content, String category) {
        String haystack = normalized(contentSearchText(content));
        String selectedCategory = normalized(category);
        if (haystack.contains(selectedCategory)) {
            return true;
        }
        return Arrays.stream(selectedCategory.split("\\s+"))
                .filter(word -> word.length() > 2)
                .anyMatch(haystack::contains);
    }

    private boolean matchesContentType(String contentType, String selectedType) {
        String normalizedContentType = normalized(contentType);
        String normalizedSelectedType = normalized(selectedType);
        if (normalizedContentType.isBlank() || normalizedSelectedType.isBlank()) {
            return false;
        }
        return normalizedContentType.equals(normalizedSelectedType)
                || normalizedContentType.contains(normalizedSelectedType)
                || normalizedSelectedType.contains(normalizedContentType);
    }

    private String contentSearchText(Content content) {
        return String.join(" ",
                safe(content.getTitulo()),
                safe(content.getTipoContenido()),
                safe(content.getTextoPlano()),
                safe(content.getStoragePath()));
    }

    private boolean containsNormalized(String source, String query) {
        return normalized(source).contains(normalized(query));
    }

    private String normalized(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase()
                .replace("_", " ")
                .replace("-", " ")
                .trim();
    }

    private String safe(String value) {
        return value != null ? value : "";
    }

    private boolean isVerified(Content content) {
        String status = normalized(content.getEstadoProcesamiento());
        return status.contains("complet")
                || status.contains("procesad")
                || status.contains("processed")
                || status.contains("complete")
                || status.contains("success");
    }

    private int calculateAiRelevance(Content content) {
        int score = 25;
        if (content.getTextoPlano() != null && !content.getTextoPlano().isBlank()) {
            score += 45;
        }
        if (isVerified(content)) {
            score += 20;
        }
        if (content.getTitulo() != null && !content.getTitulo().isBlank()) {
            score += 10;
        }
        return Math.min(score, 100);
    }

    private boolean isWithinDateRange(Content content) {
        if (content.getCreatedAt() == null) {
            return true;
        }
        OffsetDateTime now = OffsetDateTime.now(ZoneId.systemDefault());
        return switch (activeDateRange) {
            case "12 horas" -> content.getCreatedAt().isAfter(now.minusHours(12));
            case "3 dias" -> content.getCreatedAt().isAfter(now.minusDays(3));
            case "ultima semana" -> content.getCreatedAt().isAfter(now.minusWeeks(1));
            case "ultimo mes" -> content.getCreatedAt().isAfter(now.minusMonths(1));
            case "Siempre" -> true;
            default -> true;
        };
    }

    // Short excerpt helper for descriptions
    private String excerpt(String text, int maxChars) {
        if (text == null || text.isBlank()) return "";
        // Normalize whitespace and remove extra newlines
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxChars) return normalized;
        // Try to cut at sentence boundary before maxChars
        int idx = normalized.lastIndexOf('.', maxChars);
        if (idx == -1 || idx < maxChars / 2) {
            // fallback: cut at last space before maxChars
            idx = normalized.lastIndexOf(' ', maxChars);
            if (idx == -1) idx = maxChars;
        }
        return normalized.substring(0, idx).trim() + "...";
    }

    private void clearFilters() {
        activeCategories.clear();
        activeContentTypes.clear();
        activeDateRange = "Siempre";
        verifiedOnly = false;
        minAiRelevance = 0;
        searchQuery = "";
        if (searchField != null && !searchField.isEmpty()) {
            searchField.clear();
        }
        currentPage = 1;
        applyFilters();
    }

    private Dialog createFilterDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Filtrar Contenido");
        dialog.setWidth("800px");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(true);

        Span resultsCount = new Span();
        updateResultsCount(resultsCount);

        HorizontalLayout filtersLayout = new HorizontalLayout();
        filtersLayout.setWidthFull();
        filtersLayout.setSpacing(true);

        VerticalLayout leftColumn = new VerticalLayout();
        leftColumn.setPadding(false);
        leftColumn.setSpacing(true);

        List<String> categories = Arrays.asList("Backend", "Frontend", "Cloud Computing", "Databases", "Data Analysis", "Cybersecurity", "Artificial Intelligence", "software architecture", "Q/A");
        FlexLayout categoriesLayout = new FlexLayout();
        categoriesLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        categoriesLayout.getStyle().set("gap", "8px");

        Map<String, Button> categoryButtons = new HashMap<>();
        for (String category : categories) {
            Button toggle = createToggleButton(category, getColorForCategory(category));
            if (activeCategories.contains(category)) {
                toggle.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            }
            toggle.addClickListener(e -> {
                if (activeCategories.contains(category)) {
                    activeCategories.remove(category);
                    toggle.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
                } else {
                    activeCategories.add(category);
                    toggle.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                }
                updateResultsCount(resultsCount);
            });
            categoriesLayout.add(toggle);
            categoryButtons.put(category, toggle);
        }
        leftColumn.add(createFilterSection("Categorías", categoriesLayout));

        List<String> contentTypes = Arrays.asList("texto", "pdf", "word", "excel", "url", "video", "articulo");
        FlexLayout typesLayout = new FlexLayout();
        typesLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        typesLayout.getStyle().set("gap", "8px");

        Map<String, Button> contentTypeButtons = new HashMap<>();
        for (String type : contentTypes) {
            Button toggle = createToggleButton(type, "#64748b");
            if (activeContentTypes.contains(type)) {
                toggle.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            }
            toggle.addClickListener(e -> {
                if (activeContentTypes.contains(type)) {
                    activeContentTypes.remove(type);
                    toggle.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
                } else {
                    activeContentTypes.add(type);
                    toggle.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                }
                updateResultsCount(resultsCount);
            });
            typesLayout.add(toggle);
            contentTypeButtons.put(type, toggle);
        }
        leftColumn.add(createFilterSection("Tipo de Contenido", typesLayout));

        VerticalLayout rightColumn = new VerticalLayout();
        rightColumn.setPadding(false);
        rightColumn.setSpacing(true);

        RadioButtonGroup<String> dateRange = new RadioButtonGroup<>();
        dateRange.setItems("12 horas", "3 dias", "ultima semana", "ultimo mes", "Siempre");
        dateRange.setValue(activeDateRange);
        dateRange.addValueChangeListener(e -> {
            activeDateRange = e.getValue();
            updateResultsCount(resultsCount);
        });
        rightColumn.add(createFilterSection("Rango de Fechas", dateRange));

        NativeRangeInput relevanceSlider = new NativeRangeInput(0, 100, minAiRelevance);
        relevanceSlider.getStyle().set("width", "100%"); // Set width using Component's style
        relevanceSlider.addValueChangeListener(source -> {
            minAiRelevance = source.getValue();
            updateResultsCount(resultsCount);
        });
        rightColumn.add(createFilterSection("Relevancia IA", relevanceSlider));

        Checkbox verifiedCheckbox = new Checkbox("Mostrar solo verificados");
        verifiedCheckbox.setValue(verifiedOnly);
        verifiedCheckbox.addValueChangeListener(e -> {
            verifiedOnly = e.getValue();
            updateResultsCount(resultsCount);
        });
        rightColumn.add(createFilterSection("Verificación", verifiedCheckbox));

        filtersLayout.add(leftColumn, rightColumn);

        HorizontalLayout bottomBar = new HorizontalLayout(resultsCount);
        bottomBar.setAlignItems(Alignment.CENTER);

        Button clearButton = new Button("Limpiar Filtros", e -> {
            clearFilters();
            
            categoryButtons.values().forEach(b -> b.removeThemeVariants(ButtonVariant.LUMO_PRIMARY));
            contentTypeButtons.values().forEach(b -> b.removeThemeVariants(ButtonVariant.LUMO_PRIMARY));
            dateRange.setValue("Siempre");
            relevanceSlider.setValue(0);
            verifiedCheckbox.setValue(false);
            
            updateResultsCount(resultsCount);
        });
        clearButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button applyButton = new Button("Aplicar Filtros", e -> {
            currentPage = 1;
            applyFilters();
            dialog.close();
        });
        applyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.getFooter().add(clearButton, applyButton);

        mainLayout.add(filtersLayout, bottomBar);
        dialog.add(mainLayout);

        return dialog;
    }

    private void updateResultsCount(Span resultsCount) {
        long count = allContents.stream()
                .filter(this::matchesFilters)
                .count();
        resultsCount.setText(count + " resultados");
    }


    private VerticalLayout createFilterSection(String title, com.vaadin.flow.component.Component... components) {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        section.getStyle()
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "8px")
                .set("padding", "12px");

        H5 header = new H5(title);
        header.getStyle()
                .set("margin", "0 0 8px 0")
                .set("font-size", "14px");

        section.add(header);
        section.add(components);
        return section;
    }

    private Button createToggleButton(String text, String color) {
        Button button = new Button(text);
        button.getStyle()
                .set("border-radius", "16px")
                .set("border", "1px solid " + color)
                .set("color", color);
        return button;
    }



    // ==========================================
    // FABRICA DE TARJETAS
    // ==========================================
    private VerticalLayout createCard(String category, String color, boolean isVerified, String date,
                                      String title, String description, List<String> tags,
                                      String contentType, boolean aiReady) {
        return createCard(null, category, color, isVerified, date, title, description, tags, contentType, aiReady);
    }

    private VerticalLayout createCard(Content content, String category, String color, boolean isVerified, String date,
                                      String title, String description, List<String> tags,
                                      String contentType, boolean aiReady) {
        VerticalLayout card = new VerticalLayout();
        card.setWidthFull();
        card.setPadding(false);
        card.setSpacing(false);
        card.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        card.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "14px")
                .set("box-shadow", "0 2px 6px rgba(0,0,0,0.03)")
                .set("border", "1px solid #f1f5f9")
                .set("height", "240px") // Use fixed height
                .set("padding", "16px")
                .set("box-sizing", "border-box")
                .set("display", "flex") // Use flexbox
                .set("flex-direction", "column"); // Arrange items vertically

        // --- Card Header ---
        HorizontalLayout top = new HorizontalLayout();
        top.setWidthFull();
        top.setJustifyContentMode(JustifyContentMode.BETWEEN);
        top.setAlignItems(Alignment.CENTER);
        top.getStyle().set("flex-shrink", "0"); // Prevent header from shrinking

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
        body.getStyle().set("flex-grow", "1").set("overflow", "hidden"); // Allow body to grow and hide overflow

        H4 titleHeader = new H4(title);
        titleHeader.getStyle()
                .set("margin", "8px 0 4px 0")
                .set("font-size", "15px")
                .set("font-weight", "700");

        Paragraph descP = new Paragraph(description);
        descP.getElement().getStyle()
                .set("color", "#64748b")
                .set("font-size", "12px")
                .set("margin", "0")
                .set("line-height", "1.4")
                .set("display", "-webkit-box")
                .set("webkit-box-orient", "vertical")
                .set("webkit-line-clamp", "3")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis");

        body.add(titleHeader, descP);

        // --- Card Tags ---
        FlexLayout tagsLayout = new FlexLayout();
        tagsLayout.getStyle()
                .set("gap", "6px")
                .set("flex-wrap", "wrap")
                .set("margin-top", "8px")
                .set("flex-shrink", "0"); // Prevent tags from shrinking

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
        FlexLayout footer = new FlexLayout();
        footer.setWidthFull();
        footer.setJustifyContentMode(JustifyContentMode.BETWEEN);
        footer.setAlignItems(Alignment.CENTER);
        footer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        footer.getStyle()
                .set("margin-top", "10px")
                .set("gap", "1rem")
                .set("flex-shrink", "0"); // Prevent footer from shrinking

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

        String safeContentType = contentType != null ? contentType : "General";
        Icon typeIcon = switch (safeContentType.toLowerCase()) {
            case "course" -> VaadinIcon.ACADEMY_CAP.create();
            case "documentation" -> VaadinIcon.BOOK.create();
            default -> VaadinIcon.FILE_TEXT.create();
        };
        typeIcon.setSize("12px");
        typeIcon.setColor("#94a3b8");

        Span typeText = new Span(safeContentType);
        typeText.getStyle().set("color", "#94a3b8").set("font-size", "11px");

        typeLayout.add(typeIcon, typeText);

        HorizontalLayout actionLayout = new HorizontalLayout();
        actionLayout.setAlignItems(Alignment.CENTER);
        actionLayout.setSpacing(true);

        if (content != null) {
            Button deleteButton = new Button("Eliminar", VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            deleteButton.getStyle()
                    .set("border-radius", "8px")
                    .set("font-weight", "600");
            deleteButton.addClickListener(event -> confirmDeleteContent(content));
            actionLayout.add(deleteButton);
        }

        HorizontalLayout rightItems = new HorizontalLayout(typeLayout, actionLayout);
        rightItems.setAlignItems(Alignment.CENTER);
        rightItems.setSpacing(true);
        
        footer.add(aiStatus, rightItems);

        card.add(top, body, tagsLayout, footer);
        return card;
    }

    private void confirmDeleteContent(Content content) {
        if (content == null || content.getId() == null) {
            Notification.show("No se puede borrar este documento porque no tiene un identificador válido.", 3000, Notification.Position.BOTTOM_END);
            return;
        }

        String title = content.getTitulo() != null && !content.getTitulo().isBlank()
                ? content.getTitulo()
                : "este documento";

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Eliminar documento");

        Paragraph message = new Paragraph("¿Quieres borrar \"" + title + "\"? Esta acción eliminará el archivo del almacenamiento y el registro de la biblioteca.");
        message.getStyle().set("margin", "0");

        Button cancelButton = new Button("Cancelar", e -> dialog.close());
        Button confirmButton = new Button("Eliminar", e -> {
            dialog.close();
            deleteContent(content);
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout actions = new HorizontalLayout(cancelButton, confirmButton);
        actions.setJustifyContentMode(JustifyContentMode.END);
        actions.setWidthFull();

        dialog.add(message, actions);
        dialog.open();
    }

    private void deleteContent(Content content) {
        try {
            supabaseService.deleteContentForUser(content.getId(), content.getStoragePath());
            Notification.show("Documento eliminado correctamente.", 3000, Notification.Position.BOTTOM_END);
            loadDataForAuthenticatedUser();
        } catch (Exception e) {
            Notification.show("No se pudo eliminar el documento: " + e.getMessage(), 5000, Notification.Position.BOTTOM_END);
        }
    }
}
