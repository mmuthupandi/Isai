import os
import shutil

# 1. Move directories
def move_directories(root_dir):
    for root, dirs, files in os.walk(root_dir, topdown=False):
        for d in dirs:
            if d == 'auxio' and os.path.basename(root) == 'oxycblt':
                old_path = os.path.join(root, d)
                new_path = os.path.join(root, 'isai')
                print(f"Moving directory: {old_path} -> {new_path}")
                shutil.move(old_path, new_path)

# 2. Rename files containing 'auxio'
def rename_files(root_dir):
    for root, dirs, files in os.walk(root_dir, topdown=False):
        for f in files:
            if 'auxio' in f.lower():
                old_path = os.path.join(root, f)
                new_name = f.replace('auxio', 'isai').replace('Auxio', 'Isai')
                new_path = os.path.join(root, new_name)
                print(f"Renaming file: {old_path} -> {new_path}")
                shutil.move(old_path, new_path)

# 3. Replace content in files
def replace_in_files(root_dir):
    valid_extensions = {
        '.kt', '.java', '.xml', '.gradle', '.kts', '.md', '.pro', '.properties', '.txt', '.yml'
    }
    exclude_dirs = {'.git', '.gradle', 'build', '.idea', 'build-logic-settings'}
    
    for root, dirs, files in os.walk(root_dir):
        dirs[:] = [d for d in dirs if d not in exclude_dirs]
        for f in files:
            ext = os.path.splitext(f)[1]
            if ext in valid_extensions or f == 'NOTICE':
                file_path = os.path.join(root, f)
                try:
                    with open(file_path, 'r', encoding='utf-8') as file:
                        content = file.read()
                    
                    new_content = content.replace('org.oxycblt.auxio', 'org.oxycblt.isai')
                    new_content = new_content.replace('Auxio', 'Isai')
                    new_content = new_content.replace('auxio', 'isai')
                    
                    if new_content != content:
                        with open(file_path, 'w', encoding='utf-8') as file:
                            file.write(new_content)
                        print(f"Updated content in: {file_path}")
                except Exception as e:
                    print(f"Could not process {file_path}: {e}")

if __name__ == '__main__':
    project_root = '/home/muthupandi/Projects/Auxio'
    print("Moving directories...")
    move_directories(project_root)
    print("Renaming files...")
    rename_files(project_root)
    print("Replacing content...")
    replace_in_files(project_root)
    print("Migration complete!")
