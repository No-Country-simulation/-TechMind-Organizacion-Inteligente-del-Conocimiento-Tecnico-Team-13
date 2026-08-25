package com.application.Views.Layout;

import com.application.Views.AI.AiConsultantView;
import com.application.Views.Annotations.AnnotationsView;
import com.application.Views.Categories.CategoriesView;
import com.application.Views.Concept.ConceptGraphView;
import com.application.Views.Content.AddContentView;
import com.application.Views.Dashboard.DashboardView;
import com.application.Views.Library.LibraryView;
import com.application.Views.Settings.SettingsView;
import com.application.model.User;
import com.application.service.AuthService;
import com.application.service.UserSession;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLink;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AppSidebar extends VerticalLayout implements BeforeEnterObserver {

    private final List<NavItemContainer> navItems = new ArrayList<>();
    private final AuthService authService;
    private Runnable onToggleCollapseListener;

    // Estructura interna para gestionar la referencia del item y su clase de vista
    private record NavItemContainer(
            RouterLink link, 
            HorizontalLayout itemLayout, 
            Icon icon, 
            Span label, 
            Class<? extends Component> viewClass
    ) {}

    public AppSidebar(UserSession userSession, AuthService authService) {
        this(userSession, authService, null);
    }

    public AppSidebar(UserSession userSession, AuthService authService, Runnable onToggleCollapseListener) {
        this.authService = authService;
        this.onToggleCollapseListener = onToggleCollapseListener;

        setWidth("260px");
        setHeight("100vh");
        setPadding(true);
        setSpacing(true);
        setMargin(false);

        getStyle()
                .set("background-color", "#0b1329")
                .set("color", "#94a3b8")
                .set("height", "100vh")
                .set("max-height", "100vh")
                .set("overflow-y", "auto")
                .set("overflow-x", "hidden")
                .set("flex-shrink", "0")
                .set("box-sizing", "border-box")
                .set("transition", "all 0.25s cubic-bezier(0.4, 0, 0.2, 1)");

        getElement().getStyle()
                .set("align-self", "flex-start")
                .set("min-height", "100vh")
                .set("position", "sticky")
                .set("top", "0")
                .set("z-index", "20");

        // Cabecera: Logo de la app + Botón de Ocultar Menú
        HorizontalLayout logoLayout = new HorizontalLayout();
        logoLayout.setAlignItems(Alignment.CENTER);
        logoLayout.setWidthFull();
        logoLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        logoLayout.getStyle().set("padding", "6px 0 6px 0");

        HorizontalLayout logoBrand = new HorizontalLayout();
        logoBrand.setAlignItems(Alignment.CENTER);
        logoBrand.setSpacing(true);

        Image logoImage = new Image("Logo-Logicore.svg", "LogiCore Logo");
        logoImage.setMaxHeight("38px");
        logoImage.setMaxWidth("150px");
        logoImage.getStyle().set("object-fit", "contain").set("border-radius", "6px");
        logoBrand.add(logoImage);

        // Botón para colapsar / esconder menú
        Button collapseBtn = new Button(VaadinIcon.CHEVRON_LEFT.create());
        collapseBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ICON);
        collapseBtn.setTooltipText("Ocultar menú lateral");
        collapseBtn.getStyle()
                .set("color", "#94a3b8")
                .set("cursor", "pointer")
                .set("padding", "6px")
                .set("border-radius", "8px")
                .set("background-color", "rgba(255, 255, 255, 0.05)");

        collapseBtn.addClickListener(e -> {
            if (this.onToggleCollapseListener != null) {
                this.onToggleCollapseListener.run();
            }
        });

        logoLayout.add(logoBrand, collapseBtn);
        add(logoLayout);

        // Separador
        Div separator = new Div();
        separator.getStyle().set("height", "1px").set("background-color", "#1e293b").set("margin", "6px 0 10px 0");
        add(separator);

        // Elementos de navegación principales
        add(createNavItem("Dashboard", VaadinIcon.HOME, DashboardView.class));
        add(createNavItem("Add Content", VaadinIcon.PLUS, AddContentView.class));
        add(createNavItem("Library", VaadinIcon.BOOKMARK, LibraryView.class));
        add(createNavItem("Categorías", VaadinIcon.TAGS, CategoriesView.class));
        add(createNavItem("Concept Graph", VaadinIcon.CONNECT_O, ConceptGraphView.class));

        // AI Consultant con Badge
        RouterLink aiConsultantLink = createNavItem("AI Consultant", VaadinIcon.CHAT, AiConsultantView.class);
        Span badge = new Span("AI");
        badge.getStyle()
                .set("background-color", "#00b894")
                .set("color", "#ffffff")
                .set("font-size", "10px")
                .set("padding", "2px 6px")
                .set("border-radius", "10px");

        aiConsultantLink.getChildren()
                .filter(c -> c instanceof HorizontalLayout)
                .findFirst()
                .ifPresent(c -> ((HorizontalLayout) c).add(badge));
        add(aiConsultantLink);

        add(createNavItem("Annotations", VaadinIcon.NOTEBOOK, AnnotationsView.class));

        // Espaciador flexible para empujar el perfil al fondo
        VerticalLayout spacer = new VerticalLayout();
        spacer.getStyle().set("min-height", "15px");
        add(spacer);
        expand(spacer);

        // Configuración
        add(createNavItem("Settings", VaadinIcon.COG, SettingsView.class));

        // Perfil de usuario al fondo
        HorizontalLayout profileLayout = new HorizontalLayout();
        profileLayout.setAlignItems(Alignment.CENTER);
        profileLayout.setSpacing(true);
        profileLayout.getStyle()
                .set("border-top", "1px solid #1e293b")
                .set("padding-top", "12px")
                .set("margin-top", "6px")
                .set("width", "100%")
                .set("overflow", "hidden");

        User user = userSession != null ? userSession.getAuthenticatedUser() : null;
        String fullName = "Guest User";
        String email = "guest@example.com";
        String initials = "GU";

        if (user != null) {
            fullName = user.getNombre() != null ? user.getNombre() : "Guest User";
            email = user.getEmail() != null ? user.getEmail() : "guest@example.com";
            if (user.getNombre() != null && !user.getNombre().isEmpty()) {
                initials = Arrays.stream(user.getNombre().split(" "))
                        .map(s -> s.substring(0, 1))
                        .collect(Collectors.joining())
                        .toUpperCase();
            }
        }

        Div avatar = new Div();
        avatar.setText(initials);
        avatar.getStyle()
                .set("background-color", "#00b894")
                .set("color", "#ffffff")
                .set("border-radius", "50%")
                .set("width", "36px")
                .set("height", "36px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("font-weight", "bold")
                .set("flex-shrink", "0");

        VerticalLayout profileTexts = new VerticalLayout();
        profileTexts.setPadding(false);
        profileTexts.setSpacing(false);
        profileTexts.getStyle().set("line-height", "1.2").set("overflow", "hidden").set("max-width", "150px");

        Span nameSpan = new Span(fullName);
        nameSpan.getStyle()
                .set("color", "#ffffff")
                .set("font-weight", "600")
                .set("font-size", "13px")
                .set("white-space", "nowrap")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("display", "block");

        Span emailSpan = new Span(email);
        emailSpan.getStyle()
                .set("font-size", "11px")
                .set("color", "#64748b")
                .set("white-space", "nowrap")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("display", "block");

        profileTexts.add(nameSpan, emailSpan);

        Button logoutButton = new Button(VaadinIcon.SIGN_OUT.create());
        logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        logoutButton.setAriaLabel("Cerrar sesión");
        logoutButton.getElement().setAttribute("title", "Cerrar sesión");
        logoutButton.getStyle().set("color", "#94a3b8").set("margin-left", "auto");
        logoutButton.addClickListener(e -> {
            authService.logout();
            UI.getCurrent().getPage().setLocation("/");
        });

        profileLayout.setWidthFull();
        profileLayout.add(avatar, profileTexts, logoutButton);
        profileLayout.expand(profileTexts);
        add(profileLayout);
    }

    public void setOnToggleCollapseListener(Runnable listener) {
        this.onToggleCollapseListener = listener;
    }

    private RouterLink createNavItem(String text, VaadinIcon icon, Class<? extends Component> viewClass) {
        RouterLink link = new RouterLink("", viewClass);
        link.getStyle()
                .set("text-decoration", "none")
                .set("color", "inherit")
                .set("width", "100%");

        HorizontalLayout item = new HorizontalLayout();
        item.setWidthFull();
        item.setAlignItems(Alignment.CENTER);
        item.setPadding(false);
        item.setSpacing(true);
        item.getStyle()
                .set("cursor", "pointer")
                .set("border-radius", "8px")
                .set("padding", "9px 12px")
                .set("transition", "background-color 0.2s ease");

        Icon vaadinIcon = icon.create();
        vaadinIcon.setSize("18px");
        Span label = new Span(text);
        label.getStyle().set("font-size", "14px").set("white-space", "nowrap");

        item.add(vaadinIcon, label);
        link.add(item);

        navItems.add(new NavItemContainer(link, item, vaadinIcon, label, viewClass));
        return link;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Class<?> targetView = event.getNavigationTarget();

        for (NavItemContainer navItem : navItems) {
            boolean isActive = navItem.viewClass().equals(targetView);

            if (isActive) {
                navItem.itemLayout().getStyle().set("background-color", "#1e293b");
                navItem.icon().setColor("#00b894");
                navItem.label().getStyle().set("color", "#ffffff").set("font-weight", "600");
            } else {
                navItem.itemLayout().getStyle().remove("background-color");
                navItem.icon().setColor("#64748b");
                navItem.label().getStyle().set("color", "#94a3b8").set("font-weight", "normal");
            }
        }
    }
}
