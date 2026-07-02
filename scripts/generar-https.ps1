# =============================================================================
#  Stockly — generar certificado HTTPS self-signed (para la cámara del teléfono)
# -----------------------------------------------------------------------------
#  getUserMedia (la cámara) solo funciona en HTTPS o localhost. Para escanear
#  desde el celular por la red WiFi, la app debe servirse por HTTPS. Este script
#  crea un keystore PKCS12 self-signed con keytool (incluido en el JDK).
#
#  Uso:   powershell -ExecutionPolicy Bypass -File scripts\generar-https.ps1
#         (opcional)  -DataDir "C:\ruta\a\datos"   -IpLan "192.168.1.50"
#
#  Después arranca la app con:  --spring.profiles.active=https
#  y abre en el teléfono:       https://<IP-LAN>:8443
# =============================================================================
param(
  [string]$DataDir = "./data",
  [string]$IpLan = ""
)

$ErrorActionPreference = "Stop"

# Localizar keytool (preferir el del JAVA_HOME)
$keytool = "keytool"
if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME "bin\keytool.exe"))) {
  $keytool = Join-Path $env:JAVA_HOME "bin\keytool.exe"
}

New-Item -ItemType Directory -Force -Path $DataDir | Out-Null
$ks = Join-Path $DataDir "stockly-https.p12"

if (Test-Path $ks) {
  Write-Host "Ya existe un keystore en $ks (no se regenera). Bórralo si quieres uno nuevo." -ForegroundColor Yellow
  exit 0
}

# SAN: localhost + 127.0.0.1, y la IP LAN si la pasas (reduce avisos del navegador)
$san = "dns:localhost,ip:127.0.0.1"
if ($IpLan -ne "") { $san += ",ip:$IpLan" }

& $keytool -genkeypair -alias stockly -keyalg RSA -keysize 2048 -validity 3650 `
  -storetype PKCS12 -keystore $ks -storepass stockly -keypass stockly `
  -dname "CN=Stockly, OU=NemelesRP, O=Nemeles, C=MX" `
  -ext "SAN=$san"

Write-Host ""
Write-Host "OK -> $ks" -ForegroundColor Green
Write-Host "Arranca:  java -jar stockly.jar --spring.profiles.active=https --app.data.dir=`"$DataDir`""
Write-Host "Teléfono: https://<IP-LAN>:8443  (acepta el aviso de certificado una vez)"
