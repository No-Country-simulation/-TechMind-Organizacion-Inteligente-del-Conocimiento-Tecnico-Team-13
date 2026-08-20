package com.application.Views.Annotations;

import com.application.model.Contenido;
import com.application.model.User;
import com.application.service.ContenidoService;
import com.application.service.UserSession;
import com.application.Views.Layout.MainLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@PageTitle("Annotations - KnowBase")
@Route(value = "annotations", layout = MainLayout.class)
public class AnnotationsView extends VerticalLayout implements BeforeEnterObserver {

    private final ContenidoService contenidoService;
    private final UserSession userSession;
    private VerticalLayout annotationsList;
    private H3 totalStatValue;

    public AnnotationsView(ContenidoService contenidoService, UserSession userSession) {
        this.contenidoService = contenidoService;
        this.userSession = userSession;

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

        // Contenido principal
        VerticalLayout content = createContent();
        content.setHeight("auto");
        add(content);
    }

    /**
     * Ciclo de vida de Vaadin: Se ejecuta justo antes de mostrar la vista al usuario.
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        loadDataForAuthenticatedUser();
    }

    private void loadDataForAuthenticatedUser() {
        Optional<User> maybeUser = Optional.ofNullable(userSession.getAuthenticatedUser());

        if (maybeUser.isEmpty()) {
            // Si el usuario no está autenticado en la sesión, mostrar mensaje de aviso o redirigir
            annotationsList.removeAll();
            annotationsList.add(new Span("No hay una sesión activa. Inicia sesión para ver tus contenidos."));
            return;
        }

        UUID userId = maybeUser.get().getId();

        // Cargar contenidos desde la tabla contenido (Supabase Postgres, vía JPA)
        List<Contenido> contents = contenidoService.listarPorUsuario(userId);
        annotationsList.removeAll();

        if (contents == null || contents.isEmpty()) {
            annotationsList.add(new Span("No se encontraron registros o contenidos para este usuario."));
            totalStatValue.setText("0");
            return;
        }

        // Actualizar estadísticas y renderizar elementos
        totalStatValue.setText(String.valueOf(contents.size()));

        contents.forEach(content -> {
            String timeAgo = formatTimeAgo(content.getFechaCreacion());
            String snippet = content.getTexto() != null && !content.getTexto().isBlank()
                    ? content.getTexto()
                    : "Archivo adjunto: " + content.getStoragePath();

            annotationsList.add(
                    createAnnotationItem(content.getTitulo(), snippet, timeAgo)
            );
        });
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

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);

        VerticalLayout titles = new VerticalLayout();
        titles.setPadding(false);
        titles.setSpacing(false);
        H2 mainTitle = new H2("Anotaciones y Contenidos");
        mainTitle.getStyle().set("margin", "0").set("font-size", "24px").set("color", "#0f172a");
        Span subtitle = new Span("Documentos y registros sincronizados desde Supabase");
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

        // Tarjetas de estadísticas
        HorizontalLayout stats = new HorizontalLayout();
        stats.setWidthFull();
        stats.setSpacing(true);
        stats.setJustifyContentMode(JustifyContentMode.START);

        totalStatValue = new H3("0");
        totalStatValue.getStyle()
                .set("margin", "10px 0 0 0")
                .set("color", "#00b894")
                .set("font-size", "24px");

        stats.add(createStatCard("Contenidos Totales", totalStatValue));
        container.add(stats);

        // Lista de anotaciones/contenidos
        annotationsList = createAnnotationsList();
        container.add(annotationsList);

        return container;
    }

    private VerticalLayout createStatCard(String label, H3 valueHeader) {
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

        card.add(labelSpan, valueHeader);
        return card;
    }

    private VerticalLayout createAnnotationsList() {
        VerticalLayout container = new VerticalLayout();
        container.setWidthFull();
        container.setPadding(false);
        container.setSpacing(true);
        container.getStyle().set("margin-top", "30px");

        H4 title = new H4("Registros Guardados");
        title.getStyle().set("margin-top", "0").set("color", "#0f172a");
        container.add(title);

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

        H5 titleHeader = new H5(title != null ? title : "Sin título");
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
