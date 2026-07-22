package com.application.Views.Library;

import com.application.Views.Layout.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.progressbar.ProgressBar;

@PageTitle("Library - KnowBase")
@Route(value = "library", layout = MainLayout.class)
public class LibraryView extends VerticalLayout {

    public LibraryView() {
        // Configuración del contenedor principal
        setWidthFull();
        setMinHeight("100vh");
        setPadding(true);
        setSpacing(true);
        getStyle()
                .set("background-color", "#f4f6fa")
                .set("font-family", "'Inter', sans-serif")
                .set("color", "#1e293b");

        // 1. Cabecera (Header Superior)
        add(createHeader());

        // 2. Fila Superior: Tarjetas KPI
        add(createKpiRow());

        // 3. Fila Media: Mapa de Calor de Temas + Actividad Reciente
        add(createMiddleRow());

        // 4. Fila Inferior: Último Procesado + Recomendaciones + Acciones Rápidas
        add(createBottomRow());
    }

    // ==========================================
    // 1. CABECERA
    // ==========================================
    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);
        header.getStyle().set("margin-bottom", "10px");

        // Título y subtítulo
        VerticalLayout titleLayout = new VerticalLayout();
        titleLayout.setPadding(false);
        titleLayout.setSpacing(false);

        H2 title = new H2("Biblioteca");
        title.getStyle().set("margin", "0").set("font-size", "22px").set("font-weight", "700");

        Span subtitle = new Span("Gestiona y organiza tu base de conocimiento");
        subtitle.getStyle().set("color", "#64748b").set("font-size", "13px");

        titleLayout.add(title, subtitle);

        // Controles derechos: Buscador + Botón + Iconos
        HorizontalLayout controls = new HorizontalLayout();
        controls.setAlignItems(Alignment.CENTER);
        controls.setSpacing(true);

        TextField searchField = new TextField();
        searchField.setPlaceholder("Busca concepto, tema o pr...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("280px");
        searchField.getStyle().set("border-radius", "20px");

        Button analyzeBtn = new Button("Analizar Contenido", VaadinIcon.MAGIC.create());
        analyzeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        analyzeBtn.getStyle()
                .set("background-color", "#00b894")
                .set("color", "#ffffff")
                .set("border-radius", "20px")
                .set("font-weight", "600")
                .set("padding", "0 20px");

        Icon bellIcon = VaadinIcon.BELL_O.create();
        bellIcon.getStyle().set("color", "#64748b").set("cursor", "pointer").set("padding", "6px");

        Icon cogIcon = VaadinIcon.COG_O.create();
        cogIcon.getStyle().set("color", "#64748b").set("cursor", "pointer").set("padding", "6px");

        controls.add(searchField, analyzeBtn, bellIcon, cogIcon);
        header.add(titleLayout, controls);

        return header;
    }

    // ==========================================
    // 2. FILA DE TARJETAS KPI (4 Columnas)
    // ==========================================
    private HorizontalLayout createKpiRow() {
        HorizontalLayout kpiRow = new HorizontalLayout();
        kpiRow.setWidthFull();
        kpiRow.setSpacing(true);

        kpiRow.add(createKpiCard("Total Items", "1,284", "+48 este mes", VaadinIcon.DATABASE, "#8b5cf6", "#f3e8ff"));
        kpiRow.add(createKpiCard("Tags Generadas", "3,740", "+188", VaadinIcon.TAGS, "#06b6d4", "#ecfeff"));
        kpiRow.add(createKpiCard("Consultas", "87", "Esta semana", VaadinIcon.SEARCH, "#f59e0b", "#fef3c7"));
        kpiRow.add(createKpiCard("Precisión IA", "91.4%", "+0.8% vs mes anterior", VaadinIcon.CHECK_CIRCLE, "#10b981", "#d1fae5"));

        return kpiRow;
    }

    private VerticalLayout createKpiCard(String label, String value, String subtext, VaadinIcon icon, String iconColor, String iconBg) {
        VerticalLayout card = new VerticalLayout();
        card.setWidthFull();
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "16px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.03)")
                .set("border", "1px solid #f1f5f9");

        HorizontalLayout top = new HorizontalLayout();
        top.setWidthFull();
        top.setJustifyContentMode(JustifyContentMode.BETWEEN);
        top.setAlignItems(Alignment.CENTER);

        Span titleSpan = new Span(label);
        titleSpan.getStyle().set("color", "#64748b").set("font-size", "13px").set("font-weight", "500");

        Div iconWrapper = new Div();
        Icon kpiIcon = icon.create();
        kpiIcon.setColor(iconColor);
        kpiIcon.setSize("18px");
        iconWrapper.add(kpiIcon);
        iconWrapper.getStyle()
                .set("background-color", iconBg)
                .set("border-radius", "50%")
                .set("width", "36px")
                .set("height", "36px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");

        top.add(titleSpan, iconWrapper);

        H3 valueHeader = new H3(value);
        valueHeader.getStyle().set("margin", "8px 0 4px 0").set("font-size", "26px").set("font-weight", "700");

        Span subtextSpan = new Span(subtext);
        subtextSpan.getStyle().set("font-size", "12px").set("color", subtext.contains("semana") ? "#64748b" : "#10b981");

        card.add(top, valueHeader, subtextSpan);
        return card;
    }

    // ==========================================
    // 3. FILA MEDIA (MAPA DE CALOR + ACTIVIDAD)
    // ==========================================
    private HorizontalLayout createMiddleRow() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(true);

        // --- MAPA DE CALOR DE TEMAS ---
        VerticalLayout mapCard = new VerticalLayout();
        mapCard.setWidth("68%");
        mapCard.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "16px")
                .set("padding", "20px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.03)")
                .set("border", "1px solid #f1f5f9");

        HorizontalLayout mapHeader = new HorizontalLayout();
        mapHeader.setWidthFull();
        mapHeader.setJustifyContentMode(JustifyContentMode.BETWEEN);

        VerticalLayout mapTitles = new VerticalLayout();
        mapTitles.setPadding(false);
        mapTitles.setSpacing(false);

        H4 mapTitle = new H4("Mapa de Calor de Temas");
        mapTitle.getStyle().set("margin", "0").set("font-size", "15px");
        Span mapSub = new Span("186 temas identificados por IA");
        mapSub.getStyle().set("color", "#94a3b8").set("font-size", "12px");
        mapTitles.add(mapTitle, mapSub);

        Anchor viewAllMap = new Anchor("#", "Ver todos");
        viewAllMap.getStyle().set("color", "#00b894").set("font-size", "12px").set("font-weight", "600");
        mapHeader.add(mapTitles, viewAllMap);

        FlexLayout tagsLayout = new FlexLayout();
        tagsLayout.setWidthFull();
        tagsLayout.getStyle().set("flex-wrap", "wrap").set("gap", "10px").set("margin-top", "15px");

        // Agregando las píldoras de etiquetas con estilos dinámicos
        tagsLayout.add(createPillTag("Spring Boot", "#eedffd", "#8b5cf6", "15px", false));
        tagsLayout.add(createPillTag("Java", "#f3e8ff", "#a855f7", "13px", false));
        tagsLayout.add(createPillTag("Kubernetes", "#e0f2fe", "#0284c7", "14px", false));
        tagsLayout.add(createPillTag("BACKEND", "#f3e8ff", "#7e22ce", "22px", true)); // Destacado
        tagsLayout.add(createPillTag("Docker", "#e0f2fe", "#0369a1", "13px", false));
        tagsLayout.add(createPillTag("REST APIs", "#f3e8ff", "#8b5cf6", "13px", false));
        tagsLayout.add(createPillTag("Pandas", "#fce7f3", "#db2777", "12px", false));
        tagsLayout.add(createPillTag("Python", "#fce7f3", "#be185d", "14px", false));
        tagsLayout.add(createPillTag("Microservices", "#eedffd", "#6d28d9", "15px", false));
        tagsLayout.add(createPillTag("OCI", "#fef3c7", "#d97706", "12px", false));
        tagsLayout.add(createPillTag("JWT", "#fee2e2", "#dc2626", "12px", false));
        tagsLayout.add(createPillTag("DevOps", "#e0f2fe", "#0284c7", "14px", false));
        tagsLayout.add(createPillTag("React", "#d1fae5", "#059669", "13px", false));
        tagsLayout.add(createPillTag("TypeScript", "#d1fae5", "#047857", "12px", false));
        tagsLayout.add(createPillTag("Kafka", "#f3e8ff", "#7e22ce", "12px", false));
        tagsLayout.add(createPillTag("OAuth2", "#fee2e2", "#b91c1c", "12px", false));
        tagsLayout.add(createPillTag("Data Science", "#fce7f3", "#db2777", "12px", false));
        tagsLayout.add(createPillTag("DevOps", "#e0f2fe", "#0284c7", "12px", false));
        tagsLayout.add(createPillTag("Terraform", "#fef3c7", "#b45309", "12px", false));
        tagsLayout.add(createPillTag("SpringSecurity", "#fee2e2", "#b91c1c", "12px", false));

        mapCard.add(mapHeader, tagsLayout);

        // --- ACTIVIDAD RECIENTE ---
        VerticalLayout activityCard = new VerticalLayout();
        activityCard.setWidth("32%");
        activityCard.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "16px")
                .set("padding", "20px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.03)")
                .set("border", "1px solid #f1f5f9");

        HorizontalLayout actHeader = new HorizontalLayout();
        actHeader.setWidthFull();
        actHeader.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H4 actTitle = new H4("Actividad Reciente");
        actTitle.getStyle().set("margin", "0").set("font-size", "15px");

        Anchor viewAllAct = new Anchor("#", "Ver todo");
        viewAllAct.getStyle().set("color", "#00b894").set("font-size", "12px").set("font-weight", "600");

        actHeader.add(actTitle, viewAllAct);

        VerticalLayout list = new VerticalLayout();
        list.setPadding(false);
        list.setSpacing(true);
        list.getStyle().set("margin-top", "10px");

        list.add(createActivityItem("Artículo procesado", "Spring Boot 3 — JWT y OAuth2", "2m ago", VaadinIcon.FILE_TEXT, "#8b5cf6"));
        list.add(createActivityItem("Recomendación generada", "Kubernetes: ConfigMaps y Secrets en OCI", "15m ago", VaadinIcon.CONNECT_O, "#10b981"));
        list.add(createActivityItem("Etiquetas actualizadas", "Kubernetes, Docker, Spring — DevOps", "1h ago", VaadinIcon.TAG, "#0284c7"));
        list.add(createActivityItem("Anotación vinculada", "Java Virtual Threads — Conceptos clave", "3h ago", VaadinIcon.BOOKMARK, "#f59e0b"));

        // Sparkline gráfica de actividad en 7 días
        VerticalLayout chartSection = new VerticalLayout();
        chartSection.setPadding(false);
        chartSection.setSpacing(false);
        chartSection.getStyle().set("margin-top", "15px").set("border-top", "1px solid #f1f5f9").set("padding-top", "10px");

        Span chartLabel = new Span("Contenidos añadidos (últimos 7 días)");
        chartLabel.getStyle().set("font-size", "11px").set("color", "#94a3b8");

        Div sparklineSvg = new Div();
        sparklineSvg.getElement().setProperty("innerHTML",
                "<svg width='100%' height='35' viewBox='0 0 200 35' fill='none' xmlns='http://www.w3.org/2000/svg'>" +
                "<path d='M0 25 C30 20, 60 22, 90 28 C120 32, 150 20, 200 22' stroke='#00b894' stroke-width='2' fill='none'/>" +
                "</svg>");

        HorizontalLayout daysRow = new HorizontalLayout();
        daysRow.setWidthFull();
        daysRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
        daysRow.getStyle().set("font-size", "10px").set("color", "#94a3b8");
        daysRow.add(new Span("M"), new Span("X"), new Span("J"), new Span("V"), new Span("S"), new Span("D"));

        chartSection.add(chartLabel, sparklineSvg, daysRow);
        activityCard.add(actHeader, list, chartSection);

        row.add(mapCard, activityCard);
        return row;
    }

    private Span createPillTag(String text, String bgColor, String textColor, String fontSize, boolean bold) {
        Span tag = new Span(text);
        tag.getStyle()
                .set("background-color", bgColor)
                .set("color", textColor)
                .set("padding", bold ? "8px 20px" : "5px 14px")
                .set("border-radius", "20px")
                .set("font-size", fontSize)
                .set("font-weight", bold ? "800" : "600")
                .set("display", "inline-block");
        return tag;
    }

    private HorizontalLayout createActivityItem(String title, String desc, String time, VaadinIcon icon, String color) {
        HorizontalLayout item = new HorizontalLayout();
        item.setWidthFull();
        item.setAlignItems(Alignment.START);

        Div iconBox = new Div();
        Icon ic = icon.create();
        ic.setColor(color);
        ic.setSize("15px");
        iconBox.add(ic);
        iconBox.getStyle()
                .set("background-color", "#f8fafc")
                .set("border-radius", "50%")
                .set("padding", "6px")
                .set("display", "flex");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);

        HorizontalLayout top = new HorizontalLayout();
        top.setWidthFull();
        top.setJustifyContentMode(JustifyContentMode.BETWEEN);

        Span titleSpan = new Span(title);
        titleSpan.getStyle().set("font-size", "12px").set("font-weight", "700");

        Span timeSpan = new Span(time);
        timeSpan.getStyle().set("font-size", "10px").set("color", "#94a3b8");

        top.add(titleSpan, timeSpan);

        Span descSpan = new Span(desc);
        descSpan.getStyle().set("font-size", "11px").set("color", "#64748b");

        content.add(top, descSpan);
        item.add(iconBox, content);
        item.expand(content);

        return item;
    }

    // ==========================================
    // 4. FILA INFERIOR (3 COLUMNAS)
    // ==========================================
    private HorizontalLayout createBottomRow() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(true);

        // --- TARJETA 1: ÚLTIMO PROCESADO ---
        VerticalLayout card1 = new VerticalLayout();
        card1.setWidth("33%");
        card1.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "16px")
                .set("padding", "20px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.03)")
                .set("border", "1px solid #f1f5f9");

        HorizontalLayout c1Header = new HorizontalLayout();
        c1Header.setWidthFull();
        c1Header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        HorizontalLayout c1TitleGroup = new HorizontalLayout();
        c1TitleGroup.setAlignItems(Alignment.CENTER);
        Icon clockIcon = VaadinIcon.CLOCK.create();
        clockIcon.setColor("#00b894");
        clockIcon.setSize("14px");

        Span c1Label = new Span("ÚLTIMO PROCESADO");
        c1Label.getStyle().set("color", "#00b894").set("font-weight", "700").set("font-size", "11px");
        c1TitleGroup.add(clockIcon, c1Label);

        Span timeAgo = new Span("hace 2 min");
        timeAgo.getStyle().set("color", "#94a3b8").set("font-size", "11px");

        c1Header.add(c1TitleGroup, timeAgo);

        H4 title1 = new H4("Spring Boot 3 — Configuración de Seguridad con OAuth2 y JWT");
        title1.getStyle().set("margin", "10px 0 5px 0").set("font-size", "14px").set("font-weight", "700");

        Paragraph desc1 = new Paragraph("Documentación oficial de Spring Security. Cubre filtros, token validación y refresh flow en entornos distribuidos.");
        desc1.getStyle().set("color", "#64748b").set("font-size", "12px").set("margin", "0 0 15px 0");

        FlexLayout tags1 = new FlexLayout();
        tags1.getStyle().set("gap", "6px").set("flex-wrap", "wrap");
        tags1.add(createPillTag("Spring Boot", "#f3e8ff", "#8b5cf6", "11px", false));
        tags1.add(createPillTag("Backend", "#f3e8ff", "#8b5cf6", "11px", false));
        tags1.add(createPillTag("JWT", "#f3e8ff", "#8b5cf6", "11px", false));
        tags1.add(createPillTag("Confluence", "#f3e8ff", "#8b5cf6", "11px", false));
        tags1.add(createPillTag("OAuth2 3.1", "#f3e8ff", "#8b5cf6", "11px", false));

        HorizontalLayout linkRow = new HorizontalLayout();
        linkRow.setAlignItems(Alignment.CENTER);
        linkRow.getStyle().set("margin-top", "10px");

        Icon linkIcon = VaadinIcon.LINK.create();
        linkIcon.setColor("#00b894");
        linkIcon.setSize("12px");

        Anchor link = new Anchor("https://spring.io/security", "spring.io/security");
        link.getStyle().set("color", "#00b894").set("font-size", "12px").set("font-weight", "600");

        linkRow.add(linkIcon, link);
        card1.add(c1Header, title1, desc1, tags1, linkRow);

        // --- TARJETA 2: RECOMENDACIONES INTELIGENTES ---
        VerticalLayout card2 = new VerticalLayout();
        card2.setWidth("33%");
        card2.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "16px")
                .set("padding", "20px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.03)")
                .set("border", "1px solid #f1f5f9");

        HorizontalLayout c2Header = new HorizontalLayout();
        c2Header.setAlignItems(Alignment.CENTER);

        Icon sparkIcon = VaadinIcon.MAGIC.create();
        sparkIcon.setColor("#00b894");
        sparkIcon.setSize("14px");

        Span c2Label = new Span("RECOMENDACIONES INTELIGENTES");
        c2Label.getStyle().set("color", "#00b894").set("font-weight", "700").set("font-size", "11px");
        c2Header.add(sparkIcon, c2Label);

        card2.add(c2Header);
        card2.add(createRecommendationItem("Kubernetes: ConfigMaps y Secrets en OCI", 0.94, "DevOps", "#e0f2fe", "#0284c7"));
        card2.add(createRecommendationItem("Gale Science con Pandas — Limpieza de Datos", 0.87, "Data Science", "#fce7f3", "#db2777"));
        card2.add(createRecommendationItem("API REST en Java 21 — Records y Virtual Threads", 0.79, "Backend", "#f3e8ff", "#8b5cf6"));

        // --- TARJETA 3: ACCIONES RÁPIDAS ---
        VerticalLayout card3 = new VerticalLayout();
        card3.setWidth("34%");
        card3.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "16px")
                .set("padding", "20px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.03)")
                .set("border", "1px solid #f1f5f9");

        Span c3Label = new Span("ACCIONES RÁPIDAS");
        c3Label.getStyle().set("color", "#64748b").set("font-weight", "700").set("font-size", "11px");

        Button mainActionBtn = new Button("Procesar nuevo contenido", VaadinIcon.PLUS.create());
        mainActionBtn.setWidthFull();
        mainActionBtn.getStyle()
                .set("background-color", "#00b894")
                .set("color", "#ffffff")
                .set("border-radius", "20px")
                .set("font-weight", "600");

        Button b1 = createLightButton("Explorar biblioteca", VaadinIcon.BOOK);
        Button b2 = createLightButton("Ver conceptos", VaadinIcon.CLUSTER);
        Button b3 = createLightButton("Exportar resumen", VaadinIcon.DOWNLOAD);

        // Sección del Modelo Activo
        VerticalLayout modelStatus = new VerticalLayout();
        modelStatus.setPadding(false);
        modelStatus.setSpacing(false);
        modelStatus.getStyle().set("border-top", "1px solid #f1f5f9").set("padding-top", "10px").set("margin-top", "5px");

        Span modelTitleLabel = new Span("Modelo activo");
        modelTitleLabel.getStyle().set("font-size", "11px").set("color", "#94a3b8");

        HorizontalLayout modelRow = new HorizontalLayout();
        modelRow.setWidthFull();
        modelRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
        modelRow.setAlignItems(Alignment.CENTER);

        Span modelName = new Span("KnowBase XG-2 (Local)");
        modelName.getStyle().set("font-size", "12px").set("font-weight", "700");

        Span onlineBadge = new Span("ONLINE");
        onlineBadge.getStyle()
                .set("background-color", "#d1fae5")
                .set("color", "#059669")
                .set("font-size", "9px")
                .set("font-weight", "800")
                .set("padding", "2px 8px")
                .set("border-radius", "10px");

        modelRow.add(modelName, onlineBadge);

        HorizontalLayout precRow = new HorizontalLayout();
        precRow.setWidthFull();
        precRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
        precRow.getStyle().set("margin-top", "6px");

        Span precLabel = new Span("Precisión Clasificación");
        precLabel.getStyle().set("font-size", "11px").set("color", "#64748b");

        Span precValue = new Span("89%");
        precValue.getStyle().set("font-size", "11px").set("font-weight", "700");

        precRow.add(precLabel, precValue);

        ProgressBar precBar = new ProgressBar();
        precBar.setValue(0.89);
        precBar.setWidthFull();
        precBar.getStyle().set("height", "6px");

        modelStatus.add(modelTitleLabel, modelRow, precRow, precBar);

        card3.add(c3Label, mainActionBtn, b1, b2, b3, modelStatus);

        row.add(card1, card2, card3);
        return row;
    }

    private VerticalLayout createRecommendationItem(String title, double matchValue, String tagText, String tagBg, String tagColor) {
        VerticalLayout item = new VerticalLayout();
        item.setPadding(false);
        item.setSpacing(false);
        item.getStyle().set("margin-top", "12px");

        HorizontalLayout top = new HorizontalLayout();
        top.setWidthFull();
        top.setJustifyContentMode(JustifyContentMode.BETWEEN);

        Span titleSpan = new Span(title);
        titleSpan.getStyle().set("font-size", "12px").set("font-weight", "600");

        Span pctSpan = new Span((int) (matchValue * 100) + "%");
        pctSpan.getStyle().set("font-size", "11px").set("font-weight", "700").set("color", "#00b894");

        top.add(titleSpan, pctSpan);

        ProgressBar bar = new ProgressBar();
        bar.setValue(matchValue);
        bar.setWidthFull();
        bar.getStyle().set("height", "5px").set("margin", "4px 0 6px 0");

        Span tag = createPillTag(tagText, tagBg, tagColor, "10px", false);

        item.add(top, bar, tag);
        return item;
    }

    private Button createLightButton(String text, VaadinIcon icon) {
        Button btn = new Button(text, icon.create());
        btn.setWidthFull();
        btn.getStyle()
                .set("background-color", "#f8fafc")
                .set("color", "#334155")
                .set("border-radius", "20px")
                .set("font-size", "12px")
                .set("font-weight", "500");
        return btn;
    }
}