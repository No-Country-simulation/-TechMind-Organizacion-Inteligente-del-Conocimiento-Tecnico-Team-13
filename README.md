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
<img width="698" height="157" alt="image" src="https://github.com/user-attachments/assets/bad1b9be-8a37-4a8d-9663-3f42f4d860ac" />


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
