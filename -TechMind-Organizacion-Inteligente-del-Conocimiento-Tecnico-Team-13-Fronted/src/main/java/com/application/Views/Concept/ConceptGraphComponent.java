package com.application.Views.Concept;

import com.application.data.StaticConceptData;
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

    public ConceptGraphComponent() {
        setSizeFull();
        getStyle().set("display", "block");
        getStyle().set("position", "absolute");
        getStyle().set("top", "0");
        getStyle().set("left", "0");
        getStyle().set("width", "100%");
        getStyle().set("height", "100%");
        getStyle().set("overflow", "hidden");
    }

    /**
     * Pasa los nodos y aristas desde Java hacia el Grafo en Vis.js
     */
    public void setGraphData(List<StaticConceptData.NodeDto> nodes, List<StaticConceptData.EdgeDto> edges) {
        getElement().callJsFunction("initGraph",
                Json.instance().parse(convertToJson(nodes)),
                Json.instance().parse(convertToJson(edges))
        );
    }

    public void zoomIn() {
        getElement().callJsFunction("zoomIn");
    }

    public void zoomOut() {
        getElement().callJsFunction("zoomOut");
    }

    public void fitGraph() {
        getElement().callJsFunction("fitGraph");
    }

    public void exportGraph(String fileName) {
        getElement().callJsFunction("exportGraph", fileName);
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
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            return "[]";
        }
    }
}
