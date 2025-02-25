# Desarrollo de un copiloto virtual para la asistencia de conductores basado en visión artificial y edge AI en android

Para el *Diplomado Estadística Aplicada a la Toma de Decisiones - Tercera Versión - UMSS*

 
**Autor:** Alvaro Zambrana Sejas

**Email:** azambrana777@gmail.com

**Tutor:** M.Sc. Ing. Danny Luis Huanca Sevilla

Este proyecto consiste en el desarrollo de un copiloto virtual para la asistencia de conductores de vehículos utilizando tecnologías de inteligencia artificial, como el aprendizaje profundo y la visión artificial, junto con Edge AI para procesar datos localmente en el dispositivo, el sistema identifica señales de tránsito en tiempo real y brinda alertas al conductor mediante la reproducción de sonidos.  Esta solución, implementada para Android, nace con la intención de ayudar en la mejora la seguridad vial en Cochabamba al ofrecer una herramienta accesible, eficiente y funcional incluso en entornos con conectividad limitada.

* Carpeta android-app
Contiene todo el proyecto para el IDE Android Studio, referirse al documento README.md para detall

* Carpeta assets
Contiene logos, imágenes utilizadas en diferentes documentos y en la app Android.

* Carpeta carla-tests
Contiene el proyecto para realizar pruebas utilizando el simulador CARLA.

* Carpeta cvat
Contiene la copia de respaldo del proyecto en CVAT para etiquetar el conjunto de datos.

* Carpeta demos
Contiene los archivos multimedia con capturas de video sobre el funcionamiento de la aplicación y del simulador CARLA

* Carpeta docs
En esta carpeta se encuentra algunos archivos de Tableau, así como documentación auxiliar para configurar el entorno de desarrollo

* Carpeta object-detection-models
Contiene los archivos serializados de los modelos entrenados en formato .pt.

* Carpeta runs
Contiene los resultados del entrenamiento con las diferentes versiones de YOLO.

* Carpeta traffic-sign-recognition
Contiene el proyecto de ciencia de datos en Python con notebooks para crear los metadatos, entrenar los modelos con las 3 versiones de YOLO, realizar tareas ETL, etc.
