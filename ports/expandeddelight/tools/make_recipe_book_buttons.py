#!/usr/bin/env python3
"""Lay out upstream's four recipe book filter sprites as one sheet for Minecraft 1.20.1.

Minecraft 1.21.1 draws the filter button from the sprites textures/gui/sprites/recipe_book/juicer_*.png.
Minecraft 1.20.1 draws it from one texture with StateSwitchingButton#initTextureValues(0, 0, 28, 18):
the plain state at (0, 0), the filtering state 28 pixels to the right and the hovered states 18 pixels
lower. Run from ports/expandeddelight with Pillow installed:

    python3 -I tools/make_recipe_book_buttons.py
"""
from PIL import Image

SRC = "src/main/resources/assets/expandeddelight/textures/gui/sprites/recipe_book/"
DST = "src/main/resources/assets/expandeddelight/textures/gui/recipe_book_buttons.png"
LAYOUT = {
    "juicer_disabled": (0, 0),
    "juicer_enabled": (28, 0),
    "juicer_disabled_highlighted": (0, 18),
    "juicer_enabled_highlighted": (28, 18),
}

sheet = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
for name, pos in LAYOUT.items():
    sheet.paste(Image.open(SRC + name + ".png").convert("RGBA"), pos)
sheet.save(DST, optimize=True)
