package com.application.Views.Layout;

import com.application.service.AuthService;
import com.application.service.UserSession;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLayout;

public class MainLayout extends HorizontalLayout implements RouterLayout {

    private final VerticalLayout contentArea;

    public MainLayout(UserSession userSession, AuthService authService) {
        setSizeFull();
        setSpacing(false);
        setPadding(false);
        setMargin(false);
        // Ensure layout covers at least the viewport height; allow natural document scrolling
        getStyle().set("min-height", "100vh");
        // Ensure children stretch vertically when possible
        setAlignItems(Alignment.STRETCH);
        getStyle().set("background-color", "#f8fafc");

        // Crear el sidebar
        AppSidebar sidebar = new AppSidebar(userSession, authService);
        sidebar.setHeightFull();

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
                .set("min-width", "0") // prevent flexbox overflow horizontally
                .set("min-height", "0") // allow vertical overflow to be handled by this container
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
