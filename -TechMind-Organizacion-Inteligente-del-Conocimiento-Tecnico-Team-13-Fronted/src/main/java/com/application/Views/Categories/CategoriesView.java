package com.application.Views.Categories;

import com.application.model.Content;
import com.application.model.User;
import com.application.service.SupabaseService;
import com.application.service.UserSession;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.*;
import java.util.stream.Collectors;

@PageTitle("Categorías - KnowBase")
@Route(value = "categorias", layout = MainLayout.class)
public class CategoriesView extends VerticalLayout {

    private final SupabaseService supabaseService;
    private final UserSession userSession;
    private List<Content> allContents = new ArrayList<>();
    private Map<String, List<Content>> categorizedContents = new HashMap<>();

    // Color mapping for categories
    private static final Map<String, String> CATEGORY_COLORS = Map.ofEntries(
            Map.entry("Backend", "#8B5CF6"),
            Map.entry("DevOps", "#0EA5E9"),
            Map.entry("Frontend", "#10B981"),
            Map.entry("Cloud", "#F59E0B"),
            Map.entry("Data Science", "#EC4899"),
            Map.entry("Security", "#EF4444"),
            Map.entry("Architecture", "#6366F1")
    );

    private static final Map<String, String> CATEGORY_BG_COLORS = Map.ofEntries(
            Map.entry("Backend", "#F3E8FF"),
            Map.entry("DevOps", "#E0F2FE"),
            Map.entry("Frontend", "#D1FAE5"),
            Map.entry("Cloud", "#FEF3C7"),
            Map.entry("Data Science", "#FCE7F3"),
            Map.entry("Security", "#FEE2E2"),
            Map.entry("Architecture", "#E0E7FF")
    );

    public CategoriesView(SupabaseService supabaseService, UserSession userSession) {
        this.supabaseService = supabaseService;
        this.userSession = userSession;

        setSizeFull();
        setPadding(false);
        setMargin(false);
        setSpacing(false);
        getStyle().set("background-color", "#F8FAFC").set("overflow-y", "auto");

        loadUserContents();
        categorizeContents();

        add(createHeader());
        add(createMainContent());
    }

    private void loadUserContents() {
        try {
            Optional<User> maybeUser = Optional.ofNullable(userSession.getAuthenticatedUser());
            if (maybeUser.isPresent()) {
                allContents = supabaseService.getContentsForUser(maybeUser.get().getId());
            }
        } catch (Exception e) {
            allContents = new ArrayList<>();
        }
    }

    private void categorizeContents() {
        categorizedContents.clear();
        for (Content c : allContents) {
            String category = c.getTipoContenido() != null && !c.getTipoContenido().isBlank()
                    ? c.getTipoContenido()
                    : "Uncategorized";
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
                .set("padding", "24px");

        // Title section
        H1 title = new H1("Categorías");
        title.getStyle().set("margin", "0").set("color", "#0F172A").set("font-weight", "600");

        Span subtitle = new Span("Organiza y gestiona las categorías de tu base de conocimiento");
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
        searchField.setPlaceholder("Search categories...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setSuffixComponent(new Span("⌘K"));
        searchField.setWidth("300px");
        searchField.getStyle().set("border-radius", "20px");

        HorizontalLayout actionButtons = new HorizontalLayout();
        actionButtons.setAlignItems(FlexComponent.Alignment.CENTER);
        actionButtons.setSpacing(true);

        Button newCategoryBtn = new Button("+ Nueva Categoría", VaadinIcon.SEARCH.create());
        newCategoryBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newCategoryBtn.getStyle()
                .set("background-color", "#10B981")
                .set("color", "#FFFFFF")
                .set("border-radius", "20px")
                .set("font-weight", "600");

        Icon bellIcon = VaadinIcon.BELL_O.create();
        bellIcon.getStyle().set("cursor", "pointer").set("color", "#64748B");

        Icon cogIcon = VaadinIcon.COG_O.create();
        cogIcon.getStyle().set("cursor", "pointer").set("color", "#64748B");

        actionButtons.add(newCategoryBtn, bellIcon, cogIcon);

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

        Div categoriesGrid = new Div();
        categoriesGrid.setWidthFull();
        categoriesGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(2, 1fr)")
                .set("gap", "20px");

        for (Map.Entry<String, List<Content>> entry : categorizedContents.entrySet()) {
            String categoryName = entry.getKey();
            List<Content> contents = entry.getValue();
            categoriesGrid.add(createCategoryCard(categoryName, contents));
        }

        leftPanel.add(categoriesGrid);
        leftPanel.expand(categoriesGrid);

        // Right panel: Metrics
        VerticalLayout rightPanel = new VerticalLayout();
        rightPanel.setWidth("30%");
        rightPanel.setPadding(false);
        rightPanel.setSpacing(true);
        rightPanel.getStyle().set("overflow-y", "auto");

        rightPanel.add(createDistributionCard());
        rightPanel.add(createSummaryCard());

        mainLayout.add(leftPanel, rightPanel);
        return mainLayout;
    }

    private Component createCategoryCard(String categoryName, List<Content> contents) {
        VerticalLayout card = new VerticalLayout();
        card.setWidthFull();
        card.setPadding(true);
        card.setSpacing(true);
        card.getStyle()
                .set("background-color", "#FFFFFF")
                .set("border-radius", "16px")
                .set("border", "1px solid #E2E8F0")
                .set("box-shadow", "0px 2px 4px rgba(0, 0, 0, 0.02)")
                .set("padding", "20px");

        // Icon and title
        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        titleLayout.setSpacing(true);

        Div iconBox = new Div();
        iconBox.setText("📦");
        String color = CATEGORY_COLORS.getOrDefault(categoryName, "#94A3B8");
        String bgColor = CATEGORY_BG_COLORS.getOrDefault(categoryName, "#F1F5F9");
        iconBox.getStyle()
                .set("background-color", bgColor)
                .set("color", color)
                .set("width", "48px")
                .set("height", "48px")
                .set("border-radius", "12px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("font-size", "24px");

        VerticalLayout titleText = new VerticalLayout();
        titleText.setPadding(false);
        titleText.setSpacing(false);

        H4 catTitle = new H4(categoryName);
        catTitle.getStyle().set("margin", "0").set("color", "#0F172A").set("font-weight", "600");

        Span resourceCount = new Span(contents.size() + " recursos");
        resourceCount.getStyle().set("color", "#64748B").set("font-size", "12px");

        titleText.add(catTitle, resourceCount);
        titleLayout.add(iconBox, titleText);

        // Growth badge
        Span growthBadge = new Span("+" + (int)(Math.random() * 20) + "%");
        growthBadge.getStyle()
                .set("background-color", "#ECFDF5")
                .set("color", "#059669")
                .set("font-size", "12px")
                .set("font-weight", "600")
                .set("padding", "2px 8px")
                .set("border-radius", "12px");

        HorizontalLayout headerRow = new HorizontalLayout(titleLayout, growthBadge);
        headerRow.setWidthFull();
        headerRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // Progress bar
        ProgressBar progressBar = new ProgressBar(0, 100, contents.size() * 10 % 100);
        progressBar.setWidthFull();
        progressBar.getStyle().set("height", "6px");

        // Tags
        HorizontalLayout tagsLayout = new HorizontalLayout();
        tagsLayout.setSpacing(true);
        tagsLayout.getStyle().set("flex-wrap", "wrap");

        List<String> sampleTags = List.of("Spring Boot", "Kafka", "REST APIs");
        for (int i = 0; i < Math.min(2, sampleTags.size()); i++) {
            Span tag = new Span(sampleTags.get(i));
            tag.getStyle()
                    .set("background-color", bgColor)
                    .set("color", color)
                    .set("font-size", "11px")
                    .set("padding", "4px 8px")
                    .set("border-radius", "6px");
            tagsLayout.add(tag);
        }

        Span moreTags = new Span("+1");
        moreTags.getStyle()
                .set("background-color", "#F1F5F9")
                .set("color", "#475569")
                .set("font-size", "11px")
                .set("padding", "4px 8px")
                .set("border-radius", "6px");
        tagsLayout.add(moreTags);

        // Action buttons
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        Button editBtn = new Button("Editar");
        editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        editBtn.getStyle()
                .set("background-color", "#F1F5F9")
                .set("color", "#475569")
                .set("border-radius", "20px");

        Button viewBtn = new Button("Ver recursos");
        viewBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        viewBtn.getStyle()
                .set("background-color", "#D1FAE5")
                .set("color", "#047857")
                .set("border-radius", "20px");

        Button deleteBtn = new Button("Eliminar");
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        deleteBtn.getStyle()
                .set("background-color", "#FEE2E2")
                .set("color", "#B91C1C")
                .set("border-radius", "20px");

        actions.add(editBtn, viewBtn, deleteBtn);

        card.add(headerRow, progressBar, tagsLayout, actions);
        return card;
    }

    private Component createDistributionCard() {
        VerticalLayout card = new VerticalLayout();
        card.setWidthFull();
        card.setPadding(true);
        card.setSpacing(true);
        card.getStyle()
                .set("background-color", "#FFFFFF")
                .set("border-radius", "16px")
                .set("border", "1px solid #E2E8F0")
                .set("padding", "20px");

        H4 title = new H4("Distribución");
        title.getStyle().set("margin", "0").set("color", "#0F172A").set("font-weight", "600");

        // Progress bars for each category
        VerticalLayout progressBars = new VerticalLayout();
        progressBars.setPadding(false);
        progressBars.setSpacing(true);

        // Find max count to scale progress bars
        int maxCount = categorizedContents.values().stream()
                .mapToInt(List::size)
                .max()
                .orElse(1);

        for (Map.Entry<String, List<Content>> entry : categorizedContents.entrySet()) {
            String categoryName = entry.getKey();
            int count = entry.getValue().size();
            double percentage = (double) count / maxCount * 100;

            // Row container for category
            VerticalLayout categoryRow = new VerticalLayout();
            categoryRow.setPadding(false);
            categoryRow.setSpacing(false);
            categoryRow.setWidthFull();

            // Header with category name and count
            HorizontalLayout header = new HorizontalLayout();
            header.setWidthFull();
            header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
            header.setAlignItems(FlexComponent.Alignment.CENTER);
            header.setPadding(false);
            header.setSpacing(true);

            Span categoryLabel = new Span(categoryName);
            categoryLabel.getStyle().set("font-size", "13px").set("font-weight", "500").set("color", "#0F172A");

            Span countSpan = new Span(count + " recursos");
            String color = CATEGORY_COLORS.getOrDefault(categoryName, "#94A3B8");
            countSpan.getStyle().set("font-size", "12px").set("font-weight", "600").set("color", color);

            header.add(categoryLabel, countSpan);

            // Progress bar
            ProgressBar progressBar = new ProgressBar();
            progressBar.setMin(0);
            progressBar.setMax(100);
            progressBar.setValue(percentage);
            progressBar.setWidthFull();
            progressBar.getStyle()
                    .set("--vaadin-progress-bar-value-background-color", color)
                    .set("height", "8px")
                    .set("border-radius", "4px");

            categoryRow.add(header, progressBar);
            progressBars.add(categoryRow);
        }

        card.add(title, progressBars);
        return card;
    }

    private Component createSummaryCard() {
        VerticalLayout card = new VerticalLayout();
        card.setWidthFull();
        card.setPadding(true);
        card.setSpacing(true);
        card.getStyle()
                .set("background-color", "#FFFFFF")
                .set("border-radius", "16px")
                .set("border", "1px solid #E2E8F0")
                .set("padding", "20px");

        H4 title = new H4("Resumen");
        title.getStyle().set("margin", "0").set("color", "#0F172A").set("font-weight", "600");

        // Metrics
        addMetric(card, "Total categorías", String.valueOf(categorizedContents.size()), VaadinIcon.ARCHIVES);
        addMetric(card, "Categoría más grande", getBiggestCategory(), VaadinIcon.STAR);
        addMetric(card, "Mayor crecimiento", "Data Science", VaadinIcon.TRENDING_UP);
        addMetric(card, "Sin categorizar", "23", VaadinIcon.QUESTION);

        Button autoBtn = new Button("Auto-categorizar pendientes", VaadinIcon.MAGIC.create());
        autoBtn.setWidthFull();
        autoBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        autoBtn.getStyle()
                .set("background-color", "#10B981")
                .set("color", "#FFFFFF")
                .set("border-radius", "20px")
                .set("font-weight", "600")
                .set("margin-top", "12px");

        card.add(autoBtn);
        return card;
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
}
