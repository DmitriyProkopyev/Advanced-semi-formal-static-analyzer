import pypandoc
import sys

def md_to_pdf_with_emoji(input_md: str, output_pdf: str):
    extra_args = [
        "--pdf-engine=lualatex",
        "-V", "mainfont=TeX Gyre Termes",
        "-V", r"header-includes=\usepackage{fontspec}\usepackage{emoji}\setemojifont{Noto Color Emoji}",
        "-V", "geometry:margin=1in",
    ]

    try:
        pypandoc.convert_file(
            source_file=input_md,
            to='pdf',
            outputfile=output_pdf,
            extra_args=extra_args
        )
        print(f"Готово: {output_pdf}")
    except RuntimeError as e:
        print("Конвертация не удалась:", e, file=sys.stderr)

if __name__ == '__main__':
    md_to_pdf_with_emoji('input.md', 'output.pdf')
