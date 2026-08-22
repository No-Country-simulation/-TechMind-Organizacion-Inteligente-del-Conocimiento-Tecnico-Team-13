package com.application.Views.Layout;

import com.application.service.UserSession;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLayout;

public class MainLayout extends HorizontalLayout implements RouterLayout {

    private final AppSidebar sidebar;
    private final VerticalLayout contentArea;
    private final Button floatingMenuBtn;
    private boolean sidebarVisible = true;

    public MainLayout(UserSession userSession) {
        setSizeFull();
        setSpacing(false);
        setPadding(false);
        setMargin(false);
        getStyle()
                .set("min-height", "100vh")
                .set("height", "100vh")
                .set("overflow", "hidden")
                .set("background-color", "#f8fafc");

        setAlignItems(Alignment.STRETCH);

        // Sidebar con callback para colapsar
        sidebar = new AppSidebar(userSession, this::toggleSidebar);

        // Botón flotante para reabrir el menú cuando está oculto
        floatingMenuBtn = new Button(VaadinIcon.MENU.create(), e -> toggleSidebar());
        floatingMenuBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ICON);
        floatingMenuBtn.setTooltipText("Mostrar menú lateral");
        floatingMenuBtn.getStyle()
                .set("position", "fixed")
                .set("top", "16px")
                .set("left", "16px")
                .set("z-index", "1000")
                .set("background-color", "#0b1329")
                .set("color", "#00b894")
                .set("border-radius", "10px")
                .set("box-shadow", "0 4px 12px rgba(0, 0, 0, 0.15)")
                .set("width", "40px")
                .set("height", "40px")
                .set("cursor", "pointer")
                .set("display", "none"); // Oculto inicialmente porque el sidebar está visible

        // Contenedor del área de contenido
        contentArea = new VerticalLayout();
        contentArea.setSizeFull();
        contentArea.setPadding(false);
        contentArea.setMargin(false);
        contentArea.setSpacing(false);
        contentArea.getStyle()
                .set("overflow-y", "auto")
                .set("overflow-x", "hidden")
                .set("flex", "1")
                .set("width", "100%")
                .set("min-width", "0")
                .set("min-height", "0")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("position", "relative");

        add(sidebar, contentArea, floatingMenuBtn);
        expand(contentArea);
    }

    public void toggleSidebar() {
        sidebarVisible = !sidebarVisible;
        sidebar.setVisible(sidebarVisible);

        if (sidebarVisible) {
            floatingMenuBtn.getStyle().set("display", "none");
        } else {
            floatingMenuBtn.getStyle().set("display", "flex");
        }
    }

    @Override
    public void showRouterLayoutContent(HasElement content) {
        contentArea.removeAll();
        if (content != null) {
            contentArea.getElement().appendChild(content.getElement());
        }
    }

    public VerticalLayout getContentArea() {
        return contentArea;
    }
}
