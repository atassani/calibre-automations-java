# Calibre Automations Java

Replaces the `readorder` tag with the values of the custom field. The custom field takes precedence over the tag. If custom field is 0.0, it will be cleared.

Updates the tile to reflect is an audiobook if contains the tag `format:audiobook`. "Title (audiobook): subtitle"

## Building and Running
Build using:
```bash
./gradlew build
```

Execute the jar file using:
```bash
java -jar build/libs/calibreUpdater-1.0.jar --dry-run
```

```
usage: CalibreUpdater
 -a,--audiobook      Process audiobooks
 -d,--dry-run        Run the updater in dry-run mode
 -r,--readorder      Process read order
    --read-order     Process read order
    --readOrder      Process read order

```