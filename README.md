# FaceDetector

Para poner en marcha el proyecto es necesario descargarse la librería OpenCV para el sistema operativo que
corresponda [en la página de releases de GitHub de OpenCV](https://github.com/opencv/opencv/releases) y:

 - Copiar el fichero jar en la carpeta libs/ (corrigiendo el nombre indicado en el pom si es necesario)
 - Poner la dll en una ruta consistente con java.library.path. Una manera rápida de hacer esto es ponerla
 en el raíz del proyecto (por defecto la carpeta "." se incluye en java.library.path) y utilizarlo como
 workingdir cuando se lance la aplicación
 
 En el caso de windows, versión 4.3.0 esto sería [el enlace de GitHub opencv-4.3.0-vc14_vc15.exe](https://github.com/opencv/opencv/releases/download/4.3.0/opencv-4.3.0-vc14_vc15.exe). Este fichero es un
 autocomprimido, se puede abrir con 7zip directamente y extraer los dos ficheros necesarios de
 opencv/build/java.

 ## Launcher

Para lanzarlo se necesitan las librerías de JavaFX 14 o superior. Las librerías están preparadas para ser
integradas como módulos con jlink. Para evitarse esto en el modo de desarrollo se pueden descargar el SDK de
JavaFX (ver https://gluonhq.com/products/javafx/) y linkar los jar de la carpeta lib en el launcher del
proyecto (de la clase es.indra.dlabs.dsesteban.detector.cdi.Launcher) añadiendo:

```
--module-path "${PATH_TO_FX}\lib" --add-modules javafx.controls,javafx.fxml,javafx.swing
```
