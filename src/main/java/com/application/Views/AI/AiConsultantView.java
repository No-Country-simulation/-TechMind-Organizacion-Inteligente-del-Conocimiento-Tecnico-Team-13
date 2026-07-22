package com.application.Views.AI;

import com.application.Views.Layout.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("AI Consultant - KnowBase")
@Route(value = "ai-consultant", layout = MainLayout.class)
public class AiConsultantView extends VerticalLayout {

    public AiConsultantView() {
        setSizeFull();
        setMargin(false);
        setSpacing(true);
        setPadding(false);
        getStyle()
                .set("overflow-y", "auto")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("padding", "20px");

        // Cabecera
        HorizontalLayout header = createHeader();
        header.setHeight("auto");
        add(header);

        // Chat area
        VerticalLayout chatContainer = createChatContainer();
        chatContainer.getStyle().set("flex", "1");
        add(chatContainer);
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
        Span subtitle = new Span("Consulta al asistente técnico impulsado por IA");
        subtitle.getStyle().set("color", "#64748b").set("font-size", "14px");
        titles.add(mainTitle, subtitle);

        header.add(titles);
        return header;
    }

    private VerticalLayout createChatContainer() {
        VerticalLayout container = new VerticalLayout();
        container.setWidthFull();
        container.setHeight("500px");
        container.getStyle()
                .set("background-color", "#ffffff")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "12px")
                .set("padding", "20px")
                .set("display", "flex")
                .set("flex-direction", "column");

        // Área de chat
        Div chatArea = new Div();
        chatArea.setWidthFull();
        chatArea.setHeight("400px");
        chatArea.getStyle()
                .set("background-color", "#f8fafc")
                .set("border-radius", "8px")
                .set("padding", "15px")
                .set("overflow-y", "auto")
                .set("border", "1px solid #e2e8f0")
                .set("margin-bottom", "15px");

        Span placeholder = new Span("Área de chat - Escribe tu pregunta técnica");
        placeholder.getStyle().set("color", "#94a3b8").set("font-size", "13px");
        chatArea.add(placeholder);

        // Área de entrada
        HorizontalLayout inputArea = new HorizontalLayout();
        inputArea.setWidthFull();
        inputArea.setSpacing(true);
        inputArea.setAlignItems(Alignment.CENTER);

        TextArea inputField = new TextArea();
        inputField.setPlaceholder("Escribe tu pregunta...");
        inputField.setHeight("60px");
        inputField.getStyle().set("margin", "0");

        Button sendBtn = new Button("Enviar");
        sendBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        sendBtn.setIcon(VaadinIcon.ARROW_FORWARD.create());
        sendBtn.getStyle().set("background-color", "#00b894").set("color", "#ffffff");
        sendBtn.setHeight("60px");

        inputArea.add(inputField, sendBtn);
        inputArea.expand(inputField);

        container.add(chatArea, inputArea);
        container.expand(chatArea);

        return container;
    }
}
