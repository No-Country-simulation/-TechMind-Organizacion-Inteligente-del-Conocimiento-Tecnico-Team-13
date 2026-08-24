package com.application.Views.Concept;

import com.application.Views.AI.AiConsultantView;
import com.application.Views.Layout.MainLayout;
import com.application.Views.Library.LibraryView;
import com.application.model.User;
import com.application.service.ConceptGraphService;
import com.application.service.UserSession;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Grafo de conceptos: nodos = Contenido guardado, aristas = similitud coseno de sus embeddings
 * (mismos datos que "relacionados" en ContenidoService y el contexto de RagChatService). Reusa el
 * componente vis-network que ya trae main; solo cambia de dónde saca los datos.
 */
@PageTitle("Concept Graph")
@Route(value = "concept-graph", layout = MainLayout.class)
public class ConceptGraphView extends VerticalLayout implements BeforeEnterObserver {

    private static final Map<String, String> CATEGORY_COLORS = Map.ofEntries(
            Map.entry("Backend", "#8a2be2"),
            Map.entry("Frontend", "#059669"),
            Map.entry("Cloud Computing", "#0284c7"),
            Map.entry("Databases", "#0891b2"),
            Map.entry("Data Analysis", "#7c3aed"),
            Map.entry("Cybersecurity", "#dc2626"),
            Map.entry("Artificial Intelligence", "#db2777"),
            Map.entry("Software Architecture", "#d97706"),
            Map.entry("Q/A", "#64748b")
    );

    private final ConceptGraphService conceptGraphService;
    private final UserSession userSession;
    private final ConceptGraphComponent graphComponent;
    private final VerticalLayout sidebar;

    private Map<String, ConceptGraphService.Node> nodesById = new HashMap<>();
    private Map<String, List<ConceptGraphService.Node>> vecinosPorNodo = new HashMap<>();
    private ConceptGraphService.Node nodoSeleccionado;

    public ConceptGraphView(ConceptGraphService conceptGraphService, UserSession userSession) {
        this.conceptGraphService = conceptGraphService;
        this.userSession = userSession;

        // Sin esto la altura de la vista colapsa a 0: graphContainer solo tiene hijos con
        // position:absolute (graphComponent y la leyenda), que no aportan altura a su padre, así
        // que expand(mainContent) no tiene nada que expandir. El grafo (a 100% de un contenedor de
        // 0px) queda invisible; la leyenda se ve igual porque, al ser absoluta, escapa del
        // contenedor de altura 0 en vez de quedar recortada por él.
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        graphComponent = new ConceptGraphComponent();
        sidebar = createSidebar();

        Component header = createHeader();
        Component mainContent = createMainContent();

        add(header, mainContent);
        expand(mainContent);

        graphComponent.addNodeSelectedListener(event -> showSidebarDetails(event.getNodeId()));
        graphComponent.addNodeDeselectedListener(event -> sidebar.setVisible(false));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        loadGraphData();
    }

    private Component createHeader() {
        H2 title = new H2("Concept Graph");
        title.getStyle().set("margin", "0");
        Span subtitle = new Span("Clusters de contenido por similitud de embeddings");
        subtitle.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        VerticalLayout titleContainer = new VerticalLayout(title, subtitle);
        titleContainer.setPadding(false);
        titleContainer.setSpacing(false);
        titleContainer.setAlignItems(Alignment.START);

        TextField search = new TextField();
        search.setPlaceholder("Buscar en la biblioteca... (Ctrl+K)");
        search.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        search.setWidth("300px");
        search.addKeyDownListener(com.vaadin.flow.component.Key.ENTER,
                e -> UI.getCurrent().navigate(LibraryView.class));

        Button notificationsButton = new Button(new Icon(VaadinIcon.BELL));
        notificationsButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button settingsButton = new Button(new Icon(VaadinIcon.COG));
        settingsButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout toolbar = new HorizontalLayout(search, notificationsButton, settingsButton);
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
        layout.setVisible(false);
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

        graphContainer.add(graphComponent, legend);

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

        CATEGORY_COLORS.forEach((name, color) -> addLegendItem(legendLayout, name, color));

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
        text.getStyle().set("vertical-align", "middle").set("font-size", "13px");

        HorizontalLayout item = new HorizontalLayout(colorDot, text);
        item.setAlignItems(Alignment.CENTER);
        item.setSpacing(false);
        item.getStyle().set("padding-bottom", "4px");
        container.add(item);
    }

    private void loadGraphData() {
        User authenticatedUser = userSession != null ? userSession.getAuthenticatedUser() : null;
        if (authenticatedUser == null) {
            nodesById = new HashMap<>();
            vecinosPorNodo = new HashMap<>();
            graphComponent.setGraphData(List.of(), List.of());
            return;
        }
        UUID userId = authenticatedUser.getId();
        ConceptGraphService.Graph graph = conceptGraphService.build(userId, 4);

        nodesById = new HashMap<>();
        vecinosPorNodo = new HashMap<>();
        for (ConceptGraphService.Node nodo : graph.nodos()) {
            nodesById.put(nodo.id().toString(), nodo);
            vecinosPorNodo.put(nodo.id().toString(), new ArrayList<>());
        }

        List<ConceptGraphComponent.NodeDto> nodeDtos = graph.nodos().stream()
                .map(n -> new ConceptGraphComponent.NodeDto(n.id().toString(), n.titulo(), n.categoria()))
                .toList();

        List<ConceptGraphComponent.EdgeDto> edgeDtos = new ArrayList<>();
        for (ConceptGraphService.Edge arista : graph.aristas()) {
            String origen = arista.origenId().toString();
            String destino = arista.destinoId().toString();
            edgeDtos.add(new ConceptGraphComponent.EdgeDto(origen, destino));

            ConceptGraphService.Node nodoDestino = nodesById.get(destino);
            ConceptGraphService.Node nodoOrigen = nodesById.get(origen);
            if (nodoDestino != null) {
                vecinosPorNodo.get(origen).add(nodoDestino);
            }
            if (nodoOrigen != null) {
                vecinosPorNodo.get(destino).add(nodoOrigen);
            }
        }

        graphComponent.setGraphData(nodeDtos, edgeDtos);
    }

    private void showSidebarDetails(String nodeId) {
        sidebar.removeAll();

        ConceptGraphService.Node nodo = nodesById.get(nodeId);
        if (nodo == null) {
            sidebar.setVisible(false);
            return;
        }
        nodoSeleccionado = nodo;
        sidebar.setVisible(true);

        VerticalLayout contentWrapper = new VerticalLayout();
        contentWrapper.setPadding(false);
        contentWrapper.setSpacing(false);
        contentWrapper.getStyle().set("overflow-y", "auto");

        Button closeButton = new Button(new Icon(VaadinIcon.CLOSE_SMALL), e -> sidebar.setVisible(false));
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Span categoryBadge = new Span("• " + nodo.categoria());
        categoryBadge.getElement().getThemeList().add("badge");
        categoryBadge.getStyle()
                .set("color", getCategoryColor(nodo.categoria()))
                .set("background-color", getCategoryColor(nodo.categoria()) + "1A")
                .set("padding", "4px 8px")
                .set("border-radius", "12px")
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("font-weight", "500");

        HorizontalLayout header = new HorizontalLayout(categoryBadge, closeButton);
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);
        header.getStyle().set("padding", "12px 24px");

        H3 title = new H3(nodo.titulo());
        title.getStyle().set("margin", "0");
        Paragraph description = new Paragraph(nodo.resumen() != null && !nodo.resumen().isBlank()
                ? nodo.resumen() : "Sin texto disponible.");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        VerticalLayout infoSection = new VerticalLayout(title, description);
        infoSection.setSpacing(false);
        infoSection.setPadding(false);
        infoSection.getStyle().set("padding", "0 24px");

        contentWrapper.add(infoSection, createSeparator(), createRelatedConceptsSection(nodo));

        Component actionButtons = createSidebarActionButtons();
        Div footer = new Div(actionButtons);
        footer.getStyle()
                .set("padding", "16px 24px")
                .set("background-color", "var(--lumo-contrast-5pct)");

        sidebar.add(header, contentWrapper, footer);
        sidebar.expand(contentWrapper);
    }

    private VerticalLayout createRelatedConceptsSection(ConceptGraphService.Node nodo) {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(false);
        section.getStyle().set("padding", "0 24px");

        H4 title = new H4("Contenido relacionado");
        title.getStyle().set("margin", "0 0 8px 0");
        section.add(title);

        List<ConceptGraphService.Node> vecinos = vecinosPorNodo.getOrDefault(nodo.id().toString(), List.of());
        if (vecinos.isEmpty()) {
            section.add(new Span("No hay contenido lo bastante parecido todavía."));
        } else {
            vecinos.forEach(vecino -> {
                Span colorDot = new Span();
                colorDot.getStyle()
                        .set("display", "inline-block")
                        .set("width", "8px")
                        .set("height", "8px")
                        .set("border-radius", "50%")
                        .set("background-color", getCategoryColor(vecino.categoria()))
                        .set("margin-right", "12px");

                Span name = new Span(vecino.titulo());
                Icon arrow = new Icon(VaadinIcon.ARROW_RIGHT);
                arrow.getStyle().set("color", "var(--lumo-contrast-50pct)");

                HorizontalLayout row = new HorizontalLayout(colorDot, name, arrow);
                row.setAlignItems(Alignment.CENTER);
                row.expand(name);
                row.getStyle().set("cursor", "pointer").set("padding", "4px 0");
                row.addClickListener(e -> showSidebarDetails(vecino.id().toString()));
                section.add(row);
            });
        }
        return section;
    }

    private Component createSidebarActionButtons() {
        Button consultAI = new Button("Consultar IA sobre este contenido", new Icon(VaadinIcon.MAGIC));
        consultAI.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        consultAI.setWidthFull();
        consultAI.addClickListener(e -> UI.getCurrent().navigate(AiConsultantView.class));

        Button viewInLibrary = new Button("Ver en Biblioteca", new Icon(VaadinIcon.BOOK));
        viewInLibrary.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        viewInLibrary.setWidthFull();
        viewInLibrary.addClickListener(e -> UI.getCurrent().navigate(LibraryView.class));

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
        return CATEGORY_COLORS.getOrDefault(category, "#64748b");
    }
}
