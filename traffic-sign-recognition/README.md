Proyecto Python que contiene scripts y notebooks para procesamiento, aumento de datos, entrenamiento, pruebas de inferencia, etc.

Para el *Diplomado Estadística Aplicada a la Toma de Decisiones - Tercera Versión - UMSS*

El proyecto puede ser abierto con PyCharm o cualquier IDE que soporte Python.

# Requirements:

## Software
- Windows 11
- Anaconda3
- PyCharm Professional
- CUDA Toolkit 12.4 (Opcional)

## Hardware
- Una tarjeta gráfica RTX para el ajuste fino

# Configurar:

```
conda create -n copiloto-virtual python=3.10
conda activate copiloto-virtual
```

Configurar el IDE para usar el entorno virtual creado.

```

### 1. Instalar las dependencias para los notebooks

```
pip install ffmpeg-python
pip install opencv-python
pip install piexif
pip install pillow
pip install jupyter jupyterlab

# O simplemente

pip install ffmpeg-python opencv-python piexif pillow jupyter jupyterlab

```

Nota: Cada notebooks contiene los comandos suficientes para ejecularlo, incluyendo sus dependencias.

## GPU con CUDA, NVidia RTX

Instalar el CUDA Toolkit de NVidia (Opcional): https://developer.nvidia.com/cuda-downloads

Revisar https://pytorch.org/ para la última versión disponible.  En caso de tener una versión antigua, desinstalarlo.

```
pip uninstall torchvision
```

Instalar la versión más reciente.

```
pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cu126
```

## YOLO

Para la instalación de YOLO, revisar los notebooks correspondientes a cada versión

* local-train_yolov8_object_detection_on_custom_dataset_with_augmented_data.ipynb
* local-train_yolov10_object_detection_on_custom_dataset_with_augmented_data.ipynb
* local-train_yolov11_object_detection_on_custom_dataset_with_augmented_data.ipynb

Nota: Desinstalar cualquier versión de Ultralytics differente

```
pip uninstall ultralytics supervision
```

## Troubleshooting

### Recrear el entorno con conda

```
conda env list
conda deactivate
conda remove --name copiloto-virtual --all
```

### PyTorch + CUDA

Problemas con torchvision o torh
Instalar versiones antiguas torch==2.5.1 + CUDA 12.4, por ejemplo.