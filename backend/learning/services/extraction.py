import fitz  # PyMuPDF
from django.core.files.base import File

class ExtractionError(Exception):
    pass

def extract_pdf_text(file_obj: File) -> str:
    """Extracts text from a PDF file."""
    try:
        # Read the file content into memory
        file_bytes = file_obj.read()
        
        # Rewind the file pointer so it can be saved normally by Django
        file_obj.seek(0)
        
        # Open PDF from memory stream
        doc = fitz.open(stream=file_bytes, filetype="pdf")
        
        text_parts = []
        for page in doc:
            text_parts.append(page.get_text())
            
        doc.close()
        
        # Clean up excessive newlines
        extracted = "\n\n".join(text_parts).strip()
        # Fallback to empty string if no text is extractable (e.g., scanned PDF without OCR)
        return extracted
    except Exception as e:
        raise ExtractionError(f"Failed to extract PDF text: {str(e)}")
