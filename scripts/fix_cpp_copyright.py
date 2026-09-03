import os
import re

EXTENSIONS = {'.cpp', '.h', '.hpp', '.c'}
DIR_BLACKLIST = {'.git', 'build', '.idea', 'buildSrc'}

# Matches:  * Copyright (c) 2024 Auxio Project
pattern = re.compile(r'(\s*\*\s*Copyright \(c\) )(\d{4})( Auxio Project)')

def process_file(filepath):
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
    except UnicodeDecodeError:
        return

    def replacer(match):
        prefix = match.group(1)
        year = match.group(2)
        new_attribution = f"{prefix}2026 Muthupandi (Isai Project)\n"
        original_attribution = f"{prefix}{year} OxygenCobalt (Auxio Project)"
        return new_attribution + original_attribution

    new_content, num_subs = pattern.subn(replacer, content)
    
    # Also replace "is part of Auxio." with "is part of Isai."
    new_content = new_content.replace('is part of Auxio.', 'is part of Isai.')

    if new_content != content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Fixed copyrights in: {filepath}")

def main():
    for root, dirs, files in os.walk('.'):
        dirs[:] = [d for d in dirs if d not in DIR_BLACKLIST]
        for file in files:
            if any(file.endswith(ext) for ext in EXTENSIONS):
                process_file(os.path.join(root, file))

if __name__ == "__main__":
    print("Fixing GPL v3.0 copyright violations for C++ files...")
    main()
    print("Done!")
