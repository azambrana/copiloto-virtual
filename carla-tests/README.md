# Scripts en Python para realizar Pruebas del modelo YOLO en CARLA Simulator versión Unreal Engine 4

Para el *Diplomado Estadística Aplicada a la Toma de Decisiones - Tercera Versión - UMSS*
 
**Autor:** Alvaro Zambrana Sejas

**Email:** azambrana777@gmail.com

**Tutor:** M.Sc. Ing. Danny Luis Huanca Sevilla

## Prerequisitos

1. Descarar Anaconda de 
2. Instalar Anaconda 3

```
conda create -n carla-ue4 python=3.8.10
conda activate carla-ue4
```

## Pasos para ejecutar

1. Descargar CARLA_0.9.15.zip y AdditionalMaps_0.9.15.zip de https://github.com/carla-simulator/carla/releases
2. Descomprimir CARLA_0.9.15.zip en la raíz de la unidad `C`
- Resultado: `c:\carla-0.9.15`
3. Descomprimir AdditionalMaps_0.9.15.zip en `c:\carla_0.9.15\`
4. Descomprimir `copiloto-virtual\carla-tests\Cochabamba_Bolivia.zip` en la ruta
`c:\carla-0.9.15\Unreal\CarlaUE4\Content\Carla\Static\TrafficSign\`
5. Abrir un mapa y modificarlo agregando las señales de tránsito de Cochabamba_Bolivia, ver los video de copiloto-virtual
- `AlvaroZambrana-CarlaUE4.mp4`
- `PruebaDeInferencia_YOLO_CarlaUE4.mp4`
6. Seguir los pasos para instalar las dependencias de CARLA para poder ejecutar los scripts de Python
- Ejemplo:  
```python3 -m pip install -r requirements.txt```

7. Ejecutar inferencia_carla_yolov8.py desde línea de comandos o desde cualquier editor Python, ejemplo: PyCharm 2024

## Referencias
https://carla.readthedocs.io/en/0.9.15/tutorials/
https://carla.readthedocs.io/en/0.9.15/start_quickstart/#running-carla


