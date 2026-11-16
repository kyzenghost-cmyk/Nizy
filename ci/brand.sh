#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../engine/CloniX"
python3 - <<'PY'
from pathlib import Path
p = Path("app/src/main/res/values/strings.xml")
s = p.read_text()
replacements = {
    "<string name=\"app_name\">CloniX</string>": "<string name=\"app_name\">CLONE X</string>",
    "<string name=\"app_title\">CloniX</string>": "<string name=\"app_title\">CLONE X</string>",
    "True multi-instance app cloning with distinct launcher icons and independent data storage.": "Duplique aplicativos em instalações independentes.",
    "Installed apps": "Aplicativos instalados",
    "Search installed apps": "Buscar aplicativos instalados",
    "No matching apps found.": "Nenhum aplicativo encontrado.",
    "Loading apps…": "Carregando aplicativos…",
    "<string name=\"nav_apps\">Apps</string>": "<string name=\"nav_apps\">Aplicativos</string>",
    "<string name=\"nav_clones\">Clones</string>": "<string name=\"nav_clones\">Cópias</string>",
    "<string name=\"nav_settings\">Settings</string>": "<string name=\"nav_settings\">Ajustes</string>",
    "Your clones": "Suas cópias",
    "No Clones Created Yet": "Nenhuma cópia criada",
    "Browse Apps": "Ver aplicativos",
    "Start Cloning": "CRIAR CÓPIAS",
    "Number of clones": "Quantidade de cópias",
    "Add number to App Name": "Numerar o nome do aplicativo",
    "Add number to Icon Badge": "Mostrar número no ícone",
    "Hue-shift icon colors": "Variar aparência dos ícones",
    "Launch": "Abrir",
    "Uninstall": "Desinstalar",
    "Settings": "Ajustes",
    "Cloning in progress": "Criando cópia",
    "Clone installed: %1$s": "Cópia instalada: %1$s",
    "About CloniX": "Sobre o CLONE X",
    "CloniX v1.0.0 Pro": "CLONE X · Edição inicial",
}
for a,b in replacements.items():
    s=s.replace(a,b)
p.write_text(s)
p = Path("app/src/main/res/values/colors.xml")
s = p.read_text()
for a,b in {"#1B1F2E":"#000000","#B69CFF":"#FFFFFF","#28174A":"#000000","#151823":"#080808"}.items():
    s=s.replace(a,b)
p.write_text(s)
p = Path("app/src/main/java/dev/cloneforge/app/CloneForgeApp.kt")
s = p.read_text().replace("com.google.android.material.color.DynamicColors.applyToActivitiesIfAvailable(this)", "androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)")
p.write_text(s)
Path("app/src/main/res/values/themes.xml").write_text("""<resources>
    <style name="Theme.CloneForge" parent="Theme.Material3.DayNight.NoActionBar">
        <item name="colorPrimary">@color/cf_primary</item>
        <item name="colorOnPrimary">@color/cf_on_primary</item>
        <item name="colorSecondary">@color/cf_primary</item>
        <item name="colorOnSecondary">@color/cf_on_primary</item>
        <item name="colorSurface">#080808</item>
        <item name="colorOnSurface">#FFFFFF</item>
        <item name="colorBackground">#000000</item>
        <item name="colorOnBackground">#FFFFFF</item>
        <item name="android:statusBarColor">#000000</item>
        <item name="android:navigationBarColor">#000000</item>
        <item name="android:windowLightStatusBar">false</item>
    </style>
</resources>
""")
PY
