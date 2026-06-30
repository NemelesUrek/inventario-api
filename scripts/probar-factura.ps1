# =============================================================================
#  probar-factura.ps1 — Prueba el endpoint POST /api/facturas contra Facturama.
#
#  REQUISITO: la app debe estar CORRIENDO en otra ventana, levantada con tus
#  credenciales de Facturama en variables de entorno, por ejemplo:
#     $env:FACTURAMA_USER = Read-Host "Usuario Facturama"
#     $env:FACTURAMA_PASSWORD = Read-Host "Password Facturama"
#     .\mvnw.cmd -DskipTests spring-boot:run
#
#  USO:  .\scripts\probar-factura.ps1
#  (manda el cuerpo JSON desde un archivo para que PowerShell no lo parta por
#   los espacios — esa era la causa de las respuestas vacías.)
# =============================================================================
param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$Usuario = "admin",
    [string]$Clave   = "admin123"
)

$cj       = Join-Path $env:TEMP "nemelesinv-fac-cookie.txt"
$bodyFile = Join-Path $env:TEMP "nemelesinv-factura.json"

# Factura de prueba (receptor de pruebas estándar de Facturama).
$body = @'
{
  "pais": "MX",
  "comprobante": {
    "pais": "MX", "tipo": "I", "moneda": "MXN", "formaPago": "01", "metodoPago": "PUE",
    "emisor": { "lugarExpedicion": "26015" },
    "receptor": {
      "identificadorFiscal": "URE180429TM6",
      "nombre": "UNIVERSIDAD ROBOTICA ESPANOLA",
      "codigoPostal": "65000", "regimenFiscal": "601", "usoCfdi": "G03"
    },
    "lineas": [
      {
        "claveProdServ": "10111302", "claveUnidad": "H87", "unidad": "Pieza",
        "descripcion": "producto prueba cfdi4.0", "cantidad": 1,
        "valorUnitario": 1.00, "importe": 1.00
      }
    ]
  }
}
'@
Set-Content -Path $bodyFile -Value $body -Encoding utf8

Write-Host "1) Iniciando sesion en la app como '$Usuario'..." -ForegroundColor Cyan
curl.exe -s -o NUL -c $cj -X POST --data "username=$Usuario&password=$Clave" "$BaseUrl/api/auth/login"

Write-Host "2) Enviando factura de prueba a $BaseUrl/api/facturas ...`n" -ForegroundColor Cyan
curl.exe -s -b $cj -X POST -H "Content-Type: application/json" --data-binary "@$bodyFile" "$BaseUrl/api/facturas"
Write-Host "`n`n--- Fin. Si ves estado ACEPTADO + un UUID, timbro correctamente. ---" -ForegroundColor Green
