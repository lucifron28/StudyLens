from unittest.mock import patch, MagicMock
from unittest import TestCase
from types import SimpleNamespace
from django.core.files.base import ContentFile
from django.test import SimpleTestCase
from rest_framework import serializers
from learning.models import Module
from learning.serializers import MAX_MODULE_FILE_SIZE_BYTES, ModuleSerializer
from learning.services.extraction import extract_pdf_text, extract_docx_text, extract_pptx_text, ExtractionError

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

    def test_extract_docx_text_success(self):
        mock_doc = MagicMock()
        mock_p1 = MagicMock()
        mock_p1.text = "Paragraph 1"
        mock_p2 = MagicMock()
        mock_p2.text = "Paragraph 2"
        mock_doc.paragraphs = [mock_p1, mock_p2]
        
        file_obj = ContentFile(b"fake docx", name="dummy.docx")
        
        with patch("learning.services.extraction.docx.Document", return_value=mock_doc):
            text = extract_docx_text(file_obj)
            self.assertEqual(text, "Paragraph 1\n\nParagraph 2")
            
    def test_extract_pptx_text_success(self):
        mock_prs = MagicMock()
        mock_slide = MagicMock()
        mock_shape = MagicMock()
        mock_shape.text = "Slide 1 Text"
        mock_slide.shapes = [mock_shape]
        mock_prs.slides = [mock_slide]
        
        file_obj = ContentFile(b"fake pptx", name="dummy.pptx")
        
        with patch("learning.services.extraction.Presentation", return_value=mock_prs):
            text = extract_pptx_text(file_obj)
            self.assertEqual(text, "Slide 1 Text")


class ModuleFileValidationTests(SimpleTestCase):
    def test_accepts_matching_pdf_file(self):
        module_file = ContentFile(b"%PDF-1.4", name="android-layouts.pdf")

        validated_data = ModuleSerializer().validate(
            {"module_file": module_file, "content_type": Module.ContentType.PDF}
        )

        self.assertEqual(validated_data["module_file"], module_file)

    def test_rejects_file_with_mismatched_extension(self):
        module_file = ContentFile(b"not a PDF", name="android-layouts.docx")

        with self.assertRaises(serializers.ValidationError):
            ModuleSerializer().validate(
                {"module_file": module_file, "content_type": Module.ContentType.PDF}
            )

    def test_rejects_file_larger_than_limit(self):
        module_file = SimpleNamespace(
            name="android-layouts.pdf",
            size=MAX_MODULE_FILE_SIZE_BYTES + 1,
        )

        with self.assertRaises(serializers.ValidationError):
            ModuleSerializer().validate(
                {"module_file": module_file, "content_type": Module.ContentType.PDF}
            )
