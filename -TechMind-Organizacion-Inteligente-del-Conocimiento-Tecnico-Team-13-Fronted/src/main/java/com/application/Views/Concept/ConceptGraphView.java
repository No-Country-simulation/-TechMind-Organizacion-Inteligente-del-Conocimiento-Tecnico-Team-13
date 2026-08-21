package com.application.Views.Concept;

import com.application.data.StaticConceptData;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.application.Views.Layout.MainLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Concept Graph")
@Route(value = "concept-graph", layout = MainLayout.class)
public class ConceptGraphView extends VerticalLayout {

    private final ConceptGraphComponent graphComponent;
    private final VerticalLayout sidebar;

    public ConceptGraphView() {
        setPadding(false);
        setSpacing(false);

        // 1. Inicializar componentes principales
        graphComponent = new ConceptGraphComponent();
        sidebar = createSidebar();

        // 2. Construir el layout
        Component header = createHeader();
        Component mainContent = createMainContent();

        add(header, mainContent);
        expand(mainContent); // FIX: Make mainContent fill the remaining vertical space

        // 3. Escuchar los eventos del Grafo y cargar datos
        graphComponent.addNodeSelectedListener(event -> showSidebarDetails(event.getNodeId()));
        graphComponent.addNodeDeselectedListener(event -> sidebar.setVisible(false));
        loadGraphData();
    }

    private Component createHeader() {
        H2 title = new H2("Concept Graph");
        title.getStyle().set("margin", "0");
        Span subtitle = new Span("Mapa visual de relaciones entre conceptos técnicos");
        subtitle.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        VerticalLayout titleContainer = new VerticalLayout(title, subtitle);
        titleContainer.setPadding(false);
        titleContainer.setSpacing(false);
        titleContainer.setAlignItems(Alignment.START);

        TextField search = new TextField();
        search.setPlaceholder("Buscar... (Ctrl+K)");
        search.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        search.setWidth("300px");

        Button exportButton = new Button("Exportar Grafo", new Icon(VaadinIcon.DOWNLOAD_ALT));
        exportButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        Button notificationsButton = new Button(new Icon(VaadinIcon.BELL));
        notificationsButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button settingsButton = new Button(new Icon(VaadinIcon.COG));
        settingsButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout toolbar = new HorizontalLayout(search, exportButton, notificationsButton, settingsButton);
        toolbar.setAlignItems(Alignment.CENTER);
        toolbar.setSpacing(true);

        HorizontalLayout header = new HorizontalLayout(titleContainer, toolbar);
        header.setWidthFull();
        header.getStyle().set("padding", "16px 24px");
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);
        header.getStyle().set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

        return header;
    }

    private VerticalLayout createSidebar() {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidth("25%");
        layout.setHeightFull();
        layout.getStyle().set("border-left", "1px solid var(--lumo-contrast-10pct)");
        layout.setVisible(false); // Oculto hasta seleccionar un nodo
        layout.setPadding(false);
        layout.setSpacing(false);
        return layout;
    }

    private Component createMainContent() {
        Div graphContainer = new Div();
        graphContainer.getStyle().set("position", "relative");
        graphContainer.setSizeFull();

        graphComponent.setSizeFull();

        Component legend = createLegendCard();
        legend.getStyle().set("position", "absolute").set("top", "24px").set("left", "24px").set("zIndex", "1").set("width", "auto");

        Component graphControls = createGraphControls();
        graphControls.getStyle().set("position", "absolute").set("top", "24px").set("right", "24px").set("zIndex", "1").set("width", "auto");

        graphContainer.add(graphComponent, legend, graphControls);

        HorizontalLayout mainLayout = new HorizontalLayout(graphContainer, sidebar);
        mainLayout.setSizeFull();
        mainLayout.expand(graphContainer);

        return mainLayout;
    }

    private Component createLegendCard() {
        VerticalLayout legendLayout = new VerticalLayout();
        legendLayout.setSpacing(false);
        legendLayout.setPadding(false);
        legendLayout.getStyle()
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("padding", "16px");

        H4 title = new H4("Categorías");
        title.getStyle().set("margin-top", "0").set("margin-bottom", "8px");
        legendLayout.add(title);

        addLegendItem(legendLayout, "DevOps", "#4D96FF");
        addLegendItem(legendLayout, "Backend", "#FF6B6B");
        addLegendItem(legendLayout, "Cloud", "#6BFFB8");
        addLegendItem(legendLayout, "Frontend", "#FFD166");
        addLegendItem(legendLayout, "Data Science", "#A96BFF");

        return legendLayout;
    }

    private void addLegendItem(VerticalLayout container, String name, String color) {
        Span colorDot = new Span();
        colorDot.getStyle()
                .set("display", "inline-block")
                .set("width", "12px")
                .set("height", "12px")
                .set("border-radius", "50%")
                .set("background-color", color)
                .set("margin-right", "8px");

        Span text = new Span(name);
        text.getStyle().set("vertical-align", "middle");

        HorizontalLayout item = new HorizontalLayout(colorDot, text);
        item.setAlignItems(Alignment.CENTER);
        item.setSpacing(false);
        item.getStyle().set("padding-bottom", "4px");
        container.add(item);
    }

    private Component createGraphControls() {
        Button zoomIn = new Button(new Icon(VaadinIcon.PLUS));
        zoomIn.addThemeVariants(ButtonVariant.LUMO_ICON);
        Button zoomOut = new Button(new Icon(VaadinIcon.MINUS));
        zoomOut.addThemeVariants(ButtonVariant.LUMO_ICON);
        Button expand = new Button(new Icon(VaadinIcon.EXPAND_FULL));
        expand.addThemeVariants(ButtonVariant.LUMO_ICON);

        VerticalLayout controls = new VerticalLayout(zoomIn, zoomOut, expand);
        controls.setSpacing(false);
        controls.setPadding(false);
        controls.getStyle()
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("padding", "4px");

        return controls;
    }

    private void loadGraphData() {
        graphComponent.setGraphData(StaticConceptData.NODES, StaticConceptData.EDGES);
    }

    private void showSidebarDetails(String nodeId) {
        sidebar.removeAll();

        StaticConceptData.ConceptDetail concept = StaticConceptData.CONCEPTS.get(nodeId);
        if (concept == null) {
            sidebar.setVisible(false);
            return;
        }

        sidebar.setVisible(true);

        // --- Contenido principal del sidebar (scrollable) ---
        VerticalLayout contentWrapper = new VerticalLayout();
        // FIX: setSizeFull() was causing a resize loop. Width is 100% by default.
        contentWrapper.setPadding(false);
        contentWrapper.setSpacing(false);
        contentWrapper.getStyle().set("overflow-y", "auto");

        // --- Header del Sidebar ---
        Button closeButton = new Button(new Icon(VaadinIcon.CLOSE_SMALL), e -> sidebar.setVisible(false));
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Span categoryBadge = new Span("• " + concept.category());
        categoryBadge.getElement().getThemeList().add("badge");
        categoryBadge.getStyle()
                .set("color", getCategoryColor(concept.category()))
                .set("background-color", getCategoryBackgroundColor(concept.category()))
                .set("padding", "4px 8px")
                .set("border-radius", "12px")
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("font-weight", "500");

        HorizontalLayout header = new HorizontalLayout(categoryBadge, closeButton);
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);
        header.getStyle().set("padding", "12px 24px");

        // --- Título y Resumen ---
        H3 title = new H3(concept.name());
        title.getStyle().set("margin", "0");
        Paragraph description = new Paragraph(concept.description());
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        VerticalLayout infoSection = new VerticalLayout(title, description);
        infoSection.setSpacing(false);
        infoSection.setPadding(false);
        infoSection.getStyle().set("padding", "0 24px");

        Div separator1 = createSeparator();
        Div separator2 = createSeparator();

        contentWrapper.add(
                infoSection,
                separator1,
                createRelatedConceptsSection(concept),
                separator2,
                createContentItemsSection(concept));

        // --- Acciones (Footer) ---
        Component actionButtons = createSidebarActionButtons();
        Div footer = new Div(actionButtons);
        footer.getStyle()
                .set("padding", "16px 24px")
                .set("background-color", "var(--lumo-contrast-5pct)");

        sidebar.add(header, contentWrapper, footer);
        sidebar.expand(contentWrapper);
    }

    private VerticalLayout createRelatedConceptsSection(StaticConceptData.ConceptDetail concept) {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(false);
        section.getStyle().set("padding", "0 24px");

        H4 title = new H4("Conceptos Relacionados");
        title.getStyle().set("margin", "0 0 8px 0");
        section.add(title);

        if (concept.relatedConcepts().isEmpty()) {
            section.add(new Span("No hay conceptos relacionados."));
        } else {
            concept.relatedConcepts().forEach(related -> {
                StaticConceptData.ConceptDetail relatedConcept = StaticConceptData.CONCEPTS.values().stream()
                        .filter(c -> c.name().equals(related)).findFirst().orElse(null);

                Span colorDot = new Span();
                colorDot.getStyle()
                        .set("display", "inline-block")
                        .set("width", "8px")
                        .set("height", "8px")
                        .set("border-radius", "50%")
                        .set("background-color", relatedConcept != null ? getCategoryColor(relatedConcept.category()) : "#ccc")
                        .set("margin-right", "12px");

                Span name = new Span(related);
                Icon arrow = new Icon(VaadinIcon.ARROW_RIGHT);
                arrow.getStyle().set("color", "var(--lumo-contrast-50pct)");

                HorizontalLayout row = new HorizontalLayout(colorDot, name, arrow);
                row.setAlignItems(Alignment.CENTER);
                row.expand(name);
                row.getStyle().set("cursor", "pointer").set("padding", "4px 0");
                section.add(row);
            });
        }
        return section;
    }

    private VerticalLayout createContentItemsSection(StaticConceptData.ConceptDetail concept) {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(false);
        section.getStyle().set("padding", "0 24px");

        H4 title = new H4("Contenidos con este Concepto");
        title.getStyle().set("margin", "0 0 8px 0");
        section.add(title);

        if (concept.contents().isEmpty()) {
            section.add(new Span("No hay contenidos asociados."));
        } else {
            concept.contents().forEach(contentName -> {
                Span contentTitle = new Span(contentName);
                contentTitle.getStyle().set("font-weight", "500");
                Span meta = new Span("Article • 24 Jul 2024");
                meta.getStyle().set("font-size", "var(--lumo-font-size-s)").set("color", "var(--lumo-secondary-text-color)");

                VerticalLayout card = new VerticalLayout(contentTitle, meta);
                card.setSpacing(false);
                card.getStyle()
                        .set("border", "1px solid var(--lumo-contrast-10pct)")
                        .set("border-radius", "var(--lumo-border-radius-m)")
                        .set("padding", "12px");
                section.add(card);
            });
        }
        return section;
    }

    private Component createSidebarActionButtons() {
        Button consultAI = new Button("Consultar IA sobre este Concepto", new Icon(VaadinIcon.MAGIC));
        consultAI.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        consultAI.setWidthFull();

        Button viewInLibrary = new Button("Ver en Biblioteca", new Icon(VaadinIcon.BOOK));
        viewInLibrary.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        viewInLibrary.setWidthFull();

        VerticalLayout layout = new VerticalLayout(consultAI, viewInLibrary);
        layout.setSpacing(true);
        layout.setPadding(false);
        return layout;
    }

    private Div createSeparator() {
        Div separator = new Div();
        separator.setHeight("1px");
        separator.setWidthFull();
        separator.getStyle()
                .set("background-color", "var(--lumo-contrast-10pct)")
                .set("margin", "16px 0");
        return separator;
    }

    private String getCategoryColor(String category) {
        return switch (category) {
            case "DevOps" -> "#4D96FF";
            case "Backend" -> "#FF6B6B";
            case "Cloud" -> "#6BFFB8";
            case "Frontend" -> "#FFD166";
            case "Data Science" -> "#A96BFF";
            default -> "grey";
        };
    }

    private String getCategoryBackgroundColor(String category) {
        return switch (category) {
            case "DevOps" -> "hsla(217, 100%, 65%, 0.1)";
            case "Backend" -> "hsla(0, 100%, 71%, 0.1)";
            case "Cloud" -> "hsla(150, 100%, 71%, 0.1)";
            case "Frontend" -> "hsla(45, 100%, 70%, 0.1)";
            case "Data Science" -> "hsla(271, 100%, 71%, 0.1)";
            default -> "var(--lumo-contrast-10pct)";
        };
    }
}