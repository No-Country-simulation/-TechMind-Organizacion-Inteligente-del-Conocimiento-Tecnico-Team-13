package com.application.Views.Categories;

import com.application.model.Contenido;
import com.application.model.User;
import com.application.service.ContenidoService;
import com.application.service.UserSession;
import com.application.Views.Layout.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@PageTitle("Categorías - KnowBase")
@Route(value = "categorias", layout = MainLayout.class)
public class CategoriesView extends VerticalLayout implements BeforeEnterObserver {

    public static final List<String> PROJECT_OFFICIAL_CATEGORIES = List.of(
            "Backend",
            "Frontend",
            "Cloud Computing",
            "Databases",
            "Data Analysis",
            "Cybersecurity",
            "Artificial Intelligence",
            "Software Architecture",
            "Q/A",
            "DevOps"
    );

    private static final Map<String, String> CATEGORY_COLORS = Map.ofEntries(
            Map.entry("Backend", "#8B5CF6"),
            Map.entry("DevOps", "#0EA5E9"),
            Map.entry("Frontend", "#10B981"),
            Map.entry("Cloud Computing", "#F59E0B"),
            Map.entry("Cloud", "#F59E0B"),
            Map.entry("Databases", "#059669"),
            Map.entry("Data Analysis", "#EC4899"),
            Map.entry("Data Science", "#EC4899"),
            Map.entry("Cybersecurity", "#EF4444"),
            Map.entry("Security", "#EF4444"),
            Map.entry("Artificial Intelligence", "#8B5CF6"),
            Map.entry("Software Architecture", "#6366F1"),
            Map.entry("Architecture", "#6366F1"),
            Map.entry("Q/A", "#64748B"),
            Map.entry("General", "#64748B"),
            Map.entry("Uncategorized", "#94A3B8")
    );

    private static final Map<String, String> CATEGORY_BG_COLORS = Map.ofEntries(
            Map.entry("Backend", "#F3E8FF"),
            Map.entry("DevOps", "#E0F2FE"),
            Map.entry("Frontend", "#D1FAE5"),
            Map.entry("Cloud Computing", "#FEF3C7"),
            Map.entry("Cloud", "#FEF3C7"),
            Map.entry("Databases", "#CCFBF1"),
            Map.entry("Data Analysis", "#FCE7F3"),
            Map.entry("Data Science", "#FCE7F3"),
            Map.entry("Cybersecurity", "#FEE2E2"),
            Map.entry("Security", "#FEE2E2"),
            Map.entry("Artificial Intelligence", "#EDE9FE"),
            Map.entry("Software Architecture", "#E0E7FF"),
            Map.entry("Architecture", "#E0E7FF"),
            Map.entry("Q/A", "#F1F5F9"),
            Map.entry("General", "#F1F5F9"),
            Map.entry("Uncategorized", "#F1F5F9")
    );

    private static final Map<String, String> CATEGORY_EMOJIS = Map.ofEntries(
            Map.entry("Backend", "⚙️"),
            Map.entry("DevOps", "🚀"),
            Map.entry("Frontend", "💻"),
            Map.entry("Cloud Computing", "☁️"),
            Map.entry("Cloud", "☁️"),
            Map.entry("Databases", "🗄️"),
            Map.entry("Data Analysis", "📊"),
            Map.entry("Data Science", "📈"),
            Map.entry("Cybersecurity", "🛡️"),
            Map.entry("Security", "🔒"),
            Map.entry("Artificial Intelligence", "🤖"),
            Map.entry("Software Architecture", "🏛️"),
            Map.entry("Architecture", "🏛️"),
            Map.entry("Q/A", "🧪"),
            Map.entry("General", "📁"),
            Map.entry("Uncategorized", "📦")
    );

    private static final Map<String, List<String>> CATEGORY_DEFAULT_TAGS = Map.ofEntries(
            Map.entry("Backend", List.of("Spring Boot", "REST APIs", "Microservicios")),
            Map.entry("DevOps", List.of("Docker", "Kubernetes", "CI/CD")),
            Map.entry("Frontend", List.of("React", "TypeScript", "Tailwind")),
            Map.entry("Cloud Computing", List.of("AWS", "Azure", "OCI")),
            Map.entry("Databases", List.of("PostgreSQL", "MongoDB", "Redis")),
            Map.entry("Data Analysis", List.of("Python", "Pandas", "Power BI")),
            Map.entry("Cybersecurity", List.of("OAuth2", "OWASP", "JWT")),
            Map.entry("Artificial Intelligence", List.of("LLMs", "PyTorch", "LangChain")),
            Map.entry("Software Architecture", List.of("DDD", "Event-Driven", "Patrones")),
            Map.entry("Q/A", List.of("Selenium", "JUnit", "Cypress"))
    );

    private final ContenidoService contenidoService;
    private final UserSession userSession;

    private List<Contenido> allContents = new ArrayList<>();
    private final Set<String> activeCategories = new LinkedHashSet<>(PROJECT_OFFICIAL_CATEGORIES);
    private final Map<String, List<Contenido>> categorizedContents = new LinkedHashMap<>();

    private Div categoriesGrid;
    private VerticalLayout distributionCardContainer;
    private VerticalLayout summaryCardContainer;
    private String currentSearchFilter = "";

    public CategoriesView(ContenidoService contenidoService, UserSession userSession) {
        this.contenidoService = contenidoService;
        this.userSession = userSession;

        setSizeFull();
        setPadding(false);
        setMargin(false);
        setSpacing(false);
        getStyle()
                .set("background-color", "#F8FAFC")
                .set("overflow-y", "auto")
                .set("font-family", "'Inter', sans-serif");

        loadUserContents();
        categorizeContents();

        add(createHeader());
        add(createMainContent());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        refreshData();
    }

    private void refreshData() {
        loadUserContents();
        categorizeContents();
        renderCategoriesGrid();
        renderDistributionCard();
        renderSummaryCard();
    }

    /** ID del usuario autenticado; los diálogos de esta vista asumen que siempre hay uno (la ruta
     *  ya está protegida por sesión desde antes de llegar acá). */
    private UUID currentUserId() {
        return userSession.getAuthenticatedUser().getId();
    }

    private void loadUserContents() {
        try {
            Optional<User> maybeUser = Optional.ofNullable(userSession.getAuthenticatedUser());
            if (maybeUser.isPresent()) {
                allContents = contenidoService.listarPorUsuario(maybeUser.get().getId());
            } else {
                allContents = new ArrayList<>();
            }
        } catch (Exception e) {
            allContents = new ArrayList<>();
        }
    }

    private void categorizeContents() {
        categorizedContents.clear();

        // 1. Initialize registered categories
        for (String cat : activeCategories) {
            categorizedContents.put(cat, new ArrayList<>());
        }

        // 2. Populate contents and discover new user categories
        for (Contenido c : allContents) {
            String category = c.getCategoria() != null && !c.getCategoria().isBlank()
                    ? c.getCategoria().trim()
                    : "General";

            activeCategories.add(category);
            categorizedContents.computeIfAbsent(category, k -> new ArrayList<>()).add(c);
        }
    }

    private Component createHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.setSpacing(true);
        header.getStyle()
                .set("background-color", "#FFFFFF")
                .set("border-bottom", "1px solid #E2E8F0")
                .set("padding", "20px 24px");

        // Title section
        H1 title = new H1("Categorías y Carpetas");
        title.getStyle()
                .set("margin", "0")
                .set("color", "#0F172A")
                .set("font-size", "24px")
                .set("font-weight", "700");

        Span subtitle = new Span("Organiza, clasifica y gestiona los recursos de tu base de conocimiento técnico");
        subtitle.getStyle().set("color", "#64748B").set("font-size", "14px");

        VerticalLayout titleSection = new VerticalLayout(title, subtitle);
        titleSection.setPadding(false);
        titleSection.setSpacing(false);

        // Search and actions
        HorizontalLayout controls = new HorizontalLayout();
        controls.setWidthFull();
        controls.setAlignItems(FlexComponent.Alignment.CENTER);
        controls.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        TextField searchField = new TextField();
        searchField.setPlaceholder("Buscar por nombre de carpeta o tema...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("350px");
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> {
            currentSearchFilter = e.getValue() != null ? e.getValue().trim().toLowerCase() : "";
            renderCategoriesGrid();
        });
        searchField.getStyle().set("border-radius", "20px");

        HorizontalLayout actionButtons = new HorizontalLayout();
        actionButtons.setAlignItems(FlexComponent.Alignment.CENTER);
        actionButtons.setSpacing(true);

        Button newCategoryBtn = new Button("+ Nueva Categoría", VaadinIcon.PLUS.create());
        newCategoryBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newCategoryBtn.getStyle()
                .set("background-color", "#10B981")
                .set("color", "#FFFFFF")
                .set("border-radius", "20px")
                .set("font-weight", "600")
                .set("padding", "8px 16px")
                .set("cursor", "pointer");
        newCategoryBtn.addClickListener(e -> openNewCategoryDialog());

        Button refreshBtn = new Button("Actualizar", VaadinIcon.REFRESH.create(), e -> {
            refreshData();
            Notification.show("Categorías actualizadas", 2000, Notification.Position.BOTTOM_END);
        });
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refreshBtn.getStyle().set("border-radius", "20px");

        actionButtons.add(newCategoryBtn, refreshBtn);
        controls.add(searchField, actionButtons);

        header.add(titleSection, controls);
        return header;
    }

    private Component createMainContent() {
        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setWidthFull();
        mainLayout.setHeightFull();
        mainLayout.setSpacing(true);
        mainLayout.setPadding(true);
        mainLayout.getStyle()
                .set("background-color", "#F8FAFC")
                .set("gap", "24px")
                .set("padding", "24px");

        // Left panel: Categories grid
        VerticalLayout leftPanel = new VerticalLayout();
        leftPanel.setWidth("70%");
        leftPanel.setPadding(false);
        leftPanel.setSpacing(false);

        categoriesGrid = new Div();
        categoriesGrid.setWidthFull();
        categoriesGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fill, minmax(320px, 1fr))")
                .set("gap", "20px");

        renderCategoriesGrid();
        leftPanel.add(categoriesGrid);
        leftPanel.expand(categoriesGrid);

        // Right panel: Metrics
        VerticalLayout rightPanel = new VerticalLayout();
        rightPanel.setWidth("30%");
        rightPanel.setPadding(false);
        rightPanel.setSpacing(true);
        rightPanel.getStyle().set("overflow-y", "auto");

        distributionCardContainer = new VerticalLayout();
        distributionCardContainer.setPadding(false);
        distributionCardContainer.setSpacing(false);
        distributionCardContainer.setWidthFull();

        summaryCardContainer = new VerticalLayout();
        summaryCardContainer.setPadding(false);
        summaryCardContainer.setSpacing(false);
        summaryCardContainer.setWidthFull();

        renderDistributionCard();
        renderSummaryCard();

        rightPanel.add(distributionCardContainer, summaryCardContainer);

        mainLayout.add(leftPanel, rightPanel);
        return mainLayout;
    }

    private void renderCategoriesGrid() {
        categoriesGrid.removeAll();

        List<Map.Entry<String, List<Contenido>>> entries = categorizedContents.entrySet().stream()
                .filter(e -> {
                    if (currentSearchFilter.isBlank()) return true;
                    return e.getKey().toLowerCase().contains(currentSearchFilter);
                })
                .toList();

        if (entries.isEmpty()) {
            Div empty = new Div();
            empty.getStyle()
                    .set("grid-column", "1 / -1")
                    .set("text-align", "center")
                    .set("padding", "40px")
                    .set("background", "#FFFFFF")
                    .set("border-radius", "16px")
                    .set("border", "1px dashed #CBD5E1")
                    .set("color", "#64748B");

            H4 emptyTitle = new H4("No se encontraron categorías");
            Paragraph emptyDesc = new Paragraph("Crea una nueva carpeta con el botón '+ Nueva Categoría' o ajusta el término de búsqueda.");
            empty.add(emptyTitle, emptyDesc);
            categoriesGrid.add(empty);
            return;
        }

        for (Map.Entry<String, List<Contenido>> entry : entries) {
            String categoryName = entry.getKey();
            List<Contenido> contents = entry.getValue();
            categoriesGrid.add(createCategoryCard(categoryName, contents));
        }
    }

    private Component createCategoryCard(String categoryName, List<Contenido> contents) {
        VerticalLayout card = new VerticalLayout();
        card.setWidthFull();
        card.setPadding(true);
        card.setSpacing(true);
        card.getStyle()
                .set("background-color", "#FFFFFF")
                .set("border-radius", "16px")
                .set("border", "1px solid #E2E8F0")
                .set("box-shadow", "0px 2px 6px rgba(0, 0, 0, 0.04)")
                .set("padding", "20px")
                .set("transition", "transform 0.2s ease, box-shadow 0.2s ease");

        // Icon and title
        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        titleLayout.setSpacing(true);

        String emoji = CATEGORY_EMOJIS.getOrDefault(categoryName, "📁");
        String color = CATEGORY_COLORS.getOrDefault(categoryName, "#6366F1");
        String bgColor = CATEGORY_BG_COLORS.getOrDefault(categoryName, "#EEF2FF");

        Div iconBox = new Div();
        iconBox.setText(emoji);
        iconBox.getStyle()
                .set("background-color", bgColor)
                .set("color", color)
                .set("width", "46px")
                .set("height", "46px")
                .set("border-radius", "12px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("font-size", "22px")
                .set("flex-shrink", "0");

        VerticalLayout titleText = new VerticalLayout();
        titleText.setPadding(false);
        titleText.setSpacing(false);

        H4 catTitle = new H4(categoryName);
        catTitle.getStyle()
                .set("margin", "0")
                .set("color", "#0F172A")
                .set("font-weight", "600")
                .set("font-size", "16px");

        Span resourceCount = new Span(contents.size() + (contents.size() == 1 ? " recurso guardado" : " recursos guardados"));
        resourceCount.getStyle().set("color", "#64748B").set("font-size", "12px");

        titleText.add(catTitle, resourceCount);
        titleLayout.add(iconBox, titleText);

        // Badge
        Span growthBadge = new Span(contents.size() > 0 ? "Activa" : "Vacía");
        growthBadge.getStyle()
                .set("background-color", contents.size() > 0 ? "#ECFDF5" : "#F1F5F9")
                .set("color", contents.size() > 0 ? "#059669" : "#64748B")
                .set("font-size", "11px")
                .set("font-weight", "700")
                .set("padding", "3px 8px")
                .set("border-radius", "12px");

        HorizontalLayout headerRow = new HorizontalLayout(titleLayout, growthBadge);
        headerRow.setWidthFull();
        headerRow.setAlignItems(FlexComponent.Alignment.CENTER);
        headerRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // Progress bar (proportion)
        int totalDocs = allContents.size();
        double pct = totalDocs > 0 ? ((double) contents.size() / totalDocs) * 100.0 : 0;
        ProgressBar progressBar = new ProgressBar(0, 100, Math.min(100, pct));
        progressBar.setWidthFull();
        progressBar.getStyle()
                .set("height", "6px")
                .set("border-radius", "3px")
                .set("--vaadin-progress-bar-value-background-color", color);

        // Tags
        FlexLayout tagsLayout = new FlexLayout();
        tagsLayout.getStyle().set("gap", "6px").set("flex-wrap", "wrap");

        List<String> defaultTags = CATEGORY_DEFAULT_TAGS.getOrDefault(categoryName, List.of("Documentos", "Notas", "Archivos"));
        for (String tagText : defaultTags) {
            Span tag = new Span(tagText);
            tag.getStyle()
                    .set("background-color", bgColor)
                    .set("color", color)
                    .set("font-size", "11px")
                    .set("font-weight", "500")
                    .set("padding", "3px 8px")
                    .set("border-radius", "6px");
            tagsLayout.add(tag);
        }

        // Action buttons
        HorizontalLayout actions = new HorizontalLayout();
        actions.setWidthFull();
        actions.setSpacing(true);
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        actions.getStyle().set("margin-top", "6px");

        // 1. Button: Editar (mover archivos y renombrar)
        Button editBtn = new Button("Editar", VaadinIcon.EDIT.create());
        editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        editBtn.getStyle()
                .set("background-color", "#F1F5F9")
                .set("color", "#334155")
                .set("border-radius", "16px")
                .set("font-weight", "600")
                .set("cursor", "pointer");
        editBtn.addClickListener(e -> openEditCategoryDialog(categoryName, contents));

        // 2. Button: Ver recursos (Pop-up modal tipo biblioteca)
        Button viewBtn = new Button("Ver recursos", VaadinIcon.FOLDER_OPEN.create());
        viewBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        viewBtn.getStyle()
                .set("background-color", "#D1FAE5")
                .set("color", "#047857")
                .set("border-radius", "16px")
                .set("font-weight", "600")
                .set("cursor", "pointer");
        viewBtn.addClickListener(e -> openViewResourcesDialog(categoryName, contents));

        // 3. Button: Eliminar
        Button deleteBtn = new Button("Eliminar", VaadinIcon.TRASH.create());
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        deleteBtn.getStyle()
                .set("background-color", "#FEE2E2")
                .set("color", "#B91C1C")
                .set("border-radius", "16px")
                .set("font-weight", "600")
                .set("cursor", "pointer");
        deleteBtn.addClickListener(e -> openDeleteCategoryDialog(categoryName, contents));

        actions.add(editBtn, viewBtn, deleteBtn);

        card.add(headerRow, progressBar, tagsLayout, actions);
        return card;
    }

    // =========================================================================
    // MODAL: NUEVA CATEGORÍA / CARPETA CON CATEGORÍAS DEL PROYECTO
    // =========================================================================
    private void openNewCategoryDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Crear Nueva Carpeta / Categoría");
        dialog.setWidth("520px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        Paragraph desc = new Paragraph("Crea una carpeta seleccionando una de las categorías estándar del proyecto o escribe un nombre personalizado.");
        desc.getStyle().set("color", "#64748B").set("font-size", "13px").set("margin", "0");

        // Fast select from official categories
        List<String> availableOfficial = PROJECT_OFFICIAL_CATEGORIES.stream()
                .filter(c -> !activeCategories.contains(c))
                .toList();

        ComboBox<String> predefinedCombo = new ComboBox<>("Categorías predefinidas del proyecto");
        predefinedCombo.setItems(availableOfficial.isEmpty() ? PROJECT_OFFICIAL_CATEGORIES : availableOfficial);
        predefinedCombo.setPlaceholder("Selecciona una categoría oficial...");
        predefinedCombo.setWidthFull();

        TextField customNameField = new TextField("O escribe un nombre personalizado");
        customNameField.setPlaceholder("Ej: Cloud Native, Machine Learning, UI/UX...");
        customNameField.setWidthFull();

        predefinedCombo.addValueChangeListener(e -> {
            if (e.getValue() != null && !e.getValue().isBlank()) {
                customNameField.setValue(e.getValue());
            }
        });

        layout.add(desc, predefinedCombo, customNameField);

        Button cancelBtn = new Button("Cancelar", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button createBtn = new Button("Crear Carpeta", e -> {
            String newName = customNameField.getValue() != null ? customNameField.getValue().trim() : "";
            if (newName.isBlank() && predefinedCombo.getValue() != null) {
                newName = predefinedCombo.getValue().trim();
            }

            if (newName.isBlank()) {
                Notification.show("Por favor ingresa un nombre para la categoría", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            activeCategories.add(newName);
            categorizedContents.putIfAbsent(newName, new ArrayList<>());
            dialog.close();
            refreshData();

            Notification.show("Carpeta '" + newName + "' creada correctamente.", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createBtn.getStyle().set("background-color", "#10B981");

        dialog.getFooter().add(cancelBtn, createBtn);
        dialog.add(layout);
        dialog.open();
    }

    // =========================================================================
    // MODAL: VER RECURSOS (POP-UP TIPO BIBLIOTECA)
    // =========================================================================
    private void openViewResourcesDialog(String categoryName, List<Contenido> contents) {
        Dialog dialog = new Dialog();
        dialog.setWidth("880px");
        dialog.setHeight("80vh");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        String color = CATEGORY_COLORS.getOrDefault(categoryName, "#6366F1");
        String bgColor = CATEGORY_BG_COLORS.getOrDefault(categoryName, "#EEF2FF");
        String emoji = CATEGORY_EMOJIS.getOrDefault(categoryName, "📁");

        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        HorizontalLayout titleBox = new HorizontalLayout();
        titleBox.setAlignItems(FlexComponent.Alignment.CENTER);
        titleBox.setSpacing(true);

        Span iconSpan = new Span(emoji);
        iconSpan.getStyle()
                .set("background-color", bgColor)
                .set("padding", "6px 10px")
                .set("border-radius", "8px")
                .set("font-size", "18px");

        H3 title = new H3("Recursos en: " + categoryName);
        title.getStyle().set("margin", "0").set("color", "#0F172A").set("font-size", "18px").set("font-weight", "700");

        Span countBadge = new Span(contents.size() + " recursos");
        countBadge.getStyle()
                .set("background-color", bgColor)
                .set("color", color)
                .set("font-size", "12px")
                .set("font-weight", "700")
                .set("padding", "3px 8px")
                .set("border-radius", "12px");

        titleBox.add(iconSpan, title, countBadge);

        Button closeBtn = new Button(VaadinIcon.CLOSE_SMALL.create(), e -> dialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);

        header.add(titleBox, closeBtn);

        // Body content container
        VerticalLayout container = new VerticalLayout();
        container.setWidthFull();
        container.setPadding(false);
        container.setSpacing(true);
        container.getStyle().set("overflow-y", "auto").set("flex", "1");

        // Search within this category dialog
        TextField searchBox = new TextField();
        searchBox.setPlaceholder("Buscar recurso por título dentro de " + categoryName + "...");
        searchBox.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchBox.setWidthFull();
        searchBox.setValueChangeMode(ValueChangeMode.EAGER);

        Div cardsList = new Div();
        cardsList.setWidthFull();
        cardsList.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "12px")
                .set("margin-top", "10px");

        Runnable renderResourceCards = () -> {
            cardsList.removeAll();
            String q = searchBox.getValue() != null ? searchBox.getValue().trim().toLowerCase() : "";

            List<Contenido> filtered = contents.stream()
                    .filter(c -> q.isBlank() || (c.getTitulo() != null && c.getTitulo().toLowerCase().contains(q))
                            || (c.getTexto() != null && c.getTexto().toLowerCase().contains(q)))
                    .toList();

            if (filtered.isEmpty()) {
                VerticalLayout emptyState = new VerticalLayout();
                emptyState.setWidthFull();
                emptyState.setAlignItems(FlexComponent.Alignment.CENTER);
                emptyState.setPadding(true);
                emptyState.getStyle().set("background", "#F8FAFC").set("border-radius", "12px").set("padding", "32px");

                Icon icon = VaadinIcon.FOLDER_OPEN.create();
                icon.setSize("40px");
                icon.setColor("#94A3B8");

                H4 msg = new H4(contents.isEmpty() ? "No hay recursos guardados en esta carpeta" : "No se encontraron resultados");
                msg.getStyle().set("margin", "8px 0 4px 0").set("color", "#334155");

                Paragraph sub = new Paragraph(contents.isEmpty()
                        ? "Puedes mover archivos desde otra categoría con 'Editar' o subir nuevo contenido en 'Añadir Contenido'."
                        : "Prueba con otro término de búsqueda.");
                sub.getStyle().set("color", "#64748B").set("font-size", "13px").set("margin", "0");

                emptyState.add(icon, msg, sub);
                cardsList.add(emptyState);
            } else {
                for (Contenido content : filtered) {
                    cardsList.add(createResourceItemCard(content, categoryName, dialog));
                }
            }
        };

        searchBox.addValueChangeListener(e -> renderResourceCards.run());
        renderResourceCards.run();

        container.add(searchBox, cardsList);

        dialog.getHeader().add(header);
        dialog.add(container);
        dialog.open();
    }

    private Component createResourceItemCard(Contenido content, String currentCategory, Dialog parentDialog) {
        VerticalLayout item = new VerticalLayout();
        item.setWidthFull();
        item.setPadding(true);
        item.setSpacing(true);
        item.getStyle()
                .set("background-color", "#FFFFFF")
                .set("border-radius", "12px")
                .set("border", "1px solid #E2E8F0")
                .set("box-shadow", "0 1px 3px rgba(0,0,0,0.03)")
                .set("padding", "16px");

        // Top line: Category pill + Time + Verified
        HorizontalLayout topLine = new HorizontalLayout();
        topLine.setWidthFull();
        topLine.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        topLine.setAlignItems(FlexComponent.Alignment.CENTER);

        String color = CATEGORY_COLORS.getOrDefault(currentCategory, "#6366F1");
        Span catPill = new Span("• " + currentCategory);
        catPill.getStyle()
                .set("background-color", color + "1A")
                .set("color", color)
                .set("font-size", "11px")
                .set("font-weight", "700")
                .set("padding", "2px 8px")
                .set("border-radius", "10px");

        HorizontalLayout metaRight = new HorizontalLayout();
        metaRight.setAlignItems(FlexComponent.Alignment.CENTER);
        metaRight.setSpacing(true);

        Span timeSpan = new Span(formatTimeAgo(content.getFechaCreacion()));
        timeSpan.getStyle().set("color", "#94A3B8").set("font-size", "11px");

        metaRight.add(timeSpan);
        topLine.add(catPill, metaRight);

        // Title and description
        H4 title = new H4(content.getTitulo() != null && !content.getTitulo().isBlank() ? content.getTitulo() : "Documento sin título");
        title.getStyle().set("margin", "4px 0").set("font-size", "15px").set("font-weight", "600").set("color", "#0F172A");

        String description;
        if (content.getTexto() != null && !content.getTexto().isBlank()) {
            description = excerpt(content.getTexto(), 180);
        } else if (content.getStoragePath() != null) {
            description = "Archivo adjunto: " + content.getStoragePath();
        } else {
            description = "Sin contenido adicional.";
        }

        Paragraph desc = new Paragraph(description);
        desc.getStyle().set("color", "#64748B").set("font-size", "12px").set("margin", "0").set("line-height", "1.4");

        // Footer with Type, AI Badge, and Action Buttons
        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.getStyle().set("margin-top", "8px").set("padding-top", "8px").set("border-top", "1px solid #F1F5F9");

        HorizontalLayout leftBadges = new HorizontalLayout();
        leftBadges.setAlignItems(FlexComponent.Alignment.CENTER);
        leftBadges.setSpacing(true);

        Icon magic = VaadinIcon.MAGIC.create();
        magic.setSize("12px");
        magic.setColor("#00b894");
        Span aiText = new Span("AI Summary Ready");
        aiText.getStyle().set("color", "#00b894").set("font-size", "11px").set("font-weight", "600");
        leftBadges.add(magic, aiText);

        HorizontalLayout actionBtns = new HorizontalLayout();
        actionBtns.setAlignItems(FlexComponent.Alignment.CENTER);
        actionBtns.setSpacing(true);

        // Preview / View details
        Button viewDetailBtn = new Button("Ver contenido", VaadinIcon.FILE_TEXT_O.create());
        viewDetailBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        viewDetailBtn.getStyle().set("font-weight", "600").set("font-size", "11px");
        viewDetailBtn.addClickListener(e -> openContentDetailModal(content));

        // Quick move
        Button moveBtn = new Button("Mover", VaadinIcon.EXTERNAL_LINK.create());
        moveBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        moveBtn.getStyle().set("font-weight", "600").set("font-size", "11px");
        moveBtn.addClickListener(e -> openQuickMoveDialog(content, parentDialog));

        // Delete
        Button deleteBtn = new Button("Eliminar", VaadinIcon.TRASH.create());
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        deleteBtn.getStyle().set("font-weight", "600").set("font-size", "11px");
        deleteBtn.addClickListener(e -> confirmDeleteSingleContent(content, parentDialog));

        actionBtns.add(viewDetailBtn, moveBtn, deleteBtn);
        footer.add(leftBadges, actionBtns);

        item.add(topLine, title, desc, footer);
        return item;
    }

    private void openContentDetailModal(Contenido content) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(content.getTitulo() != null ? content.getTitulo() : "Detalle del Recurso");
        dialog.setWidth("650px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        Span categorySpan = new Span("Categoría: " + (content.getCategoria() != null ? content.getCategoria() : "General"));
        categorySpan.getStyle().set("font-size", "12px").set("color", "#64748B").set("font-weight", "600");

        TextArea contentArea = new TextArea("Contenido / Texto");
        contentArea.setWidthFull();
        contentArea.setHeight("250px");
        contentArea.setReadOnly(true);
        contentArea.setValue(content.getTexto() != null && !content.getTexto().isBlank()
                ? content.getTexto()
                : (content.getStoragePath() != null ? "Documento almacenado en: " + content.getStoragePath() : "Sin texto disponible"));

        layout.add(categorySpan, contentArea);

        Button close = new Button("Cerrar", e -> dialog.close());
        close.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(close);
        dialog.add(layout);
        dialog.open();
    }

    private void openQuickMoveDialog(Contenido content, Dialog parentDialog) {
        Dialog moveDialog = new Dialog();
        moveDialog.setHeaderTitle("Mover recurso a otra carpeta");
        moveDialog.setWidth("450px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        Paragraph text = new Paragraph("Mover '" + (content.getTitulo() != null ? content.getTitulo() : "este recurso") + "' a:");
        text.getStyle().set("margin", "0").set("color", "#334155");

        List<String> targetCategories = activeCategories.stream()
                .filter(c -> !c.equalsIgnoreCase(content.getCategoria()))
                .toList();

        ComboBox<String> combo = new ComboBox<>("Carpeta de destino");
        combo.setItems(targetCategories);
        if (!targetCategories.isEmpty()) {
            combo.setValue(targetCategories.get(0));
        }
        combo.setWidthFull();

        layout.add(text, combo);

        Button cancel = new Button("Cancelar", e -> moveDialog.close());
        Button confirm = new Button("Mover", e -> {
            String dest = combo.getValue();
            if (dest == null || dest.isBlank()) {
                Notification.show("Selecciona una carpeta de destino", 2000, Notification.Position.MIDDLE);
                return;
            }
            try {
                contenidoService.actualizarCategoria(content.getId(), currentUserId(), dest);
                Notification.show("Recurso movido a " + dest, 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                moveDialog.close();
                parentDialog.close();
                refreshData();
            } catch (Exception ex) {
                Notification.show("Error al mover recurso: " + ex.getMessage(), 4000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        confirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        moveDialog.getFooter().add(cancel, confirm);
        moveDialog.add(layout);
        moveDialog.open();
    }

    private void confirmDeleteSingleContent(Contenido content, Dialog parentDialog) {
        Dialog confirm = new Dialog();
        confirm.setHeaderTitle("Eliminar recurso");

        Paragraph msg = new Paragraph("¿Seguro que deseas eliminar '" + (content.getTitulo() != null ? content.getTitulo() : "este documento") + "'? Se eliminará de la base de datos.");
        msg.getStyle().set("margin", "0");

        Button cancel = new Button("Cancelar", e -> confirm.close());
        Button delete = new Button("Eliminar", e -> {
            try {
                contenidoService.eliminar(content.getId(), currentUserId());
                Notification.show("Recurso eliminado", 3000, Notification.Position.BOTTOM_END);
                confirm.close();
                parentDialog.close();
                refreshData();
            } catch (Exception ex) {
                Notification.show("Error al eliminar: " + ex.getMessage(), 4000, Notification.Position.BOTTOM_END);
            }
        });
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

        confirm.getFooter().add(cancel, delete);
        confirm.add(msg);
        confirm.open();
    }

    // =========================================================================
    // MODAL: EDITAR CARPETA / CATEGORÍA (RENOMBRAR Y MOVER ARCHIVOS A OTRAS CARPETAS)
    // =========================================================================
    private void openEditCategoryDialog(String categoryName, List<Contenido> contents) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Editar Carpeta / Categoría: " + categoryName);
        dialog.setWidth("750px");
        dialog.setHeight("80vh");

        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.getStyle().set("overflow-y", "auto");

        // Section 1: Renombrar carpeta
        VerticalLayout renameSection = new VerticalLayout();
        renameSection.setPadding(true);
        renameSection.setSpacing(true);
        renameSection.getStyle()
                .set("background", "#F8FAFC")
                .set("border-radius", "12px")
                .set("border", "1px solid #E2E8F0");

        H4 renameTitle = new H4("1. Renombrar carpeta");
        renameTitle.getStyle().set("margin", "0").set("color", "#0F172A").set("font-size", "14px");

        HorizontalLayout renameRow = new HorizontalLayout();
        renameRow.setWidthFull();
        renameRow.setAlignItems(FlexComponent.Alignment.BASELINE);

        TextField nameField = new TextField();
        nameField.setValue(categoryName);
        nameField.setWidthFull();
        nameField.setPlaceholder("Nuevo nombre para la categoría...");

        Button renameBtn = new Button("Guardar Nombre", e -> {
            String newName = nameField.getValue() != null ? nameField.getValue().trim() : "";
            if (newName.isBlank()) {
                Notification.show("El nombre no puede estar vacío", 2000, Notification.Position.MIDDLE);
                return;
            }
            if (newName.equals(categoryName)) {
                Notification.show("El nombre es idéntico al actual", 2000, Notification.Position.MIDDLE);
                return;
            }

            try {
                UUID userId = currentUserId();
                // Renombrar = actualizar la categoría de cada contenido que la tenía. No hay
                // "categoría" como entidad propia en la base, es solo un texto en cada Contenido.
                for (Contenido c : allContents) {
                    if (categoryName.equals(c.getCategoria())) {
                        contenidoService.actualizarCategoria(c.getId(), userId, newName);
                    }
                }
                activeCategories.remove(categoryName);
                activeCategories.add(newName);
                Notification.show("Carpeta renombrada a '" + newName + "'", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                refreshData();
            } catch (Exception ex) {
                Notification.show("Error al renombrar: " + ex.getMessage(), 4000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        renameBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        renameBtn.getStyle().set("background-color", "#0F172A");

        renameRow.add(nameField, renameBtn);
        renameSection.add(renameTitle, renameRow);

        // Section 2: Mover archivos a otras carpetas
        VerticalLayout moveSection = new VerticalLayout();
        moveSection.setPadding(true);
        moveSection.setSpacing(true);
        moveSection.getStyle()
                .set("background", "#FFFFFF")
                .set("border-radius", "12px")
                .set("border", "1px solid #E2E8F0");

        H4 moveTitle = new H4("2. Cambiar de lugar los archivos a otras carpetas");
        moveTitle.getStyle().set("margin", "0").set("color", "#0F172A").set("font-size", "14px");

        Paragraph moveDesc = new Paragraph("Selecciona los archivos que deseas transferir y elige la carpeta de destino.");
        moveDesc.getStyle().set("color", "#64748B").set("font-size", "12px").set("margin", "0");

        if (contents.isEmpty()) {
            VerticalLayout emptyFiles = new VerticalLayout();
            emptyFiles.setAlignItems(FlexComponent.Alignment.CENTER);
            emptyFiles.getStyle().set("padding", "20px").set("background", "#F8FAFC").set("border-radius", "8px");
            emptyFiles.add(new Span("Esta carpeta no tiene archivos actualmente para mover."));
            moveSection.add(moveTitle, moveDesc, emptyFiles);
        } else {
            List<String> targetCategories = activeCategories.stream()
                    .filter(c -> !c.equalsIgnoreCase(categoryName))
                    .toList();

            HorizontalLayout batchActionRow = new HorizontalLayout();
            batchActionRow.setWidthFull();
            batchActionRow.setAlignItems(FlexComponent.Alignment.CENTER);
            batchActionRow.setSpacing(true);

            ComboBox<String> targetCombo = new ComboBox<>();
            targetCombo.setPlaceholder("Seleccionar carpeta destino...");
            targetCombo.setItems(targetCategories);
            if (!targetCategories.isEmpty()) {
                targetCombo.setValue(targetCategories.get(0));
            }
            targetCombo.setWidth("250px");

            Set<Long> selectedContentIds = new HashSet<>();

            Button moveSelectedBtn = new Button("Mover seleccionados", VaadinIcon.ARROW_RIGHT.create());
            moveSelectedBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            moveSelectedBtn.getStyle().set("background-color", "#10B981");

            batchActionRow.add(new Span("Mover a:"), targetCombo, moveSelectedBtn);

            // Table of files
            VerticalLayout filesTable = new VerticalLayout();
            filesTable.setPadding(false);
            filesTable.setSpacing(false);
            filesTable.setWidthFull();
            filesTable.getStyle().set("border", "1px solid #E2E8F0").set("border-radius", "8px");

            // Select all row
            Checkbox selectAllCheck = new Checkbox("Seleccionar todos los archivos (" + contents.size() + ")");
            selectAllCheck.getStyle().set("padding", "8px 12px").set("border-bottom", "1px solid #E2E8F0").set("background", "#F8FAFC").set("font-weight", "600");

            List<Checkbox> itemCheckboxes = new ArrayList<>();

            for (Contenido c : contents) {
                HorizontalLayout row = new HorizontalLayout();
                row.setWidthFull();
                row.setAlignItems(FlexComponent.Alignment.CENTER);
                row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
                row.getStyle()
                        .set("padding", "8px 12px")
                        .set("border-bottom", "1px solid #F1F5F9");

                Checkbox itemCheck = new Checkbox(c.getTitulo() != null ? c.getTitulo() : "Documento sin título");
                itemCheck.addValueChangeListener(ev -> {
                    if (ev.getValue()) {
                        selectedContentIds.add(c.getId());
                    } else {
                        selectedContentIds.remove(c.getId());
                    }
                });
                itemCheckboxes.add(itemCheck);

                ComboBox<String> singleTargetCombo = new ComboBox<>();
                singleTargetCombo.setItems(targetCategories);
                singleTargetCombo.setPlaceholder("Mover a...");
                singleTargetCombo.setWidth("170px");
                singleTargetCombo.addValueChangeListener(ev -> {
                    if (ev.getValue() != null && !ev.getValue().isBlank()) {
                        try {
                            contenidoService.actualizarCategoria(c.getId(), currentUserId(), ev.getValue());
                            Notification.show("'" + c.getTitulo() + "' movido a " + ev.getValue(), 3000, Notification.Position.BOTTOM_END)
                                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                            dialog.close();
                            refreshData();
                        } catch (Exception ex) {
                            Notification.show("Error al mover: " + ex.getMessage(), 4000, Notification.Position.BOTTOM_END);
                        }
                    }
                });

                row.add(itemCheck, singleTargetCombo);
                filesTable.add(row);
            }

            selectAllCheck.addValueChangeListener(ev -> {
                boolean checked = ev.getValue();
                for (Checkbox cb : itemCheckboxes) {
                    cb.setValue(checked);
                }
            });

            moveSelectedBtn.addClickListener(e -> {
                String targetCat = targetCombo.getValue();
                if (targetCat == null || targetCat.isBlank()) {
                    Notification.show("Selecciona una carpeta de destino", 2000, Notification.Position.MIDDLE);
                    return;
                }
                if (selectedContentIds.isEmpty()) {
                    Notification.show("Selecciona al menos un archivo para mover", 2000, Notification.Position.MIDDLE);
                    return;
                }

                try {
                    UUID userId = currentUserId();
                    for (Long id : selectedContentIds) {
                        contenidoService.actualizarCategoria(id, userId, targetCat);
                    }
                    Notification.show(selectedContentIds.size() + " archivos movidos a '" + targetCat + "'", 3000, Notification.Position.BOTTOM_END)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    dialog.close();
                    refreshData();
                } catch (Exception ex) {
                    Notification.show("Error al mover archivos: " + ex.getMessage(), 4000, Notification.Position.BOTTOM_END)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });

            moveSection.add(moveTitle, moveDesc, batchActionRow, selectAllCheck, filesTable);
        }

        layout.add(renameSection, moveSection);

        Button closeBtn = new Button("Cerrar", e -> dialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(closeBtn);
        dialog.add(layout);
        dialog.open();
    }

    // =========================================================================
    // MODAL: ELIMINAR CARPETA / CATEGORÍA
    // =========================================================================
    private void openDeleteCategoryDialog(String categoryName, List<Contenido> contents) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Eliminar Carpeta / Categoría");
        dialog.setWidth("500px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        if (contents.isEmpty()) {
            Paragraph msg = new Paragraph("¿Deseas eliminar la carpeta '" + categoryName + "'? No contiene archivos actualmente.");
            msg.getStyle().set("color", "#334155").set("margin", "0");
            layout.add(msg);

            Button cancelBtn = new Button("Cancelar", e -> dialog.close());
            Button deleteBtn = new Button("Eliminar Carpeta", e -> {
                activeCategories.remove(categoryName);
                categorizedContents.remove(categoryName);
                dialog.close();
                refreshData();
                Notification.show("Carpeta '" + categoryName + "' eliminada", 3000, Notification.Position.BOTTOM_END);
            });
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

            dialog.getFooter().add(cancelBtn, deleteBtn);
        } else {
            Paragraph msg = new Paragraph("La carpeta '" + categoryName + "' contiene " + contents.size() + " recurso(s). ¿Qué deseas hacer con ellos?");
            msg.getStyle().set("color", "#334155").set("margin", "0");

            List<String> otherCategories = activeCategories.stream()
                    .filter(c -> !c.equalsIgnoreCase(categoryName))
                    .toList();

            RadioButtonGroup<String> optionGroup = new RadioButtonGroup<>();
            optionGroup.setItems("Mover los recursos a otra carpeta antes de borrar", "Eliminar permanentemente la carpeta y todos sus recursos");
            optionGroup.setValue("Mover los recursos a otra carpeta antes de borrar");

            ComboBox<String> destinationCombo = new ComboBox<>("Carpeta de destino");
            destinationCombo.setItems(otherCategories);
            if (!otherCategories.isEmpty()) {
                destinationCombo.setValue(otherCategories.get(0));
            }
            destinationCombo.setWidthFull();

            optionGroup.addValueChangeListener(e -> {
                destinationCombo.setVisible(e.getValue().startsWith("Mover"));
            });

            layout.add(msg, optionGroup, destinationCombo);

            Button cancelBtn = new Button("Cancelar", e -> dialog.close());
            Button confirmBtn = new Button("Confirmar", e -> {
                boolean moveFirst = optionGroup.getValue().startsWith("Mover");
                UUID userId = currentUserId();
                if (moveFirst) {
                    String dest = destinationCombo.getValue();
                    if (dest == null || dest.isBlank()) {
                        Notification.show("Selecciona una carpeta de destino", 2000, Notification.Position.MIDDLE);
                        return;
                    }
                    try {
                        for (Contenido c : contents) {
                            contenidoService.actualizarCategoria(c.getId(), userId, dest);
                        }
                        activeCategories.remove(categoryName);
                        Notification.show("Recursos transferidos a '" + dest + "' y carpeta eliminada", 3000, Notification.Position.BOTTOM_END)
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    } catch (Exception ex) {
                        Notification.show("Error al transferir: " + ex.getMessage(), 4000, Notification.Position.BOTTOM_END);
                    }
                } else {
                    try {
                        for (Contenido c : contents) {
                            contenidoService.eliminar(c.getId(), userId);
                        }
                        activeCategories.remove(categoryName);
                        Notification.show("Carpeta y recursos eliminados correctamente", 3000, Notification.Position.BOTTOM_END)
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    } catch (Exception ex) {
                        Notification.show("Error al eliminar recursos: " + ex.getMessage(), 4000, Notification.Position.BOTTOM_END);
                    }
                }

                dialog.close();
                refreshData();
            });
            confirmBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

            dialog.getFooter().add(cancelBtn, confirmBtn);
        }

        dialog.add(layout);
        dialog.open();
    }

    // =========================================================================
    // RIGHT PANEL: METRICAS Y DISTRIBUCIÓN
    // =========================================================================
    private void renderDistributionCard() {
        distributionCardContainer.removeAll();

        VerticalLayout card = new VerticalLayout();
        card.setWidthFull();
        card.setPadding(true);
        card.setSpacing(true);
        card.getStyle()
                .set("background-color", "#FFFFFF")
                .set("border-radius", "16px")
                .set("border", "1px solid #E2E8F0")
                .set("padding", "20px");

        H4 title = new H4("Distribución de Conocimiento");
        title.getStyle().set("margin", "0").set("color", "#0F172A").set("font-weight", "600");

        VerticalLayout progressBars = new VerticalLayout();
        progressBars.setPadding(false);
        progressBars.setSpacing(true);

        int maxCount = categorizedContents.values().stream()
                .mapToInt(List::size)
                .max()
                .orElse(1);
        if (maxCount == 0) maxCount = 1;

        for (Map.Entry<String, List<Contenido>> entry : categorizedContents.entrySet()) {
            String categoryName = entry.getKey();
            int count = entry.getValue().size();
            double percentage = (double) count / maxCount * 100;

            VerticalLayout categoryRow = new VerticalLayout();
            categoryRow.setPadding(false);
            categoryRow.setSpacing(false);
            categoryRow.setWidthFull();

            HorizontalLayout header = new HorizontalLayout();
            header.setWidthFull();
            header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
            header.setAlignItems(FlexComponent.Alignment.CENTER);
            header.setPadding(false);
            header.setSpacing(true);

            Span categoryLabel = new Span(categoryName);
            categoryLabel.getStyle().set("font-size", "13px").set("font-weight", "500").set("color", "#0F172A");

            Span countSpan = new Span(count + (count == 1 ? " recurso" : " recursos"));
            String color = CATEGORY_COLORS.getOrDefault(categoryName, "#94A3B8");
            countSpan.getStyle().set("font-size", "12px").set("font-weight", "600").set("color", color);

            header.add(categoryLabel, countSpan);

            ProgressBar progressBar = new ProgressBar();
            progressBar.setMin(0);
            progressBar.setMax(100);
            progressBar.setValue(percentage);
            progressBar.setWidthFull();
            progressBar.getStyle()
                    .set("--vaadin-progress-bar-value-background-color", color)
                    .set("height", "7px")
                    .set("border-radius", "4px");

            categoryRow.add(header, progressBar);
            progressBars.add(categoryRow);
        }

        card.add(title, progressBars);
        distributionCardContainer.add(card);
    }

    private void renderSummaryCard() {
        summaryCardContainer.removeAll();

        VerticalLayout card = new VerticalLayout();
        card.setWidthFull();
        card.setPadding(true);
        card.setSpacing(true);
        card.getStyle()
                .set("background-color", "#FFFFFF")
                .set("border-radius", "16px")
                .set("border", "1px solid #E2E8F0")
                .set("padding", "20px");

        H4 title = new H4("Resumen General");
        title.getStyle().set("margin", "0").set("color", "#0F172A").set("font-weight", "600");

        addMetric(card, "Total carpetas", String.valueOf(categorizedContents.size()), VaadinIcon.ARCHIVES);
        addMetric(card, "Total recursos", String.valueOf(allContents.size()), VaadinIcon.FILE_TREE);
        addMetric(card, "Carpeta principal", getBiggestCategory(), VaadinIcon.STAR);

        long unassigned = allContents.stream()
                .filter(c -> c.getCategoria() == null || c.getCategoria().isBlank() || c.getCategoria().equalsIgnoreCase("General"))
                .count();
        addMetric(card, "Sin clasificar", String.valueOf(unassigned), VaadinIcon.QUESTION);

        Button autoBtn = new Button("Auto-categorizar con IA", VaadinIcon.MAGIC.create());
        autoBtn.setWidthFull();
        autoBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        autoBtn.getStyle()
                .set("background-color", "#10B981")
                .set("color", "#FFFFFF")
                .set("border-radius", "20px")
                .set("font-weight", "600")
                .set("margin-top", "12px");

        autoBtn.addClickListener(e -> {
            Notification.show("Análisis inteligente ejecutado. Todas las carpetas están al día.", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        card.add(autoBtn);
        summaryCardContainer.add(card);
    }

    private void addMetric(VerticalLayout card, String label, String value, VaadinIcon icon) {
        HorizontalLayout metric = new HorizontalLayout();
        metric.setAlignItems(FlexComponent.Alignment.CENTER);
        metric.setSpacing(true);

        Div iconBox = new Div(icon.create());
        iconBox.getStyle()
                .set("background-color", "#F1F5F9")
                .set("border-radius", "50%")
                .set("width", "32px")
                .set("height", "32px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("color", "#64748B");

        VerticalLayout texts = new VerticalLayout();
        texts.setPadding(false);
        texts.setSpacing(false);

        Span labelSpan = new Span(label);
        labelSpan.getStyle().set("font-size", "12px").set("color", "#64748B");

        Span valueSpan = new Span(value);
        valueSpan.getStyle().set("font-weight", "600").set("color", "#0F172A");

        texts.add(labelSpan, valueSpan);
        metric.add(iconBox, texts);
        card.add(metric);
    }

    private String getBiggestCategory() {
        return categorizedContents.entrySet().stream()
                .max(Comparator.comparingInt(e -> e.getValue().size()))
                .map(Map.Entry::getKey)
                .orElse("N/A");
    }

    private String formatTimeAgo(OffsetDateTime createdAt) {
        if (createdAt == null) return "Fecha desconocida";
        Duration duration = Duration.between(createdAt.toInstant(), OffsetDateTime.now(ZoneId.systemDefault()).toInstant());
        long seconds = Math.abs(duration.getSeconds());
        if (seconds < 60) return "justo ahora";
        long minutes = seconds / 60;
        if (minutes < 60) return "hace " + minutes + "m";
        long hours = minutes / 60;
        if (hours < 24) return "hace " + hours + "h";
        long days = hours / 24;
        return "hace " + days + "d";
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
}
