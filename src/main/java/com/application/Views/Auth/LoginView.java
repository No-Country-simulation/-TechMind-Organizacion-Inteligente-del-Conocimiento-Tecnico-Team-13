package com.application.Views.Auth;

import com.application.Views.Dashboard.DashboardView;
import com.application.service.AuthenticatedUser;
import com.application.service.SupabaseAuthService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.component.UI;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("Logicore | Login")
@Route("login")
@AnonymousAllowed
public class LoginView extends VerticalLayout {

    private final SupabaseAuthService authService;

    @Autowired
    public LoginView(SupabaseAuthService authService) {
        this.authService = authService;
        setSizeFull();
        setPadding(false);
        setMargin(false);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassName("login-page");

        Div card = new Div();
        card.addClassName("login-card");

        Div leftPanel = new Div();
        leftPanel.addClassName("branding-panel");

        HorizontalLayout logoRow = new HorizontalLayout();
        logoRow.setAlignItems(Alignment.CENTER);
        logoRow.setSpacing(true);
        logoRow.addClassName("logo-row");

        Div logoMark = new Div("LGCore");
        logoMark.addClassName("logo-mark");

        Span logoText = new Span("Logicore");
        logoText.addClassName("logo-text");

        logoRow.add(logoMark, logoText);

        Div targetGraphic = new Div();
        targetGraphic.addClassName("target-graphic");
        targetGraphic.getElement().setProperty("innerHTML",
                "<div class='speed-lines'></div><div class='target-ring target-ring-1'></div><div class='target-ring target-ring-2'></div><div class='target-ring target-ring-3'></div><div class='target-center'></div><div class='target-arrow'></div><div class='plus plus-1'>+</div><div class='plus plus-2'>+</div><div class='plus plus-3'>+</div><div class='plus plus-4'>+</div>");

        H3 welcomeTitle = new H3("Welcome!");
        welcomeTitle.addClassName("welcome-title");

        Span description = new Span("Get a real intranet on top of your Office 365 environment, with Upteamist.");
        description.addClassName("welcome-description");

        Div pagination = new Div();
        pagination.addClassName("login-pagination");
        pagination.getElement().setProperty("innerHTML",
                "<span class='page-dot page-dot-outline'></span><span class='page-dot page-dot-outline'></span><span class='page-dot page-dot-filled'></span>");

        leftPanel.add(logoRow, targetGraphic, welcomeTitle, description, pagination);

        VerticalLayout rightPanel = new VerticalLayout();
        rightPanel.setPadding(false);
        rightPanel.setSpacing(false);
        rightPanel.setMargin(false);
        rightPanel.addClassName("form-panel");

        H2 formTitle = new H2("Log In");
        formTitle.addClassName("form-title");

        HorizontalLayout accountRow = new HorizontalLayout();
        accountRow.setSpacing(false);
        accountRow.setPadding(false);
        accountRow.setAlignItems(Alignment.CENTER);
        Span accountText = new Span("Don't have an account?");
        accountText.addClassName("form-muted");
        Anchor createAccount = new Anchor("register", "Create an account");
        createAccount.addClassName("create-account-link");
        accountRow.add(accountText, createAccount);

        Span shortHint = new Span("It will take less than a minute.");
        shortHint.addClassName("form-hint");

        EmailField emailField = new EmailField();
        emailField.setPlaceholder("Correo electrónico");
        emailField.setWidthFull();
        emailField.setPrefixComponent(VaadinIcon.ENVELOPE.create());
        emailField.addClassName("login-input");

        PasswordField passwordField = new PasswordField();
        passwordField.setPlaceholder("Contraseña");
        passwordField.setWidthFull();
        passwordField.setPrefixComponent(VaadinIcon.LOCK.create());
        passwordField.addClassName("login-input");

        HorizontalLayout actionsRow = new HorizontalLayout();
        actionsRow.setWidthFull();
        actionsRow.setSpacing(true);
        actionsRow.setAlignItems(FlexComponent.Alignment.CENTER);

        Button signInButton = new Button("Sign in", event -> {
            String email = emailField.getValue();
            String password = passwordField.getValue();

            if (email.isBlank() || password.isBlank()) {
                Notification.show("Ingresa correo y contraseña", 3000, Notification.Position.MIDDLE);
                return;
            }

            AuthenticatedUser authUser = authService.signIn(email, password);
            if (authUser != null) {
                VaadinSession.getCurrent().setAttribute(AuthenticatedUser.class, authUser);
                Notification.show("Bienvenido " + authUser.name(), 3000, Notification.Position.MIDDLE);
                UI.getCurrent().navigate(DashboardView.class);
            } else {
                Notification.show("Correo o contraseña inválidos", 3000, Notification.Position.MIDDLE);
            }
        });
        signInButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        signInButton.addClassName("sign-in-button");

        Checkbox rememberPassword = new Checkbox("Remember password");
        rememberPassword.addClassName("remember-checkbox");

        actionsRow.add(signInButton, rememberPassword);

        Anchor forgotPassword = new Anchor("create-account-link", "Olvidé mi contraseña");
        forgotPassword.addClassName("forgot-password-link");

        rightPanel.add(formTitle, accountRow, shortHint, emailField, passwordField, actionsRow, forgotPassword);

        card.add(leftPanel, rightPanel);
        add(card);
    }
}
