<#
  Empaqueta Stockly como app de escritorio para Windows (.exe con JRE embebido)
  usando jpackage --type app-image. NO requiere que el usuario final tenga Java,
  ni WiX (no genera instalador .msi, sino una carpeta portable con Stockly.exe).

  Uso:   powershell -ExecutionPolicy Bypass -File packaging\empaquetar-exe.ps1
  Requisitos: JDK 17 con jpackage (por defecto C:\jdk17\jdk-17.0.19+10).
  Salida: dist\Stockly\  (carpeta con Stockly.exe) + dist\Stockly-Windows.zip
#>
param(
  [string]$JavaHome = 'C:\jdk17\jdk-17.0.19+10',
  [string]$AppVersion = '2.0.0'
)
$ErrorActionPreference = 'Stop'
$repo = Split-Path -Parent $PSScriptRoot
Set-Location $repo
$env:JAVA_HOME = $JavaHome
$env:PATH = "$JavaHome\bin;$env:PATH"

Write-Host '== 1/4  Compilando el jar (mvnw package) ==' -ForegroundColor Cyan
& "$repo\mvnw.cmd" -B -ntp clean package
if ($LASTEXITCODE -ne 0) { throw 'Fallo el build de Maven' }

$jar = Get-ChildItem "$repo\target\inventario-api-*.jar" |
       Where-Object { $_.Name -notlike '*.original' } | Select-Object -First 1

Write-Host '== 2/4  Preparando carpeta de entrada ==' -ForegroundColor Cyan
$in = "$repo\build\jp-input"; $out = "$repo\dist"
Remove-Item -Recurse -Force $in, $out -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force $in | Out-Null
Copy-Item $jar.FullName $in

Write-Host '== 3/4  jpackage (app-image con JRE embebido) ==' -ForegroundColor Cyan
& "$JavaHome\bin\jpackage.exe" `
  --type app-image `
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
  --dest $out
if ($LASTEXITCODE -ne 0) { throw 'Fallo jpackage' }

Copy-Item "$PSScriptRoot\COMO USAR - LEEME.txt" "$out\Stockly\" -ErrorAction SilentlyContinue

Write-Host '== 4/4  Comprimiendo ZIP ==' -ForegroundColor Cyan
$zip = "$out\Stockly-Windows.zip"
Compress-Archive -Path "$out\Stockly" -DestinationPath $zip -CompressionLevel Optimal -Force
Write-Host ("LISTO -> {0}  ({1:N1} MB)" -f $zip, ((Get-Item $zip).Length/1MB)) -ForegroundColor Green
