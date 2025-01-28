class MapillaryConfig:
    """
    Configuración por defecto para el transformador de Mapillary.
    """
    ruta_origen_imagenes = 'dataset/raw'
    ruta_destino_imagenes = 'dataset/interim'

    ancho = 1280
    alto = 1280

    recursivo = False

    def __init__(self, ruta_origen_imagenes=None, ruta_destino_imagenes=None, ancho=None, alto=None, recursivo=None):
        if ruta_origen_imagenes:
            self.ruta_origen_imagenes = ruta_origen_imagenes
        if ruta_destino_imagenes:
            self.ruta_destino_imagenes = ruta_destino_imagenes
        if ancho:
            self.ancho = ancho
        if alto:
            self.alto = alto
        if recursivo:
            self.recursivo = recursivo

    def __str__(self):

        return f"Configuración de transformación de Mapillary:\n" \
               f"\tRuta de origen de imágenes: {self.ruta_origen_imagenes}\n" \
               f"\tRuta de destino de imágenes: {self.ruta_destino_imagenes}\n" \
               f"\tAncho de imagen: {self.ancho}\n" \
               f"\tAlto de imagen: {self.alto}"

    def __repr__(self):
        return self.__str__()