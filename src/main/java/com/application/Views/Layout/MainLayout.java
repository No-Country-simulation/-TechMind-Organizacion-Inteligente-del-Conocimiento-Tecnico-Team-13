package com.application.Views.Layout;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLayout;

public class MainLayout extends HorizontalLayout implements RouterLayout {

    private final VerticalLayout contentArea;

    public MainLayout() {
        setSizeFull();
        setSpacing(false);
        setPadding(false);
        setMargin(false);
        getStyle().set("background-color", "#f8fafc");

        // Crear el sidebar
        AppSidebar sidebar = new AppSidebar();
        sidebar.setHeight("100vh");

        // Crear el área de contenido
        contentArea = new VerticalLayout();
        contentArea.setSizeFull();
        contentArea.setPadding(false);
        contentArea.setMargin(false);
        contentArea.setSpacing(false);
        contentArea.getStyle()
                .set("overflow-y", "auto")
                .set("flex", "1")
                .set("width", "100%")
                .set("display", "flex")
                .set("flex-direction", "column");

        // Añadir al layout
        add(sidebar, contentArea);
        expand(contentArea);
    }

    public VerticalLayout getContentArea() {
        return contentArea;
    }
}
