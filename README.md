# G9-LATAM-Team 13
# TechMind-Organizacion-Inteligente-del-Conocimiento-Tecnico
Plataforma inteligente que optimiza la gestión de contenido técnico (documentación, tutoriales y artículos) mediante Ciencia de Datos. El sistema procesa y clasifica los textos automáticamente, identificando la información clave para facilitar su rápida consulta, organización y reutilización eficiente.
Tabla de contenidos


El problema
La solución
Arquitectura
Tecnologías utilizadas
Estructura del proyecto
Instalación y ejecución
Uso de la API
Integración con OCI
Ciencia de Datos — Notebook
Testing
Equipo
Roadmap futuro
Licencia



🧩 El problema

Cada día, estudiantes y profesionales de tecnología consumen enormes volúmenes de contenido técnico: documentación, tutoriales, artículos, apuntes de curso. Ese contenido casi nunca queda organizado — se acumula sin categorizar, sin etiquetas y sin forma sencilla de encontrarlo o reutilizarlo más adelante.

El resultado es previsible: tiempo perdido buscando información que ya se leyó antes, catalogación manual que nadie sostiene en el tiempo, y bases de conocimiento que crecen en desorden en lugar de crecer en valor.

KnowledgeSort ataca ese problema construyendo una capa de organización automática: recibe texto técnico y devuelve, de forma estructurada, qué es, de qué trata y con qué otro contenido se relaciona.

💡 La solución

Una API que recibe contenido técnico (título + texto) y devuelve:


Categoría temática del contenido (ej. Backend, Frontend, Datos, DevOps)
Palabras clave relevantes extraídas automáticamente
Contenidos relacionados ya existentes en la base de conocimiento (búsqueda semántica)
Resultado siempre en JSON, listo para ser consumido por otras aplicaciones (LMS, wikis internas, portales de documentación)


🏗️ Arquitectura

Arquitectura de dos capas para separar responsabilidades por lenguaje/expertise del equipo:

#mermaid-rqk-r1 { font-family: "Anthropic Sans", system-ui, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; font-size: 16px; fill: rgb(25, 25, 25); }
#mermaid-rqk-r1 .edge-animation-slow { stroke-dashoffset: 900; animation: 50s linear 0s infinite normal none running dash; stroke-linecap: round; stroke-dasharray: 9, 5 !important; }
#mermaid-rqk-r1 .edge-animation-fast { stroke-dashoffset: 900; animation: 20s linear 0s infinite normal none running dash; stroke-linecap: round; stroke-dasharray: 9, 5 !important; }
#mermaid-rqk-r1 .error-icon { fill: rgb(204, 120, 92); }
#mermaid-rqk-r1 .error-text { fill: rgb(51, 135, 163); stroke: rgb(51, 135, 163); }
#mermaid-rqk-r1 .edge-thickness-normal { stroke-width: 1px; }
#mermaid-rqk-r1 .edge-thickness-thick { stroke-width: 3.5px; }
#mermaid-rqk-r1 .edge-pattern-solid { stroke-dasharray: 0; }
#mermaid-rqk-r1 .edge-thickness-invisible { stroke-width: 0; fill: none; }
#mermaid-rqk-r1 .edge-pattern-dashed { stroke-dasharray: 3; }
#mermaid-rqk-r1 .edge-pattern-dotted { stroke-dasharray: 2; }
#mermaid-rqk-r1 .marker { fill: rgb(145, 145, 141); stroke: rgb(145, 145, 141); }
#mermaid-rqk-r1 .marker.cross { stroke: rgb(145, 145, 141); }
#mermaid-rqk-r1 svg { font-family: "Anthropic Sans", system-ui, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; font-size: 16px; }
#mermaid-rqk-r1 p { margin: 0px; }
#mermaid-rqk-r1 .label { font-family: "Anthropic Sans", system-ui, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; color: rgb(25, 25, 25); }
#mermaid-rqk-r1 .cluster-label text { fill: rgb(51, 135, 163); }
#mermaid-rqk-r1 .cluster-label span { color: rgb(51, 135, 163); }
#mermaid-rqk-r1 .cluster-label span p { background-color: transparent; }
#mermaid-rqk-r1 .label text, #mermaid-rqk-r1 span { fill: rgb(25, 25, 25); color: rgb(25, 25, 25); }
#mermaid-rqk-r1 .node rect, #mermaid-rqk-r1 .node circle, #mermaid-rqk-r1 .node ellipse, #mermaid-rqk-r1 .node polygon, #mermaid-rqk-r1 .node path { fill: rgb(240, 240, 235); stroke: rgb(217, 216, 213); stroke-width: 1px; }
#mermaid-rqk-r1 .rough-node .label text, #mermaid-rqk-r1 .node .label text, #mermaid-rqk-r1 .image-shape .label, #mermaid-rqk-r1 .icon-shape .label { text-anchor: middle; }
#mermaid-rqk-r1 .node .katex path { fill: rgb(0, 0, 0); stroke: rgb(0, 0, 0); stroke-width: 1px; }
#mermaid-rqk-r1 .rough-node .label, #mermaid-rqk-r1 .node .label, #mermaid-rqk-r1 .image-shape .label, #mermaid-rqk-r1 .icon-shape .label { text-align: center; }
#mermaid-rqk-r1 .node.clickable { cursor: pointer; }
#mermaid-rqk-r1 .root .anchor path { stroke-width: 0; stroke: rgb(145, 145, 141); fill: rgb(145, 145, 141) !important; }
#mermaid-rqk-r1 .arrowheadPath { fill: rgb(11, 11, 11); }
#mermaid-rqk-r1 .edgePath .path { stroke: rgb(145, 145, 141); stroke-width: 1px; }
#mermaid-rqk-r1 .flowchart-link { stroke: rgb(145, 145, 141); fill: none; }
#mermaid-rqk-r1 .edgeLabel { background-color: rgb(245, 230, 216); text-align: center; }
#mermaid-rqk-r1 .edgeLabel p { background-color: rgb(245, 230, 216); }
#mermaid-rqk-r1 .edgeLabel rect { opacity: 0.5; background-color: rgb(245, 230, 216); fill: rgb(245, 230, 216); }
#mermaid-rqk-r1 .labelBkg { background-color: rgba(245, 230, 216, 0.5); }
#mermaid-rqk-r1 .cluster rect { fill: rgb(204, 120, 92); stroke: rgb(138, 115, 107); stroke-width: 1px; }
#mermaid-rqk-r1 .cluster text { fill: rgb(51, 135, 163); }
#mermaid-rqk-r1 .cluster span { color: rgb(51, 135, 163); }
#mermaid-rqk-r1 div.mermaidTooltip { position: absolute; text-align: center; max-width: 200px; padding: 2px; font-family: "Anthropic Sans", system-ui, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; font-size: 12px; background: rgb(204, 120, 92); border: 1px solid rgb(138, 115, 107); border-radius: 2px; pointer-events: none; z-index: 100; }
#mermaid-rqk-r1 .flowchartTitleText { text-anchor: middle; font-size: 18px; fill: rgb(25, 25, 25); }
#mermaid-rqk-r1 rect.text { fill: none; stroke-width: 0; }
#mermaid-rqk-r1 .icon-shape, #mermaid-rqk-r1 .image-shape { background-color: rgb(245, 230, 216); text-align: center; }
#mermaid-rqk-r1 .icon-shape p, #mermaid-rqk-r1 .image-shape p { background-color: rgb(245, 230, 216); padding: 2px; }
#mermaid-rqk-r1 .icon-shape .label rect, #mermaid-rqk-r1 .image-shape .label rect { opacity: 0.5; background-color: rgb(245, 230, 216); fill: rgb(245, 230, 216); }
#mermaid-rqk-r1 .label-icon { display: inline-block; height: 1em; overflow: visible; vertical-align: -0.125em; }
#mermaid-rqk-r1 .node .label-icon path { fill: currentcolor; stroke: revert; stroke-width: revert; }
#mermaid-rqk-r1 .node .neo-node { stroke: rgb(217, 216, 213); }
#mermaid-rqk-r1 [data-look="neo"].node rect, #mermaid-rqk-r1 [data-look="neo"].cluster rect, #mermaid-rqk-r1 [data-look="neo"].node polygon { stroke: url("#mermaid-rqk-r1-gradient"); filter: drop-shadow(rgb(185, 185, 185) 1px 2px 2px); }
#mermaid-rqk-r1 [data-look="neo"].node path { stroke: url("#mermaid-rqk-r1-gradient"); stroke-width: 1px; }
#mermaid-rqk-r1 [data-look="neo"].node .outer-path { filter: drop-shadow(rgb(185, 185, 185) 1px 2px 2px); }
#mermaid-rqk-r1 [data-look="neo"].node .neo-line path { stroke: rgb(217, 216, 213); filter: none; }
#mermaid-rqk-r1 [data-look="neo"].node circle { stroke: url("#mermaid-rqk-r1-gradient"); filter: drop-shadow(rgb(185, 185, 185) 1px 2px 2px); }
#mermaid-rqk-r1 [data-look="neo"].node circle .state-start { fill: rgb(0, 0, 0); }
#mermaid-rqk-r1 [data-look="neo"].icon-shape .icon { fill: url("#mermaid-rqk-r1-gradient"); filter: drop-shadow(rgb(185, 185, 185) 1px 2px 2px); }
#mermaid-rqk-r1 [data-look="neo"].icon-shape .icon-neo path { stroke: url("#mermaid-rqk-r1-gradient"); filter: drop-shadow(rgb(185, 185, 185) 1px 2px 2px); }
#mermaid-rqk-r1 :root { --mermaid-font-family: "Anthropic Sans",system-ui,"Segoe UI",Roboto,Helvetica,Arial,sans-serif; }POST /contenidovalida y orquestacarga modeloembeddingspersiste resultadorespuesta JSONCliente / PostmanAPI Java · Spring BootMicroservicio Python ·FastAPIOCI Object StorageOpenAI API vía LangChainPostgreSQL / Oracle DB


API Java (Spring Boot): punto de entrada público, validación, orquestación y persistencia.
Microservicio Python (FastAPI): inferencia del modelo entrenado + búsqueda semántica.
OCI Object Storage: almacenamiento del modelo serializado y documentos procesados.
Base de datos: persistencia de resultados y contenidos ya catalogados.


🛠️ Tecnologías utilizadas

CapaTecnologíaVersión sugeridaCiencia de DatosPython, Pandas, Scikit-learn, TF-IDFPython 3.11Serialización de modelojoblib—Servicio de modeloFastAPI, Uvicorn0.110.xBúsqueda semánticaLangChain + OpenAI API (embeddings)—API principalJava 17, Spring Boot, MavenSpring Boot 3.xPersistenciaPostgreSQL u Oracle Autonomous DB15.x / 19cCloudOCI Object Storage, OCI Compute (opcional)—ContenedoresDocker, Docker Compose24.xTestingPostman, pytest, JUnit—DiseñoFigma—

📁 Estructura del proyecto

knowledgesort/
├── data-science/
│   ├── notebook_eda_entrenamiento.ipynb
│   ├── dataset/
│   └── modelo/
│       └── modelo_clasificacion.joblib
├── model-service/                # FastAPI
│   ├── app/
│   │   ├── main.py
│   │   ├── inferencia.py
│   │   └── busqueda_semantica.py
│   ├── requirements.txt
│   └── Dockerfile
├── api-service/                  # Spring Boot
│   ├── src/main/java/...
│   ├── src/main/resources/application.yml
│   ├── pom.xml
│   └── Dockerfile
├── postman/
│   └── coleccion_knowledgesort.postman_collection.json
├── docker-compose.yml
├── .env.example
└── README.md
