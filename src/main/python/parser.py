import pypandoc


def convert_md_to_pdf(
    input_md: str,
    output_pdf: str,
    mainfont: str = "TeX Gyre Termes",
    emoji_font: str = "Noto Color Emoji",
    margin: str = "1in",
    pdf_engine: str = "lualatex"
) -> None:

    extra_args = [
        f"--pdf-engine={pdf_engine}",
        "-V", f"mainfont={mainfont}",
        "-V", r"header-includes=\usepackage{fontspec}\usepackage{emoji}"
               + rf"\setemojifont{{{emoji_font}}}",
        "-V", f"geometry:margin={margin}",
    ]

    pypandoc.convert_file(
        source_file=input_md,
        to="pdf",
        outputfile=output_pdf,
        extra_args=extra_args
    )
