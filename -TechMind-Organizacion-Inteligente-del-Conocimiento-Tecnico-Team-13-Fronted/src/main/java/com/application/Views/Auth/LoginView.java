package com.application.Views.Auth;

import com.application.Views.Dashboard.DashboardView;
import com.application.exception.EmailNotConfirmedException;
import com.application.exception.SupabaseAuthException;
import com.application.exception.SupabaseRateLimitException;
import com.application.service.AuthService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.List;
import java.util.regex.Pattern;

@Route("")
@PageTitle("Login | Logicore")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthService authService;
    private VerticalLayout loginForm;
    private VerticalLayout registerForm;

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
    private static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
    private static final String NAME_REGEX = "^[a-zA-Z\\s]+$";

    public LoginView(AuthService authService) {
        this.authService = authService;
        
        // Configuración del contenedor principal
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        getStyle().set("background-color", "#f4f6f9");

        // Tarjeta principal
        HorizontalLayout cardContainer = new HorizontalLayout();
        cardContainer.setWidth("850px");
        cardContainer.setHeight("520px");
        cardContainer.setPadding(false);
        cardContainer.setSpacing(false);
        cardContainer.getStyle()
                .set("border-radius", "16px")
                .set("box-shadow", "0 10px 25px rgba(0, 0, 0, 0.1)")
                .set("overflow", "hidden");

        // Paneles
        cardContainer.add(createLeftPanel(), createRightPanel());
        add(cardContainer);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        List<String> confirmed = event.getLocation().getQueryParameters().getParameters().get("confirmed");
        if (confirmed != null && confirmed.contains("true")) {
            Notification.show("¡Correo confirmado con éxito! Ya puedes iniciar sesión.", 4000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
    }

    private VerticalLayout createLeftPanel() {
        VerticalLayout leftPanel = new VerticalLayout();
        leftPanel.setWidth("50%");
        leftPanel.setHeightFull();
        leftPanel.setPadding(true);
        leftPanel.getStyle()
                .set("background-color", "#0b1329")
                .set("color", "white")
                .set("padding", "32px");

        HorizontalLayout logoLayout = new HorizontalLayout();
        Image brandLogo = new Image("Logo-Logicore.svg", "Logicore Logo");
        brandLogo.setMaxHeight("26px");
        brandLogo.setMaxWidth("26px");
        brandLogo.getStyle().set("object-fit", "contain");
        Span brandName = new Span("Logicore");
        brandName.getStyle().set("font-weight", "700").set("font-size", "1.2rem");
        logoLayout.add(brandLogo, brandName);
        logoLayout.setAlignItems(Alignment.CENTER);

        Image illustration = new Image("Logo-Logicore.svg", "Illustration");
        
        illustration.setWidth("100%");
        illustration.setMaxWidth("260px");
        illustration.getStyle().set("margin", "auto");

        H2 welcomeTitle = new H2("¡Bienvenido a Logicore!");
        welcomeTitle.getStyle().set("margin", "0").set("color", "white").set("font-weight", "600");

        Paragraph description = new Paragraph("Conectando con la red del conocimiento de Logicore. Listo para clasificar y potenciar tus ideas.");
        description.getStyle().set("color", "#d0e1ff").set("font-size", "0.85rem").set("margin-top", "8px");

        HorizontalLayout dotsLayout = new HorizontalLayout();
        dotsLayout.setSpacing(true);
        for (int i = 0; i < 3; i++) {
            Span dot = new Span();
            dot.setWidth("8px");
            dot.setHeight("8px");
            dot.getStyle().set("border-radius", "50%").set("border", "1.5px solid white").set("display", "inline-block");
            if (i == 1) {
                dot.getStyle().set("background-color", "white");
            }
            dotsLayout.add(dot);
        }

        VerticalLayout bottomContent = new VerticalLayout(welcomeTitle, description, dotsLayout);
        bottomContent.setPadding(false);
        bottomContent.setSpacing(false);

        leftPanel.add(logoLayout, illustration, bottomContent);
        leftPanel.expand(illustration);
        return leftPanel;
    }

    private VerticalLayout createRightPanel() {
        VerticalLayout rightPanel = new VerticalLayout();
        rightPanel.setWidth("50%");
        rightPanel.setHeightFull();
        rightPanel.getStyle().set("background-color", "#ffffff").set("padding", "40px");
        rightPanel.setJustifyContentMode(JustifyContentMode.CENTER);

        // Login Form
        loginForm = new VerticalLayout();
        loginForm.setPadding(false);
        loginForm.setSpacing(false);
        loginForm.setMargin(false);

        H2 loginTitle = new H2("Log In");
        loginTitle.getStyle().set("color", "#0066FF").set("margin-bottom", "4px").set("font-weight", "600");

        Span subtitle = new Span("Welcome back! Please enter your details.");
        subtitle.getStyle().set("font-size", "0.8rem").set("color", "#a0aec0").set("margin-bottom", "20px");

        TextField usernameField = new TextField();
        usernameField.setPlaceholder("Username");
        usernameField.setSuffixComponent(VaadinIcon.USER.create());
        usernameField.setWidthFull();
        applyUnderlineStyle(usernameField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPlaceholder("Password");
        passwordField.setSuffixComponent(VaadinIcon.LOCK.create());
        passwordField.setWidthFull();
        applyUnderlineStyle(passwordField);

        Runnable performLogin = () -> {
            String username = usernameField.getValue().trim();
            String password = passwordField.getValue();
            if (username.isEmpty() || password.isEmpty()) {
                Notification.show("Username and password are required.", 3000, Notification.Position.TOP_CENTER);
                return;
            }
            try {
                if (authService.authenticate(username, password) != null) {
                    UI.getCurrent().navigate(DashboardView.class);
                } else {
                    Notification.show("Invalid username or password.", 3000, Notification.Position.TOP_CENTER);
                }
            } catch (EmailNotConfirmedException e) {
                Notification.show("Debes confirmar tu correo antes de iniciar sesión. Revisa tu bandeja de entrada.", 5000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        };

        Button signInBtn = new Button("Sign in", event -> performLogin.run());
        signInBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        signInBtn.getStyle().set("background-color", "#0066FF").set("border-radius", "6px").set("padding", "0 24px");

        Checkbox rememberPassword = new Checkbox("Remember password");
        rememberPassword.getStyle().set("font-size", "0.8rem").set("color", "#7e8e9f");

        HorizontalLayout actionLayout = new HorizontalLayout(signInBtn, rememberPassword);
        actionLayout.setWidthFull();
        actionLayout.setAlignItems(Alignment.CENTER);
        actionLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        actionLayout.getStyle().set("margin-top", "24px");

        Anchor forgotPassword = new Anchor("#", "Forget your password?");
        forgotPassword.getStyle().set("font-size", "0.8rem").set("color", "#0066FF").set("margin-top", "18px").set("align-self", "center");

        // Botón Crear Cuenta con la misma estética, ubicado al final debajo de olvidaste contraseña
        Button createAccountBtn = new Button("Create an account", e -> showRegisterForm());
        createAccountBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createAccountBtn.setWidthFull();
        createAccountBtn.getStyle()
                .set("background-color", "#ffffff")
                .set("color", "#0066FF")
                .set("border", "1.5px solid #0066FF")
                .set("border-radius", "6px")
                .set("padding", "0 24px")
                .set("font-weight", "600")
                .set("margin-top", "16px")
                .set("cursor", "pointer");

        loginForm.add(loginTitle, subtitle, usernameField, passwordField, actionLayout, forgotPassword, createAccountBtn);

        // Atajo: Enter en usuario o contraseña dispara "Sign in". Se hace con JS puro (en vez de
        // Shortcuts/addClickShortcut de Vaadin) porque el PasswordField envuelve su <input> en un
        // <form> interno para el autocompletado del navegador, y ese <form> puede interceptar el
        // Enter antes de que llegue al listener de Flow. Escuchando directo en cada campo y
        // llamando preventDefault() se evita ese conflicto de forma determinística.
        bindEnterToClick(usernameField, signInBtn);
        bindEnterToClick(passwordField, signInBtn);

        // Register Form
        registerForm = new VerticalLayout();
        registerForm.setVisible(false);
        registerForm.setPadding(false);
        registerForm.setSpacing(false);
        registerForm.setMargin(false);

        H2 registerTitle = new H2("Create your account");
        registerTitle.getStyle().set("color", "#0066FF").set("margin-bottom", "4px").set("font-weight", "600");
        
        TextField registerNombreField = new TextField("Nombre");
        registerNombreField.setWidthFull();
        applyUnderlineStyle(registerNombreField);

        TextField registerUsernameField = new TextField("Username (Email)");
        registerUsernameField.setWidthFull();
        applyUnderlineStyle(registerUsernameField);

        PasswordField registerPasswordField = new PasswordField("Password");
        registerPasswordField.setWidthFull();
        registerPasswordField.setHelperText("Mínimo 8 caracteres, 1 mayúscula, 1 minúscula, 1 dígito, 1 carácter especial.");
        applyUnderlineStyle(registerPasswordField);

        Runnable performRegister = () -> {
            String nombre = registerNombreField.getValue().trim();
            String username = registerUsernameField.getValue().trim();
            String password = registerPasswordField.getValue();
            if (!validateInput(nombre, username, password)) {
                return;
            }
            try {
                if (authService.register(username, password, nombre) != null) {
                    Notification.show("Cuenta creada. Revisa tu correo para confirmar tu cuenta antes de iniciar sesión.", 5000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    showLoginForm();
                } else {
                    Notification.show("Username already exists.", 3000, Notification.Position.TOP_CENTER);
                }
            } catch (SupabaseRateLimitException | SupabaseAuthException e) {
                Notification.show(e.getMessage(), 5000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        };

        Button registerButton = new Button("Register", event -> performRegister.run());
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.getStyle().set("background-color", "#0066FF").set("border-radius", "6px").set("padding", "0 24px");

        Button backToLogin = new Button("Back to login", e -> showLoginForm());
        backToLogin.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backToLogin.getStyle().set("font-size", "0.8rem").set("color", "#0066FF");

        registerForm.add(registerTitle, registerNombreField, registerUsernameField, registerPasswordField, registerButton, backToLogin);

        // Atajo: Enter en cualquier campo del registro dispara "Register".
        bindEnterToClick(registerNombreField, registerButton);
        bindEnterToClick(registerUsernameField, registerButton);
        bindEnterToClick(registerPasswordField, registerButton);

        rightPanel.add(loginForm, registerForm);
        return rightPanel;
    }

    private void showLoginForm() {
        loginForm.setVisible(true);
        registerForm.setVisible(false);
    }

    private void showRegisterForm() {
        loginForm.setVisible(false);
        registerForm.setVisible(true);
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

    /** Enter en `field` hace clic en `button`. Se escucha en fase de CAPTURA (tercer argumento
     *  `true` de addEventListener): el Shadow DOM interno de vaadin-text-field/password-field
     *  maneja Enter por su cuenta (para el <form> interno del autocompletado) y puede detener la
     *  propagación antes de que un listener normal (fase de burbuja) la reciba; en captura, en
     *  cambio, nuestro listener en el elemento host se dispara ANTES de que baje al Shadow DOM, así
     *  que nada dentro del componente puede bloquearlo primero. */
    private void bindEnterToClick(Component field, Button button) {
        field.getElement().executeJs(
                "this.addEventListener('keydown', function(e) {" +
                        "  if (e.key === 'Enter') {" +
                        "    e.preventDefault();" +
                        "    e.stopPropagation();" +
                        "    if (!$0.disabled) { $0.click(); }" +
                        "  }" +
                        "}, true);",
                button.getElement());
    }

    private void applyUnderlineStyle(com.vaadin.flow.component.HasStyle component) {
        component.getStyle()
                .set("--vaadin-input-field-border-width", "0 0 1px 0")
                .set("--lumo-border-radius", "0px")
                .set("margin-bottom", "12px");
    }
}
