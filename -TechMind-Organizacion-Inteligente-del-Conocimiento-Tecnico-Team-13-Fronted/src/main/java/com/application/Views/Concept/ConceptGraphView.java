package com.application.Views.Concept;

import com.application.Views.Layout.MainLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Concept Graph - KnowBase")
@Route(value = "concept-graph", layout = MainLayout.class)
public class ConceptGraphView extends VerticalLayout {

    public ConceptGraphView() {
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

        // Área de gráfico
        VerticalLayout graphContainer = createGraphContainer();
        graphContainer.getStyle().set("flex", "1");
        add(graphContainer);
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);

        VerticalLayout titles = new VerticalLayout();
        titles.setPadding(false);
        titles.setSpacing(false);
        H2 mainTitle = new H2("Grafo de Conceptos");
        mainTitle.getStyle().set("margin", "0").set("font-size", "24px").set("color", "#0f172a");
        Span subtitle = new Span("Visualización de relaciones entre conceptos e ideas");
        subtitle.getStyle().set("color", "#64748b").set("font-size", "14px");
        titles.add(mainTitle, subtitle);

        header.add(titles);
        return header;
    }

    private VerticalLayout createGraphContainer() {
        VerticalLayout container = new VerticalLayout();
        container.setWidthFull();
        container.setHeight("600px");
        container.getStyle()
                .set("background-color", "#ffffff")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "12px")
                .set("padding", "20px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("position", "relative");

        // Área de visualización (placeholder)
        Div graphArea = new Div();
        graphArea.getStyle()
                .set("width", "100%")
                .set("height", "100%")
                .set("background", "linear-gradient(135deg, #f3e8ff 0%, #ecfeff 100%)")
                .set("border-radius", "8px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("color", "#64748b");

        Paragraph placeholder = new Paragraph("Grafo interactivo de conceptos - Próximamente");
        placeholder.getStyle().set("text-align", "center").set("font-size", "14px");
        graphArea.add(placeholder);

        // Panel de información
        VerticalLayout infoPanel = createInfoPanel();

        container.add(graphArea);
        add(infoPanel);

        return container;
    }

    private VerticalLayout createInfoPanel() {
        VerticalLayout panel = new VerticalLayout();
        panel.getStyle()
                .set("background-color", "#ffffff")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "12px")
                .set("padding", "20px")
                .set("margin-top", "20px");

        H4 title = new H4("Estadísticas del Grafo");
        title.getStyle().set("margin-top", "0");

        FlexLayout stats = new FlexLayout();
        stats.setWidthFull();
        stats.getStyle().set("gap", "20px").set("flex-wrap", "wrap");

        stats.add(createStatCard("Conceptos Totales", "342"));
        stats.add(createStatCard("Relaciones", "1,247"));
        stats.add(createStatCard("Clusters", "18"));
        stats.add(createStatCard("Actualizaciones", "Hace 2h"));

        panel.add(title, stats);
        return panel;
    }

    private VerticalLayout createStatCard(String label, String value) {
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
                .set("background-color", "#f8fafc")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "8px")
                .set("padding", "15px")
                .set("flex", "1")
                .set("min-width", "150px");
        card.setSpacing(false);

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "12px")
                .set("color", "#64748b")
                .set("font-weight", "500");

        H3 valueHeader = new H3(value);
        valueHeader.getStyle()
                .set("margin", "5px 0 0 0")
                .set("color", "#00b894")
                .set("font-size", "24px");

        card.add(labelSpan, valueHeader);
        return card;
    }
}
