"""

Transformador de Mapillary.  Este transformador se encarga de procesar las imágenes capturadas por Mapillary,
redimensionarlas y recortarlas en dos regiones de 1280 x 1280.

Las imágenes capturadas por Mapillary tienen una resolución de 4624 x 3468.  Se redimensionan a la mitad y se recortan
en dos regiones de 1280 x 1280, sin perder la información de la orientación de la imagen y la información EXIF.

@autor: Alvaro Zambrana Sejas
@fecha: 2024-09-28

"""

import os
import piexif
import shutil

from src.copiloto_virtual.config.MapillaryConfig import MapillaryConfig
from src.copiloto_virtual.transformers.ITransformer import ITransformer
from typing import Dict, Any
from PIL import Image
import cv2

class MapillaryTransformer(ITransformer):

    config = None

    def __init__(self, *, config: MapillaryConfig):
        self.config = config

    def transform(self) -> bool:
        """
        Procesa un directorio de imágenes para redimensionarlas y recortarlas.
        """

        input_folder = self.config.ruta_origen_imagenes
        output_folder = self.config.ruta_destino_imagenes
        ancho = self.config.ancho
        alto = self.config.alto

        if not os.path.exists(input_folder):
            raise FileNotFoundError(f'La ruta {input_folder} no existe')

        if not os.path.exists(output_folder):
            os.makedirs(output_folder)
        #else:
        #    shutil.rmtree(output_folder)

        # self.process_folder(input_folder, output_folder, ancho, alto)

        # copiar el directorio de origen al directorio de destino recursivamente

        shutil.copytree(input_folder, output_folder, dirs_exist_ok=True)

        self.process_folder_v2(output_folder, ancho, alto)

        return True

    def process_folder_v2(self, target_folder, ancho, alto):
        """
        Procesa un directorio de imágenes para redimensionarlas y recortarlas.

        :param input_folder: Ruta del directorio de las imágenes
        :param output_folder: Ruta del directorio de salida
        :param ancho: Ancho de la región a recortar
        :param alto: Altura de la región a recortar
        :return: Ruta del directorio de salida
        """
        print(f"Procesando directorio {target_folder}")

        # if not os.path.exists(output_folder):
        #   os.makedirs(output_folder)
        # else:
        #    # shutil.rmtree(output_folder)

        for root, dirs, files in os.walk(target_folder):
            for file in files:
                if not file.endswith('.jpg'):
                    continue

                input_file = os.path.join(root, file)
                print(f"Procesando fichero {input_file}")
                #if os.path.exists(os.path.join(output_folder, file)):
                #    print(f"El fichero {input_file} ya fue procesado.  Se omite.")
                #    continue

                # check if the file is an image file

                input_file_path = self.rotate_image_if_upside_down(input_file)
                resized_image_path = self.resize_image_to_half_v2(input_file_path)
                _ = self.crop_image_region_v2(resized_image_path, root, ancho, alto)

                os.remove(input_file_path)
                # os.remove(resized_image_path)

        return target_folder

    def process_folder(self, input_folder, output_folder, ancho, alto):
        """
        Procesa un directorio de imágenes para redimensionarlas y recortarlas.

        :param input_folder: Ruta del directorio de las imágenes
        :param output_folder: Ruta del directorio de salida
        :param ancho: Ancho de la región a recortar
        :param alto: Altura de la región a recortar
        :return: Ruta del directorio de salida
        """
        print(f"Procesando directorio {input_folder}")

        # if not os.path.exists(output_folder):
        #   os.makedirs(output_folder)
        # else:
        #    # shutil.rmtree(output_folder)

        for root, dirs, files in os.walk(input_folder):
            if self.config.recursivo:
                for dir in dirs:
                    self.process_folder(os.path.join(root, dir), output_folder, ancho, alto)
            for file in files:
                input_file = os.path.join(root, file)
                print(f"Procesando fichero {input_file}")
                if os.path.exists(os.path.join(output_folder, file)):
                    print(f"El fichero {input_file} ya fue procesado.  Se omite.")
                    continue
                input_file_copy = self.copy_image_to(input_file=input_file, output_folder=output_folder)
                if input_file_copy is not None:
                    input_file_path = self.rotate_image_if_upside_down(input_file_copy)
                    print(input_file_path)
                    resized_image_path = self.resize_image_to_half(input_file_path, output_folder)
                    _ = self.crop_image_region_v2(resized_image_path, output_folder, ancho, alto)

                    os.remove(input_file_path)
                    os.remove(resized_image_path)

        return output_folder

    def check_image_upside_down(self, image_path):
        exif_data = piexif.load(image_path)

        orientation = exif_data['0th'].get(piexif.ImageIFD.Orientation)

        if orientation is None:
            return False

        return orientation == 3


    def copy_image_to(self, *, input_file, output_folder):

        if not input_file.endswith('.jpg'):
            return None

        try:
            file_name = os.path.basename(input_file)
            output_path = os.path.join(output_folder, file_name)

            return shutil.copy(input_file, output_path)
        except Exception as e:
            print(f"Ocurrió un error al copiar el fichero: {e}")
            return None

    def crop_image_region_v2(self, image_path, output_folder, width, height):

        """
        Recorta la imagen en dos regiones, una en la parte izquierda y otra en la parte derecha.

        :param image_path: Ruta al archivo de la imagen
        :param output_folder: Ruta al directorio de salida
        :param width: Ancho de la región a recortar
        :param height: Altura de la región a recortar
        :return: La ruta a los archivos de las regiones recortadas
        """

        print(f"Procesando {image_path}")

        image = Image.open(image_path)
        _, ext = os.path.splitext(image_path)

        current_image_width, current_image_height = image.size

        # recorte region inferior izquierda
        crop_1 = image.crop((0, current_image_height - height, width, current_image_height))

        # recorte region inferior derecha
        crop_2 = image.crop((current_image_width - width, current_image_height - height, current_image_width, current_image_height))

        image_path_crop1 = self.build_file_path(image_path, output_folder, width, height, '.left-region')
        image_path_crop2 = self.build_file_path(image_path, output_folder, width, height, '.right-region')

        driver = ext[1:].upper()
        if driver == 'JPG':
            driver = 'JPEG'

        if 'exif' in image.info:
            exif_dict = piexif.load(image.info['exif'])
            exif_dict['Exif'][piexif.ExifIFD.PixelXDimension] = width
            exif_dict['Exif'][piexif.ExifIFD.PixelYDimension] = height
            exif_dict['Exif'][41729] = b'1'
            crop_1.save(image_path_crop1, driver, exif=piexif.dump(exif_dict), quality=100)
            crop_2.save(image_path_crop2, driver, exif=piexif.dump(exif_dict), quality=100)
        else:
            crop_1.save(image_path_crop1, driver, quality=100)
            crop_2.save(image_path_crop2, driver, quality=100)

        image.close()

        return [image_path_crop1, image_path_crop2]

    def build_file_path(self, file_path, output_folder, width, height, suffix=''):
        """
        Construye la ruta al archivo de salida con el nuevo tamaño de la imagen.

        :param file_path: Ruta del archivo de la imagen
        :param output_folder: Rutal del directorio de salida
        :param width: Ancho de la imagen
        :param height: Altura de la imagen
        :param suffix: Sufix para el nombre del archivo
        :return: Ruta con el nombre del archivo de salida
        """
        image_name = os.path.basename(file_path)
        image_name_without_extension = os.path.splitext(image_name)[0]
        image_extension = os.path.splitext(file_path)[1]
        new_image_path = f'{output_folder}/{image_name_without_extension}_{width}x{height}{suffix}{image_extension}'
        return new_image_path

    # image_path = '../dataset/interim/zona-escolar-1/2024_09_11_14_18_50_849_-0400_2312x1734.jpg'
    # image_path = 'output/2024_09_08_17_18_09_167_-0400_2312x1734.jpg'
    # crop_image_region(image_path, './output', 1280, 1280)
    # %%


    def rotate_image_if_upside_down(self, image_path):
        """
        Rotar la imagen 180 grados si está al revés.  Esto debido a que se capturó la imagen con el teléfono al revés.
        :param image_path: Rutal al archivo de la imagen
        :return: Rutal al archivo de la imagen rotada
        """

        image = Image.open(image_path)

        exif_dict = piexif.load(image.info['exif'])

        if self.check_image_upside_down(image_path):
            exif_dict['0th'][piexif.ImageIFD.Orientation] = 1
            exif_dict['Exif'][41729] = b'1'

            flipped_img = image.rotate(180, expand=True)

            if exif_dict is not None:
                flipped_img.save(image_path, "JPEG", quality=100, exif=piexif.dump(exif_dict))
            else:
                flipped_img.save(image_path, "JPEG", quality=100)

            print(f"The image was flipped and saved as {image_path}")
        else:
            print(f"The image was not upside down. Saved as {image_path}")

        image.close()

        return image_path

    def resize_image_to_half(self, image_path, output_path):
        image = cv2.imread(image_path)
        height, width, _ = image.shape
        width_half = width // 2
        height_half = height // 2
        return self.resize_image_v2(
            image_path,
            self.build_file_path(image_path, output_path, width_half, height_half, '.half'),
            (width_half, height_half),
            True
        )

    def resize_image_to_half_v2(self, image_path):
        image = cv2.imread(image_path)
        height, width, _ = image.shape
        width_half = width // 2
        height_half = height // 2
        """
         return self.resize_image_v2(
            image_path,
            self.build_file_path(image_path, output_path, width_half, height_half, '.half'),
            (width_half, height_half),
            True
        )
        """
        return self.resize_image_v2(
            image_path,
            image_path,
            (width_half, height_half),
            True
        )

    def resize_image_v2(self, image_path, out_path, resize_to, out_path_is_file=False):
        """

        Redimensiona una imagen a un nuevo tamaño.

        :param image_path: Ruta al archivo de la imagen a redimensionar
        :param out_path: Ruta al archivo de salida o directorio
        :param resize_to: Procentaje a redimensionar ("porcentaje%") o en pixeles
        :param out_path_is_file: True si out_path es un fichero, False si es un directorio
        """

        try:
            im = Image.open(image_path)
            _, ext = os.path.splitext(image_path)
            if out_path_is_file:
                resized_image_path = out_path
            else:
                resized_image_path = os.path.join(out_path, os.path.basename(image_path))

            print(f"Resizing {image_path} to {resized_image_path}")

            resized_width = resize_to[0]
            resized_height = resize_to[1]

            im.thumbnail((resized_width, resized_height), Image.LANCZOS)

            driver = ext[1:].upper()
            if driver == 'JPG':
                driver = 'JPEG'

            if 'exif' in im.info:
                exif_dict = piexif.load(im.info['exif'])
                exif_dict['Exif'][piexif.ExifIFD.PixelXDimension] = resized_width
                exif_dict['Exif'][piexif.ExifIFD.PixelYDimension] = resized_height

                # https://github.com/hMatoba/Piexif/issues/95
                exif_dict['Exif'][41729] = b'1'

                im.save(resized_image_path, driver, exif=piexif.dump(exif_dict), quality=100)
            else:
                im.save(resized_image_path, driver, quality=100)

            im.close()

            print("{} ({}x{}) --> {} ({}x{})".format(image_path, resized_width, resized_height, resized_image_path, resized_width,
                                                     resized_height))

            return resized_image_path
        except (IOError, ValueError) as e:
            im.close()
            print("Error: No se pudo redimensionar {}: {}.".format(image_path, str(e)))

        return None
