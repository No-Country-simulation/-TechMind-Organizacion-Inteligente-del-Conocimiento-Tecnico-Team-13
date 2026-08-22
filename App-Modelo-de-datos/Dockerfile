FROM python:3.11-slim

WORKDIR /app

ENV PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1

# requirements.txt primero (aparte de app.py/pipeline) para que Docker cachee la capa de
# dependencias y no reinstale spaCy/scikit-learn en cada build por un cambio en app.py.
COPY requirements.txt .
RUN pip install -r requirements.txt

COPY app.py pipeline_completo.pkl ./

EXPOSE 5000

# gunicorn en vez de "python app.py" (servidor de desarrollo de Flask, no pensado para
# producción). 2 workers: cada uno carga su propia copia del pipeline + modelo de spaCy en
# memoria, ajustar según la RAM disponible en el servidor de despliegue.
CMD ["gunicorn", "--bind", "0.0.0.0:5000", "--workers", "2", "--timeout", "60", "app:app"]
