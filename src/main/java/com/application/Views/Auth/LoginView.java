package com.application.Views.Auth;

import com.application.Views.Dashboard.DashboardView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
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

@PageTitle("Logicore | Login")
@Route("")
@AnonymousAllowed
public class LoginView extends VerticalLayout {

    public LoginView() {
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
        Anchor createAccount = new Anchor("#", "Create an account");
        createAccount.addClassName("create-account-link");
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

        Button signInButton = new Button("Sign in", event -> UI.getCurrent().navigate(DashboardView.class));
        signInButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        signInButton.addClassName("sign-in-button");

        Checkbox rememberPassword = new Checkbox("Remember password");
        rememberPassword.addClassName("remember-checkbox");

        actionsRow.add(signInButton, rememberPassword);

        Anchor forgotPassword = new Anchor("#", "Forget your password?");
        forgotPassword.addClassName("forgot-password-link");

        rightPanel.add(formTitle, accountRow, shortHint, usernameField, passwordField, actionsRow, forgotPassword);

        card.add(leftPanel, rightPanel);
        add(card);
    }
}
