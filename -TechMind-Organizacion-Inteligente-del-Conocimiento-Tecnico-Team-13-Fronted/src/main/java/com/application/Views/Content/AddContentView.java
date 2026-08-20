package com.application.Views.Content;

import com.application.Views.Layout.MainLayout;
import com.application.dto.ContenidoRequestDTO;
import com.application.dto.ContenidoResponseDTO;
import com.application.events.ContentAddedEvent;
import com.application.model.User;
import com.application.service.ContenidoService;
import com.application.service.SupabaseService;
import com.application.service.UserSession;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.context.ApplicationEventPublisher;

import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@PageTitle("Add Content - KnowBase")
@Route(value = "add-content", layout = MainLayout.class)
public class AddContentView extends VerticalLayout {

    private final ContenidoService contenidoService;
    private final SupabaseService supabaseService;
    private final UserSession userSession;
    private final UUID currentUserId;
    private final ApplicationEventPublisher eventPublisher;

    // Componentes de formulario
    private TextArea textArea;
    private TextField titleField;
    private MemoryBuffer memoryBuffer;
    private Upload upload;
    private String uploadedStoragePath = null;
    private String detectedFileType = "texto_plano";
    private HorizontalLayout inputsRow;

    // Panel de vista previa de análisis IA
    private VerticalLayout analysisPanel;
    private Span analysisPlaceholder;
    private Span categoriaPill;
    private FlexLayout palabrasClaveLayout;
    private VerticalLayout relacionadosLayout;


    public AddContentView(ContenidoService contenidoService, SupabaseService supabaseService,
                           UserSession userSession, ApplicationEventPublisher eventPublisher) {
        this.contenidoService = contenidoService;
        this.supabaseService = supabaseService;
        this.userSession = userSession;
        this.eventPublisher = eventPublisher;

        // Get the current user's ID from the session
        this.currentUserId = Optional.ofNullable(userSession.getAuthenticatedUser())
                                     .map(User::getId)
                                     .orElse(null);

        if (currentUserId == null) {
            Notification.show("Error: No se pudo obtener el ID del usuario. Por favor, inicie sesión de nuevo.", 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            setEnabled(false);
        }

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background-color", "#f4f6fa").set("font-family", "'Inter', sans-serif");

        add(createHeader());
        add(createTabsSection());
        add(createMainWorkspace());
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        VerticalLayout titleLayout = new VerticalLayout();
        titleLayout.setPadding(false);
        titleLayout.setSpacing(false);

        H2 title = new H2("Añadir Contenido");
        title.getStyle().set("margin", "0").set("font-size", "22px").set("font-weight", "700").set("color", "#0f172a");

        Span subtitle = new Span("Pega, arrastra o enlaza contenido técnico para procesarlo con IA");
        subtitle.getStyle().set("color", "#64748b").set("font-size", "13px");

        titleLayout.add(title, subtitle);
        header.add(titleLayout);
        return header;
    }

    private HorizontalLayout createTabsSection() {
        HorizontalLayout tabs = new HorizontalLayout();
        tabs.setSpacing(true);
        tabs.getStyle().set("margin-top", "10px").set("margin-bottom", "5px");

        tabs.add(createTab("Texto / Artículo", VaadinIcon.FILE_TEXT, true));
        tabs.add(createTab("URL / Enlace", VaadinIcon.LINK, false));
        tabs.add(createTab("PDF / Documentación", VaadinIcon.FILE_O, false));
        return tabs;
    }

    private Button createTab(String text, VaadinIcon icon, boolean isActive) {
        Button btn = new Button(text, icon.create());
        if (isActive) {
            btn.getStyle().set("background-color", "#00b894").set("color", "white").set("border-radius", "20px");
        } else {
            btn.getStyle().set("background-color", "white").set("color", "#64748b").set("border-radius", "20px");
            btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        }
        return btn;
    }

    private HorizontalLayout createMainWorkspace() {
        HorizontalLayout workspace = new HorizontalLayout();
        workspace.setSizeFull();
        workspace.setSpacing(true);

        VerticalLayout leftPanel = createLeftPanel();
        VerticalLayout rightPanel = createRightPanel();

        workspace.add(leftPanel, rightPanel);
        workspace.setFlexGrow(1, leftPanel);
        workspace.setFlexGrow(1, rightPanel);

        return workspace;
    }

    private VerticalLayout createLeftPanel() {
        VerticalLayout panel = new VerticalLayout();
        panel.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.03)")
                .set("border", "1px solid #f1f5f9");
        panel.setSizeFull();

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        Span title = new Span("Contenido a Procesar");
        title.getStyle().set("font-weight", "600").set("font-size", "14px");
        header.add(title);

        textArea = new TextArea();
        textArea.setSizeFull();
        textArea.setPlaceholder("Escribe o pega el texto técnico aquí...");
        textArea.getStyle()
                .set("font-family", "'Consolas', 'Courier New', monospace")
                .set("font-size", "13px")
                .set("border", "none")
                .set("background", "transparent");

        inputsRow = new HorizontalLayout();
        inputsRow.setWidthFull();

        titleField = new TextField();
        titleField.setPlaceholder("Título del contenido");
        titleField.getStyle().set("flex-grow", "1");

        createUploadComponent();

        inputsRow.add(titleField, upload);

        Button analyzeBtn = new Button("Procesar y Guardar", VaadinIcon.MAGIC.create());
        analyzeBtn.setWidthFull();
        analyzeBtn.getStyle()
                .set("background-color", "#00b894")
                .set("color", "white")
                .set("font-weight", "600")
                .set("border-radius", "8px")
                .set("padding", "20px 0");

        analyzeBtn.addClickListener(e -> processAndSaveContent());

        panel.add(header, textArea, inputsRow, analyzeBtn);
        return panel;
    }

    private void createUploadComponent() {
        memoryBuffer = new MemoryBuffer();
        upload = new Upload(memoryBuffer);
        upload.setAcceptedFileTypes(".pdf", ".txt", ".md", ".docx");
        upload.setMaxFiles(1);
        upload.setDropLabel(new Span("Arrastra un archivo (.pdf, .txt, .md)"));

        upload.addSucceededListener(event -> {
            if (currentUserId == null) {
                Notification.show("Error: No se puede subir el archivo sin un usuario autenticado.", 4000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            try {
                InputStream inputStream = memoryBuffer.getInputStream();
                String originalFileName = event.getFileName();
                String mimeType = event.getMIMEType();

                if (originalFileName.endsWith(".pdf")) detectedFileType = "pdf";
                else if (originalFileName.endsWith(".md")) detectedFileType = "markdown";
                else if (originalFileName.endsWith(".doc") || originalFileName.endsWith(".docx")) detectedFileType = "word";

                // The service will create the full path including user id and timestamp
                uploadedStoragePath = supabaseService.uploadFileToStorage(
                        currentUserId, originalFileName, inputStream, mimeType
                );

                if (titleField.isEmpty()) {
                    titleField.setValue(originalFileName);
                }

                Notification.show("Archivo subido correctamente a Storage", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            } catch (Exception e) {
                Notification.show("Error al subir archivo: " + e.getMessage(), 4000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
    }

    private void processAndSaveContent() {
        if (currentUserId == null) {
            Notification.show("Error: No se puede guardar el contenido sin un usuario autenticado.", 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (titleField.isEmpty()) {
            Notification.show("Debes asignar un título al contenido", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (textArea.getValue() == null || textArea.getValue().isBlank()) {
            Notification.show("Debes escribir o pegar el texto a procesar", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            String title = titleField.getValue();
            ContenidoRequestDTO request = new ContenidoRequestDTO(title, textArea.getValue());

            // Clasificación (FastAPI) + embedding (OpenAI) + detección de casi-duplicados + persistencia.
            ContenidoService.GuardadoResult resultado = contenidoService.procesarYGuardar(
                    currentUserId, request, detectedFileType, uploadedStoragePath);

            showAnalysisPreview(resultado.contenido());

            if (!resultado.posiblesDuplicados().isEmpty()) {
                String detalle = resultado.posiblesDuplicados().stream()
                        .map(d -> String.format("\"%s\" (%.0f%% parecido)", d.titulo(), d.similitud() * 100))
                        .collect(Collectors.joining(", "));
                Notification.show("Aviso: este contenido se parece a lo ya guardado: " + detalle, 7000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
            }

            Notification.show("Contenido registrado exitosamente (ID: " + resultado.contenido().id() + ")", 4000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            eventPublisher.publishEvent(new ContentAddedEvent(this, resultado.contenido().id(), title));

            textArea.clear();
            titleField.clear();

            inputsRow.remove(upload);
            createUploadComponent();
            inputsRow.add(upload);

            uploadedStoragePath = null;
            detectedFileType = "texto_plano";

        } catch (Exception e) {
            Notification.show("Error al guardar: " + e.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private VerticalLayout createRightPanel() {
        VerticalLayout panel = new VerticalLayout();
        panel.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.03)")
                .set("border", "1px solid #f1f5f9");
        panel.setSizeFull();

        Span title = new Span("Vista previa de análisis IA");
        title.getStyle().set("font-weight", "700").set("font-size", "14px");

        analysisPlaceholder = new Span("Procesa un contenido para ver aquí la categoría, palabras clave y contenido relacionado.");
        analysisPlaceholder.getStyle().set("color", "#94a3b8").set("font-size", "13px").set("margin-top", "10px");

        categoriaPill = new Span();
        categoriaPill.getStyle()
                .set("background-color", "#00b8941A")
                .set("color", "#00b894")
                .set("font-size", "12px")
                .set("font-weight", "700")
                .set("padding", "4px 12px")
                .set("border-radius", "12px")
                .set("display", "none");

        palabrasClaveLayout = new FlexLayout();
        palabrasClaveLayout.getStyle().set("gap", "6px").set("flex-wrap", "wrap").set("margin-top", "10px");

        relacionadosLayout = new VerticalLayout();
        relacionadosLayout.setPadding(false);
        relacionadosLayout.setSpacing(false);
        relacionadosLayout.getStyle().set("margin-top", "10px");

        analysisPanel = new VerticalLayout(categoriaPill, palabrasClaveLayout, relacionadosLayout);
        analysisPanel.setPadding(false);
        analysisPanel.setSpacing(false);

        panel.add(title, analysisPlaceholder, analysisPanel);
        return panel;
    }

    private void showAnalysisPreview(ContenidoResponseDTO contenido) {
        analysisPlaceholder.setVisible(false);

        if (contenido.categoria() != null) {
            categoriaPill.setText("• " + contenido.categoria());
            categoriaPill.getStyle().set("display", "inline-block");
        } else {
            categoriaPill.setText("Sin clasificar (clasificador no disponible)");
            categoriaPill.getStyle().set("display", "inline-block");
        }

        palabrasClaveLayout.removeAll();
        contenido.palabrasClave().forEach(palabra -> {
            Span chip = new Span(palabra);
            chip.getStyle()
                    .set("background-color", "#f1f5f9")
                    .set("color", "#475569")
                    .set("font-size", "11px")
                    .set("padding", "3px 10px")
                    .set("border-radius", "6px");
            palabrasClaveLayout.add(chip);
        });

        relacionadosLayout.removeAll();
        if (!contenido.contenidosRelacionados().isEmpty()) {
            Span relacionadosTitle = new Span("Contenido relacionado:");
            relacionadosTitle.getStyle().set("font-weight", "600").set("font-size", "12px").set("color", "#334155");
            relacionadosLayout.add(relacionadosTitle);
            contenido.contenidosRelacionados().forEach(titulo -> {
                Span item = new Span("• " + titulo);
                item.getStyle().set("font-size", "12px").set("color", "#64748b");
                relacionadosLayout.add(item);
            });
        }
    }
}
