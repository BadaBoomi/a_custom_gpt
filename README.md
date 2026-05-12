# a_custom_gpt

## Projektbeschreibung

a_custom_gpt ist eine anpassbare Android-App, die als Client für GPT-basierte KI-Assistenten dient. Sie ermöglicht es, verschiedene Assistenten-IDs und API-Keys zu hinterlegen und bietet eine übersichtliche Chat- und Raumverwaltung.

## Features

- Unterstützung für mehrere Chat-Räume
- Verwaltung von Konversationen
- Setup-Dialog für API-Key und Assistant-ID
- Moderne UI mit Jetpack Compose
- Hilt für Dependency Injection
- Navigation zwischen Setup, Raum-, Chat- und Konversationsansicht

## Inhaltsverzeichnis

1. [Projektbeschreibung](#projektbeschreibung)
2. [Features](#features)
3. [Inhaltsverzeichnis](#inhaltsverzeichnis)
4. [Installation](#installation)
5. [Authentisierung](#authentisierung)
6. [Konfiguration von Starters](#konfiguration-von-starters)
7. [Nutzung](#nutzung)
8. [Lizenz](#lizenz)

## Installation

1. Repository klonen:
	```bash
	git clone https://github.com/DeinBenutzername/a_custom_gpt.git
	```
2. In Android Studio öffnen.
3. Abhängigkeiten synchronisieren lassen.
4. App auf ein Gerät oder Emulator ausführen.

## Authentisierung

Die App führt zwei Authentisierungsmethoden für Assistenten aus:

### 1. Direkte API-Key & Assistant-ID
Die primäre Authentisierungsmethode erfolgt über:
- **API-Key**: OpenAI API-Key für Authentifizierung bei der OpenAI API
- **PROMPT-ID**: Die ID des konfigurierten OpenAI Prompts
- **VECTORS-TORE-IDS**: Komma getrennte Liste von OpenAI Vector-Stores

Diese Werte werden im Setup-Dialog eingegeben und lokal verschlüsselt gespeichert.

### 2. User Authentisierung (auth.md)
Zusätzlich externe Authentisierungskonfiguration  über eine `auth.md`-Datei im Assistant definiert. Hier wird die e-mail Adresse des angemeldeten Google-Kontos gegen eine auf Assistenten Seite gehaltene Tabelle verifiziert:

**Beispiel auth.md:**
```markdown
# Authentisierungskonfiguration
|Unternehmen|Benutzer|
|---|---|
|-| norbert.nuetzlich@test.de|
```


## Konfiguration von Starters

Starters sind vordefinierte Prompt-Vorschläge, die in Konversationen schnell verfügbar sind. Sie werden im Assistenten über eine `starters.md`-Datei definiert und automatisch von der App geladen.

### Starters.md Dateistruktur

**Beispiel starters.md:**
```markdown
| Zweck | Prompt |
|-------|--------|
| Frage nach einer berühmten Persönlichkeit | Wer war eigentlich |
| Rechenaufgabe | Wieviel ist |
| Humor | Erzähle einen Witz über |
| Codierung | Schreib einen Python-Code für |
| Zusammenfassung | Fasse zusammen: |
```

### Spalten:
- **Zweck**: Kurze Beschreibung des Starters (wird in der UI angezeigt)
- **Prompt**: Der tatsächliche Prompt-Text (kann unvollständig sein oder mit Platzhaltern)

### Verwendung in der App

Nach dem Speichern der Konfiguration lädt die App die Starters automatisch:
1. Die App ruft `GET_CONFIGURATION` auf den Assistenten auf
2. Die `starters.md` wird geparst und gespeichert
3. In jeder Konversation werden die Starters als anklickbare Bubbles angezeigt
4. Beim Klick wird der Prompt in das Eingabefeld eingefügt und kann editiert werden

## Nutzung

### Erste Schritte
1. Beim ersten Start API-Key und Assistant-ID im Setup-Dialog eingeben (Endbenutzer)
2. Optional: Authentisierungs- und Starters-Konfiguration im Assistant definieren (auth.md, starters.md durch Administrator des Assistenten)


### Arbeitsablauf (Endbenutzer)
1. **Räume verwalten**: Neue Räume erstellen oder vorhandene auswählen
2. **Chats erstellen**: Innerhalb eines Raumes neue Chats für verschiedene Themen
3. **Mit Starters arbeiten**: In der Konversation auf Starter-Bubbles klicken zum schnellen Prompt-Einfügen
4. **Konversieren**: Prompts bearbeiten und Nachrichten an den Assistenten senden
5. **Raumname anzeigen**: Der Raumname ist in der Chat-Übersicht und in jeder Konversation sichtbar

### Chat-Verwaltung
- **Chats umbenennen**: Mit dem Bearbeiten-Icon
- **Chats löschen**: Mit dem Löschen-Icon
- **Chats verschieben**: Chats zwischen Räumen mit dem Pfeil-Icon verschieben

## Konfiguration

Die App kann über eine Umgebungsdatei `.env` konfiguriert werden. Die wichtigsten Einstellungen sind:

| Variable              | Beschreibung                                                                 | Beispielwert                |
|-----------------------|------------------------------------------------------------------------------|-----------------------------|
| `TOOLS`               | Aktivierte Tools, durch Kommas getrennt                                      | `web_search,calculator`     |
| `LOG_MODEL_PAYLOAD`   | Logging der Modell-Payload (true/false)                                      | `false`                     |
| `LOG_LEVEL`           | Logging-Level für Debug-Ausgaben (`DEBUG`, `INFO`, `WARN`, `ERROR`)          | `ERROR`                     |

**Beispiel `.env`:**

```env
TOOLS=web_search
LOG_MODEL_PAYLOAD=false
LOG_LEVEL=ERROR
```

**Hinweise:**
- Mehrere Tools können durch Kommas getrennt werden.
- Die `.env`-Datei befindet sich im Projektverzeichnis und wird beim Start der App eingelesen.

## Lizenz

Diese Software steht unter der MIT License. Siehe [LICENSE](LICENSE) für weitere Informationen.