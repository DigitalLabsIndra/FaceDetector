# FaceDetector

Aunque sea matar moscas a cañonazos, se ha utilizado javacv (basado en javacpp) para obtener las librerías
de OpenCV en modo nativo. Aparte de que incluyen accesos propios a OpenCV que pueden ser de interés,
incluyen los compilados nativos para varios entornos (Linux, Mac, Win) incluyendo los módulos contrib
(mientras que la oficial sólo incluye algunos módulos).

Esto afecta al tiempo de lanzamiento al tener que cargar una gran variedad de clases. Para otro momento las
optimizaciones (¿construirlo para graalvm?).

 ## Launcher

Para lanzarlo se necesitan las librerías de JavaFX 14 o superior. Las librerías están preparadas para ser
integradas como módulos con jlink. Para evitarse esto en el modo de desarrollo se pueden descargar el SDK de
JavaFX (ver https://gluonhq.com/products/javafx/) y linkar los jar de la carpeta lib en el launcher del
proyecto (de la clase es.indra.dlabs.dsesteban.detector.cdi.Launcher) añadiendo:

```
--module-path "${PATH_TO_FX}\lib" --add-modules javafx.controls,javafx.fxml,javafx.swing
```
