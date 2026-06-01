#!/usr/bin/env python3
"""
Скрипт для замены абсолютных путей на относительные в файлах проекта.
Рекурсивно обходит все файлы в папке где находится скрипт.
"""

import os
import re
from pathlib import Path
from typing import Set

# Расширения текстовых файлов, которые будут обработаны
TEXT_EXTENSIONS = {
    '.py', '.txt', '.md', '.json', '.yaml', '.yml', '.xml', '.html', 
    '.css', '.js', '.ts', '.jsx', '.tsx', '.sh', '.bash', '.conf', 
    '.config', '.ini', '.properties', '.gradle', '.maven', '.sql', 
    '.java', '.cpp', '.c', '.h', '.hpp', '.go', '.rs', '.toml'
}

def is_text_file(file_path: str) -> bool:
    """Проверяет, является ли файл текстовым по расширению."""
    _, ext = os.path.splitext(file_path)
    return ext.lower() in TEXT_EXTENSIONS

def get_relative_path(absolute_path: str, script_dir: str) -> str:
    """Преобразует абсолютный путь в относительный от корня проекта."""
    try:
        abs_path = Path(absolute_path).resolve()
        rel_path = abs_path.relative_to(Path(script_dir).resolve())
        return str(rel_path).replace('\\', '/')
    except (ValueError, OSError):
        return None

def process_file(file_path: str, script_dir: str) -> tuple[int, int]:
    """
    Обрабатывает файл и заменяет абсолютные пути на относительные.
    Возвращает кортеж (количество замен, количество ошибок).
    """
    replacements = 0
    errors = 0
    
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
    except (UnicodeDecodeError, IOError) as e:
        print(f"  ⚠ Ошибка чтения {file_path}: {e}")
        return replacements, 1
    
    original_content = content
    
    # Паттерн для поиска абсолютных путей (начинаются с / или буквы диска на Windows)
    # Ищет пути в кавычках, без кавычек, в URL и т.д.
    patterns = [
        (r'(["\'])(/?[a-zA-Z]:[\\\/]|\/[a-zA-Z0-9_\-./\\]+)', r'\1'),  # Windows пути и Unix пути в кавычках
        (r'(?<!["\'])(/?[a-zA-Z]:[\\\/]|\/[a-zA-Z0-9_\-./\\]+)(?!["\'])', None),  # Пути без кавычек
    ]
    
    # Более точный паттерн для абсолютных путей
    def replace_absolute_paths(text):
        nonlocal replacements
        
        # Паттерн для абсолютных путей (может быть в кавычках или без)
        # Ищет пути, которые начинаются с / или X:/ (Windows)
        pattern = r'(["\']?)(?:file://)?(?P<path>(?:[a-zA-Z]:[\\\/]|\/)[a-zA-Z0-9_\-./\\:~]+)(["\']?)'
        
        def replace_func(match):
            nonlocal replacements
            quote_start = match.group(1)
            path = match.group('path')
            quote_end = match.group(3)
            
            # Проверяем, что кавычки закрываются корректно
            if (quote_start and not quote_end) or (not quote_start and quote_end):
                return match.group(0)
            
            # Пытаемся получить относительный путь
            rel_path = get_relative_path(path, script_dir)
            
            if rel_path:
                replacements += 1
                return f'{quote_start}{rel_path}{quote_end}'
            
            return match.group(0)
        
        return re.sub(pattern, replace_func, text)
    
    new_content = replace_absolute_paths(original_content)
    
    # Если был изменен контент, сохраняем файл
    if new_content != original_content:
        try:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(new_content)
        except IOError as e:
            print(f"  ⚠ Ошибка записи {file_path}: {e}")
            return replacements, 1
    
    return replacements, errors

def main():
    """Главная функция скрипта."""
    script_dir = os.path.dirname(os.path.abspath(__file__))
    
    print(f"🔄 Начинаю обход директории: {script_dir}\n")
    
    total_replacements = 0
    total_errors = 0
    processed_files = 0
    
    # Рекурсивный обход всех файлов
    for root, dirs, files in os.walk(script_dir):
        # Пропускаем скрытые директории и стандартные директории игнорирования
        dirs[:] = [d for d in dirs if not d.startswith('.') and d not in ['__pycache__', 'node_modules', '.git']]
        
        for file in files:
            # Пропускаем сам скрипт
            if file == os.path.basename(__file__):
                continue
            
            file_path = os.path.join(root, file)
            
            # Обрабатываем только текстовые файлы
            if is_text_file(file_path):
                replacements, errors = process_file(file_path, script_dir)
                
                if replacements > 0 or errors > 0:
                    rel_file_path = os.path.relpath(file_path, script_dir)
                    if replacements > 0:
                        print(f"✅ {rel_file_path}: {replacements} замен(и)")
                    if errors > 0:
                        print(f"❌ {rel_file_path}: {errors} ошибок")
                    
                    total_replacements += replacements
                    total_errors += errors
                    processed_files += 1
    
    # Вывод статистики
    print(f"\n{'='*50}")
    print(f"📊 Итого:")
    print(f"   Файлов обработано: {processed_files}")
    print(f"   Всего замен: {total_replacements}")
    if total_errors > 0:
        print(f"   Ошибок: {total_errors}")
    print(f"{'='*50}")

if __name__ == "__main__":
    main()
