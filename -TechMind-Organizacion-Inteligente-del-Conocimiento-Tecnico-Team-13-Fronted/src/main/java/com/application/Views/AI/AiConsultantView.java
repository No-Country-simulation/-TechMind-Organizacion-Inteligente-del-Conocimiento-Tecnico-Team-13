package com.application.Views.AI;

import com.application.Views.Layout.MainLayout;
import com.application.Views.Library.LibraryView;
import com.application.client.openai.ChatTurn;
import com.application.exception.ModeloServiceException;
import com.application.service.RagChatService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import java.util.ArrayList;
import java.util.List;

@PageTitle("AI Consultant - KnowBase")
@Route(value = "ai-consultant", layout = MainLayout.class)
public class AiConsultantView extends VerticalLayout {

    private final RagChatService ragChatService;
    private final List<ChatTurn> historial = new ArrayList<>();

    private VerticalLayout chatArea;
    private TextArea inputField;
    private Button sendBtn;

    public AiConsultantView(RagChatService ragChatService) {
        this.ragChatService = ragChatService;

        setSizeFull();
        setMargin(false);
        setSpacing(true);
        setPadding(false);
        getStyle()
                .set("overflow-y", "auto")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("padding", "20px");

        HorizontalLayout header = createHeader();
        header.setHeight("auto");
        add(header);

        VerticalLayout chatContainer = createChatContainer();
        chatContainer.getStyle().set("flex", "1");
        add(chatContainer);

        addWelcomeMessage();
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);

        VerticalLayout titles = new VerticalLayout();
        titles.setPadding(false);
        titles.setSpacing(false);
        H2 mainTitle = new H2("Consultor IA");
        mainTitle.getStyle().set("margin", "0").set("font-size", "24px").set("color", "#0f172a");
        Span subtitle = new Span("RAG sobre tu base de conocimiento: cada respuesta cita las fuentes reales usadas");
        subtitle.getStyle().set("color", "#64748b").set("font-size", "14px");
        titles.add(mainTitle, subtitle);

        header.add(titles);
        return header;
    }

    private VerticalLayout createChatContainer() {
        VerticalLayout container = new VerticalLayout();
        container.setWidthFull();
        container.setHeight("600px");
        container.getStyle()
                .set("background-color", "#ffffff")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "12px")
                .set("padding", "20px")
                .set("display", "flex")
                .set("flex-direction", "column");

        chatArea = new VerticalLayout();
        chatArea.setWidthFull();
        chatArea.setHeight("480px");
        chatArea.setPadding(false);
        chatArea.setSpacing(true);
        chatArea.getStyle()
                .set("background-color", "#f8fafc")
                .set("border-radius", "8px")
                .set("padding", "15px")
                .set("overflow-y", "auto")
                .set("border", "1px solid #e2e8f0")
                .set("margin-bottom", "15px");

        HorizontalLayout inputArea = new HorizontalLayout();
        inputArea.setWidthFull();
        inputArea.setSpacing(true);
        inputArea.setAlignItems(Alignment.CENTER);

        inputField = new TextArea();
        inputField.setPlaceholder("Escribe tu pregunta técnica... (Enter para enviar, Shift+Enter para salto de línea)");
        inputField.setHeight("60px");
        inputField.getStyle().set("margin", "0");
        inputField.addKeyDownListener(Key.ENTER, e -> {
            if (!e.getModifiers().contains(com.vaadin.flow.component.KeyModifier.SHIFT)) {
                sendQuestion();
            }
        });

        sendBtn = new Button("Enviar");
        sendBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        sendBtn.setIcon(VaadinIcon.ARROW_FORWARD.create());
        sendBtn.getStyle().set("background-color", "#00b894").set("color", "#ffffff");
        sendBtn.setHeight("60px");
        sendBtn.addClickListener(e -> sendQuestion());

        inputArea.add(inputField, sendBtn);
        inputArea.expand(inputField);

        container.add(chatArea, inputArea);
        container.expand(chatArea);

        return container;
    }

    private void addWelcomeMessage() {
        Span placeholder = new Span("Pregunta algo sobre el contenido técnico guardado en KnowBase. " +
                "Responderé solo con lo que encuentre en la base de conocimiento y citaré las fuentes.");
        placeholder.getStyle().set("color", "#94a3b8").set("font-size", "13px");
        chatArea.add(placeholder);
    }

    private void sendQuestion() {
        String pregunta = inputField.getValue() != null ? inputField.getValue().trim() : "";
        if (pregunta.isEmpty()) {
            return;
        }

        addMessageBubble(pregunta, true, null);
        inputField.clear();
        sendBtn.setEnabled(false);
        inputField.setEnabled(false);

        Span loading = new Span("Consultando la base de conocimiento…");
        loading.getStyle().set("color", "#94a3b8").set("font-size", "12px").set("font-style", "italic");
        chatArea.add(loading);

        try {
            RagChatService.RagAnswer respuesta = ragChatService.ask(pregunta, historial);

            chatArea.remove(loading);
            addMessageBubble(respuesta.respuesta(), false, respuesta.fuentes());

            historial.add(ChatTurn.user(pregunta));
            historial.add(ChatTurn.assistant(respuesta.respuesta()));
        } catch (ModeloServiceException e) {
            chatArea.remove(loading);
            Notification.show("El Consultor IA no está disponible ahora mismo: " + e.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } finally {
            sendBtn.setEnabled(true);
            inputField.setEnabled(true);
            inputField.focus();
        }
    }

    private void addMessageBubble(String texto, boolean esUsuario, List<RagChatService.Citation> fuentes) {
        VerticalLayout bubble = new VerticalLayout();
        bubble.setPadding(false);
        bubble.setSpacing(false);
        bubble.setWidth("80%");
        bubble.getStyle()
                .set("align-self", esUsuario ? "flex-end" : "flex-start")
                .set("background-color", esUsuario ? "#0066FF" : "#ffffff")
                .set("color", esUsuario ? "#ffffff" : "#0f172a")
                .set("border", esUsuario ? "none" : "1px solid #e2e8f0")
                .set("border-radius", "12px")
                .set("padding", "10px 14px");

        Paragraph texto1 = new Paragraph(texto);
        texto1.getStyle().set("margin", "0").set("font-size", "13px").set("white-space", "pre-wrap");
        bubble.add(texto1);

        if (fuentes != null && !fuentes.isEmpty()) {
            HorizontalLayout fuentesLayout = new HorizontalLayout();
            fuentesLayout.getStyle().set("flex-wrap", "wrap").set("gap", "6px").set("margin-top", "8px");
            for (RagChatService.Citation cita : fuentes) {
                RouterLink link = new RouterLink(cita.titulo(), LibraryView.class);
                link.getStyle()
                        .set("font-size", "11px")
                        .set("background-color", "#f1f5f9")
                        .set("color", "#334155")
                        .set("padding", "3px 8px")
                        .set("border-radius", "10px")
                        .set("text-decoration", "none");
                fuentesLayout.add(link);
            }
            bubble.add(fuentesLayout);
        }

        chatArea.add(bubble);
        bubble.getElement().executeJs("this.scrollIntoView({behavior: 'smooth', block: 'end'})");
    }
}
