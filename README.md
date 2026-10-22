# GeoRegistro

Aplicación Android (Java) para registrar incidencias georreferenciadas en terreno, consultar su historial y verlas en un mapa. Evaluación formativa de Taller de Nuevas Tecnologías, IP Virginio Gómez.

## Funcionalidades

| Requisito | Implementación |
|-----------|----------------|
| RF01 Navegación | `MainActivity` con acceso a registro, listado, mapa y sensor (Activities) |
| RF02 Formulario | `RegistroActivity`: título, descripción, categoría, fecha/hora y coordenadas, con validación de obligatorios |
| RF03 Firestore | Guardado y listado persistente (`ListadoActivity`); los errores de red se muestran con un aviso sin cerrar la app |
| RF04 GPS | Permiso en tiempo de ejecución, latitud/longitud actual, manejo de permiso denegado (se pueden ingresar coordenadas a mano) |
| RF05 Google Maps | `MapaActivity` muestra un marcador por cada incidencia guardada |
| RF06 Sensor | `SensorActivity` usa el acelerómetro, muestra X/Y/Z y detecta movimiento; avisa si el dispositivo no tiene sensor |
| RF08 Seguridad | Secretos fuera del repositorio y reglas de Firestore restringidas (`firestore.rules`) |

## Requisitos

- Android Studio Koala (2024.1.1) o superior
- JDK 17 (incluido en Android Studio)
- Emulador o teléfono con Android 7.0 (API 24) o superior, con Google Play Services
- Cuenta de Google para Firebase y Google Cloud

## Instrucciones de ejecución

1. Clonar el repositorio y abrir la carpeta en Android Studio.
2. Esperar la sincronización de Gradle.
3. Configurar Firebase y Google Maps (siguiente sección).
4. Elegir un emulador o dispositivo y presionar **Run**.

## Configuración de Firebase

1. En [Firebase Console](https://console.firebase.google.com) crear un proyecto.
2. Agregar una app Android con el paquete `cl.ipvg.georegistro`.
3. Descargar `google-services.json` y guardarlo en `app/google-services.json`. Está en `.gitignore`: **no se sube al repositorio**.
4. Crear la base de datos **Cloud Firestore** (modo producción).
5. En la pestaña *Reglas* pegar el contenido de `firestore.rules` y publicar. Las reglas solo permiten leer y crear incidencias con la estructura esperada; no permiten modificar ni borrar, y el resto queda denegado.

## Configuración de Google Maps (sin publicar la clave)

1. En [Google Cloud Console](https://console.cloud.google.com) habilitar **Maps SDK for Android** en el mismo proyecto.
2. Crear una clave de API y restringirla a aplicaciones Android (paquete `cl.ipvg.georegistro` + huella SHA-1 de depuración).
3. Copiar `local.properties.example` como `local.properties` y completar:
   ```
   MAPS_API_KEY=tu_clave
   ```
   `local.properties` está en `.gitignore`. Gradle inyecta la clave en el manifest mediante `manifestPlaceholders`, así que nunca queda escrita en el código.

## Pruebas manuales

1. **Validación:** en *Registrar incidencia* presionar Guardar con campos vacíos; deben aparecer errores en título, descripción y coordenadas.
2. **GPS:** presionar *Obtener ubicación actual*, aceptar el permiso y comprobar que se llenan latitud y longitud (en el emulador: Extended controls → Location). Repetir denegando el permiso: debe aparecer el aviso y poder ingresar coordenadas a mano.
3. **Firestore:** guardar una incidencia y verla en *Ver historial*; cerrar y reabrir la app y confirmar que persiste.
4. **Error de red:** activar modo avión y abrir *Ver historial*; debe mostrarse el aviso sin cerrar la app.
5. **Mapa:** abrir *Ver mapa* y comprobar que aparece un marcador por incidencia.
6. **Sensor:** abrir *Detector de movimiento* y mover el dispositivo (emulador: Virtual sensors → Move); los valores cambian y el estado indica movimiento. En un dispositivo sin acelerómetro se muestra un aviso.

## Estructura

```
app/src/main/java/cl/ipvg/georegistro/
  MainActivity.java      Menú principal
  RegistroActivity.java  Formulario + GPS + guardado
  ListadoActivity.java   Historial desde Firestore
  MapaActivity.java      Mapa con marcadores
  SensorActivity.java    Acelerómetro
  Incidencia.java        Modelo de datos
firestore.rules          Reglas de seguridad de Firestore
DECLARACION_IA.md        Declaración de uso de IA
```

## Autor

Joaquín Ponce (Limon)
