package com.application.Views.Concept;

import com.application.Views.Layout.MainLayout;
import com.application.service.ConceptGraphService;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Clusters de contenido por similitud de embeddings (mismos vectores que usa ContenidoService
 * para "relacionados" y RagChatService para el RAG). Layout calculado en el servidor y renderizado
 * como SVG estático: sin dependencias externas, sin JS de terceros, cero riesgo de red en la demo.
 */
@PageTitle("Concept Graph - KnowBase")
@Route(value = "concept-graph", layout = MainLayout.class)
public class ConceptGraphView extends VerticalLayout implements BeforeEnterObserver {

    private static final String[] PALETTE = {
            "#0284c7", "#8a2be2", "#d97706", "#059669", "#dc2626",
            "#7c3aed", "#0891b2", "#db2777", "#65a30d", "#334155"
    };
    private static final int MAX_ARISTAS_POR_NODO = 4;

    private final ConceptGraphService conceptGraphService;
    private VerticalLayout graphContainer;
    private VerticalLayout infoPanel;

    public ConceptGraphView(ConceptGraphService conceptGraphService) {
        this.conceptGraphService = conceptGraphService;

        setSizeFull();
        setMargin(false);
        setSpacing(true);
        setPadding(false);
        getStyle()
                .set("overflow-y", "auto")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("padding", "20px");

        HorizontalLayout header = createHeader();
        header.setHeight("auto");
        add(header);

        graphContainer = new VerticalLayout();
        graphContainer.setWidthFull();
        graphContainer.setHeight("600px");
        graphContainer.getStyle()
                .set("background-color", "#ffffff")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "12px")
                .set("padding", "20px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("position", "relative");
        graphContainer.getStyle().set("flex", "1");
        add(graphContainer);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        ConceptGraphService.Graph graph = conceptGraphService.build(MAX_ARISTAS_POR_NODO);
        renderGraph(graph);
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
        Span subtitle = new Span("Clusters de contenido por similitud de embeddings (pasa el mouse sobre un nodo)");
        subtitle.getStyle().set("color", "#64748b").set("font-size", "14px");
        titles.add(mainTitle, subtitle);

        header.add(titles);
        return header;
    }

    private void renderGraph(ConceptGraphService.Graph graph) {
        graphContainer.removeAll();
        if (infoPanel != null) {
            remove(infoPanel);
        }

        if (graph.nodos().isEmpty()) {
            Paragraph placeholder = new Paragraph("Todavía no hay contenido con embedding calculado. " +
                    "Guarda algo desde \"Añadir Contenido\" para ver el grafo.");
            placeholder.getStyle().set("text-align", "center").set("color", "#64748b").set("font-size", "14px");
            graphContainer.add(placeholder);
            return;
        }

        graphContainer.add(new Html(buildSvg(graph)));

        infoPanel = createInfoPanel(graph);
        add(infoPanel);
    }

    private VerticalLayout createInfoPanel(ConceptGraphService.Graph graph) {
        long clusters = graph.nodos().stream().map(ConceptGraphService.Node::categoria).distinct().count();

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

        stats.add(createStatCard("Conceptos Totales", String.valueOf(graph.nodos().size())));
        stats.add(createStatCard("Relaciones", String.valueOf(graph.aristas().size())));
        stats.add(createStatCard("Clusters", String.valueOf(clusters)));

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

    private String buildSvg(ConceptGraphService.Graph graph) {
        Map<String, List<ConceptGraphService.Node>> porCategoria = graph.nodos().stream()
                .collect(Collectors.groupingBy(ConceptGraphService.Node::categoria, LinkedHashMap::new, Collectors.toList()));

        double width = 900, height = 560;
        double cx = width / 2, cy = height / 2;
        double clusterRadius = Math.min(width, height) / 2.0 - 90;

        List<String> categorias = new ArrayList<>(porCategoria.keySet());
        int numCategorias = categorias.size();
        Map<Long, double[]> posiciones = new HashMap<>();

        for (int i = 0; i < numCategorias; i++) {
            String categoria = categorias.get(i);
            double angle = (2 * Math.PI * i) / numCategorias;
            double anchorX = cx + clusterRadius * Math.cos(angle);
            double anchorY = cy + clusterRadius * Math.sin(angle);

            List<ConceptGraphService.Node> nodos = porCategoria.get(categoria);
            double subRadius = Math.min(70, 18 + nodos.size() * 6);
            for (int j = 0; j < nodos.size(); j++) {
                double subAngle = nodos.size() == 1 ? 0 : (2 * Math.PI * j) / nodos.size();
                double x = clamp(anchorX + subRadius * Math.cos(subAngle), 20, width - 20);
                double y = clamp(anchorY + subRadius * Math.sin(subAngle), 20, height - 20);
                posiciones.put(nodos.get(j).id(), new double[]{x, y});
            }
        }

        StringBuilder svg = new StringBuilder();
        svg.append("<svg viewBox=\"0 0 ").append((int) width).append(" ").append((int) height)
                .append("\" xmlns=\"http://www.w3.org/2000/svg\" style=\"width:100%;height:100%\">");

        for (ConceptGraphService.Edge edge : graph.aristas()) {
            double[] a = posiciones.get(edge.origenId());
            double[] b = posiciones.get(edge.destinoId());
            if (a == null || b == null) {
                continue;
            }
            double opacity = Math.max(0.12, Math.min(0.8, edge.similitud()));
            svg.append(String.format(Locale.US,
                    "<line x1=\"%.1f\" y1=\"%.1f\" x2=\"%.1f\" y2=\"%.1f\" stroke=\"#94a3b8\" stroke-width=\"1\" stroke-opacity=\"%.2f\" />",
                    a[0], a[1], b[0], b[1], opacity));
        }

        for (ConceptGraphService.Node nodo : graph.nodos()) {
            double[] pos = posiciones.get(nodo.id());
            if (pos == null) {
                continue;
            }
            svg.append(String.format(Locale.US,
                    "<circle cx=\"%.1f\" cy=\"%.1f\" r=\"8\" fill=\"%s\" stroke=\"#ffffff\" stroke-width=\"1.5\"><title>%s (%s)</title></circle>",
                    pos[0], pos[1], colorFor(nodo.categoria()), escapeXml(nodo.titulo()), escapeXml(nodo.categoria())));
        }

        for (int i = 0; i < numCategorias; i++) {
            String categoria = categorias.get(i);
            double angle = (2 * Math.PI * i) / numCategorias;
            double labelX = clamp(cx + (clusterRadius + 55) * Math.cos(angle), 40, width - 40);
            double labelY = clamp(cy + (clusterRadius + 55) * Math.sin(angle), 15, height - 5);
            svg.append(String.format(Locale.US,
                    "<text x=\"%.1f\" y=\"%.1f\" font-size=\"11\" fill=\"%s\" font-weight=\"700\" text-anchor=\"middle\">%s</text>",
                    labelX, labelY, colorFor(categoria), escapeXml(categoria)));
        }

        svg.append("</svg>");
        return svg.toString();
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private String colorFor(String categoria) {
        int idx = Math.floorMod(categoria.hashCode(), PALETTE.length);
        return PALETTE[idx];
    }

    private String escapeXml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
