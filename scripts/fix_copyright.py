#!/usr/bin/env python3
import os
import re

EXTENSIONS = {'.kt', '.java'}
DIR_BLACKLIST = {'.git', 'build', '.idea', 'buildSrc'}

# Matches something like:  * Copyright (c) 2023 Isai Project
# or: /* Copyright (c) 2021 Isai Project */
pattern = re.compile(r'(\s*\*\s*Copyright \(c\) )(\d{4})( Isai Project)')

def process_file(filepath):
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
    except UnicodeDecodeError:
        return

    def replacer(match):
        prefix = match.group(1)
        year = match.group(2)
        # We replace the single modified copyright line with two lines:
        # The new attribution, followed by the restored original attribution.
        new_attribution = f"{prefix}2026 Muthupandi (Isai Project)\n"
        original_attribution = f"{prefix}{year} OxygenCobalt (Auxio Project)"
        return new_attribution + original_attribution

    new_content, num_subs = pattern.subn(replacer, content)

    if num_subs > 0:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Fixed copyrights in: {filepath}")

def main():
    for root, dirs, files in os.walk('.'):
        dirs[:] = [d for d in dirs if d not in DIR_BLACKLIST]
        for file in files:
            if any(file.endswith(ext) for ext in EXTENSIONS):
                process_file(os.path.join(root, file))
    
    # Also fix the NOTICE file specifically if it exists
    if os.path.exists("NOTICE"):
        with open("NOTICE", 'r', encoding='utf-8') as f:
            content = f.read()
        if "Isai Project" in content:
            new_content = content.replace(
                "Copyright (c) $today.year Isai Project",
                "Copyright (c) 2026 Muthupandi (Isai Project)\n * Copyright (c) $today.year OxygenCobalt (Auxio Project)"
            )
            with open("NOTICE", 'w', encoding='utf-8') as f:
                f.write(new_content)
            print("Fixed copyrights in: NOTICE")

if __name__ == "__main__":
    print("Fixing GPL v3.0 copyright violations...")
    main()
    print("Done!")
