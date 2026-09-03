#!/usr/bin/env python3
"""
Generates:
  1. Mipmap launcher icons with proper 66% safe-zone padding
  2. High-res splash drawables (288dp × density) for crisp splash screen
"""
from PIL import Image
import os

SRC = "/home/muthupandi/Downloads/isai.png"
BASE = "/home/muthupandi/Projects/Auxio/app/src/main/res"
FILL = 0.66  # logo takes 66% of canvas (Android safe zone)


def padded(img, canvas_px, fill=FILL):
    logo_px = int(canvas_px * fill)
    logo = img.resize((logo_px, logo_px), Image.LANCZOS)
    canvas = Image.new("RGBA", (canvas_px, canvas_px), (0, 0, 0, 0))
    off = (canvas_px - logo_px) // 2
    canvas.paste(logo, (off, off), logo)
    return canvas


def save(img, folder, name):
    d = os.path.join(BASE, folder)
    os.makedirs(d, exist_ok=True)
    img.save(os.path.join(d, name), "PNG")


def main():
    img = Image.open(SRC).convert("RGBA")
    print("Source:", img.size)

    # ---------- 1. Mipmap launcher icons (with padding) ----------
    mipmap = [
        ("mipmap-mdpi",    48),
        ("mipmap-hdpi",    72),
        ("mipmap-xhdpi",   96),
        ("mipmap-xxhdpi",  144),
        ("mipmap-xxxhdpi", 192),
    ]
    for folder, px in mipmap:
        icon = padded(img, px)
        save(icon, folder, "ic_launcher.png")
        save(icon, folder, "ic_launcher_round.png")
        print(f"  launcher {folder}: {px}px")

    # ---------- 2. High-res splash drawables ----------
    # Android splash screen renders windowSplashScreenAnimatedIcon at 288dp.
    # We provide density-specific PNGs so the system never upscales a tiny image.
    # 288dp × density = required pixel size.
    splash = [
        ("drawable-mdpi",    288),   # 1.0×
        ("drawable-hdpi",    432),   # 1.5×
        ("drawable-xhdpi",   576),   # 2.0×
        ("drawable-xxhdpi",  864),   # 3.0×
        ("drawable-xxxhdpi", 1152),  # 4.0×
    ]
    for folder, px in splash:
        # Use 80% fill for splash so the logo has breathing room on screen
        icon = padded(img, px, fill=0.80)
        save(icon, folder, "ic_splash_logo.png")
        print(f"  splash  {folder}: {px}px")

    print("\nDone!")


if __name__ == "__main__":
    main()
