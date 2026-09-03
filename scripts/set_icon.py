#!/usr/bin/env python3
"""
Script to resize the new Isai logo into all required Android mipmap sizes
WITH proper padding (66% safe zone like Auxio). Also creates the splash drawable.
Run with: python3 scripts/set_icon.py
"""
from PIL import Image
import os

SRC = "/home/muthupandi/Downloads/isai.png"
BASE = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                    "app/src/main/res")

# Total canvas size → logo fills 66% centered (like Auxio adaptive icon safe zone)
CANVAS_SIZES = {
    "mipmap-mdpi":    48,
    "mipmap-hdpi":    72,
    "mipmap-xhdpi":   96,
    "mipmap-xxhdpi":  144,
    "mipmap-xxxhdpi": 192,
}

LOGO_FILL = 0.66  # logo occupies 66% of canvas


def make_padded_icon(img, canvas_size):
    """Create a transparent canvas and paste the logo centered at 66% size."""
    logo_size = int(canvas_size * LOGO_FILL)
    logo = img.resize((logo_size, logo_size), Image.LANCZOS)

    canvas = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
    offset = (canvas_size - logo_size) // 2
    canvas.paste(logo, (offset, offset), logo)
    return canvas


def main():
    img = Image.open(SRC).convert("RGBA")
    print(f"Source image: {img.size}")

    for folder, size in CANVAS_SIZES.items():
        out_dir = os.path.join(BASE, folder)
        os.makedirs(out_dir, exist_ok=True)

        icon = make_padded_icon(img, size)

        path1 = os.path.join(out_dir, "ic_launcher.png")
        icon.save(path1, "PNG")

        path2 = os.path.join(out_dir, "ic_launcher_round.png")
        icon.save(path2, "PNG")

        print(f"  ✓ {folder}: {size}x{size}px (logo: {int(size*LOGO_FILL)}px)")

    # Also generate a splash icon (200x200 with logo at 80% — bigger for splash)
    splash_dir = os.path.join(BASE, "drawable-xxxhdpi")
    os.makedirs(splash_dir, exist_ok=True)
    splash = make_padded_icon(img, 288)  # 288dp for splash screen
    splash.save(os.path.join(splash_dir, "ic_splash_isai.png"), "PNG")
    print(f"  ✓ splash icon: 288x288px")

    print("\nDone! Rebuild the app to see the new icon.")


if __name__ == "__main__":
    main()
