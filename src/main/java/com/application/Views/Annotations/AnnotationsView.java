package com.application.Views.Annotations;

import com.application.Views.Layout.MainLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Annotations - KnowBase")
@Route(value = "annotations", layout = MainLayout.class)
public class AnnotationsView extends VerticalLayout {

    public AnnotationsView() {
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

        // Contenido
        VerticalLayout content = createContent();
        content.setHeight("auto");
        add(content);
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);

        VerticalLayout titles = new VerticalLayout();
        titles.setPadding(false);
        titles.setSpacing(false);
        H2 mainTitle = new H2("Anotaciones");
        mainTitle.getStyle().set("margin", "0").set("font-size", "24px").set("color", "#0f172a");
        Span subtitle = new Span("Notas y comentarios personalizados sobre contenido");
        subtitle.getStyle().set("color", "#64748b").set("font-size", "14px");
        titles.add(mainTitle, subtitle);

        header.add(titles);
        return header;
    }

    private VerticalLayout createContent() {
        VerticalLayout container = new VerticalLayout();
        container.getStyle()
                .set("background-color", "#ffffff")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "12px")
                .set("padding", "30px");

        // Estadísticas
        HorizontalLayout stats = new HorizontalLayout();
        stats.setWidthFull();
        stats.setSpacing(true);
        stats.setJustifyContentMode(JustifyContentMode.START);

        stats.add(createStatCard("Anotaciones Totales", "156"));
        stats.add(createStatCard("Compartidas", "24"));
        stats.add(createStatCard("Esta Semana", "12"));

        container.add(stats);

        // Lista de anotaciones
        VerticalLayout annotationsList = createAnnotationsList();
        container.add(annotationsList);

        return container;
    }

    private VerticalLayout createStatCard(String label, String value) {
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
                .set("background-color", "#f8fafc")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "8px")
                .set("padding", "20px")
                .set("min-width", "150px");
        card.setSpacing(false);

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "12px")
                .set("color", "#64748b")
                .set("font-weight", "500");

        H3 valueHeader = new H3(value);
        valueHeader.getStyle()
                .set("margin", "10px 0 0 0")
                .set("color", "#00b894")
                .set("font-size", "24px");

        card.add(labelSpan, valueHeader);
        return card;
    }

    private VerticalLayout createAnnotationsList() {
        VerticalLayout container = new VerticalLayout();
        container.setWidthFull();
        container.setPadding(false);
        container.setSpacing(true);
        container.getStyle().set("margin-top", "30px");

        H4 title = new H4("Anotaciones Recientes");
        title.getStyle().set("margin-top", "0").set("color", "#0f172a");
        container.add(title);

        container.add(createAnnotationItem("Spring Boot Security", "Referencia a OAuth2 implementación", "hace 2h"));
        container.add(createAnnotationItem("Kubernetes ConfigMaps", "Ejemplo con variables de entorno", "hace 1 día"));
        container.add(createAnnotationItem("Java 21 Features", "Virtual Threads y Pattern Matching", "hace 3 días"));

        return container;
    }

    private VerticalLayout createAnnotationItem(String title, String note, String date) {
        VerticalLayout item = new VerticalLayout();
        item.getStyle()
                .set("background-color", "#f8fafc")
                .set("border", "1px solid #e2e8f0")
                .set("border-left", "4px solid #00b894")
                .set("border-radius", "8px")
                .set("padding", "15px");
        item.setSpacing(false);

        HorizontalLayout titleRow = new HorizontalLayout();
        titleRow.setWidthFull();
        titleRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
        titleRow.setAlignItems(Alignment.CENTER);

        H5 titleHeader = new H5(title);
        titleHeader.getStyle().set("margin", "0").set("color", "#0f172a");

        Span dateSpan = new Span(date);
        dateSpan.getStyle().set("font-size", "12px").set("color", "#94a3b8");

        titleRow.add(titleHeader, dateSpan);

        Span noteSpan = new Span(note);
        noteSpan.getStyle().set("color", "#64748b").set("font-size", "13px");

        item.add(titleRow, noteSpan);
        return item;
    }
}
