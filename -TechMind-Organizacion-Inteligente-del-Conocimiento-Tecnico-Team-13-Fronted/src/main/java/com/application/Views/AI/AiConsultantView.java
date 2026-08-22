package com.application.Views.AI;

import com.application.Views.Layout.MainLayout;
import com.application.Views.Library.LibraryView;
import com.application.client.openai.ChatTurn;
import com.application.exception.ModeloServiceException;
import com.application.model.User;
import com.application.service.ChatHistoryService;
import com.application.service.RagChatService;
import com.application.service.UserSession;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Consultor IA con RAG real: cada respuesta viene de RagChatService (embeddings + contexto real +
 * OpenAI), cita fuentes reales de la biblioteca (no datos de ejemplo), soporta Markdown, y puede
 * renderizar bloques ```html``` que el modelo genere como un mini "canvas" en un iframe aislado.
 */
@PageTitle("AI Consultant - KnowBase")
@Route(value = "ai-consultant", layout = MainLayout.class)
public class AiConsultantView extends HorizontalLayout {

    private static final Logger log = LoggerFactory.getLogger(AiConsultantView.class);
    private static final Pattern HTML_BLOCK_PATTERN = Pattern.compile("```html\\s*\\n(.*?)```", Pattern.DOTALL);
    private static final Parser MARKDOWN_PARSER = Parser.builder(new MutableDataSet()).build();
    private static final HtmlRenderer MARKDOWN_RENDERER = HtmlRenderer.builder(new MutableDataSet()).build();

    private final RagChatService ragChatService;
    private final ChatHistoryService chatHistoryService;
    private final UUID currentUserId;
    private UUID sessionId;
    private final List<ChatTurn> historial = new ArrayList<>();
    /** Título -> similitud media, acumulado en la sesión para "Recursos citados" / "Relevancia media". */
    private final Map<String, Double> citasDeLaSesion = new LinkedHashMap<>();
    private int consultas = 0;

    private final TextArea chatInput = new TextArea();
    private final VerticalLayout messageStream = new VerticalLayout();
    private Button sendButton;
    private VerticalLayout resourceSection;
    private VerticalLayout statsSection;

    public AiConsultantView(RagChatService ragChatService, ChatHistoryService chatHistoryService, UserSession userSession) {
        this.ragChatService = ragChatService;
        this.chatHistoryService = chatHistoryService;
        User authenticatedUser = userSession != null ? userSession.getAuthenticatedUser() : null;
        this.currentUserId = authenticatedUser != null ? authenticatedUser.getId() : null;
        this.sessionId = currentUserId != null ? chatHistoryService.cargarUltimaSesionOCrear(currentUserId) : UUID.randomUUID();

        setSizeFull();
        setSpacing(false);
        setPadding(false);

        // @keyframes para los puntitos animados del indicador de "pensando" (createThinkingIndicator).
        // Guardado por id: si se navega ida y vuelta a esta vista varias veces, no se duplica.
        getElement().executeJs(
                "if (!document.getElementById('tm-thinking-dots-style')) {" +
                        "  var s = document.createElement('style');" +
                        "  s.id = 'tm-thinking-dots-style';" +
                        "  s.textContent = '@keyframes tmBounce {0%,80%,100%{transform:scale(.6);opacity:.4} 40%{transform:scale(1);opacity:1}}';" +
                        "  document.head.appendChild(s);" +
                        "}");

        VerticalLayout mainChatLayout = createMainChatLayout();
        VerticalLayout contextSidebar = createContextSidebar();

        add(mainChatLayout, contextSidebar);
        expand(mainChatLayout);

        loadHistorialGuardado();
    }

    /** Recarga la última conversación guardada del usuario (si existe) al abrir la vista;
     *  si nunca chateó antes, muestra el mensaje de bienvenida de siempre. */
    private void loadHistorialGuardado() {
        List<ChatHistoryService.MensajeGuardado> guardados = currentUserId != null
                ? chatHistoryService.cargarMensajes(sessionId)
                : List.of();

        if (guardados.isEmpty()) {
            addWelcomeMessage();
            return;
        }

        for (ChatHistoryService.MensajeGuardado mensaje : guardados) {
            boolean esUsuario = "user".equals(mensaje.rol());
            addMessageBubble(esUsuario, mensaje.contenido(), esUsuario ? null : mensaje.citas());
            historial.add(esUsuario ? ChatTurn.user(mensaje.contenido()) : ChatTurn.assistant(mensaje.contenido()));
            if (esUsuario) {
                consultas++;
            } else if (mensaje.citas() != null) {
                mensaje.citas().forEach(c -> citasDeLaSesion.merge(c.titulo(), c.similitud(), Math::max));
            }
        }
        updateResourceSection(List.of());
        updateStatsSection();
    }

    private VerticalLayout createMainChatLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);

        VerticalLayout chatContainer = new VerticalLayout();
        chatContainer.setSizeFull();
        chatContainer.setPadding(true);
        chatContainer.setSpacing(true);
        chatContainer.getStyle().set("overflow-y", "auto");
        chatContainer.add(createMessageStream());

        VerticalLayout inputWrapper = new VerticalLayout();
        inputWrapper.setWidthFull();
        inputWrapper.setPadding(false);
        inputWrapper.setSpacing(true);
        inputWrapper.add(createPromptChips(), createChatInput());

        chatContainer.add(inputWrapper);
        layout.add(createHeader(), chatContainer);
        layout.expand(chatContainer);
        return layout;
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.getStyle().set("padding", "var(--lumo-space-m)").set("border-bottom", "1px solid #e2e8f0");

        VerticalLayout titles = new VerticalLayout();
        titles.setPadding(false);
        titles.setSpacing(false);
        H2 mainTitle = new H2("Consultor IA");
        mainTitle.getStyle().set("margin", "0").set("font-size", "24px").set("color", "#0f172a");
        Span subtitle = new Span("RAG sobre tu base de conocimiento: cada respuesta cita fuentes reales");
        subtitle.getStyle().set("color", "#64748b").set("font-size", "14px");
        titles.add(mainTitle, subtitle);

        Button historialButton = new Button("Historial", new Icon(VaadinIcon.CLOCK));
        historialButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        historialButton.getStyle().set("margin-left", "auto");
        historialButton.addClickListener(e -> openHistorialDialog());

        Button newSessionButton = new Button("Nueva sesión", new Icon(VaadinIcon.REFRESH));
        newSessionButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        newSessionButton.addClickListener(e -> resetSession());

        header.add(titles, historialButton, newSessionButton);
        return header;
    }

    /** Panel compacto ("command palette") con las últimas 10 conversaciones del usuario: buscador
     *  arriba, lista angosta que se ajusta a su contenido (no deja un cuadro vacío cuando hay
     *  pocos resultados) y con scroll propio solo si hace falta. Clic en cualquier tarjeta la
     *  carga y reemplaza lo que se ve en pantalla. Modal a propósito: es lo único que garantiza,
     *  de forma confiable entre versiones de Vaadin, que un clic afuera lo cierre — un intento
     *  anterior con Dialog no-modal + reposicionamiento manual rompía justo eso. */
    private void openHistorialDialog() {
        if (currentUserId == null) {
            Notification.show("Inicia sesión para ver tu historial.", 3000, Notification.Position.BOTTOM_END);
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setWidth("360px");
        dialog.setMaxHeight("min(480px, 70vh)");
        dialog.addThemeVariants(DialogVariant.LUMO_NO_PADDING);

        VerticalLayout root = new VerticalLayout();
        root.setWidthFull();
        root.setPadding(false);
        root.setSpacing(false);
        root.getStyle().set("box-sizing", "border-box");

        Div header = new Div();
        header.getStyle()
                .set("padding", "14px 16px 10px")
                .set("border-bottom", "1px solid #e2e8f0")
                .set("box-sizing", "border-box");
        H3 title = new H3("Historial");
        title.getStyle().set("margin", "0 0 8px").set("font-size", "16px").set("color", "#0f172a");

        TextField search = new TextField();
        search.setPlaceholder("Buscar en tus conversaciones…");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setWidthFull();
        search.setValueChangeMode(ValueChangeMode.EAGER);
        search.getStyle().set("box-sizing", "border-box");

        header.add(title, search);

        VerticalLayout list = new VerticalLayout();
        list.setWidthFull();
        list.setPadding(true);
        list.setSpacing(false);
        // Sin flex/altura fija: la lista crece con su contenido hasta el max-height del diálogo y
        // recién ahí empieza a scrollear (overflow-y: auto), en vez de reservar siempre el mismo
        // alto y dejar un cuadro vacío cuando hay 1-2 conversaciones.
        list.getStyle()
                .set("overflow-y", "auto")
                .set("overflow-x", "hidden")
                .set("box-sizing", "border-box")
                .set("max-height", "calc(min(480px, 70vh) - 92px)");

        List<ChatHistoryService.SesionResumen> sesiones = chatHistoryService.listarSesiones(currentUserId);
        if (sesiones.isEmpty()) {
            Span empty = new Span("Todavía no tienes conversaciones guardadas.");
            empty.getStyle().set("color", "#94a3b8").set("font-size", "13px");
            list.add(empty);
        } else {
            Map<ChatHistoryService.SesionResumen, Div> tarjetas = new LinkedHashMap<>();
            for (ChatHistoryService.SesionResumen sesion : sesiones) {
                Div card = createSessionCard(sesion, sesion.sessionId().equals(sessionId), () -> {
                    switchToSession(sesion.sessionId());
                    dialog.close();
                });
                tarjetas.put(sesion, card);
                list.add(card);
            }
            search.addValueChangeListener(e -> {
                String query = e.getValue() == null ? "" : e.getValue().trim().toLowerCase();
                tarjetas.forEach((sesion, card) -> {
                    String preview = sesion.primeraPregunta() != null ? sesion.primeraPregunta().toLowerCase() : "";
                    card.setVisible(query.isEmpty() || preview.contains(query));
                });
            });
        }

        root.add(header, list);
        dialog.add(root);
        dialog.open();
    }

    private Div createSessionCard(ChatHistoryService.SesionResumen sesion, boolean esActual, Runnable onClick) {
        Div card = new Div();
        card.setWidthFull();
        card.getStyle()
                .set("box-sizing", "border-box")
                .set("cursor", "pointer")
                .set("background-color", esActual ? "#e6f9f4" : "#f8fafc")
                .set("border", esActual ? "1px solid #00C48C" : "1px solid #e2e8f0")
                .set("border-radius", "10px")
                .set("padding", "10px 12px")
                .set("margin-bottom", "6px")
                .set("transition", "background-color 0.15s");
        card.getElement().addEventListener("mouseover", e -> {
            if (!esActual) {
                card.getStyle().set("background-color", "#eef2f7");
            }
        });
        card.getElement().addEventListener("mouseout", e -> {
            if (!esActual) {
                card.getStyle().set("background-color", "#f8fafc");
            }
        });

        String preview = sesion.primeraPregunta() != null && !sesion.primeraPregunta().isBlank()
                ? sesion.primeraPregunta() : "(sin mensajes)";
        Span previewSpan = new Span(preview);
        previewSpan.getStyle()
                .set("display", "block").set("font-weight", "600").set("font-size", "13px").set("color", "#0f172a")
                .set("overflow", "hidden").set("text-overflow", "ellipsis").set("white-space", "nowrap");

        String meta = (sesion.ultimoMensaje() != null ? DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(sesion.ultimoMensaje()) : "")
                + " · " + sesion.totalMensajes() + " mensaje(s)" + (esActual ? " · actual" : "");
        Span metaSpan = new Span(meta);
        metaSpan.getStyle()
                .set("display", "block").set("font-size", "11px").set("color", "#94a3b8").set("margin-top", "2px")
                .set("overflow", "hidden").set("text-overflow", "ellipsis").set("white-space", "nowrap");

        card.add(previewSpan, metaSpan);
        card.getElement().addEventListener("click", e -> onClick.run());
        return card;
    }

    /** Cambia a otra conversación guardada (elegida en el diálogo de Historial) y la muestra. */
    private void switchToSession(UUID targetSessionId) {
        this.sessionId = targetSessionId;
        historial.clear();
        citasDeLaSesion.clear();
        consultas = 0;
        messageStream.removeAll();
        loadHistorialGuardado();
        updateStatsSection();
    }

    private void resetSession() {
        // Arranca un session_id nuevo: la conversación anterior queda guardada en
        // chat_mensaje, solo se deja de mostrar (no se borra nada).
        sessionId = UUID.randomUUID();
        historial.clear();
        citasDeLaSesion.clear();
        consultas = 0;
        messageStream.removeAll();
        addWelcomeMessage();
        updateResourceSection(List.of());
        updateStatsSection();
    }

    private VerticalLayout createMessageStream() {
        messageStream.setWidthFull();
        messageStream.setSpacing(true);
        return messageStream;
    }

    private void addWelcomeMessage() {
        Span placeholder = new Span("Pregunta algo sobre el contenido técnico guardado en KnowBase. " +
                "Respondo solo con lo que encuentro en la base de conocimiento y cito las fuentes.");
        placeholder.getStyle().set("color", "#94a3b8").set("font-size", "13px").set("padding", "0 8px");
        messageStream.add(placeholder);
    }

    private FlexLayout createPromptChips() {
        FlexLayout chipLayout = new FlexLayout();
        chipLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        chipLayout.getStyle().set("gap", "8px");

        String[] prompts = {
                "¿Qué contenido tengo sobre Kubernetes?",
                "Resume lo que guardé sobre autenticación",
                "¿Qué categorías tienen más contenido guardado?",
                "Compárame dos temas que haya guardado"
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
        sendButton = new Button(new Icon(VaadinIcon.PAPERPLANE_O));
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        sendButton.setAriaLabel("Enviar mensaje");
        sendButton.getStyle()
                .set("background-color", "#00C48C")
                .set("color", "#ffffff")
                .set("border-radius", "50%")
                .set("width", "44px")
                .set("height", "44px");
        sendButton.addClickListener(e -> sendQuestion());
        // Enter envía; Shift+Enter inserta salto de línea (comportamiento nativo del TextArea, no
        // se previene). Fase de CAPTURA (`true` en addEventListener): el Shadow DOM interno de
        // vaadin-text-area puede detener la propagación de Enter antes de que un listener normal
        // (fase de burbuja) la reciba; en captura, el listener del elemento host se dispara antes
        // de bajar al Shadow DOM, así que nada dentro puede bloquearlo ni insertar la línea primero.
        // `if (!$0.disabled)`: un Enter mantenido presionado (auto-repeat del teclado) puede
        // disparar varios keydown antes de que el primer ui.push() le confirme al cliente que el
        // botón ya se deshabilitó; sin este chequeo, un segundo click() sintético se cuela y
        // dispara sendQuestion() dos veces (dos burbujas de "pensando" superpuestas).
        chatInput.getElement().executeJs(
                "this.addEventListener('keydown', function(e) {" +
                        "  if (e.key === 'Enter' && !e.shiftKey) {" +
                        "    e.preventDefault();" +
                        "    e.stopPropagation();" +
                        "    if (!$0.disabled) { $0.click(); }" +
                        "  }" +
                        "}, true);",
                sendButton.getElement());

        inputLayout.add(chatInput, sendButton);
        inputLayout.expand(chatInput);

        Span helpText = new Span("Enter para enviar · Shift+Enter para nueva línea");
        helpText.getStyle().set("font-size", "11px").set("color", "#94a3b8").set("margin-left", "4px").set("margin-top", "4px");

        layout.add(inputLayout, helpText);
        return layout;
    }

    private void sendQuestion() {
        String pregunta = chatInput.getValue() != null ? chatInput.getValue().trim() : "";
        if (pregunta.isEmpty()) {
            return;
        }

        addMessageBubble(true, pregunta, null);
        chatInput.clear();
        sendButton.setEnabled(false);
        chatInput.setEnabled(false);

        // Indicador de "el agente está pensando": puntitos animados + una línea de estado
        // (embedding, búsqueda, generación), actualizada por el callback onStage de
        // RagChatService.askStreaming. Se quita apenas llega el primer token.
        ThinkingIndicator thinking = createThinkingIndicator();

        UI ui = getUI().orElse(null);
        if (ui == null) {
            return;
        }
        // Copia del historial: se lee desde el hilo de fondo mientras el campo `historial` real
        // sigue siendo mutable desde el hilo de UI (p.ej. "Nueva sesión"); evita pisarse.
        List<ChatTurn> historialSnapshot = new ArrayList<>(historial);

        // TODO EL TRABAJO PESADO (embedding, búsqueda, streaming de OpenAI) va en un hilo de
        // fondo, no en el hilo que atendió el clic. Motivo real (visto en el log): los chunks SSE
        // de OpenAI llegan y disparan onToken/onStage en un hilo de Netty (red), no en el hilo de
        // Vaadin — tocar componentes de UI ahí tira "Cannot access state in VaadinSession or UI
        // without locking the session". Cada toque a la UI, sea desde ese hilo de Netty o desde
        // este hilo de fondo, tiene que pasar por ui.access(...), que adquiere el lock de la
        // sesión de forma segura (y con @Push hace el push automático al terminar cada bloque).
        CompletableFuture.runAsync(() -> {
            Div[] respuestaDiv = new Div[1];
            StringBuilder acumulado = new StringBuilder();
            try {
                RagChatService.RagAnswer respuesta = ragChatService.askStreaming(pregunta, historialSnapshot,
                        etapa -> ui.access(() -> thinking.estadoSpan().setText(etapa)),
                        token -> ui.access(() -> {
                            if (respuestaDiv[0] == null) {
                                removeIfPresent(thinking.root());
                                respuestaDiv[0] = createAssistantBubbleShell();
                            }
                            acumulado.append(token);
                            renderAssistantContent(respuestaDiv[0], acumulado.toString());
                        }));

                ui.access(() -> {
                    Div div = respuestaDiv[0];
                    if (div == null) {
                        // No llegó ningún token vía streaming (poco probable, pero no dejar sin respuesta).
                        removeIfPresent(thinking.root());
                        div = createAssistantBubbleShell();
                        renderAssistantContent(div, respuesta.respuesta());
                    }
                    appendCitations(div, respuesta.fuentes());

                    historial.add(ChatTurn.user(pregunta));
                    historial.add(ChatTurn.assistant(respuesta.respuesta()));

                    if (currentUserId != null) {
                        chatHistoryService.guardarMensaje(sessionId, currentUserId, "user", pregunta, null);
                        chatHistoryService.guardarMensaje(sessionId, currentUserId, "assistant", respuesta.respuesta(), respuesta.fuentes());
                    }

                    consultas++;
                    respuesta.fuentes().forEach(c -> citasDeLaSesion.merge(c.titulo(), c.similitud(), Math::max));
                    updateResourceSection(respuesta.fuentes());
                    updateStatsSection();

                    sendButton.setEnabled(true);
                    chatInput.setEnabled(true);
                    chatInput.focus();
                });
            } catch (ModeloServiceException e) {
                ui.access(() -> {
                    removeIfPresent(thinking.root());
                    Notification.show("El Consultor IA no está disponible ahora mismo: " + e.getMessage(), 6000, Notification.Position.BOTTOM_END)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    sendButton.setEnabled(true);
                    chatInput.setEnabled(true);
                    chatInput.focus();
                });
            } catch (Exception e) {
                log.error("Error inesperado procesando la pregunta \"{}\"", pregunta, e);
                ui.access(() -> {
                    removeIfPresent(thinking.root());
                    Notification.show("Ocurrió un error inesperado: " + e.getMessage(), 6000, Notification.Position.BOTTOM_END)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    sendButton.setEnabled(true);
                    chatInput.setEnabled(true);
                    chatInput.focus();
                });
            }
        });
    }

    private void addMessageBubble(boolean isUser, String message, List<RagChatService.Citation> citas) {
        if (!isUser) {
            Div div = createAssistantBubbleShell();
            renderAssistantContent(div, message);
            appendCitations(div, citas);
            return;
        }

        HorizontalLayout bubbleLayout = new HorizontalLayout();
        bubbleLayout.setSpacing(true);
        bubbleLayout.setAlignItems(FlexComponent.Alignment.START);
        bubbleLayout.setWidthFull();
        bubbleLayout.getStyle().set("flex-direction", "row-reverse");

        Div avatar = new Div();
        avatar.setText("Tú");
        avatar.getStyle()
                .set("width", "36px")
                .set("height", "36px")
                .set("border-radius", "50%")
                .set("flex-shrink", "0")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("background-color", "#0f172a").set("color", "#ffffff").set("font-size", "11px");

        VerticalLayout messageContentLayout = new VerticalLayout();
        messageContentLayout.setSpacing(false);
        messageContentLayout.setPadding(false);
        messageContentLayout.setWidthFull();
        messageContentLayout.getStyle().set("align-items", "flex-end");

        Div messageBubbleDiv = new Div();
        messageBubbleDiv.getStyle()
                .set("margin", "0").set("max-width", "100%")
                .set("background-color", "#0f172a").set("color", "#ffffff")
                .set("padding", "10px 15px").set("border-radius", "12px");
        messageBubbleDiv.add(new Span(message));

        messageContentLayout.add(messageBubbleDiv);
        bubbleLayout.add(avatar, messageContentLayout);
        bubbleLayout.expand(messageContentLayout);

        messageStream.add(bubbleLayout);
        bubbleLayout.getElement().executeJs("this.scrollIntoView({behavior: 'smooth', block: 'end'})");
    }

    private record ThinkingIndicator(Component root, Span estadoSpan) {
    }

    /** Quita `component` del stream si sigue siendo su hijo. Con try/catch a propósito: incluso
     *  con el chequeo de getParent(), Vaadin puede seguir tirando IllegalArgumentException
     *  ("Trying to detach an element from parent that does not have it") en algún caso borde de
     *  timing entre callbacks — y si esa excepción escapa desde el bloque catch de sendQuestion(),
     *  tapa el Notification.show() del error real y dej a la burbuja de "pensando" congelada en
     *  pantalla para siempre, sin que el usuario vea ningún mensaje. Esto nunca debe poder pasar. */
    private void removeIfPresent(Component component) {
        try {
            if (component.getParent().isPresent()) {
                messageStream.remove(component);
            }
        } catch (IllegalArgumentException ignored) {
            // Ya no es hijo de messageStream (lo quitó otra rama); no hay nada que limpiar.
        }
    }

    /** Burbuja compacta de "el agente está pensando": 3 puntitos animados + una línea de estado
     *  (actualizada por onStage) — no ocupa toda la pantalla, se ve y se comporta como el resto de
     *  los mensajes del stream. Se quita apenas llega el primer token de la respuesta real. */
    private ThinkingIndicator createThinkingIndicator() {
        HorizontalLayout bubbleLayout = new HorizontalLayout();
        bubbleLayout.setSpacing(true);
        bubbleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        bubbleLayout.setWidthFull();

        Div avatar = new Div();
        avatar.getStyle()
                .set("width", "36px")
                .set("height", "36px")
                .set("border-radius", "50%")
                .set("flex-shrink", "0")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("background-color", "#e6f9f4");
        Icon icon = VaadinIcon.AUTOMATION.create();
        icon.setColor("#00C48C");
        avatar.add(icon);

        Div bubble = new Div();
        bubble.getStyle()
                .set("background-color", "#ffffff").set("border", "1px solid #e2e8f0")
                .set("padding", "10px 15px").set("border-radius", "12px")
                .set("display", "inline-flex").set("align-items", "center").set("gap", "8px")
                .set("width", "fit-content");

        Html dots = new Html(
                "<span style='display:inline-flex;gap:4px;'>" +
                        "<i style='width:6px;height:6px;border-radius:50%;background:#94a3b8;display:inline-block;animation:tmBounce 1.2s infinite ease-in-out;'></i>" +
                        "<i style='width:6px;height:6px;border-radius:50%;background:#94a3b8;display:inline-block;animation:tmBounce 1.2s infinite ease-in-out .15s;'></i>" +
                        "<i style='width:6px;height:6px;border-radius:50%;background:#94a3b8;display:inline-block;animation:tmBounce 1.2s infinite ease-in-out .3s;'></i>" +
                        "</span>");

        Span estado = new Span("Iniciando…");
        estado.getStyle().set("font-size", "12px").set("color", "#64748b").set("font-style", "italic").set("white-space", "nowrap");

        bubble.add(dots, estado);
        bubbleLayout.add(avatar, bubble);

        messageStream.add(bubbleLayout);
        bubbleLayout.getElement().executeJs("this.scrollIntoView({behavior: 'smooth', block: 'end'})");
        return new ThinkingIndicator(bubbleLayout, estado);
    }

    /** Crea (y agrega al stream) la burbuja de una respuesta del asistente, vacía de contenido:
     *  se rellena en incrementos vía renderAssistantContent a medida que llegan tokens. */
    private Div createAssistantBubbleShell() {
        HorizontalLayout bubbleLayout = new HorizontalLayout();
        bubbleLayout.setSpacing(true);
        bubbleLayout.setAlignItems(FlexComponent.Alignment.START);
        bubbleLayout.setWidthFull();

        Div avatar = new Div();
        avatar.getStyle()
                .set("width", "36px")
                .set("height", "36px")
                .set("border-radius", "50%")
                .set("flex-shrink", "0")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("background-color", "#e6f9f4");
        Icon icon = VaadinIcon.AUTOMATION.create();
        icon.setColor("#00C48C");
        avatar.add(icon);

        VerticalLayout messageContentLayout = new VerticalLayout();
        messageContentLayout.setSpacing(false);
        messageContentLayout.setPadding(false);
        messageContentLayout.setWidthFull();

        Div messageBubbleDiv = new Div();
        messageBubbleDiv.getStyle()
                .set("margin", "0").set("max-width", "100%")
                .set("background-color", "#ffffff").set("border", "1px solid #e2e8f0")
                .set("padding", "10px 15px").set("border-radius", "12px");

        messageContentLayout.add(messageBubbleDiv);
        bubbleLayout.add(avatar, messageContentLayout);
        bubbleLayout.expand(messageContentLayout);

        messageStream.add(bubbleLayout);
        bubbleLayout.getElement().executeJs("this.scrollIntoView({behavior: 'smooth', block: 'end'})");
        return messageBubbleDiv;
    }

    /** Re-renderiza el Markdown/HTML acumulado dentro de la burbuja; se llama en cada token
     *  durante streaming, así que reemplaza todo el contenido (no hay forma barata de "append" con
     *  Markdown, ya que un fragmento a medias puede cambiar cómo se interpreta el resto). */
    private void renderAssistantContent(Div messageBubbleDiv, String textoCompleto) {
        messageBubbleDiv.removeAll();
        List<String> htmlBlocks = new ArrayList<>();
        String markdownOnly = extractHtmlBlocks(textoCompleto, htmlBlocks);
        messageBubbleDiv.add(new Html("<div>" + MARKDOWN_RENDERER.render(MARKDOWN_PARSER.parse(markdownOnly)) + "</div>"));
        htmlBlocks.forEach(block -> messageBubbleDiv.add(buildCanvasFrame(block)));
    }

    private void appendCitations(Div messageBubbleDiv, List<RagChatService.Citation> citas) {
        if (citas != null && !citas.isEmpty()) {
            messageBubbleDiv.add(buildCitationsRow(citas));
        }
    }

    /** Saca los bloques ```html``` del texto (para renderizarlos aparte en un iframe) y deja el resto como Markdown. */
    private String extractHtmlBlocks(String message, List<String> htmlBlocksOut) {
        Matcher matcher = HTML_BLOCK_PATTERN.matcher(message);
        StringBuilder remaining = new StringBuilder();
        int lastEnd = 0;
        while (matcher.find()) {
            remaining.append(message, lastEnd, matcher.start());
            htmlBlocksOut.add(matcher.group(1));
            lastEnd = matcher.end();
        }
        remaining.append(message.substring(lastEnd));
        return remaining.toString();
    }

    private Html buildCanvasFrame(String htmlContent) {
        String escaped = htmlContent
                .replace("&", "&amp;")
                .replace("\"", "&quot;");
        String iframe = "<iframe sandbox=\"allow-scripts\" "
                + "style=\"width:100%;height:320px;border:1px solid #e2e8f0;border-radius:8px;margin-top:8px;background:#fff;\" "
                + "srcdoc=\"" + escaped + "\"></iframe>";
        return new Html(iframe);
    }

    private Component buildCitationsRow(List<RagChatService.Citation> citas) {
        HorizontalLayout fuentesLayout = new HorizontalLayout();
        fuentesLayout.getStyle().set("flex-wrap", "wrap").set("gap", "6px").set("margin-top", "10px");
        for (RagChatService.Citation cita : citas) {
            RouterLink link = new RouterLink(String.format("%s (%.0f%%)", cita.titulo(), cita.similitud() * 100), LibraryView.class);
            link.getStyle()
                    .set("font-size", "11px")
                    .set("background-color", "#f1f5f9")
                    .set("color", "#334155")
                    .set("padding", "3px 8px")
                    .set("border-radius", "10px")
                    .set("text-decoration", "none");
            fuentesLayout.add(link);
        }
        return fuentesLayout;
    }

    // ==========================================
    // SIDEBAR DE CONTEXTO
    // ==========================================
    private VerticalLayout createContextSidebar() {
        VerticalLayout sidebar = new VerticalLayout();
        sidebar.setWidth("340px");
        sidebar.setHeightFull();
        sidebar.getStyle()
                .set("background-color", "#ffffff")
                .set("border-left", "1px solid #e2e8f0")
                .set("padding", "var(--lumo-space-m)");
        sidebar.setSpacing(true);

        Span title = new Span("CONTEXTO ACTIVO");
        title.getStyle()
                .set("font-weight", "600")
                .set("color", "#475569")
                .set("font-size", "12px")
                .set("letter-spacing", "0.05em");
        sidebar.add(title);

        resourceSection = createResourceSection();
        sidebar.add(resourceSection);
        updateResourceSection(List.of());

        statsSection = createStatsSection();
        sidebar.add(statsSection);
        updateStatsSection();

        Button exportButton = new Button("Guardar Hilo de Conversación", new Icon(VaadinIcon.DOWNLOAD));
        exportButton.setWidthFull();
        exportButton.addClickListener(e -> exportConversation());
        sidebar.add(exportButton);

        return sidebar;
    }

    private VerticalLayout createResourceSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);

        H4 subTitle = new H4("RECURSOS REFERENCIADOS");
        subTitle.getStyle().set("font-size", "14px").set("font-weight", "600").set("margin", "0");
        section.add(subTitle);
        return section;
    }

    private void updateResourceSection(List<RagChatService.Citation> ultimasCitas) {
        // Conserva el título (H4) y reemplaza solo las tarjetas.
        resourceSection.removeAll();
        H4 subTitle = new H4("RECURSOS REFERENCIADOS");
        subTitle.getStyle().set("font-size", "14px").set("font-weight", "600").set("margin", "0");
        resourceSection.add(subTitle);

        if (ultimasCitas == null || ultimasCitas.isEmpty()) {
            Span empty = new Span("Todavía no hay citas en esta sesión.");
            empty.getStyle().set("color", "#94a3b8").set("font-size", "12px");
            resourceSection.add(empty);
            return;
        }
        for (RagChatService.Citation cita : ultimasCitas) {
            resourceSection.add(createResourceCard(cita.titulo(), (int) Math.round(cita.similitud() * 100)));
        }
    }

    private Div createResourceCard(String title, int percentage) {
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

        Span titleSpan = new Span(title);
        titleSpan.getStyle().set("font-weight", "500").set("font-size", "13px");

        Span percentageSpan = new Span(percentage + "%");
        percentageSpan.getStyle()
                .set("color", "#00C48C")
                .set("font-weight", "600")
                .set("font-size", "14px")
                .set("margin-left", "auto");

        content.add(titleSpan, percentageSpan);
        content.expand(titleSpan);
        card.add(content);
        return card;
    }

    private VerticalLayout createStatsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        section.getStyle().set("border-top", "1px solid #e2e8f0").set("padding-top", "var(--lumo-space-m)");
        return section;
    }

    private void updateStatsSection() {
        statsSection.removeAll();
        H4 subTitle = new H4("ESTADÍSTICAS DE SESIÓN");
        subTitle.getStyle().set("font-size", "14px").set("font-weight", "600").set("margin-bottom", "var(--lumo-space-s)");
        statsSection.add(subTitle);

        double relevanciaMedia = citasDeLaSesion.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        statsSection.add(createStatRow("Consultas:", String.valueOf(consultas)));
        statsSection.add(createStatRow("Recursos citados:", String.valueOf(citasDeLaSesion.size())));
        statsSection.add(createStatRow("Relevancia media:", citasDeLaSesion.isEmpty() ? "—" : Math.round(relevanciaMedia * 100) + "%"));
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

    private void exportConversation() {
        if (historial.isEmpty()) {
            Notification.show("Todavía no hay conversación para guardar.", 3000, Notification.Position.BOTTOM_END);
            return;
        }
        StringBuilder sb = new StringBuilder("# Conversación - Consultor IA KnowBase\n\n");
        for (ChatTurn turn : historial) {
            sb.append(turn.role().equals("user") ? "**Tú:** " : "**Consultor IA:** ").append(turn.content()).append("\n\n");
        }
        Notification.show("Transcripción lista (" + sb.length() + " caracteres). Copiarla a un archivo se agrega en una próxima iteración.",
                4000, Notification.Position.BOTTOM_END);
    }
}
