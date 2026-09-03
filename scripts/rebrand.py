#!/usr/bin/env python3
import os
import shutil

# Files to process
EXTENSIONS = {'.kt', '.xml', '.gradle', '.kts', '.md', '.pro', '.yml', '.yaml', '.properties'}
DIR_BLACKLIST = {'.git', 'build', '.gradle', '.idea', 'buildSrc'}

# Replacements (ordered by specificity)
REPLACEMENTS = [
    ("org.oxycblt.isai", "com.muthupandi.isai"),
    ("org.oxycblt.musikr", "com.muthupandi.musikr"),
    ("org.oxycblt.auxio", "com.muthupandi.isai"),
    ("OxygenCobalt", "Muthupandi"),
    ("oxygencobalt", "muthupandi"),
    ("oxyzencobalt", "muthupandi"),
    ("oxycblt", "muthupandi"),
    ("Auxio", "Isai"),
    ("auxio", "isai"),
]

def replace_in_file(filepath):
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
    except UnicodeDecodeError:
        return # Skip binary files just in case
        
    new_content = content
    for old, new in REPLACEMENTS:
        new_content = new_content.replace(old, new)
        
    if new_content != content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Updated: {filepath}")

def process_directory(path):
    for root, dirs, files in os.walk(path):
        # Don't recurse into blacklisted dirs
        dirs[:] = [d for d in dirs if d not in DIR_BLACKLIST]
        
        for file in files:
            if any(file.endswith(ext) for ext in EXTENSIONS):
                replace_in_file(os.path.join(root, file))

def move_java_dirs():
    # Source structures to move
    moves = [
        ("app/src/main/java/org/oxycblt/isai", "app/src/main/java/com/muthupandi/isai"),
        ("app/src/debug/java/org/oxycblt/isai", "app/src/debug/java/com/muthupandi/isai"),
        ("app/src/test/java/org/oxycblt/isai", "app/src/test/java/com/muthupandi/isai"),
        ("app/src/androidTest/java/org/oxycblt/isai", "app/src/androidTest/java/com/muthupandi/isai"),
        ("musikr/src/main/java/org/oxycblt/musikr", "musikr/src/main/java/com/muthupandi/musikr"),
        ("musikr/src/test/java/org/oxycblt/musikr", "musikr/src/test/java/com/muthupandi/musikr"),
        ("musikr/src/androidTest/java/org/oxycblt/musikr", "musikr/src/androidTest/java/com/muthupandi/musikr"),
    ]
    
    for src, dst in moves:
        if os.path.exists(src):
            print(f"Moving {src} -> {dst}")
            os.makedirs(os.path.dirname(dst), exist_ok=True)
            shutil.move(src, dst)
            
            # Clean up empty old directories if possible (org/oxycblt)
            try:
                os.rmdir(os.path.dirname(src)) # removes org/oxycblt
                os.rmdir(os.path.dirname(os.path.dirname(src))) # removes org if empty
            except OSError:
                pass # directory not empty, that's fine

if __name__ == "__main__":
    print("Starting mass replacement...")
    process_directory(".")
    print("Moving Java directories...")
    move_java_dirs()
    print("Done!")
