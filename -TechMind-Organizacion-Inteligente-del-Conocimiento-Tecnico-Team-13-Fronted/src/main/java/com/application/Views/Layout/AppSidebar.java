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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;

import java.util.Arrays;
import java.util.stream.Collectors;

public class AppSidebar extends VerticalLayout {

    public AppSidebar(UserSession userSession, AuthService authService) {
        setWidth("260px");
        setHeightFull();
        setPadding(true);
        setSpacing(true);
        setMargin(false);
        // ensure sidebar stretches with layout and doesn't shrink
        getStyle()
                .set("background-color", "#0b1329")
                .set("color", "#94a3b8")
                .set("overflow-y", "auto")
                .set("flex-shrink", "0");
        // ensure the component itself stretches to parent height
        // Allow the sidebar to follow the page scroll when content exceeds viewport
        getElement().getStyle().set("align-self", "flex-start").set("min-height", "0");
        // Keep the sidebar visually sticky within viewport but allow page scroll to move it when necessary
        getElement().getStyle().set("position", "sticky").set("top", "0");

        // Logo / Título de la app
        HorizontalLayout logoLayout = new HorizontalLayout();
        logoLayout.setAlignItems(Alignment.CENTER);
        Icon logoIcon = VaadinIcon.DATABASE.create();
        logoIcon.setColor("#00b894"); // Verde brillante
        H3 logoTitle = new H3("LogiCore");
        logoTitle.getStyle().set("color", "#ffffff").set("margin", "0");
        Span logoSubtitle = new Span("Technical Co-pilot");
        logoSubtitle.getStyle().set("font-size", "11px").set("color", "#64748b");

        VerticalLayout titleWrapper = new VerticalLayout(logoTitle, logoSubtitle);
        titleWrapper.setPadding(false);
        titleWrapper.setSpacing(false);
        logoLayout.add(logoIcon, titleWrapper);
        add(logoLayout);

        // Separador
        Div separator = new Div();
        separator.getStyle().set("height", "1px").set("background-color", "#1e293b").set("margin", "10px 0");
        add(separator);

        // Elementos de navegación
        add(createNavItem("Dashboard", VaadinIcon.HOME, DashboardView.class, true));
        add(createNavItem("Add Content", VaadinIcon.PLUS, AddContentView.class, false));
        add(createNavItem("Library", VaadinIcon.BOOKMARK, LibraryView.class, false));
        add(createNavItem("Categorías", VaadinIcon.TAGS, CategoriesView.class, false));
        add(createNavItem("Concept Graph", VaadinIcon.CONNECT_O, ConceptGraphView.class, false));

        // Item con badge
        RouterLink aiConsultantLink = createNavItem("AI Consultant", VaadinIcon.CHAT, AiConsultantView.class, false);
        Span badge = new Span("AI");
        badge.getStyle()
                .set("background-color", "#00b894")
                .set("color", "#ffffff")
                .set("font-size", "10px")
                .set("padding", "2px 6px")
                .set("border-radius", "10px");
        // Agregar el badge al contenedor del item
        aiConsultantLink.getChildren()
                .filter(c -> c instanceof HorizontalLayout)
                .findFirst()
                .ifPresent(c -> ((HorizontalLayout) c).add(badge));
        add(aiConsultantLink);

        add(createNavItem("Annotations", VaadinIcon.NOTEBOOK, AnnotationsView.class, false));

        // Espaciador para empujar el perfil al fondo
        VerticalLayout spacer = new VerticalLayout();
        add(spacer);
        expand(spacer);

        // Configuración
        add(createNavItem("Settings", VaadinIcon.COG, SettingsView.class, false));

        // Perfil de usuario al fondo
        HorizontalLayout profileLayout = new HorizontalLayout();
        profileLayout.setAlignItems(Alignment.CENTER);
        profileLayout.getStyle()
                .set("border-top", "1px solid #1e293b")
                .set("padding-top", "15px")
                .set("width", "100%");

        User user = userSession.getAuthenticatedUser();
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
                .set("font-weight", "bold");

        VerticalLayout profileTexts = new VerticalLayout(
                new Span(fullName),
                new Span(email));
        profileTexts.setPadding(false);
        profileTexts.setSpacing(false);
        profileTexts.getStyle().set("line-height", "1.2");
        profileTexts.getChildren().findFirst().ifPresent(c -> c.getStyle().set("color", "#ffffff").set("font-weight", "bold"));
        profileTexts.getChildren().skip(1).findFirst().ifPresent(c -> c.getStyle().set("font-size", "11px").set("color", "#64748b"));

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

    private RouterLink createNavItem(String text, VaadinIcon icon, Class<? extends Component> viewClass, boolean active) {
        RouterLink link = new RouterLink("", viewClass);
        link.getStyle()
                .set("text-decoration", "none")
                .set("color", "inherit")
                .set("width", "100%");

        HorizontalLayout item = new HorizontalLayout();
        item.setWidthFull();
        item.setAlignItems(Alignment.CENTER);
        item.setPadding(true);
        item.setSpacing(true);
        item.getStyle()
                .set("cursor", "pointer")
                .set("border-radius", "8px")
                .set("padding", "10px 15px");

        Icon vaadinIcon = icon.create();
        Span label = new Span(text);

        if (active) {
            item.getStyle().set("background-color", "#1e293b");
            vaadinIcon.setColor("#00b894");
            label.getStyle().set("color", "#ffffff").set("font-weight", "600");
        } else {
            vaadinIcon.setColor("#64748b");
            label.getStyle().set("color", "#94a3b8");
        }

        item.add(vaadinIcon, label);
        link.add(item);

        return link;
    }
}
