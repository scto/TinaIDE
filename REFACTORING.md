# MobileIDE Projekt-Refaktorierungsbericht

Dieses Dokument beschreibt die im gesamten Projekt durchgeführten Refaktorierungsmaßnahmen zur Umstellung des Paketnamens, Projektnamens, der Lokalisierungen und der Autoreninformationen.

---

## Durchgeführte Änderungen

### 1. Paketname & Import-Aktualisierungen
* **Refaktoriertes Paket**: `com.wuxianggujun.tinaide` wurde durch `com.scto.mobileide` ersetzt.
* **Importe & Quelldateien**: Alle Kotlin-, Java-, XML-, Gradle- und Manifest-Dateien wurden dahingehend angepasst.
* **Physische Ordnerstrukturen**:
  * Die Ordnerstrukturen wurden von `com/wuxianggujun/tinaide` nach `com/scto/mobileide` verschoben.
  * In den APK-Vorlagen (`tools/template-*`) wurden die Paketpfade von `com/tinaide` nach `com/mobileide` verschoben.

### 2. Projektumbenennung & Namenskonventionen ("Tina" -> "Mobile")
* **Projektname**: In der `settings.gradle.kts` wurde `rootProject.name` von `TinaIDE` auf `MobileIDE` geändert.
* **Dateien, Module und Ordner**: Alle Dateien und Unterordner, deren Namen mit `tina` begannen, wurden auf `mobile` geändert.
  * **Module**: `tina-exec` -> `mobile-exec`, `tina-android-tree-sitter` -> `mobile-android-tree-sitter`.
  * **Klassen & Interfaces**: `TinaApplication` -> `MobileApplication`, `TinaIDETheme` -> `MobileIDETheme`, etc.
  * **Assets & Skripte**: `tina-toolchain` -> `mobile-toolchain`, `tinaide.cpp.snippets` -> `mobileide.cpp.snippets` usw.
  * **Plugins**: Dateiendungen von `.tinaplug` wurden in `.mobileplug` umbenannt.

### 3. Anpassung der Internationalisierung (Res-Layout)
* In allen Modulen, die sowohl `values` (bisher mit chinesischen Texten) als auch `values-en` (mit englischen Texten) enthielten (wie `core/editor-view`, `tools/template-common`, `tools/template-terminal`), wurden die Verzeichnisse wie folgt getauscht:
  * `values` (Chinesisch) -> `values-zh`
  * `values-en` (Englisch) -> `values` (als neuer Standard-Fallback)

### 4. Autoren- & Kontaktdaten
* Der Urheber/Autor `wuxianggujun` wurde in Urheberrechtshinweisen (wie der `LICENSE`), Dokumentationen (`README.md`) und Skripten durch `Thomas Schmid` ersetzt.
* Die Platzhalter- oder Standard-E-Mails wurden auf `tschmid35@gmail.com` aktualisiert.

---

## Verifizierung & Build-Status
Die Integrität des Projekts wurde erfolgreich geprüft. Nach der Behebung vereinzelter Over-Replacements (z. B. fälschliche Ersetzungen des Wortes `destination` zu `desmobiletion` aufgrund der Teilübereinstimmung mit `tina`) kompiliert das Projekt einwandfrei:

```bash
./gradlew :app:compileArm64DebugKotlin
# BUILD SUCCESSFUL
```
