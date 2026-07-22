package com.application.Views.Dashboard;


import com.application.Views.Layout.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Dashboard - KnowBase")
@Route(value = "dashboard", layout = MainLayout.class)
public class DashboardView extends VerticalLayout {

    public DashboardView() {
        // Configuración de la vista
        setSizeFull();
        setMargin(false);
        setSpacing(true);
        setPadding(false);
        getStyle()
                .set("overflow-y", "auto")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("padding", "20px");

        // Cabecera principal
        HorizontalLayout header = createHeader();
        header.setHeight("auto");
        add(header);

        // Fila 1: Tarjetas KPI
        HorizontalLayout kpiRow = createKpiRow();
        kpiRow.setHeight("auto");
        add(kpiRow);

        // Fila 2: Mapa de calor de temas + Actividad reciente
        HorizontalLayout middleRow = createMiddleRow();
        middleRow.setHeight("auto");
        middleRow.getStyle().set("flex", "1");
        add(middleRow);

        // Fila 3: Último Procesado + Recomendaciones + Acciones rápidas
        HorizontalLayout bottomRow = createBottomRow();
        bottomRow.setHeight("auto");
        add(bottomRow);
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        // Títulos
        VerticalLayout titles = new VerticalLayout();
        titles.setPadding(false);
        titles.setSpacing(false);
        H2 mainTitle = new H2("Panel de Control");
        mainTitle.getStyle().set("margin", "0").set("font-size", "24px").set("color", "#0f172a");
        Span subtitle = new Span("Visión inteligente de tu base de conocimiento");
        subtitle.getStyle().set("color", "#64748b").set("font-size", "14px");
        titles.add(mainTitle, subtitle);

        // Buscador y Botón acción
        HorizontalLayout actions = new HorizontalLayout();
        actions.setAlignItems(Alignment.CENTER);

        TextField searchField = new TextField();
        searchField.setPlaceholder("Busca concepto, tema o pr...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("280px");

        Button analyzeBtn = new Button("Analizar Contenido", VaadinIcon.MAGIC.create());
        analyzeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        analyzeBtn.getStyle().set("background-color", "#00b894").set("color", "#ffffff");

        Icon bellIcon = VaadinIcon.BELL_O.create();
        bellIcon.getStyle().set("cursor", "pointer");

        actions.add(searchField, analyzeBtn, bellIcon);
        header.add(titles, actions);
        return header;
    }

    private HorizontalLayout createKpiRow() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(true);

        row.add(createKpiCard("Total Items", "1,284", "+48 este mes", VaadinIcon.DATABASE, "#a855f7", "#f3e8ff"));
        row.add(createKpiCard("Tags Generadas", "3,740", "+188", VaadinIcon.TAGS, "#06b6d4", "#ecfeff"));
        row.add(createKpiCard("Consultas", "87", "Esta semana", VaadinIcon.SEARCH, "#f59e0b", "#fef3c7"));
        row.add(createKpiCard("Precisión IA", "91.4%", "+0.8% vs mes anterior", VaadinIcon.CHECK_CIRCLE, "#10b981", "#d1fae5"));

        return row;
    }

    private VerticalLayout createKpiCard(String title, String value, String subtext, VaadinIcon icon, String iconColor, String iconBg) {
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
                .set("background-color", "#ffffff")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "12px")
                .set("box-shadow", "0 1px 3px rgba(0,0,0,0.05)")
                .set("padding", "20px");
        card.setSpacing(false);

        HorizontalLayout top = new HorizontalLayout();
        top.setWidthFull();
        top.setJustifyContentMode(JustifyContentMode.BETWEEN);
        top.setAlignItems(Alignment.CENTER);

        Span titleSpan = new Span(title);
        titleSpan.getStyle().set("color", "#64748b").set("font-size", "14px").set("font-weight", "500");

        Div iconWrapper = new Div();
        Icon kpiIcon = icon.create();
        kpiIcon.setColor(iconColor);
        iconWrapper.add(kpiIcon);
        iconWrapper.getStyle()
                .set("background-color", iconBg)
                .set("border-radius", "8px")
                .set("padding", "8px")
                .set("display", "flex")
                .set("align-items", "center");

        top.add(titleSpan, iconWrapper);

        H3 valueHeader = new H3(value);
        valueHeader.getStyle().set("margin", "10px 0 5px 0").set("font-size", "28px").set("color", "#0f172a");

        Span subtextSpan = new Span(subtext);
        subtextSpan.getStyle().set("color", "#10b981").set("font-size", "12px").set("font-weight", "600");
        if (subtext.contains("semana")) subtextSpan.getStyle().set("color", "#64748b");

        card.add(top, valueHeader, subtextSpan);
        return card;
    }

    private HorizontalLayout createMiddleRow() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setHeight("auto");
        row.setSpacing(true);
        row.setFlexGrow(1, row);
        row.getStyle().set("flex", "1");

        // 1. Mapa de Calor (Izquierda - Ancho)
        VerticalLayout mapCard = new VerticalLayout();
        mapCard.setWidth("70%");
        mapCard.setHeight("auto");
        mapCard.setFlexGrow(1, mapCard);
        mapCard.getStyle()
                .set("background-color", "#ffffff")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "12px")
                .set("padding", "20px");

        HorizontalLayout mapHeader = new HorizontalLayout();
        mapHeader.setWidthFull();
        mapHeader.setJustifyContentMode(JustifyContentMode.BETWEEN);
        
        VerticalLayout mapTitles = new VerticalLayout();
        mapTitles.setPadding(false);
        mapTitles.setSpacing(false);
        H4 mapTitle = new H4("Mapa de Calor de Temas");
        mapTitle.getStyle().set("margin", "0");
        Span mapSubtitle = new Span("186 temas identificados por IA");
        mapSubtitle.getStyle().set("color", "#64748b").set("font-size", "12px");
        mapTitles.add(mapTitle, mapSubtitle);

        Anchor viewAll = new Anchor("#", "Ver todos");
        viewAll.getStyle().set("color", "#00b894").set("font-weight", "600");
        mapHeader.add(mapTitles, viewAll);

        // Contenedor de Tags (Flex para que se ajusten automáticamente)
        FlexLayout tagsLayout = new FlexLayout();
        tagsLayout.setWidthFull();
        tagsLayout.getStyle().set("flex-wrap", "wrap").set("gap", "10px").set("margin-top", "20px");

        // Añadir tags/badges con distintos tamaños y colores simulando la imagen
        tagsLayout.add(createTag("Spring Boot", "#eedffd", "#8b5cf6", "16px"));
        tagsLayout.add(createTag("Java", "#e0f2fe", "#0284c7", "14px"));
        tagsLayout.add(createTag("Kubernetes", "#ecfeff", "#0891b2", "14px"));
        tagsLayout.add(createTag("BACKEND", "#fae8ff", "#c084fc", "22px bold")); // Destacado gigante
        tagsLayout.add(createTag("Docker", "#e0f2fe", "#0284c7", "14px"));
        tagsLayout.add(createTag("REST APIs", "#eedffd", "#8b5cf6", "14px"));
        tagsLayout.add(createTag("Pandas", "#fce7f3", "#db2777", "12px"));
        tagsLayout.add(createTag("Python", "#fce7f3", "#db2777", "14px"));
        tagsLayout.add(createTag("Microservices", "#eedffd", "#8b5cf6", "16px"));
        tagsLayout.add(createTag("OCI", "#fef3c7", "#d97706", "12px"));
        tagsLayout.add(createTag("JWT", "#fee2e2", "#dc2626", "12px"));
        tagsLayout.add(createTag("DevOps", "#e0f2fe", "#0284c7", "14px"));
        tagsLayout.add(createTag("React", "#d1fae5", "#059669", "14px"));
        tagsLayout.add(createTag("TypeScript", "#d1fae5", "#059669", "12px"));

        mapCard.add(mapHeader, tagsLayout);

        // 2. Actividad Reciente (Derecha - Estrecho)
        VerticalLayout activityCard = new VerticalLayout();
        activityCard.setWidth("30%");
        activityCard.setHeight("auto");
        activityCard.setFlexGrow(1, activityCard);
        activityCard.getStyle()
                .set("background-color", "#ffffff")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "12px")
                .set("padding", "20px");

        HorizontalLayout actHeader = new HorizontalLayout();
        actHeader.setWidthFull();
        actHeader.setJustifyContentMode(JustifyContentMode.BETWEEN);
        H4 actTitle = new H4("Actividad Reciente");
        actTitle.getStyle().set("margin", "0");
        Anchor viewAllAct = new Anchor("#", "Ver todo");
        viewAllAct.getStyle().set("color", "#00b894").set("font-weight", "600");
        actHeader.add(actTitle, viewAllAct);

        VerticalLayout timeline = new VerticalLayout();
        timeline.setPadding(false);
        timeline.setSpacing(true);
        timeline.getStyle().set("margin-top", "15px");

        timeline.add(createTimelineItem("Artículo procesado", "Spring Boot 3 - JWT y OAuth2", "hace 2 min", VaadinIcon.FILE_TEXT, "#8b5cf6"));
        timeline.add(createTimelineItem("Recomendación generada", "Kubernetes: ConfigMaps en OCI", "hace 15 min", VaadinIcon.LIGHTBULB, "#10b981"));
        timeline.add(createTimelineItem("Etiquetas actualizadas", "Docker, Kubernetes, Spring Dev", "hace 1 hora", VaadinIcon.TAGS, "#0284c7"));

        activityCard.add(actHeader, timeline);

        row.add(mapCard, activityCard);
        return row;
    }

    private Span createTag(String text, String bgColor, String textColor, String fontStyle) {
        Span tag = new Span(text);
        tag.getStyle()
                .set("background-color", bgColor)
                .set("color", textColor)
                .set("padding", "6px 14px")
                .set("border-radius", "20px")
                .set("font-weight", fontStyle.contains("bold") ? "bold" : "500")
                .set("font-size", fontStyle.replace("bold", "").trim());
        return tag;
    }

    private HorizontalLayout createTimelineItem(String action, String detail, String time, VaadinIcon icon, String color) {
        HorizontalLayout item = new HorizontalLayout();
        item.setAlignItems(Alignment.START);
        item.getStyle().set("font-size", "13px");

        Icon actIcon = icon.create();
        actIcon.setColor(color);
        actIcon.setSize("16px");

        VerticalLayout texts = new VerticalLayout();
        texts.setPadding(false);
        texts.setSpacing(false);

        Span actionSpan = new Span(action);
        actionSpan.getStyle().set("font-weight", "bold").set("color", "#0f172a");

        Span detailSpan = new Span(detail);
        detailSpan.getStyle().set("color", "#64748b");

        Span timeSpan = new Span(time);
        timeSpan.getStyle().set("font-size", "11px").set("color", "#94a3b8");

        texts.add(actionSpan, detailSpan, timeSpan);
        item.add(actIcon, texts);
        return item;
    }

    private HorizontalLayout createBottomRow() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(true);

        // 1. Último Procesado
        VerticalLayout lastProcessed = new VerticalLayout();
        lastProcessed.setWidth("38%");
        lastProcessed.getStyle()
                .set("background-color", "#ffffff")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "12px")
                .set("padding", "20px");

        HorizontalLayout lpHeader = new HorizontalLayout();
        lpHeader.setWidthFull();
        lpHeader.setJustifyContentMode(JustifyContentMode.BETWEEN);
        Span lpLabel = new Span("ÚLTIMO PROCESADO");
        lpLabel.getStyle().set("color", "#10b981").set("font-weight", "bold").set("font-size", "12px");
        Span lpTime = new Span("hace 2 min");
        lpTime.getStyle().set("color", "#94a3b8").set("font-size", "11px");
        lpHeader.add(lpLabel, lpTime);

        H4 lpTitle = new H4("Spring Boot 3 - Configuración de Seguridad con OAuth2 y JWT");
        lpTitle.getStyle().set("margin", "10px 0");
        
        Paragraph lpText = new Paragraph("Documentación oficial de Spring Security. Cubre filtros, token validación y flujo en entornos distribuidos.");
        lpText.getStyle().set("color", "#64748b").set("font-size", "13px").set("margin", "0 0 15px 0");

        FlexLayout lpTags = new FlexLayout();
        lpTags.getStyle().set("gap", "5px");
        lpTags.add(createTag("Spring Boot", "#f3e8ff", "#8b5cf6", "11px"));
        lpTags.add(createTag("Backend", "#fae8ff", "#c084fc", "11px"));
        lpTags.add(createTag("JWT", "#fee2e2", "#dc2626", "11px"));

        Anchor link = new Anchor("#", "spring.io/security");
        link.getStyle().set("color", "#0284c7").set("font-size", "12px").set("margin-top", "10px");

        lastProcessed.add(lpHeader, lpTitle, lpText, lpTags, link);

        // 2. Recomendaciones Inteligentes
        VerticalLayout recommendations = new VerticalLayout();
        recommendations.setWidth("38%");
        recommendations.getStyle()
                .set("background-color", "#ffffff")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "12px")
                .set("padding", "20px");

        Span recLabel = new Span("RECOMENDACIONES INTELIGENTES");
        recLabel.getStyle().set("color", "#00b894").set("font-weight", "bold").set("font-size", "12px");
        recommendations.add(recLabel);

        recommendations.add(createRecommendationItem("Kubernetes: ConfigMaps y Secrets en OCI", 0.94));
        recommendations.add(createRecommendationItem("Data Science con Pandas - Limpieza", 0.87));
        recommendations.add(createRecommendationItem("API REST en Java 21 - Records y Virtual Threads", 0.79));

        // 3. Acciones Rápidas
        VerticalLayout quickActions = new VerticalLayout();
        quickActions.setWidth("24%");
        quickActions.getStyle()
                .set("background-color", "#ffffff")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "12px")
                .set("padding", "20px");

        H4 qaTitle = new H4("ACCIONES RÁPIDAS");
        qaTitle.getStyle().set("margin", "0 0 15px 0").set("font-size", "12px").set("color", "#64748b");
        quickActions.add(qaTitle);

        Button btn1 = new Button("Procesar nuevo contenido", VaadinIcon.PLUS.create());
        btn1.setWidthFull();
        btn1.getStyle().set("background-color", "#00b894").set("color", "#ffffff");

        Button btn2 = new Button("Explorar biblioteca", VaadinIcon.BOOK.create());
        btn2.setWidthFull();
        btn2.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button btn3 = new Button("Ver conceptos", VaadinIcon.CLUSTER.create());
        btn3.setWidthFull();
        btn3.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        quickActions.add(btn1, btn2, btn3);

        row.add(lastProcessed, recommendations, quickActions);
        return row;
    }

    private VerticalLayout createRecommendationItem(String title, double matchValue) {
        VerticalLayout item = new VerticalLayout();
        item.setPadding(false);
        item.setSpacing(false);
        item.getStyle().set("margin-top", "10px");

        HorizontalLayout top = new HorizontalLayout();
        top.setWidthFull();
        top.setJustifyContentMode(JustifyContentMode.BETWEEN);

        Span titleSpan = new Span(title);
        titleSpan.getStyle().set("font-size", "13px").set("font-weight", "500").set("color", "#0f172a");

        Span pctSpan = new Span((int)(matchValue * 100) + "%");
        pctSpan.getStyle().set("font-size", "12px").set("font-weight", "bold").set("color", "#10b981");

        top.add(titleSpan, pctSpan);

        ProgressBar bar = new ProgressBar();
        bar.setValue(matchValue);
        bar.getStyle().set("height", "6px").set("margin-top", "5px");

        item.add(top, bar);
        return item;
    }
}
