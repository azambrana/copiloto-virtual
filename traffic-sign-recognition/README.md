
# Requirements:

- Anaconda3
- Python 3.10 

# Configurar:

```
conda create -n copiloto-virtual python=3.10
conda activate copiloto-virtual
```

### 1. Download the code and install the required packages

```
pip install ffmpeg-python
pip install opencv-python
pip install piexif
pip install pillow
pip install jupyter jupyterlab

# OR

pip install ffmpeg-python opencv-python piexif pillow jupyter jupyterlab

```


## GPU

```
pip uninstall torchvision
pip install torchvision --upgrade --extra-index-url https://download.pytorch.org/whl/cu124 
```

Nota: Incluso teniendo CUDA v12.6, no se puede instalar la versión de torchvision que requiere CUDA v12.6, por lo que se debe instalar la versión de torchvision que requiere CUDA v12.4.

## YOLO

```
!pip install -q git+https://github.com/THU-MIG/yolov10.git
!pip install huggingface_hub

```

## Windows

### Re-crear el entorno con conda

```
conda env list
conda deactivate
conda remove --name copiloto-virtual --all
```
## Linux
