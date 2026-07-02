<#
  Genera un INSTALADOR de un solo archivo para Windows (Stockly-<version>.msi) con
  jpackage --type msi. El MSI instala Stockly con acceso directo + menu Inicio +
  entrada en "Agregar o quitar programas".

  Requisitos:
    - JDK 17 con jpackage (por defecto C:\jdk17\jdk-17.0.19+10).
    - WiX 3.14 (candle.exe/light.exe). Si no esta en el PATH, este script
      descarga los binarios PORTABLES de WiX (sin instalar, sin admin) a build\wix.
      OJO: WiX 3.x necesita que .NET Framework 3.5 este activado en el equipo.

  Uso:   powershell -ExecutionPolicy Bypass -File packaging\empaquetar-instalador.ps1
  Salida: dist\Stockly-<version>.msi
#>
param(
  [string]$JavaHome = 'C:\jdk17\jdk-17.0.19+10',
  [string]$AppVersion = '2.0.0'
)
$ErrorActionPreference = 'Stop'
$repo = Split-Path -Parent $PSScriptRoot
Set-Location $repo
$env:JAVA_HOME = $JavaHome

# --- WiX: usar el del PATH, o descargar los binarios portables ---
$candle = (Get-Command candle.exe -ErrorAction SilentlyContinue).Source
if (-not $candle) {
  $wixDir = "$repo\build\wix"
  if (-not (Test-Path "$wixDir\candle.exe")) {
    Write-Host '== Descargando WiX 3.14 (binarios portables) ==' -ForegroundColor Cyan
    New-Item -ItemType Directory -Force $wixDir | Out-Null
    $zip = "$repo\build\wix314-binaries.zip"
    Invoke-WebRequest -Uri 'https://github.com/wixtoolset/wix3/releases/download/wix3141rtm/wix314-binaries.zip' -OutFile $zip
    Expand-Archive -Path $zip -DestinationPath $wixDir -Force
  }
  $env:PATH = "$wixDir;$env:PATH"
}
$env:PATH = "$JavaHome\bin;$env:PATH"

Write-Host '== 1/3  Compilando el jar (mvnw package) ==' -ForegroundColor Cyan
& "$repo\mvnw.cmd" -B -ntp clean package
if ($LASTEXITCODE -ne 0) { throw 'Fallo el build de Maven' }

$jar = Get-ChildItem "$repo\target\inventario-api-*.jar" |
       Where-Object { $_.Name -notlike '*.original' } | Select-Object -First 1
$in = "$repo\build\msi-input"; $out = "$repo\dist"
Remove-Item -Recurse -Force $in -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force $in | Out-Null
Copy-Item $jar.FullName $in

Write-Host '== 2/3  jpackage --type msi (candle/light + JRE embebido) ==' -ForegroundColor Cyan
& "$JavaHome\bin\jpackage.exe" `
  --type msi `
  --name Stockly `
  --app-version $AppVersion `
  --vendor 'Nemeles' `
  --description 'Stockly - Control de inventario' `
  --input $in `
  --main-jar $jar.Name `
  --icon "$PSScriptRoot\stockly.ico" `
  --runtime-image "$JavaHome" `
  --java-options '-Dapp.desktop=true' `
  --java-options '-Dserver.port=18080' `
  --java-options '-Xss512k' `
  --win-console `
  --win-shortcut `
  --win-menu `
  --win-menu-group 'Stockly' `
  --win-dir-chooser `
  --dest $out
if ($LASTEXITCODE -ne 0) { throw 'Fallo jpackage --type msi' }

Write-Host '== 3/3  Listo ==' -ForegroundColor Cyan
Get-ChildItem "$out\Stockly-*.msi" | ForEach-Object {
  Write-Host ("INSTALADOR -> {0}  ({1:N1} MB)" -f $_.FullName, ($_.Length/1MB)) -ForegroundColor Green
}
