from pathlib import Path
import subprocess
import tempfile

from django.core.files.base import ContentFile, File


class ConversionError(Exception):
    """Raised when LibreOffice cannot convert an uploaded office document."""


def convert_office_to_pdf(source_file: File) -> ContentFile:
    """Convert a DOCX or PPTX upload to a PDF with headless LibreOffice."""
    source_name = Path(source_file.name).name
    source_path = Path(source_name)

    with tempfile.TemporaryDirectory() as temporary_directory:
        temporary_path = Path(temporary_directory)
        input_path = temporary_path / source_path.name
        output_path = temporary_path / f"{source_path.stem}.pdf"

        try:
            with input_path.open("wb") as destination:
                for chunk in source_file.chunks():
                    destination.write(chunk)
        finally:
            source_file.seek(0)

        try:
            result = subprocess.run(
                [
                    "soffice",
                    "--headless",
                    "--nologo",
                    "--nofirststartwizard",
                    "--convert-to",
                    "pdf",
                    "--outdir",
                    str(temporary_path),
                    str(input_path),
                ],
                capture_output=True,
                text=True,
                timeout=120,
                check=False,
            )
        except FileNotFoundError as error:
            raise ConversionError("LibreOffice is not available on the server.") from error
        except subprocess.TimeoutExpired as error:
            raise ConversionError("Document conversion timed out. Try a smaller file.") from error

        if result.returncode != 0 or not output_path.is_file():
            raise ConversionError("LibreOffice could not convert this document to PDF.")

        return ContentFile(output_path.read_bytes(), name=output_path.name)
