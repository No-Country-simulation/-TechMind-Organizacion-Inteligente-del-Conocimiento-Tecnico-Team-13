package com.application.Views.AI;

import com.application.Views.Layout.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Arrays;
import java.util.List;

@PageTitle("AI Consultant - KnowBase")
@Route(value = "ai-consultant", layout = MainLayout.class)
public class AiConsultantView extends HorizontalLayout {

    private final TextArea chatInput = new TextArea();
    private final VerticalLayout messageStream = new VerticalLayout();

    public AiConsultantView() {
        setSizeFull();
        setSpacing(false);
        setPadding(false);

        // 1. Columna Principal de Chat
        VerticalLayout mainChatLayout = createMainChatLayout();

        // 2. Sidebar de Contexto Activo
        VerticalLayout contextSidebar = createContextSidebar();

        add(mainChatLayout, contextSidebar);
        expand(mainChatLayout);
    }

    private VerticalLayout createMainChatLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);

        // Sub-Header de estado
        HorizontalLayout statusSubHeader = createStatusSubHeader();

        // Contenedor del stream de mensajes y el input
        VerticalLayout chatContainer = new VerticalLayout();
        chatContainer.setSizeFull();
        chatContainer.setPadding(true);
        chatContainer.setSpacing(true);
        chatContainer.getStyle().set("overflow-y", "auto");
        chatContainer.add(createMessageStream());

        // Fila de input
        VerticalLayout inputWrapper = new VerticalLayout();
        inputWrapper.setWidthFull();
        inputWrapper.setPadding(false);
        inputWrapper.setSpacing(true);
        inputWrapper.add(createPromptChips(), createChatInput());

        chatContainer.add(inputWrapper);
        layout.add(createHeader(), statusSubHeader, chatContainer);
        layout.expand(chatContainer);
        return layout;
    }
    
    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.getStyle().set("padding", "var(--lumo-space-m)");

        VerticalLayout titles = new VerticalLayout();
        titles.setPadding(false);
        titles.setSpacing(false);
        H2 mainTitle = new H2("Consultor IA");
        mainTitle.getStyle().set("margin", "0").set("font-size", "24px").set("color", "#0f172a");
        Span subtitle = new Span("Consulta al asistente técnico impulsado por IA");
        subtitle.getStyle().set("color", "#64748b").set("font-size", "14px");
        titles.add(mainTitle, subtitle);

        header.add(titles);
        return header;
    }

    private HorizontalLayout createStatusSubHeader() {
        HorizontalLayout subHeader = new HorizontalLayout();
        subHeader.setWidthFull();
        subHeader.setPadding(true);
        subHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        subHeader.getStyle()
                .set("background-color", "#f8fafc")
                .set("border-bottom", "1px solid #e2e8f0");

        Div statusBadge = new Div();
        statusBadge.getStyle()
                .set("width", "10px")
                .set("height", "10px")
                .set("background-color", "#00C48C")
                .set("border-radius", "50%");

        Span statusText = new Span("KnowBase XG-2 · 1,284 recursos indexados · Última sync: hace 2 min");
        statusText.getStyle().set("font-size", "13px").set("color", "#475569").set("margin-left", "8px");

        Button newSessionButton = new Button("Nueva sesión", new Icon(VaadinIcon.REFRESH));
        newSessionButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        newSessionButton.getStyle().set("margin-left", "auto");

        subHeader.add(statusBadge, statusText, newSessionButton);
        return subHeader;
    }

    private VerticalLayout createMessageStream() {
        messageStream.setWidthFull();
        messageStream.setSpacing(true);

        // Mensajes de ejemplo
        messageStream.add(createMessageBubble(false, "¡Hola! Soy tu asistente de IA. ¿En qué puedo ayudarte hoy?", "hace 3 min", null));
        messageStream.add(createMessageBubble(true, "¿Puedes explicarme la diferencia entre OAuth2 y JWT para la autenticación en microservicios?", "hace 2 min", null));
        
        String aiResponse = "¡Claro! Aquí tienes un resumen:\n" +
                "• **OAuth2** es un **protocolo de autorización** que permite a una aplicación obtener acceso limitado a una cuenta de usuario en un servicio HTTP. Es ideal para delegar permisos.\n" +
                "• **JWT (JSON Web Token)** es un **estándar de token** compacto y autónomo para transmitir información de forma segura entre partes como un objeto JSON. Se usa comúnmente para la **autenticación** una vez que el usuario ha iniciado sesión.\n" +
                "\n" +
                "En tu base de conocimiento, los recursos más relevantes son:\n" +
                "- `Spring Boot 3 — OAuth2 y JWT` (confianza: 94%)\n" +
                "- `Keycloak Integration` (confianza: 76%)\n" +
                "- `JWT Best Practices` (confianza: 71%)";
        messageStream.add(createMessageBubble(false, aiResponse, "hace 1 min", Arrays.asList("Spring Boot 3 — OAuth2 y JWT", "Keycloak Integration", "JWT Best Practices")));
        
        return messageStream;
    }

    private HorizontalLayout createMessageBubble(boolean isUser, String message, String timestamp, List<String> references) {
        HorizontalLayout bubbleLayout = new HorizontalLayout();
        bubbleLayout.setSpacing(true);
        bubbleLayout.setAlignItems(FlexComponent.Alignment.START);

        Div avatar = new Div();
        avatar.getStyle()
                .set("width", "36px")
                .set("height", "36px")
                .set("border-radius", "50%")
                .set("flex-shrink", "0")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");

        VerticalLayout messageContentLayout = new VerticalLayout();
        messageContentLayout.setSpacing(false);
        messageContentLayout.setPadding(false);

        Div messageBubbleDiv = new Div();
        messageBubbleDiv.getStyle().set("margin", "0");

        String[] lines = message.split("\\n");
        for (String line : lines) {
            if (line.isEmpty()) {
                messageBubbleDiv.add(new Div()); // Add an empty div for line breaks
            } else if (line.trim().startsWith("•") || line.trim().startsWith("-")) {
                messageBubbleDiv.add(new Span(line));
            } else if (line.matches(".*\\(confianza: \\d+%\\).*")) {
                String styledLine = line.replaceAll("(confianza: \\d+%)", "<span style='color: #00C48C;'>$1</span>");
                
            } else {
                messageBubbleDiv.add(new Span(line));
            }
        }

        Span time = new Span(timestamp);
        time.getStyle().set("font-size", "11px").set("color", "#94a3b8");

        messageContentLayout.add(messageBubbleDiv, time);

        if (isUser) {
            avatar.setText("AT");
            avatar.getStyle().set("background-color", "#0f172a").set("color", "#ffffff");
            bubbleLayout.getStyle().set("flex-direction", "row-reverse");
            messageContentLayout.getStyle().set("align-items", "flex-end");
            messageBubbleDiv.getStyle().set("background-color", "#0f172a").set("color", "#ffffff").set("padding", "10px 15px").set("border-radius", "12px");
        } else {
            Icon icon = VaadinIcon.AUTOMATION.create();
            icon.setColor("#00C48C");
            avatar.add(icon);
            avatar.getStyle().set("background-color", "#e6f9f4");
            messageBubbleDiv.getStyle().set("background-color", "#ffffff").set("border", "1px solid #e2e8f0").set("padding", "10px 15px").set("border-radius", "12px");
        }

        bubbleLayout.add(avatar, messageContentLayout);
        return bubbleLayout;
    }


    private FlexLayout createPromptChips() {
        FlexLayout chipLayout = new FlexLayout();
        chipLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        chipLayout.getStyle().set("gap", "8px");

        String[] prompts = {
                "¿Cómo implemento Circuit Breaker en Java?",
                "Explícame el patrón Saga en microservicios",
                "¿Qué recursos tengo sobre Kubernetes?",
                "Compara OAuth2 vs JWT en mi base de datos"
        };

        for (String prompt : prompts) {
            Button chip = new Button(prompt);
            chip.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            chip.getStyle()
                    .set("border-radius", "20px")
                    .set("font-size", "12px")
                    .set("padding", "5px 12px")
                    .set("background-color", "#f1f5f9")
                    .set("border", "1px solid #e2e8f0")
                    .set("color", "#475569");
            chip.addClickListener(e -> chatInput.setValue(prompt));
            chipLayout.add(chip);
        }
        return chipLayout;
    }

    private VerticalLayout createChatInput() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setWidthFull();

        HorizontalLayout inputLayout = new HorizontalLayout();
        inputLayout.setWidthFull();
        inputLayout.setSpacing(true);
        inputLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        chatInput.setPlaceholder("Escribe tu pregunta aquí...");
        chatInput.setWidthFull();
        chatInput.getStyle()
                .set("border-radius", "12px")
                .set("background-color", "#ffffff")
                .set("border", "1px solid #e2e8f0");

        Button sendButton = new Button(new Icon(VaadinIcon.PAPERPLANE_O));
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        sendButton.setAriaLabel("Enviar mensaje");
        sendButton.getStyle()
                .set("background-color", "#00C48C")
                .set("color", "#ffffff")
                .set("border-radius", "50%")
                .set("width", "44px")
                .set("height", "44px");

        inputLayout.add(chatInput, sendButton);
        inputLayout.expand(chatInput);

        Span helpText = new Span("Enter para enviar · Shift+Enter para nueva línea");
        helpText.getStyle().set("font-size", "11px").set("color", "#94a3b8").set("margin-left", "4px").set("margin-top", "4px");
        
        layout.add(inputLayout, helpText);
        return layout;
    }

    private VerticalLayout createContextSidebar() {
        VerticalLayout sidebar = new VerticalLayout();
        sidebar.setWidth("340px");
        sidebar.setHeightFull();
        sidebar.getStyle()
                .set("background-color", "#ffffff")
                .set("border-left", "1px solid #e2e8f0")
                .set("padding", "var(--lumo-space-m)");
        sidebar.setSpacing(true);

        // Título de la sección
        Span title = new Span("CONTEXTO ACTIVO");
        title.getStyle()
                .set("font-weight", "600")
                .set("color", "#475569")
                .set("font-size", "12px")
                .set("letter-spacing", "0.05em");
        sidebar.add(title);

        // Subsección de recursos
        sidebar.add(createResourceSection());
        
        // Subsección de estadísticas
        sidebar.add(createStatsSection());

        // Botón de exportar
        Button exportButton = new Button("Guardar Hilo de Conversación", new Icon(VaadinIcon.DOWNLOAD));
        exportButton.setWidthFull();
        sidebar.add(exportButton);

        return sidebar;
    }

    private VerticalLayout createResourceSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);

        H4 subTitle = new H4("RECURSOS REFERENCIADOS");
        subTitle.getStyle()
                .set("font-size", "14px")
                .set("font-weight", "600")
                .set("margin", "0");
        section.add(subTitle);

        section.add(createResourceCard("Spring Boot 3 — OAuth2 y JWT", "Backend", "#e2e8f0", "94%"));
        section.add(createResourceCard("Keycloak Integration", "Security", "#fee2e2", "76%"));
        section.add(createResourceCard("JWT Best Practices", "Security", "#fee2e2", "71%"));
        
        return section;
    }
    
    private Div createResourceCard(String title, String badgeText, String badgeColor, String percentage) {
        Div card = new Div();
        card.setWidthFull();
        card.getStyle()
                .set("background-color", "#f8fafc")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "8px")
                .set("padding", "12px");

        HorizontalLayout content = new HorizontalLayout();
        content.setSpacing(true);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setWidthFull();

        VerticalLayout info = new VerticalLayout();
        info.setSpacing(false);
        info.setPadding(false);
        
        Span titleSpan = new Span(title);
        titleSpan.getStyle().set("font-weight", "500").set("font-size", "14px");
        
        Span badge = new Span(badgeText);
        badge.getElement().getThemeList().add("badge");
        badge.getStyle()
            .set("background-color", badgeColor)
            .set("color", "#374151")
            .set("font-size", "11px")
            .set("padding", "2px 8px")
            .set("border-radius", "12px");
            
        info.add(titleSpan, badge);
        
        Span percentageSpan = new Span(percentage);
        percentageSpan.getStyle()
                .set("color", "#00C48C")
                .set("font-weight", "600")
                .set("font-size", "16px")
                .set("margin-left", "auto");

        content.add(info, percentageSpan);
        content.expand(info);
        card.add(content);
        return card;
    }
    
    private VerticalLayout createStatsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        section.getStyle().set("border-top", "1px solid #e2e8f0").set("padding-top", "var(--lumo-space-m)");

        H4 subTitle = new H4("ESTADÍSTICAS DE SESIÓN");
        subTitle.getStyle()
                .set("font-size", "14px")
                .set("font-weight", "600")
                .set("margin-bottom", "var(--lumo-space-s)");
        section.add(subTitle);
        
        section.add(createStatRow("Consultas:", "4"));
        section.add(createStatRow("Recursos citados:", "12"));
        section.add(createStatRow("Precisión media:", "83%"));

        return section;
    }
    
    private HorizontalLayout createStatRow(String label, String value) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Span labelSpan = new Span(label);
        labelSpan.getStyle().set("color", "#64748b").set("font-size", "14px");

        Span valueSpan = new Span(value);
        valueSpan.getStyle().set("font-weight", "600").set("color", "#0f172a").set("font-size", "14px");
        
        row.add(labelSpan, valueSpan);
        return row;
    }
}
