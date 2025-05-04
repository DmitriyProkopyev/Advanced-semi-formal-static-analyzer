#!/bin/bash

# Название файла Java
JAVA_FILE="MdToPdfConverter.java"
CLASS_NAME="MdToPdfConverter"

echo "==== Step 1: Проверка наличия Java ===="
if ! command -v javac &> /dev/null; then
    echo "❌ Java compiler (javac) не найден. Установите JDK и повторите."
    exit 1
else
    echo "✅ Java установлен."
fi

echo "==== Step 2: Проверка наличия pandoc ===="
if ! command -v pandoc &> /dev/null; then
    echo "❌ Pandoc не найден. Устанавливаю..."
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        sudo apt update && sudo apt install -y pandoc
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        brew install pandoc
    else
        echo "⚠️ Не удалось автоматически установить pandoc. Установите вручную: https://pandoc.org/installing.html"
        exit 1
    fi
else
    echo "✅ Pandoc установлен."
fi

echo "==== Step 3: Проверка наличия xelatex (LaTeX) ===="
if ! command -v xelatex &> /dev/null; then
    echo "❌ xelatex не найден. Установите TeX Live:"
    echo "  Debian/Ubuntu: sudo apt install -y texlive-xetex"
    echo "  macOS: brew install --cask mactex"
    echo "  Или вручную: https://tug.org/texlive/"
    exit 1
else
    echo "✅ xelatex установлен."
fi

echo "==== Step 4: Компиляция Java-файла ===="
if [[ -f "$JAVA_FILE" ]]; then
    javac "$JAVA_FILE"
    if [[ $? -ne 0 ]]; then
        echo "❌ Ошибка компиляции."
        exit 1
    fi
    echo "✅ Компиляция прошла успешно."
else
    echo "❌ Файл $JAVA_FILE не найден."
    exit 1
fi

echo "==== Готово! ===="
echo "Теперь вы можете запустить программу:"
echo "java $CLASS_NAME path/to/input.md [path/to/output.pdf]"
