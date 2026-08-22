package com.application.Views.Concept;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;

import elemental.json.Json;

import java.util.List;

@Tag("concept-graph")
@JsModule("./concept-graph.js")
@NpmPackage(value = "vis-network", version = "^9.1.9")
public class ConceptGraphComponent extends Component implements HasSize {

    /** DTOs del grafo esperados por concept-graph.js (vis.js DataSet: {id,label,group} / {from,to}). */
    public record NodeDto(String id, String label, String group) {
    }

    public record EdgeDto(String from, String to) {
    }

    public ConceptGraphComponent() {
        setSizeFull();
    }

    /**
     * Pasa los nodos y aristas desde Java hacia el Grafo en Vis.js
     */
    public void setGraphData(List<NodeDto> nodes, List<EdgeDto> edges) {
        getElement().callJsFunction("initGraph",
                Json.instance().parse(convertToJson(nodes)),
                Json.instance().parse(convertToJson(edges))
        );
    }

    // Evento cuando se selecciona un nodo
    public Registration addNodeSelectedListener(ComponentEventListener<NodeSelectedEvent> listener) {
        return addListener(NodeSelectedEvent.class, listener);
    }

    // Evento cuando se deselecciona
    public Registration addNodeDeselectedListener(ComponentEventListener<NodeDeselectedEvent> listener) {
        return addListener(NodeDeselectedEvent.class, listener);
    }

    // --- EVENTOS DE VAADIN ---

    @DomEvent("node-selected")
    public static class NodeSelectedEvent extends ComponentEvent<ConceptGraphComponent> {
        private final String nodeId;

        public NodeSelectedEvent(ConceptGraphComponent source, boolean fromClient, @EventData("event.detail.nodeId") String nodeId) {
            super(source, fromClient);
            this.nodeId = nodeId;
        }

        public String getNodeId() {
            return nodeId;
        }
    }

    @DomEvent("node-deselected")
    public static class NodeDeselectedEvent extends ComponentEvent<ConceptGraphComponent> {
        public NodeDeselectedEvent(ConceptGraphComponent source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    private String convertToJson(Object object) {
        // Usa ObjectMapper de Jackson o Gson para convertir tus registros a String JSON
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            return "[]";
        }
    }
}
