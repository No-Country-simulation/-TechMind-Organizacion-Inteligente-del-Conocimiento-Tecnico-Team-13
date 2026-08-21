import { LitElement, html, css } from 'lit';
import { Network } from 'vis-network/standalone';

export class ConceptGraph extends LitElement {
  static styles = css`
    :host {
      display: block;
      width: 100%;
      height: 100%;
      position: relative;
    }
    #network-container {
      width: 100%;
      height: 100%;
      background-color: #F8FAFC;
      background-image: radial-gradient(#CBD5E1 1px, transparent 1px);
      background-size: 24px 24px;
    }
  `;

  render() {
    return html`<div id="network-container"></div>`;
  }

  firstUpdated() {
    this.container = this.renderRoot.querySelector('#network-container');
  }

  // Método invocado desde Java para renderizar el grafo
  initGraph(nodesData, edgesData) {
    this.nodes = new vis.DataSet(nodesData);
    this.edges = new vis.DataSet(edgesData);

    const options = {
      nodes: {
        shape: 'dot',
        size: 16,
        font: { size: 14, face: 'Inter, sans-serif', color: '#94A3B8' },
        borderWidth: 2
      },
      edges: {
        width: 2,
        color: { color: '#E2E8F0', highlight: '#10B981' },
        dashes: true
      },
      interaction: { hover: true, selectConnectedEdges: false },
      physics: {
        enabled: true,
        solver: 'forceAtlas2Based',
        stabilization: { iterations: 150 }
      }
    };

    this.network = new Network(this.container, { nodes: this.nodes, edges: this.edges }, options);

    // Evento al seleccionar un nodo
    this.network.on('selectNode', (params) => {
      const selectedNodeId = params.nodes[0];
      this.applyFocusStyle(selectedNodeId);

      // Notificar al servidor Java mediante un evento custom
      this.dispatchEvent(new CustomEvent('node-selected', {
        detail: { nodeId: selectedNodeId },
        bubbles: true,
        composed: true
      }));
    });

    // Evento al hacer clic en el fondo vacío (deseleccionar)
    this.network.on('deselectNode', () => {
      this.resetStyle();
      this.dispatchEvent(new CustomEvent('node-deselected', {
        bubbles: true,
        composed: true
      }));
    });
  }

  // Aplica el efecto de enfoque / opacidad
  applyFocusStyle(selectedNodeId) {
    const connectedNodes = this.network.getConnectedNodes(selectedNodeId);
    const connectedEdges = this.network.getConnectedEdges(selectedNodeId);

    const activeNodes = new Set([...connectedNodes, selectedNodeId]);
    const activeEdges = new Set(connectedEdges);

    const updatedNodes = this.nodes.get().map(node => {
      const isSelected = node.id === selectedNodeId;
      const isActive = activeNodes.has(node.id);

      if (isSelected) {
        return { ...node, size: 24, font: { color: '#000000', size: 18, bold: '700' }, opacity: 1 };
      } else if (isActive) {
        return { ...node, size: 16, font: { color: '#334155', size: 14 }, opacity: 1 };
      } else {
        return { ...node, size: 12, font: { color: '#CBD5E1', size: 12 }, opacity: 0.25 };
      }
    });

    const updatedEdges = this.edges.get().map(edge => {
      if (activeEdges.has(edge.id)) {
        return { ...edge, color: { color: '#10B981' }, width: 3, dashes: false };
      } else {
        return { ...edge, color: { color: '#F1F5F9' }, width: 1, dashes: true, opacity: 0.1 };
      }
    });

    this.nodes.update(updatedNodes);
    this.edges.update(updatedEdges);
  }

  resetStyle() {
    // Restaura todos los elementos a su opacidad original
    const updatedNodes = this.nodes.get().map(node => ({
      ...node, size: 16, font: { color: '#334155', size: 14 }, opacity: 1
    }));
    const updatedEdges = this.edges.get().map(edge => ({
      ...edge, color: { color: '#E2E8F0' }, width: 2, dashes: true, opacity: 1
    }));

    this.nodes.update(updatedNodes);
    this.edges.update(updatedEdges);
  }
}

customElements.define('concept-graph', ConceptGraph);