# MinecraftServerProfiler

Ein leistungsstarkes, anpassbares Minecraft-Server-Leistungsprofiler-Plugin für Paper/Spigot-Server (1.21+). Erhalte detaillierte Einblicke in die Leistung deines Servers mit schöner, farbcodierter Ausgabe!

## Features

- **Echtzeit-Leistungsmetriken**: TPS, MSPT, CPU-Nutzung, Speichernutzung, Spieleranzahl, Tile-Entities und geladene Chunks pro Welt
- **Leistungsaufteilung**: Visualisiere, wohin die Ressourcen deines Servers gehen (Entities, Mob-KI, Wegfindung, Chunks, Hopper, Redstone, Scheduler, Plugins, Netzwerk und Garbage Collection)
- **Top-Elemente**: Identifiziere deine Top-Entity, Top-Chunk (nach Entity-Anzahl) und Top-Plugin
- **Grafische Benutzeroberfläche (GUI)**: Ein schönes Inventar-GUI für einfachen Zugriff auf alle Informationen
- **Historie**: Speichere automatisch Snapshots in regelmäßigen Abständen und vergleiche sie später
- **Exporte**: Exportiere Snapshots als JSON, CSV oder Markdown
- **Alarme**: Konfigurierbare Warnungen bei Leistungseinbußen
- **Discord-Webhooks**: Sende Alarme direkt an deinen Discord-Server
- **Vollständig anpassbar**: Ändere jede Farbe, jedes Gradient und jeden Schwellenwert nach deinen Wünschen via `config.yml`
- **MiniMessage-Unterstützung**: Nutze das MiniMessage-Format für alle Farben und Gradienten (unterstützt vordefinierte Farben, Hex-Farben, Gradienten und mehr)

## Installation

1. Lade die neueste Version des Plugins von der Releases-Seite herunter (kommt bald)
2. Platziere die `.jar`-Datei im `plugins/`-Verzeichnis deines Servers
3. Starte deinen Server neu oder lade ihn neu
4. Das Plugin generiert automatisch eine `config.yml`-Datei im `plugins/MinecraftServerProfiler/`-Verzeichnis

## Verwendung

Verwende den Befehl `/profiler` (oder `/profile`, `/sp` als Aliase), um das Leistungsprofil anzuzeigen!

### Befehle

| Befehl | Beschreibung |
|--------|--------------|
| `/profiler` | Zeigt die Leistungsinformationen an |
| `/profiler gui` | Öffnet das grafische Interface |
| `/profiler tptop` | Teleportiert zum Chunk mit den meisten Entities |
| `/profiler save` | Speichert den aktuellen Zustand als Snapshot |
| `/profiler export <json/csv/md> [id]` | Exportiert einen Snapshot (aktuell oder per ID) |
| `/profiler help` | Zeigt die Hilfe an |

### Berechtigungen

| Berechtigung | Beschreibung |
|--------------|--------------|
| `serverprofiler.use` | Erlaubt die Nutzung des `/profiler`-Befehls |

## Konfiguration

Das Plugin ist vollständig über die `config.yml`-Datei konfigurierbar. Hier sind die wichtigsten Abschnitte:

### Status-Farben (`status-colors`)
Ändere die Farben für "good", "moderate", "bad" und "critical":
```yaml
status-colors:
  good: "<green>"
  moderate: "<yellow>"
  bad: "<gold>"
  critical: "<red>"
```

### Text-Farben (`text-colors`)
Ändere die Farben für Labels, Highlights und "None"-Nachrichten:
```yaml
text-colors:
  label: "<dark_gray>"
  highlight: "<aqua>"
  none: "<gray>"
```

### Gradienten (`gradients`)
Ändere die Gradientenfarben für Abschnittsüberschriften:
```yaml
gradients:
  header: "<gradient:#1a73e8:#34a853>"
  system: "<gradient:#1a73e8:#34a853>"
  game: "<gradient:#34a853:#fbbc05>"
  worlds: "<gradient:#fbbc05:#ea4335>"
  breakdown: "<gradient:#ea4335:#1a73e8>"
  top: "<gradient:#fbbc05:#ea4335>"
```

### Schwellenwerte (`thresholds`)
Ändere die Schwellenwerte, bei denen ein Wert als "good", "moderate", "bad" oder "critical" gilt:
```yaml
thresholds:
  tps:
    good: 19.0
    moderate: 15.0
    bad: 10.0
  mspt:
    good: 30.0
    moderate: 40.0
    bad: 50.0
  percentage:
    good: 10.0
    moderate: 20.0
    bad: 30.0
  players:
    good: 50
    moderate: 100
    bad: 200
  tile-entities:
    good: 1000
    moderate: 5000
    bad: 20000
  chunks:
    good: 500
    moderate: 1500
    bad: 5000
  redstone:
    good: 500
    moderate: 2000
    bad: 5000
  ping:
    good: 50
    moderate: 100
    bad: 200
  memory:
    good: 50
    moderate: 75
    bad: 90
```

### GUI-Einstellungen (`gui`)
Konfiguriere das grafische Interface:
```yaml
gui:
  enabled: true
  title: "<gradient:#1a73e8:#34a853><bold>Server Profiler</bold></gradient>"
  size: 54
```

### Historieneinstellungen (`history`)
Konfiguriere die automatische Speicherung von Snapshots:
```yaml
history:
  enabled: true
  interval-seconds: 30
  max-snapshots: 100
  database-type: "sqlite"
  database-file: "history.db"
```

### Alarm-Einstellungen (`alerts`)
Konfiguriere Alarme und Discord-Webhooks:
```yaml
alerts:
  enabled: true
  list:
    - name: "low-tps"
      enabled: true
      type: "tps-below"
      threshold: 15
      message: "<red>[ServerProfiler] Warnung: TPS unter <threshold> gefallen! Aktuell: <value>"
      broadcast-to-ops: true
      broadcast-to-console: true
    - name: "high-mspt"
      enabled: true
      type: "mspt-above"
      threshold: 50
      message: "<red>[ServerProfiler] Warnung: MSPT über <threshold> gestiegen! Aktuell: <value>"
      broadcast-to-ops: true
      broadcast-to-console: true
    - name: "high-memory"
      enabled: true
      type: "memory-above-percent"
      threshold: 90
      message: "<red>[ServerProfiler] Warnung: Speichernutzung über <threshold>%! Aktuell: <value>%"
      broadcast-to-ops: true
      broadcast-to-console: true
  discord:
    enabled: false
    webhook-url: ""
    username: "ServerProfiler"
    avatar-url: ""
    embed-color: "#ff0000"
```

### Export-Einstellungen (`export`)
Konfiguriere die Export-Funktionalität:
```yaml
export:
  enabled: true
  default-format: "json"
  export-directory: "exports"
```

### Update-Prüfung (`update-check`)
Konfiguriere die automatische Update-Prüfung:
```yaml
update-check:
  enabled: true
  check-on-startup: true
  notify-ops-on-join: true
  repo-owner: "YOUR-USERNAME"
  repo-name: "MinecraftServerProfiler"
```

## Bauen aus dem Quellcode

1. Stelle sicher, dass du Java 21 und Maven installiert hast
2. Klone das Repository: `git clone https://github.com/RainbowFurry/MinecraftServerProfiler.git`
3. Navigiere zum Projektverzeichnis: `cd MinecraftServerProfiler`
4. Baue das Plugin: `mvn clean package`
5. Die kompilierte `.jar`-Datei befindet sich im `target/`-Verzeichnis

## Lizenz

Dieses Projekt steht unter der MIT-Lizenz (du kannst dies beliebig ändern).

## Mitwirken

Beiträge sind willkommen! Wenn du Ideen für Verbesserungen oder neue Funktionen hast, erstelle gerne ein Issue oder einen Pull Request.
