# Calibre Automations Java
Equivalient of calibre-automations in Python.

Replaces the `readorder` tag with the values of the custom field.

## TODO
- [ ] Implement update and deletion of custom fields, like in Python.

## Building and Running
Build using:
```bash
./gradlew build
```

Build the jar file in `build/libs` using:
```bash
./gradlew shadowJar
```

Execute the jar file using:
```bash
java -jar build/libs/calibreUpdateReadorder-1.0-SNAPSHOT.jar --dry-run
```