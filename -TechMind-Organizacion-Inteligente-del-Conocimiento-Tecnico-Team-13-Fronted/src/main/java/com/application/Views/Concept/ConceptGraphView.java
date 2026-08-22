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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Concept Graph")
@Route(value = "concept-graph", layout = MainLayout.class)
public class ConceptGraphView extends VerticalLayout {

    private final ConceptGraphComponent graphComponent;
    private final VerticalLayout sidebar;

    public ConceptGraphView() {
        setSizeFull();
        setHeightFull();
        setWidthFull();
        setPadding(false);
        setSpacing(false);
        setMargin(false);
        getStyle()
                .set("height", "100%")
                .set("max-height", "100%")
                .set("overflow", "hidden")
                .set("display", "flex")
                .set("flex-direction", "column");

        // 1. Inicializar componentes principales
        graphComponent = new ConceptGraphComponent();
        sidebar = createSidebar();

        // 2. Construir el layout
        Component header = createHeader();
        Component mainContent = createMainContent();

        add(header, mainContent);
        expand(mainContent);

        // 3. Escuchar los eventos del Grafo y cargar datos
        graphComponent.addNodeSelectedListener(event -> showSidebarDetails(event.getNodeId()));
        graphComponent.addNodeDeselectedListener(event -> sidebar.setVisible(false));
        loadGraphData();
    }

    private Component createHeader() {
        H2 title = new H2("Concept Graph");
        title.getStyle().set("margin", "0").set("font-size", "22px").set("font-weight", "700");
        Span subtitle = new Span("Mapa visual de relaciones entre conceptos técnicos");
        subtitle.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        VerticalLayout titleContainer = new VerticalLayout(title, subtitle);
        titleContainer.setPadding(false);
        titleContainer.setSpacing(false);
        titleContainer.setAlignItems(Alignment.START);

        Button exportButton = new Button("Exportar Grafo", new Icon(VaadinIcon.DOWNLOAD_ALT));
        exportButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        exportButton.addClickListener(event -> graphComponent.exportGraph("concept-graph.png"));

        HorizontalLayout toolbar = new HorizontalLayout(exportButton);
        toolbar.setAlignItems(Alignment.CENTER);
        toolbar.setSpacing(true);

        HorizontalLayout header = new HorizontalLayout(titleContainer, toolbar);
        header.setWidthFull();
        header.getStyle()
                .set("padding", "14px 24px")
                .set("background-color", "#FFFFFF")
                .set("flex-shrink", "0")
                .set("border-bottom", "1px solid #E2E8F0");
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        return header;
    }

    private VerticalLayout createSidebar() {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidth("320px");
        layout.setHeightFull();
        layout.getStyle()
                .set("border-left", "1px solid #E2E8F0")
                .set("background-color", "#FFFFFF")
                .set("overflow-y", "auto")
                .set("flex-shrink", "0");
        layout.setVisible(false);
        layout.setPadding(false);
        layout.setSpacing(false);
        return layout;
    }

    private Component createMainContent() {
        Div graphContainer = new Div();
        graphContainer.setSizeFull();
        graphContainer.getStyle()
                .set("position", "relative")
                .set("width", "100%")
                .set("height", "100%")
                .set("overflow", "hidden")
                .set("flex", "1");

        Component legend = createLegendCard();
        legend.getStyle()
                .set("position", "absolute")
                .set("top", "20px")
                .set("left", "20px")
                .set("zIndex", "10")
                .set("width", "auto");

        Component graphControls = createGraphControls();
        graphControls.getStyle()
                .set("position", "absolute")
                .set("top", "20px")
                .set("right", "20px")
                .set("zIndex", "10")
                .set("width", "auto");

        graphContainer.add(graphComponent, legend, graphControls);

        HorizontalLayout mainLayout = new HorizontalLayout(graphContainer, sidebar);
        mainLayout.setSizeFull();
        mainLayout.setSpacing(false);
        mainLayout.setPadding(false);
        mainLayout.setMargin(false);
        mainLayout.getStyle()
                .set("overflow", "hidden")
                .set("height", "100%")
                .set("flex", "1");
        mainLayout.expand(graphContainer);

        return mainLayout;
    }

    private Component createLegendCard() {
        VerticalLayout legendLayout = new VerticalLayout();
        legendLayout.setSpacing(false);
        legendLayout.setPadding(false);
        legendLayout.getStyle()
                .set("background-color", "rgba(255, 255, 255, 0.95)")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 12px rgba(0, 0, 0, 0.08)")
                .set("border", "1px solid #E2E8F0")
                .set("backdrop-filter", "blur(8px)")
                .set("padding", "14px");

        H4 title = new H4("Categorías");
        title.getStyle().set("margin-top", "0").set("margin-bottom", "8px").set("font-size", "13px").set("color", "#0F172A");
        legendLayout.add(title);

        StaticConceptData.NODES.stream()
                .map(StaticConceptData.NodeDto::group)
                .distinct()
                .forEach(category -> addLegendItem(legendLayout, category, getCategoryColor(category)));

        return legendLayout;
    }

    private void addLegendItem(VerticalLayout container, String name, String color) {
        Span colorDot = new Span();
        colorDot.getStyle()
                .set("display", "inline-block")
                .set("width", "10px")
                .set("height", "10px")
                .set("border-radius", "50%")
                .set("background-color", color)
                .set("margin-right", "8px")
                .set("flex-shrink", "0");

        Span text = new Span(name);
        text.getStyle().set("font-size", "12px").set("color", "#334155");

        HorizontalLayout item = new HorizontalLayout(colorDot, text);
        item.setAlignItems(Alignment.CENTER);
        item.setSpacing(false);
        item.getStyle().set("padding-bottom", "4px");
        container.add(item);
    }

    private Component createGraphControls() {
        Button zoomIn = new Button(new Icon(VaadinIcon.PLUS), e -> graphComponent.zoomIn());
        zoomIn.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        zoomIn.setTooltipText("Acercar");

        Button zoomOut = new Button(new Icon(VaadinIcon.MINUS), e -> graphComponent.zoomOut());
        zoomOut.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        zoomOut.setTooltipText("Alejar");

        Button expand = new Button(new Icon(VaadinIcon.EXPAND_FULL), e -> graphComponent.fitGraph());
        expand.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        expand.setTooltipText("Centrar grafo");

        VerticalLayout controls = new VerticalLayout(zoomIn, zoomOut, expand);
        controls.setSpacing(false);
        controls.setPadding(false);
        controls.getStyle()
                .set("background-color", "rgba(255, 255, 255, 0.95)")
                .set("border-radius", "10px")
                .set("box-shadow", "0 4px 12px rgba(0, 0, 0, 0.08)")
                .set("border", "1px solid #E2E8F0")
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

        VerticalLayout contentWrapper = new VerticalLayout();
        contentWrapper.setPadding(false);
        contentWrapper.setSpacing(false);
        contentWrapper.getStyle().set("overflow-y", "auto");

        Button closeButton = new Button(new Icon(VaadinIcon.CLOSE_SMALL), e -> sidebar.setVisible(false));
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Span categoryBadge = new Span("• " + concept.category());
        categoryBadge.getStyle()
                .set("color", getCategoryColor(concept.category()))
                .set("background-color", getCategoryBackgroundColor(concept.category()))
                .set("padding", "4px 8px")
                .set("border-radius", "12px")
                .set("font-size", "11px")
                .set("font-weight", "600");

        HorizontalLayout header = new HorizontalLayout(categoryBadge, closeButton);
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);
        header.getStyle().set("padding", "12px 18px");

        H3 title = new H3(concept.name());
        title.getStyle().set("margin", "0").set("font-size", "17px").set("font-weight", "700");
        Paragraph description = new Paragraph(concept.description());
        description.getStyle().set("color", "#64748B").set("font-size", "13px").set("margin-top", "6px");

        VerticalLayout infoSection = new VerticalLayout(title, description);
        infoSection.setSpacing(false);
        infoSection.setPadding(false);
        infoSection.getStyle().set("padding", "0 18px");

        Div separator1 = createSeparator();
        Div separator2 = createSeparator();

        contentWrapper.add(
                infoSection,
                separator1,
                createRelatedConceptsSection(concept),
                separator2,
                createContentItemsSection(concept));

        Component actionButtons = createSidebarActionButtons();
        Div footer = new Div(actionButtons);
        footer.getStyle()
                .set("padding", "14px 18px")
                .set("background-color", "#F8FAFC")
                .set("border-top", "1px solid #E2E8F0");

        sidebar.add(header, contentWrapper, footer);
        sidebar.expand(contentWrapper);
    }

    private VerticalLayout createRelatedConceptsSection(StaticConceptData.ConceptDetail concept) {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(false);
        section.getStyle().set("padding", "0 18px");

        H4 title = new H4("Conceptos Relacionados");
        title.getStyle().set("margin", "0 0 8px 0").set("font-size", "13px");
        section.add(title);

        if (concept.relatedConcepts().isEmpty()) {
            Span empty = new Span("No hay conceptos relacionados.");
            empty.getStyle().set("font-size", "12px").set("color", "#94A3B8");
            section.add(empty);
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
                        .set("margin-right", "8px");

                Span name = new Span(related);
                name.getStyle().set("font-size", "13px").set("color", "#334155");

                Icon arrow = new Icon(VaadinIcon.ARROW_RIGHT);
                arrow.setSize("12px");
                arrow.getStyle().set("color", "#94A3B8");

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
        section.getStyle().set("padding", "0 18px");

        H4 title = new H4("Contenidos con este Concepto");
        title.getStyle().set("margin", "0 0 8px 0").set("font-size", "13px");
        section.add(title);

        if (concept.contents().isEmpty()) {
            Span empty = new Span("No hay contenidos asociados.");
            empty.getStyle().set("font-size", "12px").set("color", "#94A3B8");
            section.add(empty);
        } else {
            concept.contents().forEach(contentName -> {
                Span contentTitle = new Span(contentName);
                contentTitle.getStyle().set("font-weight", "500").set("font-size", "13px").set("color", "#0F172A");
                Span meta = new Span("Recurso técnico guardado");
                meta.getStyle().set("font-size", "11px").set("color", "#64748B");

                VerticalLayout card = new VerticalLayout(contentTitle, meta);
                card.setSpacing(false);
                card.setPadding(false);
                card.getStyle()
                        .set("border", "1px solid #E2E8F0")
                        .set("border-radius", "8px")
                        .set("padding", "10px")
                        .set("background", "#F8FAFC");
                section.add(card);
            });
        }
        return section;
    }

    private Component createSidebarActionButtons() {
        Button consultAI = new Button("Consultar IA sobre este Concepto", new Icon(VaadinIcon.MAGIC));
        consultAI.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        consultAI.getStyle().set("background-color", "#10B981").set("font-size", "12px");
        consultAI.setWidthFull();

        VerticalLayout layout = new VerticalLayout(consultAI);
        layout.setSpacing(true);
        layout.setPadding(false);
        return layout;
    }

    private Div createSeparator() {
        Div separator = new Div();
        separator.setHeight("1px");
        separator.setWidthFull();
        separator.getStyle()
                .set("background-color", "#E2E8F0")
                .set("margin", "12px 0");
        return separator;
    }

    private String getCategoryColor(String category) {
        return StaticConceptData.getCategoryColor(category);
    }

    private String getCategoryBackgroundColor(String category) {
        return StaticConceptData.getCategoryBackgroundColor(category);
    }
}
