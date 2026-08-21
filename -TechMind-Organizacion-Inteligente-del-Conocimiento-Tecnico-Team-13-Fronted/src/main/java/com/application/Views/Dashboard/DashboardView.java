package com.application.Views.Dashboard;

import com.application.Views.Content.AddContentView;
import com.application.Views.Library.LibraryView;
import com.application.Views.Concept.ConceptGraphView;
import com.application.Views.Layout.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Dashboard - KnowBase")
@Route(value = "dashboard", layout = MainLayout.class)
public class DashboardView extends VerticalLayout {

    private final com.application.service.SupabaseService supabaseService;
    private final com.application.service.UserSession userSession;

    public DashboardView(com.application.service.SupabaseService supabaseService, com.application.service.UserSession userSession) {
        this.supabaseService = supabaseService;
        this.userSession = userSession;

        // Configuración de la vista
        setSpacing(true);
        setPadding(true);

        // Cabecera principal
        HorizontalLayout header = createHeader();
        header.setHeight("auto");
        add(header);

        // Fila 1: Tarjetas KPI (dinámicas)
        HorizontalLayout kpiRow = createKpiRow();
        kpiRow.setHeight("auto");
        add(kpiRow);

        // Fila 2: Mapa de calor de temas + Actividad reciente (dinámico)
        HorizontalLayout middleRow = createMiddleRow();
        middleRow.setHeight("auto");
        middleRow.getStyle().set("flex", "1");
        add(middleRow);

        // Fila 3: Último Procesado + Recomendaciones + Acciones rápidas (dinámico)
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

        Button analyzeBtn = new Button("Buscar Contenido", VaadinIcon.MAGIC.create());
        analyzeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        analyzeBtn.getStyle().set("background-color", "#00b894").set("color", "#ffffff");
        analyzeBtn.addClickListener(e -> {

            e.getSource().getUI().ifPresent(ui -> ui.navigate(AddContentView.class));
        });
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

        // Fetch user contents safely
        java.util.List<com.application.model.Content> contents = java.util.Collections.emptyList();
        if (userSession != null && userSession.getAuthenticatedUser() != null) {
            try {
                contents = supabaseService.getContentsForUser(userSession.getAuthenticatedUser().getId());
            } catch (Exception e) {
                System.err.println("[Dashboard] Error fetching contents: " + e.getMessage());
            }
        }

        int totalItems = contents != null ? contents.size() : 0;

        // Simple keyword extraction from titles for tag count (dedupe words)
        java.util.Set<String> keywords = new java.util.HashSet<>();
        if (contents != null) {
            for (com.application.model.Content c : contents) {
                if (c.getTitulo() != null) {
                    String[] parts = c.getTitulo().toLowerCase().split("[^a-z0-9áéíóúñ]+");
                    for (String p : parts) {
                        if (p == null) continue;
                        String t = p.trim();
                        if (t.length() > 2) keywords.add(t);
                    }
                }
            }
        }

        int tagsGenerated = Math.max(0, Math.min(99999, keywords.size()));

        long processedCount = 0;
        if (contents != null) {
            for (com.application.model.Content c : contents) {
                if (c.getEstadoProcesamiento() != null && !"pendiente".equalsIgnoreCase(c.getEstadoProcesamiento())) processedCount++;
            }
        }

        row.add(createKpiCard("Total Items", String.valueOf(totalItems), "+" + Math.max(0, totalItems / 20) + " este mes", VaadinIcon.DATABASE, "#a855f7", "#f3e8ff"));
        row.add(createKpiCard("Tags Generadas", String.valueOf(tagsGenerated), "+" + Math.max(0, tagsGenerated / 5), VaadinIcon.TAGS, "#06b6d4", "#ecfeff"));
        row.add(createKpiCard("Procesados", String.valueOf(processedCount), "del total", VaadinIcon.CHECK_CIRCLE_O, "#f59e0b", "#fef3c7"));
        row.add(createKpiCard("Precisión IA", "--", "Basado en últimas 100", VaadinIcon.CHECK_CIRCLE, "#10b981", "#d1fae5"));

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

        // 1. Mapa de Calor (Izquierda - Ancho) - más grande y llamativo
        VerticalLayout mapCard = new VerticalLayout();
        mapCard.setWidth("75%");
        mapCard.setHeight("auto");
        mapCard.setFlexGrow(1, mapCard);
        mapCard.getStyle()
                .set("background-color", "#ffffff")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "16px")
                .set("padding", "28px")
                .set("min-height", "420px")
                .set("box-shadow", "0 10px 30px rgba(15,23,42,0.06)");

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
        tagsLayout.getStyle().set("flex-wrap", "wrap").set("gap", "16px").set("margin-top", "24px").set("align-items", "flex-start");

        // Build tags dynamically from user contents (titles)
        java.util.List<com.application.model.Content> contents = java.util.Collections.emptyList();
        if (userSession != null && userSession.getAuthenticatedUser() != null) {
            try { contents = supabaseService.getContentsForUser(userSession.getAuthenticatedUser().getId()); } catch (Exception e) { System.err.println("[Dashboard] tags build error: " + e.getMessage()); }
        }

        java.util.Map<String, Integer> tagCounts = new java.util.HashMap<>();
        for (com.application.model.Content c : contents) {
            if (c.getTitulo() == null) continue;
            String[] parts = c.getTitulo().split("[^\\p{L}0-9]+");
            for (String p : parts) {
                String t = p.trim();
                if (t.length() < 3) continue;
                t = t.replaceAll("[^\\p{L}0-9]", "").toLowerCase();
                tagCounts.put(t, tagCounts.getOrDefault(t, 0) + 1);
            }
        }

        // pick top 12 tags and render with dynamic sizes/colors
        List<Map.Entry<String,Integer>> topEntries = tagCounts.entrySet().stream().sorted((a,b) -> b.getValue().compareTo(a.getValue())).limit(12).toList();
        if (topEntries.isEmpty()) {
            // fallback static tags
            List<String> fallback = List.of("Spring Boot","Java","Kubernetes","BACKEND","Docker","REST APIs","Pandas","Python","Microservices","OCI","JWT","DevOps");
            for (int i = 0; i < fallback.size(); i++) {
                String t = fallback.get(i);
                String bg = pickColor(i);
                String fg = "#06121a";
                tagsLayout.add(createTag(t, bg, fg, "18px bold", 18, 18 + (12 - i)));
            }
        } else {
            int maxCount = topEntries.stream().mapToInt(Map.Entry::getValue).max().orElse(1);
            int idx = 0;
            for (Map.Entry<String,Integer> en : topEntries) {
                String t = en.getKey();
                int count = en.getValue();
                // scale font between 14 and 30 depending on count
                int fontSize = 14 + (int) Math.round(((double)count / (double)maxCount) * 16);
                int padding = 12 + (int) Math.round(((double)count / (double)maxCount) * 14);
                String bg = pickColor(idx);
                String fg = pickTextColorForBg(bg);
                String fontStyle = fontSize + "px" + (fontSize > 20 ? " bold" : "");
                tagsLayout.add(createTag(t, bg, fg, fontStyle, fontSize, padding));
                idx++;
            }
        }
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

        // Build recent activity from user contents
        java.util.List<com.application.model.Content> contentsAct = java.util.Collections.emptyList();
        if (userSession != null && userSession.getAuthenticatedUser() != null) {
            try { contentsAct = supabaseService.getContentsForUser(userSession.getAuthenticatedUser().getId()); } catch (Exception e) { System.err.println("[Dashboard] activity fetch error: " + e.getMessage()); }
        }

        int added = 0;
        if (contentsAct != null && !contentsAct.isEmpty()) {
            for (com.application.model.Content c : contentsAct) {
                if (added >= 6) break;
                String action = c.getEstadoProcesamiento() != null && !"pendiente".equalsIgnoreCase(c.getEstadoProcesamiento()) ? "Artículo procesado" : "Nuevo contenido";
                String detail = c.getTitulo() != null ? c.getTitulo() : c.getStoragePath();
                String time = c.getCreatedAt() != null ? c.getCreatedAt().toString() : "hace poco";
                VaadinIcon icon = VaadinIcon.FILE_TEXT;
                String color = "#8b5cf6";
                if (action.contains("Recomendación")) { icon = VaadinIcon.LIGHTBULB; color = "#10b981"; }
                timeline.add(createTimelineItem(action, detail, time, icon, color));
                added++;
            }
        } else {
            timeline.add(createTimelineItem("Artículo procesado", "Spring Boot 3 - JWT y OAuth2", "hace 2 min", VaadinIcon.FILE_TEXT, "#8b5cf6"));
            timeline.add(createTimelineItem("Recomendación generada", "Kubernetes: ConfigMaps en OCI", "hace 15 min", VaadinIcon.LIGHTBULB, "#10b981"));
            timeline.add(createTimelineItem("Etiquetas actualizadas", "Docker, Kubernetes, Spring Dev", "hace 1 hora", VaadinIcon.TAGS, "#0284c7"));
        }

        activityCard.add(actHeader, timeline);

        row.add(mapCard, activityCard);
        return row;
    }

    // Backward-compatible overload: previous createTag(text, bg, fg, fontStyle)
    private Span createTag(String text, String bgColor, String textColor, String fontStyle) {
        int fontSize = 13;
        try {
            String num = fontStyle.replaceAll("[^0-9]", "");
            if (!num.isBlank()) fontSize = Integer.parseInt(num);
        } catch (Exception ignored) {}
        int padding = Math.max(10, fontSize - 2);
        return createTag(text, bgColor, textColor, fontStyle, fontSize, padding);
    }

    // Enhanced tag helper with size and hover behavior
    private Span createTag(String text, String bgColor, String textColor, String fontStyle, int fontSize, int padding) {
        Span tag = new Span(text);
        String paddingCss = padding + "px " + (padding + 6) + "px";
        tag.getStyle()
                .set("background-color", bgColor)
                .set("color", textColor)
                .set("padding", paddingCss)
                .set("border-radius", "28px")
                .set("font-weight", fontStyle.contains("bold") ? "700" : "600")
                .set("font-size", fontSize + "px")
                .set("box-shadow", "0 6px 18px rgba(2,6,23,0.08)")
                .set("cursor", "pointer")
                .set("transition", "transform .12s ease, box-shadow .12s ease");
        // simple hover effect using inline attributes
        tag.getElement().setAttribute("onmouseover", "this.style.transform='scale(1.06)'; this.style.boxShadow='0 12px 30px rgba(2,6,23,0.12)'");
        tag.getElement().setAttribute("onmouseout", "this.style.transform='scale(1)'; this.style.boxShadow='0 6px 18px rgba(2,6,23,0.08)'");
        return tag;
    }

    // Color palette helper (cyclic)
    private String pickColor(int idx) {
        String[] palette = new String[]{"#FFEDD5","#FEF3C7","#E9D5FF","#DBEAFE","#ECFEFF","#FCE7F3","#E6FFFA","#FEE2E2","#FFF1F2","#F0FDF4"};
        return palette[idx % palette.length];
    }

    private String pickTextColorForBg(String bg) {
        // simple contrast choices
        if (bg.equals("#FFEDD5") || bg.equals("#FEF3C7") || bg.equals("#ECFEFF") || bg.equals("#FFF1F2")) return "#78350f"; // dark amber
        if (bg.equals("#E9D5FF") || bg.equals("#FCE7F3") || bg.equals("#FEE2E2")) return "#5b21b6"; // purple/magenta
        if (bg.equals("#DBEAFE") || bg.equals("#E6FFFA") || bg.equals("#F0FDF4")) return "#0f172a"; // dark slate
        return "#0f172a";
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
        Span lpTime = new Span("hace poco");
        lpTime.getStyle().set("color", "#94a3b8").set("font-size", "11px");
        lpHeader.add(lpLabel, lpTime);

        // fetch latest content
        com.application.model.Content latest = null;
        if (userSession != null && userSession.getAuthenticatedUser() != null) {
            try {
                java.util.List<com.application.model.Content> cs = supabaseService.getContentsForUser(userSession.getAuthenticatedUser().getId());
                if (cs != null && !cs.isEmpty()) latest = cs.get(0);
            } catch (Exception e) { System.err.println("[Dashboard] latest fetch error: " + e.getMessage()); }
        }

        H4 lpTitle = new H4(latest != null && latest.getTitulo() != null ? latest.getTitulo() : "No hay contenido procesado");
        lpTitle.getStyle().set("margin", "10px 0");
        
        Paragraph lpText = new Paragraph(latest != null && latest.getTextoPlano() != null ? (latest.getTextoPlano().length() > 220 ? latest.getTextoPlano().substring(0, 220) + "..." : latest.getTextoPlano()) : "No hay descripción disponible.");
        lpText.getStyle().set("color", "#64748b").set("font-size", "13px").set("margin", "0 0 15px 0");

        FlexLayout lpTags = new FlexLayout();
        lpTags.getStyle().set("gap", "5px");
        if (latest != null && latest.getTitulo() != null) {
            String[] parts = latest.getTitulo().split("[^\\p{L}0-9]+");
            int added = 0;
            for (String p : parts) {
                String t = p.trim();
                if (t.length() < 3) continue;
                lpTags.add(createTag(t, "#f3e8ff", "#8b5cf6", "11px"));
                added++; if (added >= 5) break;
            }
        } else {
            lpTags.add(createTag("Spring Boot", "#f3e8ff", "#8b5cf6", "11px"));
            lpTags.add(createTag("Backend", "#fae8ff", "#c084fc", "11px"));
            lpTags.add(createTag("JWT", "#fee2e2", "#dc2626", "11px"));
        }

        Anchor link = new Anchor(latest != null && latest.getStoragePath() != null ? ("/storage/" + latest.getStoragePath()) : "#", "Ver recurso");
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
        btn1.addClickListener(e -> {

            e.getSource().getUI().ifPresent(ui -> ui.navigate(AddContentView.class));
        });

        Button btn2 = new Button("Explorar biblioteca", VaadinIcon.BOOK.create());
        btn2.setWidthFull();
        btn2.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btn2.addClickListener(e -> {

            e.getSource().getUI().ifPresent(ui -> ui.navigate(LibraryView.class));
        });

        Button btn3 = new Button("Ver conceptos", VaadinIcon.CLUSTER.create());
        btn3.setWidthFull();
        btn3.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btn3.addClickListener(e -> {

            e.getSource().getUI().ifPresent(ui -> ui.navigate(ConceptGraphView.class));
        });

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
