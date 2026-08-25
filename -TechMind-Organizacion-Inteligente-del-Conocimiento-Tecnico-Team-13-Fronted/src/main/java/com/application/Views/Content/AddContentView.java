package com.application.Views.Content;

import com.application.Views.Layout.MainLayout;
import com.application.dto.ContenidoRequestDTO;
import com.application.dto.ContenidoResponseDTO;
import com.application.events.ContentAddedEvent;
import com.application.model.User;
import com.application.service.ContenidoService;
import com.application.service.DocumentExtractor;
import com.application.service.SupabaseService;
import com.application.service.UserSession;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@PageTitle("Add Content - KnowBase")
@Route(value = "add-content", layout = MainLayout.class)\
public class AddContentView extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(AddContentView.class);

    private static final List<String> OFFICIAL_CATEGORIES = List.of(
            "Backend",
            "Frontend",
            "Cloud Computing",
            "Databases",
            "Data Analysis",
            "Cybersecurity",
            "Artificial Intelligence",
            "Software Architecture",
            "Q/A"
    );

    private enum InputMode { TEXT, URL, FILE }

    private final ContenidoService contenidoService;
    private final SupabaseService supabaseService;
    private final UUID currentUserId;
    private final ApplicationEventPublisher eventPublisher;

    private InputMode activeInputMode = InputMode.TEXT;
    private Button textTabButton;
    private Button urlTabButton;
    private Button fileTabButton;

    private TextArea textArea;
    private TextField urlField;
    private Upload upload;
    private MemoryBuffer memoryBuffer;
    private VerticalLayout inputContainer;

    private String uploadedStoragePath = null;
    private String uploadedOriginalFileName = null;

    private static final java.util.Map<String, String> CATEGORY_COLORS = java.util.Map.ofEntries(
            java.util.Map.entry("Backend", "#8B5CF6"),
            java.util.Map.entry("Frontend", "#10B981"),
            java.util.Map.entry("Cloud Computing", "#F59E0B"),
            java.util.Map.entry("Databases", "#0891B2"),
            java.util.Map.entry("Data Analysis", "#EC4899"),
            java.util.Map.entry("Cybersecurity", "#EF4444"),
            java.util.Map.entry("Artificial Intelligence", "#8B5CF6"),
            java.util.Map.entry("Software Architecture", "#6366F1"),
            java.util.Map.entry("Q/A", "#64748B")
    );

    // Panel de vista previa de análisis IA (poblado con el resultado real tras guardar)
    private VerticalLayout analysisPanel;
    private Span analysisPlaceholder;
    private Span statusBadge;
    private Span categoriaDot;
    private Span categoriaNombre;
    private Span confianzaBadge;
    private VerticalLayout categoriaSection;
    private VerticalLayout palabrasSection;
    private FlexLayout palabrasClaveLayout;
    private VerticalLayout relacionadosSection;
    private VerticalLayout relacionadosLayout;

    public AddContentView(ContenidoService contenidoService, SupabaseService supabaseService,
                           UserSession userSession, ApplicationEventPublisher eventPublisher) {
        this.contenidoService = contenidoService;
        this.supabaseService = supabaseService;
        this.eventPublisher = eventPublisher;

        User authenticatedUser = userSession != null ? userSession.getAuthenticatedUser() : null;
        this.currentUserId = authenticatedUser != null ? authenticatedUser.getId() : null;

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
        HorizontalLayout workspace = createMainWorkspace();
        add(workspace);
        expand(workspace);
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

        textTabButton = createTab("Texto", VaadinIcon.FILE_TEXT, true);
        urlTabButton = createTab("URL", VaadinIcon.LINK, false);
        fileTabButton = createTab("PDF / Documentos", VaadinIcon.FILE_O, false);

        textTabButton.addClickListener(event -> setActiveInputMode(InputMode.TEXT));
        urlTabButton.addClickListener(event -> setActiveInputMode(InputMode.URL));
        fileTabButton.addClickListener(event -> setActiveInputMode(InputMode.FILE));

        tabs.add(textTabButton, urlTabButton, fileTabButton);
        return tabs;
    }

    private Button createTab(String text, VaadinIcon icon, boolean isActive) {
        Button btn = new Button(text, icon.create());
        btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btn.getStyle()
                .set("border-radius", "20px")
                .set("font-weight", "600")
                .set("padding", "0.45rem 1rem");

        if (isActive) {
            btn.getStyle().set("background-color", "#00b894").set("color", "white");
        } else {
            btn.getStyle().set("background-color", "white").set("color", "#64748b");
        }
        return btn;
    }

    private void setActiveInputMode(InputMode mode) {
        this.activeInputMode = mode;
        updateTabStyles();

        if (textArea != null) {
            textArea.setVisible(mode == InputMode.TEXT);
        }
        if (urlField != null) {
            urlField.setVisible(mode == InputMode.URL);
        }
        if (upload != null) {
            upload.setVisible(mode == InputMode.FILE);
        }
    }

    private void updateTabStyles() {
        updateTabStyle(textTabButton, activeInputMode == InputMode.TEXT);
        updateTabStyle(urlTabButton, activeInputMode == InputMode.URL);
        updateTabStyle(fileTabButton, activeInputMode == InputMode.FILE);
    }

    private void updateTabStyle(Button button, boolean active) {
        if (button == null) return;

        if (active) {
            button.getStyle()
                    .set("background-color", "#00b894")
                    .set("color", "white");
        } else {
            button.getStyle()
                    .set("background-color", "white")
                    .set("color", "#64748b");
        }
    }

    private HorizontalLayout createMainWorkspace() {
        HorizontalLayout workspace = new HorizontalLayout();
        workspace.setSizeFull();
        workspace.setSpacing(true);
        workspace.getStyle().set("flex-wrap", "wrap");

        VerticalLayout leftPanel = createLeftPanel();
        VerticalLayout rightPanel = createRightPanel();

        workspace.add(leftPanel, rightPanel);
        workspace.setFlexGrow(1, leftPanel);
        workspace.setFlexGrow(1, rightPanel);
        workspace.setWidthFull();

        return workspace;
    }

    private VerticalLayout createLeftPanel() {
        VerticalLayout panel = new VerticalLayout();
        panel.setSizeFull();
        panel.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "16px")
                .set("box-shadow", "0 2px 10px rgba(15, 23, 42, 0.04)")
                .set("border", "1px solid #e2e8f0")
                .set("padding", "20px")
                .set("min-width", "min(100%, 540px)")
                .set("flex", "1 1 540px");
        panel.setWidthFull();

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);
        Span title = new Span("Contenido a Procesar");
        title.getStyle().set("font-weight", "700").set("font-size", "15px").set("color", "#0f172a");
        header.add(title);

        inputContainer = new VerticalLayout();
        inputContainer.setSizeFull();
        inputContainer.setPadding(false);
        inputContainer.setSpacing(true);
        inputContainer.setWidthFull();

        textArea = new TextArea("Texto o artículo");
        textArea.setPlaceholder("Escribe o pega el texto técnico aquí...");
        textArea.setSizeFull();
        textArea.setWidthFull();
        textArea.setMinHeight("380px");
        textArea.getStyle()
                .set("font-family", "'Consolas', 'Courier New', monospace")
                .set("font-size", "13px")
                .set("border-radius", "12px")
                .set("background-color", "#f8fafc")
                .set("box-shadow", "inset 0 0 0 1px #e2e8f0");

        urlField = new TextField("URL del recurso");
        urlField.setPlaceholder("https://example.com/articulo");
        urlField.setWidthFull();
        urlField.setVisible(false);
        urlField.setHelperText("Se guarda como referencia; el texto a analizar es el que escribas debajo del título al confirmar.");
        urlField.getStyle().set("background-color", "#f8fafc").set("border-radius", "12px");

        createUploadComponent();
        upload.setVisible(false);
        upload.getStyle().set("border-radius", "12px");

        inputContainer.add(textArea, urlField, upload);
        inputContainer.setFlexGrow(1, textArea);
        inputContainer.expand(textArea);
        setActiveInputMode(activeInputMode);

        Button analyzeBtn = new Button("Procesar", VaadinIcon.MAGIC.create());
        analyzeBtn.setWidthFull();
        analyzeBtn.getStyle()
                .set("background-color", "#00b894")
                .set("color", "white")
                .set("font-weight", "700")
                .set("border-radius", "12px")
                .set("padding", "18px 0")
                .set("box-shadow", "0 8px 18px rgba(0,184,148,0.22)");

        analyzeBtn.addClickListener(e -> processAndSaveContent());
        // Atajo: Enter en la URL dispara "Procesar". No se escucha en textArea a propósito: ahí
        // Enter debe seguir insertando saltos de línea (texto técnico multilínea).
        bindEnterToClick(urlField, analyzeBtn);

        panel.add(header, inputContainer, analyzeBtn);
        panel.setFlexGrow(1, inputContainer);
        panel.expand(inputContainer);
        return panel;
    }

    private void createUploadComponent() {
        memoryBuffer = new MemoryBuffer();
        upload = new Upload(memoryBuffer);
        upload.setAcceptedFileTypes(".pdf", ".txt", ".md", ".docx");
        upload.setMaxFiles(1);
        upload.setDropLabel(new Span("Arrastra un archivo (.pdf, .txt, .md, .docx)"));
        upload.setWidthFull();

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

                uploadedOriginalFileName = originalFileName;
                uploadedStoragePath = supabaseService.uploadFileToStorage(currentUserId, originalFileName, inputStream, mimeType);

                Notification.show("Archivo subido correctamente a Storage", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception e) {
                log.error("Error al subir archivo a Supabase Storage", e);
                Notification.show("Error al subir archivo: " + e.getMessage(), 8000, Notification.Position.MIDDLE)
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

        if (!validateCurrentInput()) {
            return;
        }

        openConfirmationDialog();
    }

    private boolean validateCurrentInput() {
        return switch (activeInputMode) {
            case TEXT -> validateTextContent();
            case URL -> validateUrlContent();
            case FILE -> validateFileContent();
        };
    }

    private boolean isValidUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }

    private boolean validateTextContent() {
        String value = textArea.getValue();
        if (value == null || value.isBlank()) {
            Notification.show("Debes escribir o pegar el contenido antes de guardar.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }
        if (value.trim().length() < 20) {
            Notification.show("El texto es muy corto. Añade más detalle para que el contenido sea útil y procesable.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            return false;
        }
        return true;
    }

    private boolean validateUrlContent() {
        String url = urlField.getValue();
        if (url == null || url.isBlank()) {
            Notification.show("Debes ingresar una URL válida antes de guardar.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }
        if (!isValidUrl(url)) {
            Notification.show("La URL debe empezar con http:// o https:// para poder procesarse correctamente.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }
        return true;
    }

    private boolean validateFileContent() {
        if (uploadedStoragePath == null || uploadedStoragePath.isBlank()) {
            Notification.show("Debes cargar un archivo PDF o documento antes de guardar.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }
        return true;
    }

    private void openConfirmationDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("560px");
        dialog.setDraggable(true);
        dialog.setModal(true);
        dialog.getElement().getStyle().set("border-radius", "20px");

        Div header = new Div();
        header.getStyle().set("padding", "1.25rem 1.5rem 0.5rem").set("background", "linear-gradient(135deg, #f0fdfa 0%, #ecfeff 100%)");
        H3 title = new H3("¡Perfecto! Vamos a organizarlo");
        title.getStyle().set("margin", "0").set("color", "#0f172a");
        header.add(title);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);

        Paragraph description = new Paragraph("Un título claro y bien formateado hace que el contenido sea mucho más fácil de encontrar, mantiene la biblioteca ordenada y mejora la lectura y la indexación de IA. La categoría es opcional: si no eliges una, se usa la que sugiera el clasificador automático.");
        description.getStyle().set("margin", "0").set("color", "#475569").set("line-height", "1.6");

        TextField titleField = new TextField("Título del contenido");
        titleField.setPlaceholder("Ejemplo: Buenas prácticas en Spring Boot");
        titleField.setWidthFull();
        titleField.getStyle().set("background-color", "#f8fafc").set("border-radius", "10px");

        if (activeInputMode == InputMode.FILE && uploadedOriginalFileName != null && !uploadedOriginalFileName.isBlank()) {
            titleField.setValue(uploadedOriginalFileName.replaceFirst("\\.[^.]+$", ""));
        }

        ComboBox<String> categorySelect = new ComboBox<>("Categoría (opcional)");
        categorySelect.setItems(OFFICIAL_CATEGORIES);
        categorySelect.setPlaceholder("Selecciona una categoría, o déjalo al clasificador");
        categorySelect.setClearButtonVisible(true);
        categorySelect.setWidthFull();
        categorySelect.getStyle().set("background-color", "#f8fafc").set("border-radius", "10px");

        content.add(description, titleField, categorySelect);

        Button cancelButton = new Button("Cancelar", event -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.getStyle().set("border-radius", "10px");

        Runnable performSave = () -> {
            String finalTitle = titleField.getValue();
            String finalCategory = categorySelect.getValue();

            if (finalTitle == null || finalTitle.isBlank()) {
                Notification.show("Debes escribir un título claro para guardar el contenido.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            if (finalTitle.trim().length() < 5) {
                Notification.show("El título es demasiado corto. Intenta algo más descriptivo.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }

            dialog.close();
            saveContent(finalTitle.trim(), finalCategory);
        };

        Button confirmButton = new Button("Guardar contenido", event -> performSave.run());
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirmButton.getStyle().set("border-radius", "10px").set("padding", "0.7rem 1.2rem");

        // Atajo: Enter en el título dispara "Guardar contenido", como en cualquier form nativo.
        bindEnterToClick(titleField, confirmButton);

        HorizontalLayout footer = new HorizontalLayout(cancelButton, confirmButton);
        footer.setJustifyContentMode(JustifyContentMode.END);
        footer.setWidthFull();
        footer.getStyle().set("padding-top", "0.5rem");

        dialog.add(header, content, footer);
        dialog.open();
    }

    private void saveContent(String title, String category) {
        try {
            String tipoContenido = switch (activeInputMode) {
                case TEXT -> "texto_plano";
                case URL -> "url";
                case FILE -> detectFileType(uploadedOriginalFileName);
            };

            String contentText = resolveContentText();
            String storagePath = (activeInputMode == InputMode.FILE) ? uploadedStoragePath : null;

            ContenidoRequestDTO request = new ContenidoRequestDTO(title, contentText);
            ContenidoService.GuardadoResult resultado = contenidoService.procesarYGuardar(
                    currentUserId, request, tipoContenido, storagePath, category);

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
            resetForm();
        } catch (Exception e) {
            log.error("Error al procesar/guardar contenido (título=\"{}\", modo={})", title, activeInputMode, e);
            Notification.show("Error al guardar: " + e.getMessage(), 8000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /** Resuelve el texto a analizar/guardar según el modo activo, extrayendo el contenido real de
     *  archivos PDF/DOCX/MD con Apache Tika en vez de guardar solo la ruta del archivo. */
    private String resolveContentText() {
        return switch (activeInputMode) {
            case TEXT -> textArea.getValue();
            case URL -> urlField.getValue();
            case FILE -> {
                byte[] bytes = supabaseService.downloadFileFromStorage(uploadedStoragePath);
                DocumentExtractor.DocumentData data = DocumentExtractor.extractFromBytes(
                        bytes, uploadedOriginalFileName != null ? uploadedOriginalFileName : uploadedStoragePath);
                yield data.content();
            }
        };
    }

    private String detectFileType(String fileName) {
        if (fileName == null) return "pdf";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf")) return "pdf";
        if (lower.endsWith(".md") || lower.endsWith(".markdown")) return "markdown";
        if (lower.endsWith(".doc") || lower.endsWith(".docx")) return "word";
        return "texto_plano";
    }

    private void resetForm() {
        textArea.clear();
        urlField.clear();
        uploadedStoragePath = null;
        uploadedOriginalFileName = null;
        if (upload != null) {
            upload.clearFileList();
        }
        setActiveInputMode(InputMode.TEXT);
    }

    private VerticalLayout createRightPanel() {
        VerticalLayout panel = new VerticalLayout();
        panel.setSizeFull();
        panel.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "16px")
                .set("box-shadow", "0 2px 10px rgba(15, 23, 42, 0.04)")
                .set("border", "1px solid #e2e8f0")
                .set("padding", "20px")
                .set("flex", "1 1 360px")
                .set("min-width", "260px");
        panel.setWidthFull();
        panel.setSpacing(false);

        HorizontalLayout headerRow = new HorizontalLayout();
        headerRow.setWidthFull();
        headerRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerRow.setAlignItems(Alignment.CENTER);

        HorizontalLayout titleGroup = new HorizontalLayout();
        titleGroup.setAlignItems(Alignment.CENTER);
        titleGroup.setSpacing(true);
        Icon magicIcon = VaadinIcon.MAGIC.create();
        magicIcon.setSize("16px");
        magicIcon.setColor("#00b894");
        Span title = new Span("Resultado del Análisis");
        title.getStyle().set("font-weight", "700").set("font-size", "15px").set("color", "#0f172a");
        titleGroup.add(magicIcon, title);

        statusBadge = new Span("PENDIENTE");
        statusBadge.getStyle()
                .set("background-color", "#f1f5f9")
                .set("color", "#64748b")
                .set("font-size", "10px")
                .set("font-weight", "700")
                .set("letter-spacing", "0.04em")
                .set("padding", "3px 10px")
                .set("border-radius", "10px");

        headerRow.add(titleGroup, statusBadge);

        analysisPlaceholder = new Span("Procesa un contenido para ver aquí la categoría, palabras clave y contenido relacionado (embeddings + clasificador).");
        analysisPlaceholder.getStyle()
                .set("color", "#94a3b8")
                .set("font-size", "13px")
                .set("margin-top", "10px")
                .set("display", "block");

        // Categoría asignada + % de confianza del clasificador
        Span categoriaLabel = new Span("CATEGORÍA ASIGNADA");
        categoriaLabel.getStyle()
                .set("font-size", "10px").set("font-weight", "700").set("color", "#94a3b8")
                .set("letter-spacing", "0.05em").set("display", "block");

        HorizontalLayout categoriaRow = new HorizontalLayout();
        categoriaRow.setAlignItems(Alignment.CENTER);
        categoriaRow.setSpacing(true);
        categoriaRow.getStyle().set("margin-top", "6px");

        categoriaDot = new Span();
        categoriaDot.getStyle().set("width", "10px").set("height", "10px").set("border-radius", "50%").set("flex-shrink", "0");

        categoriaNombre = new Span();
        categoriaNombre.getStyle().set("font-size", "16px").set("font-weight", "700").set("color", "#0f172a");

        confianzaBadge = new Span();
        confianzaBadge.getStyle()
                .set("background-color", "#ecfdf5")
                .set("color", "#059669")
                .set("font-size", "11px")
                .set("font-weight", "700")
                .set("padding", "2px 8px")
                .set("border-radius", "10px");

        categoriaRow.add(categoriaDot, categoriaNombre, confianzaBadge);

        categoriaSection = new VerticalLayout(categoriaLabel, categoriaRow);
        categoriaSection.setPadding(false);
        categoriaSection.setSpacing(false);
        categoriaSection.getStyle().set("margin-top", "16px");
        categoriaSection.setVisible(false);

        // Palabras clave del clasificador
        Span palabrasLabel = new Span("PALABRAS CLAVE");
        palabrasLabel.getStyle()
                .set("font-size", "10px").set("font-weight", "700").set("color", "#94a3b8")
                .set("letter-spacing", "0.05em").set("display", "block");

        palabrasClaveLayout = new FlexLayout();
        palabrasClaveLayout.getStyle().set("gap", "6px").set("flex-wrap", "wrap").set("margin-top", "6px");

        palabrasSection = new VerticalLayout(palabrasLabel, palabrasClaveLayout);
        palabrasSection.setPadding(false);
        palabrasSection.setSpacing(false);
        palabrasSection.getStyle().set("margin-top", "16px");
        palabrasSection.setVisible(false);

        // Contenido relacionado por similitud de embeddings (pgvector)
        Span relacionadosLabel = new Span("CONTENIDO RELACIONADO (similitud de embeddings)");
        relacionadosLabel.getStyle()
                .set("font-size", "10px").set("font-weight", "700").set("color", "#94a3b8")
                .set("letter-spacing", "0.05em").set("display", "block");

        relacionadosLayout = new VerticalLayout();
        relacionadosLayout.setPadding(false);
        relacionadosLayout.setSpacing(false);
        relacionadosLayout.getStyle().set("margin-top", "8px");

        relacionadosSection = new VerticalLayout(relacionadosLabel, relacionadosLayout);
        relacionadosSection.setPadding(false);
        relacionadosSection.setSpacing(false);
        relacionadosSection.getStyle().set("margin-top", "16px");
        relacionadosSection.setVisible(false);

        analysisPanel = new VerticalLayout(categoriaSection, palabrasSection, relacionadosSection);
        analysisPanel.setPadding(false);
        analysisPanel.setSpacing(false);

        panel.add(headerRow, analysisPlaceholder, analysisPanel);
        return panel;
    }

    private String colorForCategory(String category) {
        if (category == null) {
            return "#94a3b8";
        }
        return CATEGORY_COLORS.getOrDefault(category, "#64748b");
    }

    /** Enter en `field` hace clic en `button`. Fase de CAPTURA (`true` en addEventListener): el
     *  Shadow DOM interno de vaadin-text-field puede detener la propagación de Enter antes de que
     *  un listener normal (fase de burbuja) la reciba; en captura, nuestro listener en el elemento
     *  host se dispara antes de bajar al Shadow DOM, así que nada dentro puede bloquearlo. */
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

    private void showAnalysisPreview(ContenidoResponseDTO contenido) {
        analysisPlaceholder.setVisible(false);

        boolean clasificado = contenido.categoria() != null && !contenido.categoria().isBlank();
        statusBadge.setText(clasificado ? "COMPLETADO" : "SIN CLASIFICAR");
        statusBadge.getStyle()
                .set("background-color", clasificado ? "#d1fae5" : "#fef3c7")
                .set("color", clasificado ? "#059669" : "#b45309");

        String categoria = clasificado ? contenido.categoria() : "Sin clasificar";
        String color = colorForCategory(clasificado ? categoria : null);
        categoriaDot.getStyle().set("background-color", color);
        categoriaNombre.setText(categoria);
        categoriaNombre.getStyle().set("color", clasificado ? "#0f172a" : "#94a3b8");

        if (contenido.probabilidad() != null) {
            confianzaBadge.setText(Math.round(contenido.probabilidad() * 100) + "% confianza");
            confianzaBadge.setVisible(true);
        } else {
            confianzaBadge.setVisible(false);
        }
        categoriaSection.setVisible(true);

        palabrasClaveLayout.removeAll();
        List<String> palabras = contenido.palabrasClave();
        boolean hayPalabras = palabras != null && !palabras.isEmpty();
        palabrasSection.setVisible(hayPalabras);
        if (hayPalabras) {
            palabras.forEach(palabra -> {
                Span chip = new Span(palabra);
                chip.getStyle()
                        .set("background-color", color + "1A")
                        .set("color", color)
                        .set("font-size", "11px")
                        .set("font-weight", "600")
                        .set("padding", "3px 10px")
                        .set("border-radius", "6px");
                palabrasClaveLayout.add(chip);
            });
        }

        relacionadosLayout.removeAll();
        var relacionados = contenido.contenidosRelacionados();
        boolean hayRelacionados = relacionados != null && !relacionados.isEmpty();
        relacionadosSection.setVisible(hayRelacionados);
        if (hayRelacionados) {
            relacionados.forEach(rel -> {
                HorizontalLayout row = new HorizontalLayout();
                row.setWidthFull();
                row.setJustifyContentMode(JustifyContentMode.BETWEEN);
                row.setAlignItems(Alignment.CENTER);
                row.getStyle().set("padding", "6px 0").set("gap", "8px");

                Span itemTitulo = new Span(rel.titulo() != null ? rel.titulo() : "Sin título");
                itemTitulo.getStyle()
                        .set("font-size", "12px")
                        .set("color", "#334155")
                        .set("overflow", "hidden")
                        .set("text-overflow", "ellipsis")
                        .set("white-space", "nowrap");

                Span simBadge = new Span(Math.round(rel.similitud() * 100) + "%");
                simBadge.getStyle()
                        .set("background-color", "#f1f5f9")
                        .set("color", "#475569")
                        .set("font-size", "11px")
                        .set("font-weight", "700")
                        .set("padding", "2px 8px")
                        .set("border-radius", "10px")
                        .set("flex-shrink", "0");

                row.add(itemTitulo, simBadge);
                relacionadosLayout.add(row);
            });
        }
    }
}
