"""
Script en Python para realizar tareas de inferencia en un mapa personalizado utilizando el API del simulador CARLA utilizando el modelo YOLOv8.
El mapa ha sido personalizado con señales de tráfico para realizar la detección de objetos.
El objetivo de este script es realizar la inferencia automatizados de objetos en un entorno simulado utilizando el simulador CARLA y modelos de YOLO.

El script realiza las siguientes tareas:

1. Carga un modelo YOLOv8 pre-entrenado.
2. Carga un mapa personalizado en el simulador CARLA.
3. Carga un vehículo en el mapa.
4. Carga una cámara RGB en el vehículo.
5. Realiza la inferencia de objetos en cada frame de la cámara RGB.
6. Dibuja los objetos detectados en el frame de la cámara RGB.
7. Guarda el frame de la cámara RGB con los objetos detectados.
8. Muestra la velocidad, ubicación y FPS del vehículo en la pantalla.
9. Muestra la fecha y hora actual en la pantalla.
10. Muestra un mensaje de autoría en la pantalla.

Código fuente adaptado de https://github.com/carla-simulator/carla/tree/0.9.15.2/PythonAPI/examples

author: Alvaro Zambrana Sejas
email: azambrana777@gmail.com

"""
import datetime

import carla
import pygame
import numpy as np
import random
import os
import cv2
from ultralytics import YOLO
import faulthandler
import torch

# Initialize pygame
pygame.init()
display_width = 1280  # Screen width
display_height = 720  # Screen height
display = pygame.display.set_mode((display_width, display_height), pygame.HWSURFACE | pygame.DOUBLEBUF)
font_size = 12
font = pygame.font.SysFont('Arial', font_size)
frames = 0
# Set the target FPS
TARGET_FPS = 30
autopilot = True
vehicle = None

# Global variables
captured_image = None
watermark_image_path = 'assets/direccion_posgrado_fcyt2.png'

model = YOLO("../object-detection-models/cbba_yolov8_model.pt")  # Load YOLOv8 model Copiloto Virtual
model.to('cuda')

text_color = (255, 255, 255)
save_current_frame = False

print(torch.cuda.get_device_name())

def process_img(image):
    """
    Process the camera image, run YOLOv8 detection, and render it in pygame.
    """
    global captured_image
    global frames
    global autopilot
    global save_current_frame

    frames += 1

    captured_image = image  # Store the current image

    # Convert CARLA image to numpy array
    array = np.frombuffer(image.raw_data, dtype=np.uint8)
    array = array.reshape((image.height, image.width, 4))[:, :, :3]  # Remove alpha channel
    frame = cv2.cvtColor(array, cv2.COLOR_BGR2RGB)  # Convert to RGB for YOLO

    if frames % 3 == 0:
        # Run YOLOv8 detection
        results = model.predict(frame, save=True, save_crop=True, conf=0.5, imgsz=[display_width, display_height], device=1)

        # save frame with detections
        detections = results[0].boxes.data.cpu().numpy()  # Extract detection boxes

        # Draw detections on frame
        for det in detections:
            x1, y1, x2, y2, conf, cls = det
            label = f"{model.names[int(cls)]} {conf:.2f}"
            cv2.rectangle(frame, (int(x1), int(y1)), (int(x2), int(y2)), (0, 255, 0), 2)
            cv2.putText(frame, label, (int(x1), int(y1) - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 2)

        if detections.any():
            # Save the annotated frame
            filename = f"captured_images/frame_{image.frame}.png"
            cv2.imwrite(filename, cv2.cvtColor(frame, cv2.COLOR_RGB2BGR))
            print(f"Image saved: {filename}")
            save_current_frame = True

    add_image_watermark(frame, watermark_image_path)

    surface = pygame.surfarray.make_surface(frame.swapaxes(0, 1))
    display.blit(surface, (0, 0))

    autor = font.render("POSTULANTE: ALVARO ZAMBRANA SEJAS", 0, text_color)
    tutor = font.render("TUTOR:  M.SC. ING. DANNY LUIS HUANCA SEVILLA", 0, text_color)
    diplomado = font.render("DIPLOMADO - ESTADÍSTICA APLICADA A LA TOMA DE DECISIONES 3ra VERSIÓN - UMSS", 0, text_color)
    ciudad = font.render("Cochabamba - Bolivia",0, text_color)

    display.blit(autor, (10, font_size * 4 + font_size/2))
    display.blit(tutor, (10, font_size * 5 + font_size/2))
    display.blit(diplomado, (10, font_size * 6 + font_size/2))
    display.blit(ciudad, (10, font_size * 7 + font_size/2))

def save_image(image):
    """
    Save the current image to disk.
    """
    filename = f"captured_images/frame_{image.frame}.png"
    image.save_to_disk(filename)
    print(f"Image saved: {filename}")

def draw_stats(vehicle, clock):
    """
    Display vehicle stats such as speed, location, and FPS on the screen.
    """
    # Get vehicle velocity
    velocity = vehicle.get_velocity()
    speed = 3.6 * (velocity.x**2 + velocity.y**2 + velocity.z**2)**0.5  # Convert m/s to km/h

    # Get vehicle location
    location = vehicle.get_location()
    location_str = f"Ubicación: X={location.x:.2f}, Y={location.y:.2f}, Z={location.z:.2f}"

    # Get FPS
    fps = clock.get_fps()

    # Get current date
    current_date = font.render(f'Fecha: {datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")}', 0, text_color)

    # Render stats on the pygame screen
    speed_text = font.render(f"Velocidad: {speed:.2f} km/h", True, text_color)
    location_text = font.render(location_str, True, text_color)
    fps_text = font.render(f"FPS: {fps:.2f}", True, text_color)

    # Display on screen
    display.blit(current_date, (10, display_height - font_size - 10))
    display.blit(speed_text, (10, font_size * 1 + font_size/2))
    display.blit(location_text, (10, font_size * 2 + font_size/2))
    display.blit(fps_text, (10, font_size * 3 + font_size/2))

def main():
    global captured_image, save_current_frame
    global TARGET_FPS
    global autopilot
    global vehicle

    client = carla.Client('localhost', 2000)
    client.set_timeout(10.0)

    world = client.get_world()

    blueprint_library = world.get_blueprint_library()

    # Spawn a vehicle
    vehicle_bp = blueprint_library.filter('vehicle.tesla.model3')[0]
    spawn_points = world.get_map().get_spawn_points()
    spawn_point = random.choice(spawn_points)
    vehicle = world.try_spawn_actor(vehicle_bp, spawn_point)

    # set the vehicle's autopilot
    vehicle.set_autopilot(autopilot)

    if not vehicle:
        print("Failed to spawn vehicle. Please try again.")
        return

    # Attach an RGB camera
    camera_bp = blueprint_library.find('sensor.camera.rgb')
    camera_bp.set_attribute('image_size_x', str(display_width))
    camera_bp.set_attribute('image_size_y', str(display_height))
    camera_bp.set_attribute('fov', '110')
    camera_transform = carla.Transform(carla.Location(x=1.5, z=2.4))
    camera = world.spawn_actor(camera_bp, camera_transform, attach_to=vehicle)

    # Set up spectator view
    spectator = world.get_spectator()
    spectator_transform = vehicle.get_transform()
    spectator.set_transform(carla.Transform(spectator_transform.location + carla.Location(z=20), carla.Rotation(pitch=-90)))

    # Listen to the camera
    camera.listen(lambda image: process_img(image))

    # Clock for FPS calculation
    clock = pygame.time.Clock()

    try:
        print("Press SPACE to capture an image. Press ESC to quit.")
        while True:
            # Tick the world
            world.tick()

            # Process pygame events
            for event in pygame.event.get():
                if event.type == pygame.QUIT:
                    return

                # Handle keypress events
                if event.type == pygame.KEYDOWN:
                    if event.key == pygame.K_SPACE:  # SPACE to capture an image
                        filename = f"captured_images/screenshot_{frames}.png"
                        pygame.image.save(display, filename)
                        # if captured_image:
                        #    save_image(captured_image)
                    elif event.key == pygame.K_ESCAPE:  # ESC to quit
                        return

            # Display stats
            draw_stats(vehicle, clock)
            """
            if save_current_frame:
                # save current display frame
                filename = f"captured_images/screenshot_{frames}.png"
                pygame.image.save(display, filename)
                print(f"Image saved: {filename}")
            save_current_frame = False
            """
            # Update pygame display and tick clock
            pygame.display.flip()
            clock.tick(TARGET_FPS)
    except KeyboardInterrupt:
        print("Simulation stopped.")
    finally:
        # Clean up
        camera.stop()
        camera.destroy()
        vehicle.destroy()
        pygame.quit()
        print("Actors destroyed and pygame closed.")

def add_image_watermark(frame, watermark_image_path, position=(500, 10), alpha=0.2):
    global display_width

    # Read the watermark image
    watermark = cv2.imread(watermark_image_path, cv2.IMREAD_UNCHANGED)

    # Ensure the watermark has an alpha channel (transparency)
    if watermark.shape[2] != 4:
        raise ValueError("The watermark image must have an alpha channel (RGBA).")

    # Resize the watermark if needed
    scale = 0.33  # Scale factor for the watermark
    watermark = cv2.resize(watermark, None, fx=scale, fy=scale, interpolation=cv2.INTER_AREA)
    # change color of the watermark
    watermark = cv2.cvtColor(watermark, cv2.COLOR_BGRA2RGBA)

    # Get dimensions
    h_wm, w_wm, _ = watermark.shape

    # Update the position of the watermark
    position = (display_width - w_wm - 10, position[1])

    x, y = position

    # Get the region of interest (ROI) from the frame where the watermark will be placed
    roi = frame[y:y+h_wm, x:x+w_wm]

    # Split the watermark into its color and alpha channels
    watermark_rgb = watermark[:, :, :3]
    watermark_alpha = watermark[:, :, 3] / 255.0  # Normalize alpha to 0-1

    # Blend the watermark with the ROI
    for c in range(3):  # Loop over RGB channels
       roi[:, :, c] = (1 - watermark_alpha) * roi[:, :, c] + watermark_alpha * watermark_rgb[:, :, c]

    # Replace the ROI on the frame
    frame[y:y+h_wm, x:x+w_wm] = roi
    return frame

if __name__ == "__main__":
    faulthandler.enable()
    main()
