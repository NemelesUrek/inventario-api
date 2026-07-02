"""Genera un icono .ico de Stockly: cuadrado teal redondeado con una caja/paquete blanco."""
from PIL import Image, ImageDraw
import os

OUT = os.path.join(os.path.dirname(__file__), "stockly.ico")
TEAL = (13, 148, 136, 255)     # teal 600 (la marca de Stockly)
TEAL2 = (15, 118, 110, 255)    # teal 700 (sombra inferior del degradado)
WHITE = (255, 255, 255, 255)

def render(size):
    S = size * 4  # supersampling para bordes limpios
    img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # fondo redondeado con degradado vertical simple
    radius = int(S * 0.22)
    bg = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    bgd = ImageDraw.Draw(bg)
    for y in range(S):
        t = y / S
        c = tuple(int(TEAL[i] * (1 - t) + TEAL2[i] * t) for i in range(3)) + (255,)
        bgd.line([(0, y), (S, y)], fill=c)
    mask = Image.new("L", (S, S), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, S - 1, S - 1], radius=radius, fill=255)
    img.paste(bg, (0, 0), mask)
    d = ImageDraw.Draw(img)
    # glifo de caja/paquete (cubo isométrico simple) en blanco, trazo grueso
    cx, cy = S / 2, S / 2 + S * 0.02
    w = S * 0.46          # medio ancho
    h = S * 0.30          # medio alto de la cara superior (rombo)
    lw = max(2, int(S * 0.035))
    # rombo superior
    top = (cx, cy - h - S * 0.14)
    right = (cx + w, cy - h * 0.5 - S * 0.14)
    bottom = (cx, cy - S * 0.14)
    left = (cx - w, cy - h * 0.5 - S * 0.14)
    d.polygon([top, right, bottom, left], outline=WHITE, width=lw)
    # aristas verticales del cuerpo
    depth = S * 0.34
    d.line([left, (left[0], left[1] + depth)], fill=WHITE, width=lw)
    d.line([right, (right[0], right[1] + depth)], fill=WHITE, width=lw)
    d.line([bottom, (bottom[0], bottom[1] + depth)], fill=WHITE, width=lw)
    # base
    d.line([(left[0], left[1] + depth), (bottom[0], bottom[1] + depth)], fill=WHITE, width=lw)
    d.line([(right[0], right[1] + depth), (bottom[0], bottom[1] + depth)], fill=WHITE, width=lw)
    # línea de cinta en la cara frontal
    d.line([top, (top[0], top[1] + h)], fill=WHITE, width=lw)
    return img.resize((size, size), Image.LANCZOS)

sizes = [16, 24, 32, 48, 64, 128, 256]
base = render(256)  # Pillow genera las variantes bajando desde la imagen grande
base.save(OUT, format="ICO", sizes=[(s, s) for s in sizes])
from PIL import Image as _I
print("icono escrito:", OUT, os.path.getsize(OUT), "bytes; sizes:", _I.open(OUT).info.get("sizes"))
