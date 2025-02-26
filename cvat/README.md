# Proyecto de CVAT para administrar el conjunto de datos de la ciudad de Cochabamba

Para el *Diplomado Estadística Aplicada a la Toma de Decisiones - Tercera Versión - UMSS*

 
**Autor:** Alvaro Zambrana Sejas

**Email:** azambrana777@gmail.com

**Tutor:** M.Sc. Ing. Danny Luis Huanca Sevilla


## Prerequisitos

1. Git
2. Docker para windows


## Pasos para cargar el Proyecto de CVAT

1. Clonar el repositorio de CVAT https://github.com/cvat-ai/cvat


```
git clone https://github.com/opencv/cvat
cd cvat
```

2. Ejecutar los contenedores

```
docker compose up -d
```

3. Acceder http://localhost:8080/
4. Registrar un usuario y acceder
5. Comprimir en un archivo zip toda la carpeta "SeñalesDeTránsitoDeCochabamba" ubicado en la carpeta "copiloto-virtual\cvat\"
6. Importar el proyecto "SeñalesDeTránsitoDeCochabamba.zip"

