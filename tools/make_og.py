"""Genera la imagen OpenGraph (tarjeta social 1200x630) de Stockly.
Salida: src/main/resources/static/og.png
Uso: python tools/make_og.py
"""
import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

W, H = 1200, 630
OUT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "static", "og.png")

# Paleta (igual que la app)
BG = (11, 17, 32)
BRAND = (34, 211, 166)
BRAND_DK = (4, 38, 29)
WARM = (255, 179, 107)
TXT = (241, 245, 251)
TXT2 = (174, 185, 204)
TXT3 = (107, 120, 144)
CARD = (17, 24, 39)
BORDER = (43, 58, 85)

def font(paths, size):
    for p in paths:
        try:
            return ImageFont.truetype(p, size)
        except Exception:
            continue
    return ImageFont.load_default()

BOLD = ["C:/Windows/Fonts/segoeuib.ttf", "C:/Windows/Fonts/arialbd.ttf",
        "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"]
REG = ["C:/Windows/Fonts/segoeui.ttf", "C:/Windows/Fonts/arial.ttf",
       "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"]
MONO = ["C:/Windows/Fonts/consola.ttf", "C:/Windows/Fonts/cour.ttf",
        "/usr/share/fonts/truetype/dejavu/DejaVuSansMono.ttf"]

f_title = font(BOLD, 92)
f_h = font(BOLD, 40)
f_sub = font(REG, 33)
f_chipv = font(BOLD, 44)
f_chipl = font(REG, 23)
f_foot = font(REG, 26)
f_mono = font(MONO, 22)

img = Image.new("RGB", (W, H), BG)

# Glows
glow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
gd = ImageDraw.Draw(glow)
gd.ellipse([760, -260, 1360, 340], fill=(34, 211, 166, 70))
gd.ellipse([-200, 360, 360, 820], fill=(110, 139, 255, 45))
glow = glow.filter(ImageFilter.GaussianBlur(120))
img = Image.alpha_composite(img.convert("RGBA"), glow).convert("RGB")
d = ImageDraw.Draw(img)

# Borde sutil
d.rounded_rectangle([14, 14, W - 14, H - 14], radius=22, outline=BORDER, width=2)

PADX = 70
# Logo cube
lx, ly, ls = PADX, 64, 70
d.rounded_rectangle([lx, ly, lx + ls, ly + ls], radius=18, fill=BRAND)
cx, cy = lx + ls / 2, ly + ls / 2
pts = [(cx, cy - 22), (cx + 19, cy - 11), (cx + 19, cy + 11), (cx, cy + 22), (cx - 19, cy + 11), (cx - 19, cy - 11)]
d.line(pts + [pts[0]], fill=BRAND_DK, width=3)
d.line([(cx - 19, cy - 11), (cx, cy), (cx + 19, cy - 11)], fill=BRAND_DK, width=3)
d.line([(cx, cy), (cx, cy + 22)], fill=BRAND_DK, width=3)

# Marca
d.text((lx + ls + 24, 70), "Stockly", font=f_title, fill=TXT)
d.text((lx + ls + 28, 172), "CONTROL DE INVENTARIO", font=f_mono, fill=BRAND)

# Tagline
d.text((PADX, 250), "API REST en Java + Spring Boot, en vivo.", font=f_h, fill=TXT)
d.text((PADX, 304), "Panel, productos, auditoría, reportes y documentación interactiva.", font=f_sub, fill=TXT2)

# Stat chips
chips = [("$43,355", "valor en inventario", BRAND),
         ("8 endpoints", "API documentada", TXT),
         ("9 tests · CI", "backend probado", WARM)]
cw, ch, gap = 330, 120, 24
y0 = 376
for i, (val, lab, col) in enumerate(chips):
    x0 = PADX + i * (cw + gap)
    d.rounded_rectangle([x0, y0, x0 + cw, y0 + ch], radius=16, fill=CARD, outline=BORDER, width=2)
    d.text((x0 + 24, y0 + 22), val, font=f_chipv, fill=col)
    d.text((x0 + 26, y0 + 78), lab, font=f_chipl, fill=TXT3)

# Footer
fy = 556
d.text((PADX, fy), "Nemeles", font=f_h, fill=TXT)
nw = d.textlength("Nemeles", font=f_h)
d.text((PADX + nw + 18, fy + 10), "— Backend Engineer", font=f_sub, fill=TXT2)
stack = "Java 17 · Spring Boot · REST · SQL · Docker"
sw = d.textlength(stack, font=f_mono)
d.text((W - PADX - sw, fy + 14), stack, font=f_mono, fill=TXT3)

os.makedirs(os.path.dirname(OUT), exist_ok=True)
img.save(OUT, "PNG")
print("OK ->", os.path.abspath(OUT), img.size)
