from unittest.mock import patch, MagicMock
from unittest import TestCase
from django.core.files.base import ContentFile
from learning.services.extraction import extract_pdf_text, ExtractionError

class ExtractionServiceTests(TestCase):
    def test_extract_pdf_text_success(self):
        mock_doc = MagicMock()
        mock_page1 = MagicMock()
        mock_page1.get_text.return_value = "Page 1 text."
        mock_page2 = MagicMock()
        mock_page2.get_text.return_value = "Page 2 text."
        mock_doc.__iter__.return_value = [mock_page1, mock_page2]
        
        file_obj = ContentFile(b"%PDF-1.4...", name="dummy.pdf")
        
        with patch("learning.services.extraction.fitz.open", return_value=mock_doc):
            text = extract_pdf_text(file_obj)
            self.assertEqual(text, "Page 1 text.\n\nPage 2 text.")
            
    def test_extract_pdf_text_failure(self):
        file_obj = ContentFile(b"not a pdf", name="dummy.pdf")
        with patch("learning.services.extraction.fitz.open", side_effect=Exception("Corrupted")):
            with self.assertRaises(ExtractionError):
                extract_pdf_text(file_obj)
