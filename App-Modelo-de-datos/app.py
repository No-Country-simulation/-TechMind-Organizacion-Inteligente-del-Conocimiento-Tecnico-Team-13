
from flask import Flask, request, jsonify
import joblib
import spacy
import re
import os


# --- IMPORTANTE: Replicar las funciones limpiar_texto y las constantes ---
# Para que la API funcione, necesita tener acceso a las mismas definiciones
# que se usaron durante el entrenamiento del modelo.


# Cargar el modelo de español de spaCy
nlp = spacy.load("es_core_news_sm")


# Palabras que se cuelan por el filtro automático de spaCy y no aportan valor
STOPWORDS_EXTRA = {"él", "ella", "ellos", "ellas", "ser", "estar",
                   "haber", "tener", "poder", "hacer", "deber", "ir"}


def limpiar_texto(texto: str) -> str:
    if not isinstance(texto, str):
        return ""
    texto = texto.lower()
    texto = re.sub(r"http\S+|www\.\S+", " ", texto)
    texto = re.sub(r"[^a-záéíóúñü\s]", " ", texto)
    texto = re.sub(r"\s+", " ", texto).strip()
    doc = nlp(texto)
    tokens_limpios = []
    for token in doc:
        if token.is_stop:
            continue
        for sub_lema in token.lemma_.split():
            if len(sub_lema) > 2 and sub_lema not in STOPWORDS_EXTRA:
                tokens_limpios.append(sub_lema)
    return " ".join(tokens_limpios)


# --- Fin de la réplica de funciones ---


app = Flask(__name__)


# Ruta donde se espera encontrar el pipeline
# Asegúrate de que esta ruta sea accesible desde donde ejecutes 'app.py'
RUTA_PIPELINE = 'pipeline_completo.pkl' # O la ruta donde lo hayas guardado


pipeline_cargado = None
vectorizador_cargado = None


try:
    # Cargar el pipeline al iniciar la aplicación
    pipeline_cargado = joblib.load(RUTA_PIPELINE)
    vectorizador_cargado = pipeline_cargado.named_steps['vectorizador']
    print(f"Pipeline cargado exitosamente desde {RUTA_PIPELINE}")
except Exception as e:
    print(f"Error al cargar el pipeline: {e}")
    print("Asegúrate de que 'pipeline_completo.pkl' esté en la ruta correcta.")




def obtener_prediccion_y_palabras_clave_api(titulo: str, texto: str, top_n: int = 5) -> dict:
    """
    Adaptación de la función de predicción para ser usada en la API.
    """
    if pipeline_cargado is None:
        return {"error": "Modelo no cargado"}


    texto_completo = titulo + " " + texto
    texto_limpio = limpiar_texto(texto_completo)


    if not texto_limpio:
        return {
            "categoria": "Desconocido",
            "probabilidad": 0.0,
            "palabras_clave": [],
            "recomendaciones": []
        }


    categoria_predicha = pipeline_cargado.predict([texto_limpio])[0]


    vector_texto = vectorizador_cargado.transform([texto_limpio])
    nombres_features = vectorizador_cargado.get_feature_names_out()
    tfidf_scores = vector_texto.data
    feature_indices = vector_texto.indices


    palabra_score_map = {nombres_features[idx]: score for idx, score in zip(feature_indices, tfidf_scores)}
    palabras_clave_ordenadas = sorted(palabra_score_map.items(), key=lambda item: item[1], reverse=True)
    palabras_clave = [palabra for palabra, _ in palabras_clave_ordenadas[:top_n]]


    probabilidad = 0.89 # Placeholder o se calcularía si el modelo lo permite


    return {
        "categoria": categoria_predicha,
        "probabilidad": probabilidad,
        "palabras_clave": palabras_clave,
        "recomendaciones": [] # Las recomendaciones se implementarían por separado si es necesario
    }


@app.route('/predict', methods=['POST'])
def predict():
    if not request.is_json:
        return jsonify({"error": "Request must be JSON"}), 400


    data = request.get_json()
    titulo = data.get('titulo')
    texto = data.get('texto')
    top_n = data.get('top_n', 5)


    if not titulo or not texto:
        return jsonify({"error": "'titulo' y 'texto' son campos requeridos."}), 400


    resultado = obtener_prediccion_y_palabras_clave_api(titulo, texto, top_n)
    return jsonify(resultado)


if __name__ == '__main__':
    # Para ejecutar en un entorno de desarrollo
    # En producción, usar un servidor WSGI como Gunicorn/uWSGI
    app.run(host='0.0.0.0', port=5000)
