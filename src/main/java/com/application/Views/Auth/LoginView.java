package com.application.Views.Auth;

import com.application.Views.Dashboard.DashboardView;
import com.application.service.AuthService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.component.UI;

import java.util.regex.Pattern;

@PageTitle("Logicore | Login")
@Route("")
@AnonymousAllowed
public class LoginView extends VerticalLayout {

    private final AuthService authService;

    private VerticalLayout loginForm;
    private VerticalLayout registerForm;

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
    private static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
    private static final String NAME_REGEX = "^[a-zA-Z\s]+$";

    public LoginView(AuthService authService) {
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

        // Login Form
        loginForm = new VerticalLayout();
        loginForm.setPadding(false);
        loginForm.setSpacing(false);
        loginForm.setMargin(false);
        loginForm.addClassName("form-panel");

        H2 formTitle = new H2("Log In");
        formTitle.addClassName("form-title");

        HorizontalLayout accountRow = new HorizontalLayout();
        accountRow.setSpacing(false);
        accountRow.setPadding(false);
        accountRow.setAlignItems(Alignment.CENTER);
        Span accountText = new Span("Don't have an account?");
        accountText.addClassName("form-muted");
        Button createAccount = new Button("Create an account");
        createAccount.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        createAccount.addClassName("create-account-link");
        createAccount.addClickListener(e -> showRegisterForm());
        accountRow.add(accountText, createAccount);

        Span shortHint = new Span("It will take less than a minute.");
        shortHint.addClassName("form-hint");

        TextField usernameField = new TextField();
        usernameField.setPlaceholder("Username");
        usernameField.setWidthFull();
        usernameField.setPrefixComponent(VaadinIcon.USER.create());
        usernameField.addClassName("login-input");

        PasswordField passwordField = new PasswordField();
        passwordField.setPlaceholder("Password");
        passwordField.setWidthFull();
        passwordField.setPrefixComponent(VaadinIcon.LOCK.create());
        passwordField.addClassName("login-input");

        HorizontalLayout actionsRow = new HorizontalLayout();
        actionsRow.setWidthFull();
        actionsRow.setSpacing(true);
        actionsRow.setAlignItems(FlexComponent.Alignment.CENTER);

        Button signInButton = new Button("Sign in", event -> {
            String username = usernameField.getValue().trim();
            String password = passwordField.getValue();
            if (username.isEmpty() || password.isEmpty()) {
                Notification.show("Username and password are required.", 3000, Notification.Position.TOP_CENTER);
                return;
            }
            if (authService.authenticate(username, password) != null) {
                UI.getCurrent().navigate(DashboardView.class);
            } else {
                Notification.show("Invalid username or password.", 3000, Notification.Position.TOP_CENTER);
            }
        });
        signInButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        signInButton.addClassName("sign-in-button");

        Checkbox rememberPassword = new Checkbox("Remember password");
        rememberPassword.addClassName("remember-checkbox");

        actionsRow.add(signInButton, rememberPassword);

        Anchor forgotPassword = new Anchor("#", "Forget your password?");
        forgotPassword.addClassName("forgot-password-link");

        loginForm.add(formTitle, accountRow, shortHint, usernameField, passwordField, actionsRow, forgotPassword);

        // Register Form
        registerForm = new VerticalLayout();
        registerForm.setPadding(false);
        registerForm.setSpacing(false);
        registerForm.setMargin(false);
        registerForm.addClassName("form-panel");
        registerForm.setVisible(false);

        H2 registerFormTitle = new H2("Create your account");
        registerFormTitle.addClassName("form-title");

        Span registerHint = new Span("Use an account for internal access.");
        registerHint.addClassName("form-hint");

        TextField registerNombreField = new TextField("Nombre");
        registerNombreField.setWidthFull();

        TextField registerUsernameField = new TextField("Username (Email)");
        registerUsernameField.setWidthFull();

        PasswordField registerPasswordField = new PasswordField("Password");
        registerPasswordField.setWidthFull();
        registerPasswordField.setHelperText("Mínimo 8 caracteres, 1 mayúscula, 1 minúscula, 1 dígito, 1 carácter especial.");

        Button registerButton = new Button("Register", event -> {
            String nombre = registerNombreField.getValue().trim();
            String username = registerUsernameField.getValue().trim();
            String password = registerPasswordField.getValue();

            if (!validateInput(nombre, username, password)) {
                return;
            }

            if (authService.register(username, password, nombre) != null) {
                Notification.show("Account created successfully. Please log in.", 3000, Notification.Position.TOP_CENTER);
                showLoginForm();
            } else {
                Notification.show("Username already exists.", 3000, Notification.Position.TOP_CENTER);
            }
        });
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button loginLink = new Button("Back to login");
        loginLink.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        loginLink.addClassName("create-account-link");
        loginLink.addClickListener(e -> showLoginForm());


        registerForm.add(registerFormTitle, registerHint, registerNombreField, registerUsernameField, registerPasswordField, registerButton, loginLink);

        Div rightPanel = new Div();
        rightPanel.addClassName("right-panel");
        rightPanel.add(loginForm, registerForm);

        card.add(leftPanel, rightPanel);
        add(card);
    }

    private boolean validateInput(String nombre, String email, String password) {
        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Notification.show("Name, email, and password are required.", 3000, Notification.Position.TOP_CENTER);
            return false;
        }

        if (!Pattern.matches(NAME_REGEX, nombre)) {
            Notification.show("Please enter a valid name (only letters and spaces).", 3000, Notification.Position.TOP_CENTER);
            return false;
        }

        if (!Pattern.matches(EMAIL_REGEX, email)) {
            Notification.show("Please enter a valid email address.", 3000, Notification.Position.TOP_CENTER);
            return false;
        }

        if (!Pattern.matches(PASSWORD_REGEX, password)) {
            Notification.show("Password does not meet the complexity requirements.", 3000, Notification.Position.TOP_CENTER);
            return false;
        }

        return true;
    }

    private void showLoginForm() {
        loginForm.setVisible(true);
        registerForm.setVisible(false);
    }

    private void showRegisterForm() {
        loginForm.setVisible(false);
        registerForm.setVisible(true);
    }
}
