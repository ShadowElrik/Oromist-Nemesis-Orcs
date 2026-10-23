"""
Genera las pieles de los orcos (texturas 64x64 con el formato de skin de jugador).

Uso:   python tools/orc_skins.py            (necesita Pillow: pip install pillow)
Salida: src/main/resources/assets/oromist-npc/textures/entity/orc/orc_<n>.png
        build/orc_skins_preview.png (hoja de vista previa, solo para revisar)

Ideas de diseño (las clásicas para que un ejército parezca variado sin dibujar 30 orcos a mano):

1. Cambio de paleta por CLAN: cada clan tiene su propio color de piel, ojos, pelo y un color
   de "estandarte" (la tela del pantalón y el cinturón). A distancia se reconoce el clan.
2. Siluetas distintas con poco: peinados (cresta, rapado, moño, melena) cambian la forma de la cabeza.
3. Marcas faciales: pintura de guerra con varios patrones (franja, mandíbula de calavera,
   huella de mano, rayas, media cara). Es lo primero que mira el jugador.
4. Detalles pequeños que se combinan: tatuajes en los brazos, aros en orejas y nariz, cicatrices.

Clanes × 3 combinaciones de detalles = 24 pieles nuevas (orc_3 ... orc_26).
Las pieles orc_0, orc_1 y orc_2 son las originales y NO se regeneran, para que los orcos que
ya existen en los mundos se sigan viendo igual.

Para añadir un clan: agrega una entrada a CLANS y sube SKIN_VARIANTS en OrcEntity
(3 originales + 3 por clan).
"""
import os
import random
from PIL import Image

ROOT = os.path.join(os.path.dirname(__file__), "..")
OUT = os.path.join(ROOT, "src/main/resources/assets/oromist-npc/textures/entity/orc")
PREVIEW = os.path.join(ROOT, "build/orc_skins_preview.png")
FIRST_NEW_SKIN = 3  # orc_0..orc_2 son las originales

# --- Clanes de la Horda ---------------------------------------------------------------------
# skin: piel · eyes: ojos · hair: pelo · banner: tela del clan · paint: pintura de guerra
CLANS = [
    dict(name="Sangre",   skin=(128, 62, 48),  eyes=(250, 210, 40),  hair=(20, 14, 12),   banner=(30, 26, 26),   paint=(235, 230, 215)),
    dict(name="Pálidos",  skin=(172, 166, 146), eyes=(220, 40, 30),  hair=(210, 205, 195), banner=(110, 22, 22),  paint=(120, 20, 20)),
    dict(name="Arena",    skin=(150, 120, 70),  eyes=(255, 140, 20), hair=(60, 40, 22),   banner=(196, 168, 116), paint=(30, 24, 20)),
    dict(name="Brea",     skin=(46, 50, 42),    eyes=(130, 235, 70), hair=(14, 14, 14),   banner=(140, 72, 32),  paint=(225, 225, 210)),
    dict(name="Escarcha", skin=(98, 114, 126),  eyes=(130, 205, 255), hair=(225, 228, 232), banner=(214, 214, 208), paint=(40, 70, 130)),
    dict(name="Barro",    skin=(106, 82, 54),   eyes=(230, 50, 30),  hair=(24, 18, 12),   banner=(72, 92, 42),   paint=(205, 120, 40)),
    dict(name="Musgo",    skin=(118, 150, 60),  eyes=(240, 60, 30),  hair=(40, 30, 20),   banner=(98, 66, 40),   paint=(30, 30, 30)),
    dict(name="Hierro",   skin=(88, 96, 72),    eyes=(255, 150, 30), hair=(18, 18, 18),   banner=(120, 120, 128), paint=(160, 30, 30)),
]
HAIR_STYLES = ["mohawk", "bald", "topknot", "mane"]
PAINT_PATTERNS = ["none", "stripe", "skull", "hand", "bars", "half"]

LEATHER = (92, 62, 38)
LEATHER_D = (64, 42, 26)
METAL = (150, 150, 155)
BONE = (232, 222, 190)

# Cara frontal de la cabeza en el skin: x 8..15, y 8..15. Ojos en la fila y=11.
FX, FY = 8, 8
EYE_PIXELS = {(FX + 1, FY + 3), (FX + 2, FY + 3), (FX + 5, FY + 3), (FX + 6, FY + 3)}


def jitter(c, rnd, a=12):
    return tuple(max(0, min(255, v + rnd.randint(-a, a))) for v in c) + (255,)


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c)


def rect(img, x0, y0, x1, y1, color, rnd, a=12):
    for x in range(x0, x1):
        for y in range(y0, y1):
            img.putpixel((x, y), jitter(color, rnd, a))


def box_faces(img, u, v, w, h, d, color, rnd, a=12):
    """Rellena todas las caras de un cubo con el layout UV de Minecraft."""
    rect(img, u + d, v, u + d + w + w, v + d, color, rnd, a)
    rect(img, u, v + d, u + d + w + d + w, v + d + h, color, rnd, a)


def put(img, x, y, color):
    img.putpixel((x, y), (*color, 255) if len(color) == 3 else color)


# --- Partes --------------------------------------------------------------------------------

def base_body(img, clan, rnd):
    skin = clan["skin"]
    box_faces(img, 0, 0, 8, 8, 8, skin, rnd)                      # cabeza
    for x in range(FX, FX + 8):                                    # ceño
        put(img, x, FY + 2, jitter(shade(skin, 0.6), rnd, 6))
    for ex in (FX + 1, FX + 5):                                    # ojos
        put(img, ex, FY + 3, clan["eyes"])
        put(img, ex + 1, FY + 3, (20, 10, 5))
    put(img, FX + 3, FY + 4, shade(skin, 0.55))                    # nariz
    put(img, FX + 4, FY + 4, shade(skin, 0.55))
    for x in range(FX + 2, FX + 6):                                # boca
        put(img, x, FY + 6, (40, 20, 15))
    # Torso con chaleco de cuero
    box_faces(img, 16, 16, 8, 12, 4, skin, rnd)
    rect(img, 16, 20, 40, 32, LEATHER, rnd)
    rect(img, 22, 20, 26, 26, skin, rnd)                           # pecho al aire
    rect(img, 16, 29, 40, 30, shade(clan["banner"], 0.8), rnd, 4)  # cinturón del clan
    put(img, 23, 29, METAL)
    put(img, 24, 29, METAL)
    # Brazos con brazales
    box_faces(img, 40, 16, 4, 12, 4, skin, rnd)
    rect(img, 40, 27, 56, 31, LEATHER_D, rnd, 6)
    rect(img, 40, 31, 56, 32, shade(skin, 0.8), rnd)
    # Piernas: tela del clan y botas
    box_faces(img, 0, 16, 4, 12, 4, clan["banner"], rnd, 10)
    rect(img, 0, 28, 16, 32, LEATHER_D, rnd, 6)
    rect(img, 8, 16, 12, 20, LEATHER_D, rnd, 6)
    # Colmillos, mandíbula y orejas (zona v >= 32 que usa OrcModel)
    box_faces(img, 0, 32, 1, 2, 1, BONE, rnd, 6)
    box_faces(img, 4, 32, 1, 2, 1, BONE, rnd, 6)
    box_faces(img, 0, 40, 6, 2, 1, shade(skin, 0.9), rnd)
    for x in range(1, 7):
        put(img, x, 41, shade(skin, 0.65))
    box_faces(img, 16, 32, 2, 3, 1, skin, rnd)
    box_faces(img, 16, 36, 2, 3, 1, skin, rnd)


def hair(img, style, color, rnd):
    """El pelo va en la capa exterior de la cabeza ("hat": x 32..63, y 0..15)."""
    def h(x, y):
        put(img, x, y, jitter(color, rnd, 6))

    if style == "mohawk":                       # cresta de delante a atrás, con coleta
        for y in range(0, 8):
            h(43, y); h(44, y)
        for y in range(8, 11):
            h(43, y); h(44, y); h(59, y); h(60, y)
        for y in range(11, 15):
            h(59, y); h(60, y)
    elif style == "topknot":                    # moño en la coronilla y trenza larga
        for x in range(42, 46):
            for y in range(2, 6):
                h(x, y)
        for y in range(8, 16):
            h(59, y); h(60, y)
    elif style == "mane":                       # melena: toda la nuca y los lados
        for x in range(40, 48):
            for y in range(0, 8):
                h(x, y)
        for x in range(56, 64):
            for y in range(8, 15):
                h(x, y)
        for side in (32, 48):
            for x in range(side, side + 8):
                for y in range(8, 12):
                    h(x, y)
        for x in range(40, 48):                 # flequillo corto
            h(x, 8)
    # "bald": rapado, no se dibuja nada (deja ver la piel y la pintura)


def war_paint(img, pattern, color, rnd):
    def p(x, y):
        if (x, y) not in EYE_PIXELS:
            put(img, x, y, jitter(color, rnd, 8))

    if pattern == "stripe":                     # franja horizontal sobre los ojos
        for x in range(FX, FX + 8):
            p(x, FY + 3)
    elif pattern == "skull":                    # mandíbula de calavera con dientes
        for x in range(FX, FX + 8):
            p(x, FY + 5)
            p(x, FY + 7)
        for x in range(FX + 1, FX + 7, 2):
            p(x, FY + 6)
        p(FX + 1, FY + 4); p(FX + 6, FY + 4)
    elif pattern == "hand":                     # huella de mano sobre una mejilla
        for x in range(FX + 4, FX + 7):
            for y in range(FY + 4, FY + 7):
                p(x, y)
        for x in (FX + 4, FX + 5, FX + 6, FX + 7):
            p(x, FY + 3 if x == FX + 7 else FY + 2)
    elif pattern == "bars":                     # rayas verticales en frente y barbilla
        for x in (FX + 1, FX + 3, FX + 4, FX + 6):
            p(x, FY + 0)
            p(x, FY + 1)
            p(x, FY + 7)
    elif pattern == "half":                     # media cara pintada
        for x in range(FX, FX + 4):
            for y in range(FY + 1, FY + 6):
                p(x, y)


def tattoos(img, color, rnd):
    """Bandas tribales en ambos brazos y una marca en el pecho."""
    for x in range(40, 56):
        put(img, x, 23, jitter(color, rnd, 8))
        if x % 2 == 0:
            put(img, x, 24, jitter(color, rnd, 8))
    put(img, 23, 23, color); put(img, 24, 24, color); put(img, 23, 25, color)


def piercings(img):
    """Aros de metal en orejas y nariz."""
    put(img, 17, 35, METAL); put(img, 17, 39, METAL)
    put(img, FX + 3, FY + 5, METAL)


def scar(img, skin):
    """Cicatriz diagonal en la cara y otra en el pecho."""
    dark = shade(skin, 0.5)
    put(img, FX + 6, FY + 5, dark); put(img, FX + 7, FY + 6, dark)
    for i in range(4):
        put(img, 21 + i, 21 + i, shade(skin, 0.55))


# --- Generación ----------------------------------------------------------------------------

def make_skin(index, clan_index, variant):
    clan = CLANS[clan_index]
    rnd = random.Random(5000 + index)
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    base_body(img, clan, rnd)
    war_paint(img, PAINT_PATTERNS[(clan_index * 2 + variant) % len(PAINT_PATTERNS)], clan["paint"], rnd)
    hair(img, HAIR_STYLES[(clan_index + variant) % len(HAIR_STYLES)], clan["hair"], rnd)
    if (clan_index + variant) % 2 == 0:
        tattoos(img, clan["paint"], rnd)
    if variant == 1:
        piercings(img)
    if variant != 2:
        scar(img, clan["skin"])
    return img


def main():
    os.makedirs(OUT, exist_ok=True)
    skins = []
    index = FIRST_NEW_SKIN
    for clan_index, clan in enumerate(CLANS):
        for variant in range(3):
            img = make_skin(index, clan_index, variant)
            img.save(os.path.join(OUT, f"orc_{index}.png"))
            skins.append(img)
            index += 1
    print(f"{len(skins)} pieles nuevas: orc_{FIRST_NEW_SKIN} ... orc_{index - 1}")
    print(f"SKIN_VARIANTS en OrcEntity debe ser {index}")

    # Hoja de vista previa: cada orco visto de frente, un clan por fila.
    os.makedirs(os.path.dirname(PREVIEW), exist_ok=True)
    scale = 6
    cell_w, cell_h = 18 * scale, 34 * scale
    sheet = Image.new("RGBA", (cell_w * 3, cell_h * len(CLANS)), (40, 40, 40, 255))
    for i, img in enumerate(skins):
        figure = front_view(img).resize((16 * scale, 32 * scale), Image.NEAREST)
        sheet.paste(figure, ((i % 3) * cell_w + scale, (i // 3) * cell_h + scale), figure)
    sheet.save(PREVIEW)
    print(f"Vista previa: {os.path.normpath(PREVIEW)}")


def front_view(img):
    """Dibujo plano del orco de frente (16x32), armado con las caras frontales del skin."""
    view = Image.new("RGBA", (16, 32), (0, 0, 0, 0))
    view.paste(img.crop((8, 8, 16, 16)), (4, 0))                   # cabeza
    view.alpha_composite(img.crop((40, 8, 48, 16)), (4, 0))         # pelo
    view.paste(img.crop((20, 20, 28, 32)), (4, 8))                 # torso
    arm = img.crop((44, 20, 48, 32))
    view.paste(arm, (0, 8))
    view.paste(arm.transpose(Image.FLIP_LEFT_RIGHT), (12, 8))
    leg = img.crop((4, 20, 8, 32))
    view.paste(leg, (4, 20))
    view.paste(leg.transpose(Image.FLIP_LEFT_RIGHT), (8, 20))
    for x in (5, 10):                                              # colmillos
        view.putpixel((x, 6), (*BONE, 255))
    return view

if __name__ == "__main__":
    main()
