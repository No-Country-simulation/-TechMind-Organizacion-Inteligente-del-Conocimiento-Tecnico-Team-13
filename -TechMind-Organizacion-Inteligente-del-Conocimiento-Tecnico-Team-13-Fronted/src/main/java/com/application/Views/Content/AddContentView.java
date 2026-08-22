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
@Route(value = "add-content", layout = MainLayout.class)
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

    // Panel de vista previa de análisis IA (poblado con el resultado real tras guardar)
    private VerticalLayout analysisPanel;
    private Span analysisPlaceholder;
    private Span categoriaPill;
    private FlexLayout palabrasClaveLayout;
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
        inputContainer.setPadding(false);
        inputContainer.setSpacing(true);
        inputContainer.setWidthFull();

        textArea = new TextArea("Texto o artículo");
        textArea.setPlaceholder("Escribe o pega el texto técnico aquí...");
        textArea.setHeight("500px");
        textArea.getStyle()
                .set("font-family", "'Consolas', 'Courier New', monospace")
                .set("font-size", "13px")
                .set("border-radius", "12px")
                .set("background-color", "#f8fafc")
                .set("padding", "12px")
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
        panel.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "16px")
                .set("box-shadow", "0 2px 10px rgba(15, 23, 42, 0.04)")
                .set("border", "1px solid #e2e8f0")
                .set("padding", "20px")
                .set("flex", "1 1 360px")
                .set("min-width", "260px");
        panel.setWidthFull();

        Span title = new Span("Vista previa de análisis IA");
        title.getStyle().set("font-weight", "700").set("font-size", "15px").set("color", "#0f172a");

        analysisPlaceholder = new Span("Procesa un contenido para ver aquí la categoría, palabras clave y contenido relacionado (embeddings + clasificador).");
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

        categoriaPill.setText("• " + (contenido.categoria() != null ? contenido.categoria() : "Sin clasificar"));
        categoriaPill.getStyle().set("display", "inline-block");

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
            Span relacionadosTitle = new Span("Contenido relacionado (similitud de embeddings):");
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
