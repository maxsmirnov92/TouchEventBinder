# TouchEventBinder
Desktop utility to bind specified system keys to (x,y) screen coordinates on Android device (available via ADB) real-time. Uses JavaFX.  

### Launch:
```
java --module-path PATH_TO_YOUR_JAVAFX_LIB_FOLDER --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.web -jar yourJar.jar
```
for example:
```
java --module-path "f:\Shared Program Data\javafx-sdk-11.0.2\lib" --add-modules=javafx.controls,javafx.fxml -jar out\artifacts\toucheventbinder\toucheventbinder.jar
```

### Usage:
Load JSON config file from any available path. It should contain mapping: key code and preferences for this key.
It consists of:
```
1. [keyEventType]: 3 types PRESSED, PRESSING, RELEASED - it declares when sending adb command should trigger
2. [interval]: if previous was specified as PRESSING, then it points to interval that triggers that state after user pressed key and before he released it
3. [touchPosition]: object with x,y fields: position, in which tap should trigger to
```

Key watcher starts automatically when app launched. If intend to reload config by default path ("configs/key_config.json") or change to another, then select appropriate MenuItem.

After just press any keys, when app window is focused.
