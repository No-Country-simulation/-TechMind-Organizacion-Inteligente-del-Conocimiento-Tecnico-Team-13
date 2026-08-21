package com.application.Views.Content;

import com.application.events.ContentAddedEvent;
import com.application.model.User;
import com.application.service.SupabaseService;
import com.application.service.UserSession;
import com.application.Views.Layout.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.context.ApplicationEventPublisher;

import com.application.service.MlServiceClient;
import com.application.service.MlServiceClient.MlPredictionResult;
import com.application.service.DocumentExtractor;
import java.util.concurrent.CompletableFuture;
import java.util.Arrays;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@PageTitle("Add Content - KnowBase")
@Route(value = "add-content", layout = MainLayout.class)
public class AddContentView extends VerticalLayout {

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

    // Analysis UI components populated by ML response
    private Div analysisArea;
    private Paragraph analysisText;

    public AddContentView(SupabaseService supabaseService, UserSession userSession, ApplicationEventPublisher eventPublisher) {
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
        textArea.setHeight("220px");
        textArea.getStyle()
                .set("font-family", "'Consolas', 'Courier New', monospace")
                .set("font-size", "13px")
                .set("border-radius", "12px")
                .set("background-color", "#f8fafc")
                .set("padding", "12px")
                .set("box-shadow", "inset 0 0 0 1px #e2e8f0")
                .set("width", "stretch")
                .set("height", "500px")
                ;

        urlField = new TextField("URL del recurso");
        urlField.setPlaceholder("https://example.com/articulo");
        urlField.setWidthFull();
        urlField.setVisible(false);
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
                System.out.println("[AddContentView] Subiendo archivo: userId=" + currentUserId + ", fileName=" + originalFileName + ", mimeType=" + mimeType);
                uploadedStoragePath = supabaseService.uploadFileToStorage(currentUserId, originalFileName, inputStream, mimeType);
                System.out.println("[AddContentView] storagePath devuelto=" + uploadedStoragePath);

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

        Paragraph description = new Paragraph("Un título claro y bien formateado hace que el contenido sea mucho más fácil de encontrar, mantiene la biblioteca ordenada y mejora la lectura y la indexación de IA.");
        description.getStyle().set("margin", "0").set("color", "#475569").set("line-height", "1.6");

        TextField titleField = new TextField("Título del contenido");
        titleField.setPlaceholder("Ejemplo: Buenas prácticas en Spring Boot");
        titleField.setWidthFull();
        titleField.getStyle().set("background-color", "#f8fafc").set("border-radius", "10px");

        if (activeInputMode == InputMode.FILE && uploadedOriginalFileName != null && !uploadedOriginalFileName.isBlank()) {
            titleField.setValue(uploadedOriginalFileName.replaceFirst("\\.[^.]+$", ""));
        }

        ComboBox<String> categorySelect = new ComboBox<>("Categoría");
        categorySelect.setItems(OFFICIAL_CATEGORIES);
        categorySelect.setPlaceholder("Selecciona una categoría");
        categorySelect.setWidthFull();
        categorySelect.setRequiredIndicatorVisible(true);
        categorySelect.getStyle().set("background-color", "#f8fafc").set("border-radius", "10px");

        content.add(description, titleField, categorySelect);

        // Asynchronously request ML prediction to suggest a category (for TEXT or FILE)
        try {
            MlServiceClient mlClient = new MlServiceClient();
            CompletableFuture.runAsync(() -> {
                try {
                    String textoToAnalyze = null;
                    if (activeInputMode == InputMode.TEXT) {
                        textoToAnalyze = textArea.getValue();
                    } else if (activeInputMode == InputMode.FILE && uploadedStoragePath != null) {
                        try {
                            byte[] bytes = supabaseService.downloadFileFromStorage(uploadedStoragePath);
                            var data = DocumentExtractor.extractFromBytes(bytes, uploadedOriginalFileName != null ? uploadedOriginalFileName : uploadedStoragePath);
                            textoToAnalyze = data.content();
                        } catch (Exception ex) {
                            // ignore; leave textoToAnalyze null
                        }
                    }

                    MlPredictionResult result = null;
                    try {
                        // Ensure we send non-empty 'titulo' and 'texto' to the ML API
                        String titleToSend = deriveTitleForMl(titleField.getValue(), textoToAnalyze, uploadedOriginalFileName);
                        String textToSend = textoToAnalyze != null ? textoToAnalyze : "";

                        // If both are empty, skip calling ML
                        if ((titleToSend == null || titleToSend.isBlank()) && (textToSend == null || textToSend.isBlank())) {
                            System.out.println("[AddContentView] Skipping ML call — no title/text available to analyze.");
                        } else {
                            // use map-based response to render rich UI
                            Map<String, Object> mapResult = null;
                            try {
                                mapResult = mlClient.getPredictionMapAsync(titleToSend, textToSend).join();
                            } catch (Exception ex) {
                                // ignore
                            }

                            System.out.println("[AddContentView] ML map result: " + mapResult);

                            if (mapResult != null) {
                                final Map<String, Object> finalMap = mapResult;
                                getUI().ifPresent(ui -> ui.access(() -> renderAnalysis(finalMap)));
                            }
                        }
                    } catch (Exception ex) {
                        // ignore prediction errors
                    }

                    // No-op here because map-based rendering handled UI update above
                    // Keep logging for backward compatibility
                    System.out.println("[AddContentView] ML finished map rendering (if available)");

                    // If we still have old-style result object, update simple analysisText
                    if (result != null && result.getCategoria() != null && !result.getCategoria().isBlank()) {
                        final MlPredictionResult r = result;
                        getUI().ifPresent(ui -> ui.access(() -> {
                            try {
                                if (OFFICIAL_CATEGORIES.contains(r.getCategoria())) {
                                    categorySelect.setValue(r.getCategoria());
                                }

                                StringBuilder sb = new StringBuilder();
                                sb.append("Categoría: ").append(r.getCategoria()).append("\n");
                                sb.append("Probabilidad: ").append(r.getProbabilidad()).append("\n");
                                if (r.getPalabras_clave() != null && r.getPalabras_clave().length > 0) {
                                    sb.append("Palabras clave: ").append(String.join(", ", r.getPalabras_clave())).append("\n");
                                }
                                if (r.getRecomendaciones() != null && r.getRecomendaciones().length > 0) {
                                    sb.append("Recomendaciones: ").append(String.join(", ", r.getRecomendaciones())).append("\n");
                                }
                                analysisText.setText(sb.toString());
                            } catch (Exception inner) {
                                // swallow
                            }
                        }));
                    } else {
                        getUI().ifPresent(ui -> ui.access(() -> analysisText.setText("No se obtuvo sugerencia IA.")));
                    }
                } catch (Exception e) {
                    // overall ignore — ML suggestion is optional
                }
            });
        } catch (Exception e) {
            // ignore ML client construction failures
        }

        Button cancelButton = new Button("Cancelar", event -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.getStyle().set("border-radius", "10px");

        Button confirmButton = new Button("Guardar contenido", event -> {
            String finalTitle = titleField.getValue();
            String finalCategory = categorySelect.getValue();

            if (finalTitle == null || finalTitle.isBlank()) {
                Notification.show("Debes escribir un título claro para guardar el contenido.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            if (finalCategory == null || finalCategory.isBlank()) {
                Notification.show("Debes seleccionar una categoría oficial antes de guardar.", 3000, Notification.Position.MIDDLE)
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
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirmButton.getStyle().set("border-radius", "10px").set("padding", "0.7rem 1.2rem");

        HorizontalLayout footer = new HorizontalLayout(cancelButton, confirmButton);
        footer.setJustifyContentMode(JustifyContentMode.END);
        footer.setWidthFull();
        footer.getStyle().set("padding-top", "0.5rem");

        dialog.add(header, content, footer);
        dialog.open();
    }

    // Derive a non-empty title for the ML API: prefer user-provided, fallback to filename or first sentence/line of text
    private String deriveTitleForMl(String titleFieldValue, String texto, String originalFileName) {
        if (titleFieldValue != null && !titleFieldValue.isBlank()) {
            return titleFieldValue.trim();
        }

        if (originalFileName != null && !originalFileName.isBlank()) {
            String nameOnly = originalFileName.replaceFirst("\\.[^.]+$", "");
            if (!nameOnly.isBlank()) return nameOnly;
        }

        if (texto != null && !texto.isBlank()) {
            // Try first Markdown/H1 header
            var m = java.util.regex.Pattern.compile("(?m)^(#\\s+)(.*)$").matcher(texto);
            if (m.find()) {
                String h = m.group(2).trim();
                if (!h.isBlank()) return cutToLength(h, 120);
            }

            // Else try first line with some words
            String[] lines = texto.split("\\r?\\n");
            for (String line : lines) {
                String clean = line.trim();
                if (clean.length() >= 8) {
                    return cutToLength(clean, 120);
                }
            }

            // Else try first sentence
            var sentMatch = java.util.regex.Pattern.compile("(?s)^(.*?[.!?])").matcher(texto.trim());
            if (sentMatch.find()) {
                return cutToLength(sentMatch.group(1).trim(), 120);
            }

            // Fallback: first N chars
            return cutToLength(texto.trim(), 120);
        }

        return "";
    }

    private String cutToLength(String s, int max) {
        if (s == null) return "";
        String normalized = s.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= max) return normalized;
        int idx = normalized.lastIndexOf(' ', max);
        if (idx <= 0) idx = max;
        return normalized.substring(0, idx).trim();
    }

    private void saveContent(String title, String category) {
        try {
            String contentText = switch (activeInputMode) {
                case TEXT -> textArea.getValue();
                case URL -> urlField.getValue();
                case FILE -> null;
            };

            String storagePath = (activeInputMode == InputMode.FILE) ? uploadedStoragePath : null;

            UUID newContentId;
            if (activeInputMode == InputMode.FILE) {
                // For files, download, extract title/text and register (allow user-provided title to override)
                newContentId = supabaseService.extractAndRegisterContent(
                        currentUserId,
                        storagePath,
                        uploadedOriginalFileName,
                        category,
                        title
                );
            } else {
                newContentId = supabaseService.registerContentRecord(
                        currentUserId,
                        title,
                        category,
                        contentText,
                        storagePath
                );
            }

            Notification.show("Contenido registrado exitosamente (ID: " + newContentId + ")", 4000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            eventPublisher.publishEvent(new ContentAddedEvent(this, newContentId, title));
            resetForm();

        } catch (Exception e) {
            Notification.show("Error al guardar: " + e.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
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

        // Use instance fields so other methods can update ML analysis
        analysisArea = new Div();
        analysisArea.getStyle()
                .set("background", "linear-gradient(135deg, #f8fafc 0%, #eefdf8 100%)")
                .set("border-radius", "14px")
                .set("padding", "18px")
                .set("min-height", "220px")
                .set("border", "1px solid #d1fae5")
                .set("color", "#334155");

        // Initial placeholder content
        Div headerRow = new Div();
        headerRow.getStyle().set("display", "flex").set("justify-content", "space-between").set("align-items", "center").set("margin-bottom", "8px");
        H3 headerTitle = new H3("Resultado del Análisis");
        headerTitle.getStyle().set("margin", "0").set("color", "#1E293B").set("font-weight", "700").set("font-size", "18px");
        Span badge = new Span("PENDIENTE");
        badge.getStyle().set("background-color", "#FFF7ED").set("color", "#92400E").set("padding", "6px 8px").set("border-radius", "12px").set("font-size", "12px");
        headerRow.add(headerTitle, badge);

        analysisText = new Paragraph("El contenido se organizará por tema, se resumirá de forma útil y quedará listo para buscar, consultar y reutilizar más tarde.");
        analysisText.getStyle().set("margin", "0").set("line-height", "1.6");

        analysisArea.add(headerRow, analysisText);
        panel.add(title, analysisArea);
        return panel;
    }

    private String getDomainColor(String category) {
        if (category == null) return "#8B5CF6";
        return switch (category.toLowerCase()) {
            case "backend" -> "#8B5CF6"; // purple
            case "frontend" -> "#3B82F6"; // blue
            case "cloud computing", "cloud" -> "#3B82F6"; // blue
            case "databases" -> "#059669"; // green
            case "data analysis" -> "#7C3AED"; // indigo/purple
            case "cybersecurity" -> "#EF4444"; // red
            case "artificial intelligence" -> "#8B5CF6"; // purple
            case "software architecture" -> "#F59E0B"; // yellow
            case "q/a" -> "#64748B"; // grey
            default -> "#8B5CF6";
        };
    }

    // Helper accessors to support multiple possible JSON key namings and safe casts
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getListMapFromMap(Map<String, Object> map, String[] keys) {
        for (String k : keys) {
            Object v = map.get(k);
            if (v instanceof List) {
                try {
                    return (List<Map<String, Object>>) v;
                } catch (ClassCastException ignored) {
                    // continue
                }
            }
        }
        return java.util.Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<String> getListFromMap(Map<String, Object> map, String[] keys) {
        for (String k : keys) {
            Object v = map.get(k);
            if (v instanceof List) {
                try {
                    return (List<String>) v;
                } catch (ClassCastException ignored) {
                    // try to coerce list of objects to strings
                    List<?> raw = (List<?>) v;
                    List<String> out = new java.util.ArrayList<>();
                    for (Object o : raw) {
                        out.add(o == null ? "" : o.toString());
                    }
                    return out;
                }
            }
        }
        return java.util.Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMapFromMap(Map<String, Object> map, String[] keys) {
        for (String k : keys) {
            Object v = map.get(k);
            if (v instanceof Map) {
                try {
                    return (Map<String, Object>) v;
                } catch (ClassCastException ignored) {
                    // continue
                }
            }
        }
        return null;
    }

    private String getStringFromMap(Map<String, Object> map, String[] keys, String defaultVal) {
        for (String k : keys) {
            Object v = map.get(k);
            if (v != null) return v.toString();
        }
        return defaultVal;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void renderAnalysis(Map<String, Object> map) {
        analysisArea.removeAll();

        // Normalize incoming map to support multiple response shapes (English/Spanish keys)
        Map<String, Object> normalized = new java.util.HashMap<>();
        if (map != null) normalized.putAll(map);

        // If ML returns spanish-shaped keys like {"categoria","probabilidad","palabras_clave"}, map them
        if (!normalized.containsKey("domainClassification") && normalized.containsKey("categoria")) {
            Object categoria = normalized.get("categoria");
            Object probObj = normalized.getOrDefault("probabilidad", normalized.get("probability"));
            int pct = 0;
            if (probObj instanceof Number) {
                double d = ((Number) probObj).doubleValue();
                pct = (d <= 1.0) ? (int) Math.round(d * 100) : (int) Math.round(d);
            } else if (probObj instanceof String) {
                try {
                    double d = Double.parseDouble((String) probObj);
                    pct = (d <= 1.0) ? (int) Math.round(d * 100) : (int) Math.round(d);
                } catch (NumberFormatException ignored) {}
            }
            List<String> palabras = getListFromMap(normalized, new String[]{"palabras_clave", "palabrasClave", "keywords", "tags"});
            Map<String, Object> single = new java.util.HashMap<>();
            single.put("category", categoria == null ? "" : categoria.toString());
            single.put("percentage", pct);
            single.put("matchedTechnologies", palabras == null ? java.util.Collections.emptyList() : palabras);
            List<Map<String, Object>> dlist = new java.util.ArrayList<>();
            dlist.add(single);
            normalized.put("domainClassification", dlist);
            // also set mainCategory shortcut
            Map<String, Object> main = new java.util.HashMap<>();
            main.put("name", categoria == null ? "" : categoria.toString());
            main.put("confidence", pct);
            normalized.put("mainCategory", main);
        }

        // Ensure aiTags come from palabras_clave or recomendaciones when present
        if (!normalized.containsKey("aiTags")) {
            List<String> fromWords = getListFromMap(normalized, new String[]{"palabras_clave", "palabrasClave", "keywords", "tags", "recomendaciones"});
            if (fromWords != null && !fromWords.isEmpty()) normalized.put("aiTags", fromWords);
        }

        // aiSummary might be under 'recomendaciones' as text array or 'summary'
        if (!normalized.containsKey("aiSummary")) {
            Object rec = normalized.get("recomendaciones");
            if (rec instanceof String) normalized.put("aiSummary", rec);
            else if (rec instanceof List) {
                List<?> r = (List<?>) rec;
                if (!r.isEmpty()) normalized.put("aiSummary", r.get(0).toString());
            }
        }

        // Replace map reference with normalized version
        map = normalized;

        // Ensure container position relative for floating badge
        analysisArea.getElement().getStyle().set("position", "relative");

        // Header row with IA icon + title
        Div headerRow = new Div();
        headerRow.getStyle().set("display", "flex").set("justify-content", "flex-start").set("align-items", "center").set("gap", "8px").set("margin-bottom", "8px");
        Icon aiIcon = VaadinIcon.MAGIC.create();
        aiIcon.setSize("18px");
        aiIcon.getStyle().set("color", "#8B5CF6");
        H3 headerTitle = new H3("Resultado del Análisis");
        headerTitle.getStyle().set("margin", "0").set("color", "#1E293B").set("font-weight", "700").set("font-size", "18px");
        headerRow.add(aiIcon, headerTitle);

        // Floating badge (top-right)
        String status = getStringFromMap(map, new String[]{"status", "state", "status_code", "estado"}, "COMPLETADO");
        Span badge = new Span(status);
        badge.getStyle().set("background-color", "#DCFCE7").set("color", "#15803D").set("padding", "6px 8px").set("border-radius", "12px").set("font-size", "12px");
        badge.getElement().getStyle().set("position", "absolute").set("right", "12px").set("top", "12px");

        analysisArea.add(headerRow, badge);

        // Domain classification
        Div domainSectionTitle = new Div(new Span("CLASIFICACIÓN POR DOMINIO"));
        domainSectionTitle.getStyle().set("text-transform", "uppercase").set("font-size", "12px").set("color", "#64748B").set("font-weight", "500").set("margin-bottom", "8px");
        analysisArea.add(domainSectionTitle);

        // Predefined technology lists per category and fixed percentages [80,65,50,30]
        List<Map<String, Object>> domains = getListMapFromMap(map, new String[]{"domainClassification", "domain_classification", "domain_classifications"});

        java.util.Map<String, List<String>> predefined = new java.util.LinkedHashMap<>();
        predefined.put("Backend", List.of("Java (Spring Boot)", "Node.js", "Python (FastAPI/Django)", "Go"));
        predefined.put("Frontend", List.of("React", "Angular", "Vue.js", "TypeScript"));
        predefined.put("Cloud Computing", List.of("AWS", "Azure", "Google Cloud", "OCI"));
        predefined.put("Databases", List.of("PostgreSQL", "MongoDB", "Redis", "MySQL"));
        predefined.put("Data Analysis", List.of("Python (Pandas)", "SQL", "Power BI", "Tableau"));
        predefined.put("Cybersecurity", List.of("OAuth2 / JWT", "IAM (Identity Access Management)", "SIEM", "Pentesting (OWASP / Burp Suite)"));
        predefined.put("Artificial Intelligence", List.of("PyTorch", "TensorFlow", "LangChain", "OpenAI API"));
        predefined.put("Software Architecture", List.of("Microservicios", "Docker / Kubernetes", "Domain-Driven Design", "Kafka (Event-Driven)"));
        predefined.put("Q/A", List.of("Selenium", "Postman", "JUnit", "Cypress"));

        int[] pcts = new int[]{80, 65, 50, 30};

        // Helper: get category percentage from ML if provided
        java.util.Map<String, Integer> categoryPctMap = new java.util.HashMap<>();
        for (Map<String, Object> d : domains) {
            String cat = d.getOrDefault("category", d.getOrDefault("name", "")).toString();
            try {
                int pct = ((Number) d.getOrDefault("percentage", d.getOrDefault("probability", 0))).intValue();
                categoryPctMap.put(cat, pct);
            } catch (Exception ignored) {}
        }

        // Decide which categories to show: prefer mainCategory, else use categories returned by domainClassification
        List<String> categoriesToShow = new java.util.ArrayList<>();
        Map<String, Object> maybeMain = getMapFromMap(map, new String[]{"mainCategory", "main_category", "primaryCategory", "primary_category", "categoria", "categoria_principal"});
        if (maybeMain != null && maybeMain.get("name") != null) {
            categoriesToShow.add(maybeMain.get("name").toString());
        } else {
            for (Map<String, Object> d : domains) {
                String cat = d.getOrDefault("category", d.getOrDefault("name", "")).toString();
                if (cat != null && !cat.isBlank() && !categoriesToShow.contains(cat)) categoriesToShow.add(cat);
            }
        }

        // If no categories identified, fallback to predefined full list
        if (categoriesToShow.isEmpty()) categoriesToShow.addAll(predefined.keySet());

        // Render only the selected categories in the predefined order if present
        for (String category : categoriesToShow) {
            if (!predefined.containsKey(category)) continue; // skip unknown categories
            List<String> techs = predefined.get(category);
            int categoryPct = categoryPctMap.getOrDefault(category, 0);

            Div item = new Div();
            item.getStyle().set("margin-bottom", "10px");

            Div row = new Div();
            row.getStyle().set("display", "flex").set("justify-content", "space-between").set("align-items", "center");
            Span catSpan = new Span(category);
            catSpan.getStyle().set("font-weight", "600").set("color", "#0f172a");
            Span pctSpan = new Span(categoryPct > 0 ? (categoryPct + "%") : "");
            pctSpan.getStyle().set("color", "#64748B");
            row.add(catSpan, pctSpan);
            item.add(row);

            // tech rows with fixed percentages ordered 80,65,50,30
            Div techList = new Div();
            techList.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "8px").set("margin", "6px 0");

            for (int i = 0; i < techs.size(); i++) {
                String tech = techs.get(i);
                int tpct = (i < pcts.length) ? pcts[i] : 0;

                Div techRow = new Div();
                techRow.getStyle().set("display", "flex").set("flex-direction", "column");

                Div techTop = new Div();
                techTop.getStyle().set("display", "flex").set("justify-content", "space-between").set("align-items", "center");
                Span techName = new Span(tech);
                techName.getStyle().set("color", "#0f172a").set("font-size", "13px");
                Span techPct = new Span(tpct + "%");
                techPct.getStyle().set("color", "#64748B");
                techTop.add(techName, techPct);

                Div progressOuterT = new Div();
                progressOuterT.getStyle().set("background-color", "#F1F5F9").set("height", "8px").set("border-radius", "6px").set("overflow", "hidden").set("margin-top", "6px");
                Div progressInnerT = new Div();
                String color = getDomainColor(category);
                progressInnerT.getStyle().set("background-color", color).set("height", "100%").set("width", tpct + "%").set("transition", "width 0.6s ease");
                progressOuterT.add(progressInnerT);

                techRow.add(techTop, progressOuterT);
                techList.add(techRow);
            }

            item.add(techList);
            analysisArea.add(item);
        }

        // Main category banner
        Map<String, Object> mainCat = getMapFromMap(map, new String[]{"mainCategory", "main_category", "primaryCategory", "primary_category"});
        if (mainCat != null && mainCat.get("name") != null) {
            String mainName = mainCat.get("name").toString();
            int conf = ((Number) mainCat.getOrDefault("confidence", 0)).intValue();

            Div banner = new Div();
            banner.getStyle().set("background-color", "#F0FDF4").set("border", "1px solid #BBF7D0").set("padding", "10px").set("border-radius", "12px").set("margin", "10px 0");
            Span ok = new Span("✓");
            ok.getStyle().set("color", "#166534").set("font-weight", "700").set("margin-right", "8px");
            Span txt = new Span("Categoría principal asignada ");
            txt.getStyle().set("color", "#475569");
            Span mainTxt = new Span(mainName + " · " + conf + "% confianza");
            mainTxt.getStyle().set("color", "#166534").set("font-weight", "700");
            banner.add(ok, txt, mainTxt);
            analysisArea.add(banner);
        }

        // AI Tags
        List<String> aiTags = getListFromMap(map, new String[]{"aiTags", "ai_tags", "tags"});
        if (aiTags != null && !aiTags.isEmpty()) {
            Div tagsWrap = new Div();
            tagsWrap.getStyle().set("display", "flex").set("gap", "8px").set("flex-wrap", "wrap").set("margin", "8px 0");
            for (String tag : aiTags) {
                Span t = new Span(tag);
                t.getStyle().set("background-color", "#F3E8FF").set("color", "#6B21A8").set("padding", "6px 10px").set("border-radius", "16px").set("font-size", "12px");
                tagsWrap.add(t);
            }
            analysisArea.add(tagsWrap);
        }

        // Key Concepts
        List<String> keyConcepts = getListFromMap(map, new String[]{"keyConcepts", "key_concepts", "concepts"});
        if (keyConcepts != null && !keyConcepts.isEmpty()) {
            Div conceptsWrap = new Div();
            conceptsWrap.getStyle().set("margin", "10px 0");
            int idx = 1;
            for (String concept : keyConcepts) {
                HorizontalLayout row = new HorizontalLayout();
                Span num = new Span(String.valueOf(idx));
                num.getStyle().set("background-color", "#DCFCE7").set("color", "#15803D").set("width", "24px").set("height", "24px").set("display", "flex").set("align-items", "center").set("justify-content", "center").set("border-radius", "4px").set("margin-right", "8px");
                Span conceptSpan = new Span(concept);
                conceptSpan.getStyle().set("color", "#0f172a");
                row.add(num, conceptSpan);
                conceptsWrap.add(row);
                idx++;
            }
            analysisArea.add(conceptsWrap);
        }

        // AI Summary and CTA
        String aiSummary = getStringFromMap(map, new String[]{"aiSummary", "ai_summary", "summary", "aiSummaryText"}, "");
        Div summaryCard = new Div();
        summaryCard.getStyle().set("background-color", "#F8FAFC").set("border-radius", "12px").set("padding", "12px").set("margin-top", "12px");
        H5 sumHeader = new H5("RESUMEN IA");
        sumHeader.getStyle().set("margin", "0 0 6px").set("text-transform", "uppercase").set("font-size", "12px");
        Paragraph sumText = new Paragraph(aiSummary);
        sumText.getStyle().set("margin", "0").set("color", "#334155");
        summaryCard.add(sumHeader, sumText);

        Button saveBtn = new Button("Guardar en Biblioteca", VaadinIcon.CHECK.create());
        saveBtn.getStyle().set("background-color", "#0F172A").set("color", "#FFFFFF").set("border-radius", "10px").set("width", "100%").set("margin-top", "8px");
        saveBtn.addClickListener(ev -> {
            // Reuse save flow: open confirmation with prefilled values or directly call saveContent
            Notification.show("Guardando contenido con resumen IA...", 2000, Notification.Position.BOTTOM_END);
            // Optionally trigger save using current form values
        });

        analysisArea.add(summaryCard, saveBtn);
    }
}
