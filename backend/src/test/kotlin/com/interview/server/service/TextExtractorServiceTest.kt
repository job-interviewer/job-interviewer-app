package com.interview.server.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayInputStream

class TextExtractorServiceTest {

    private val service = TextExtractorService()

    @Test
    fun `unsupported file extension throws UnsupportedFileException`() {
        val stream = ByteArrayInputStream("content".toByteArray())
        assertThrows<UnsupportedFileException> {
            service.extractText(stream, "resume.hwp")
        }
    }

    @Test
    fun `unsupported extension txt throws UnsupportedFileException`() {
        val stream = ByteArrayInputStream("content".toByteArray())
        assertThrows<UnsupportedFileException> {
            service.extractText(stream, "resume.txt")
        }
    }

    @Test
    fun `empty pdf throws EmptyDocumentException`() {
        // Create a minimal valid PDF with no text content
        // We use a real minimal PDF bytes that PDFBox can parse but has no text
        val minimalPdf = "%PDF-1.4\n1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] >>\nendobj\nxref\n0 4\n0000000000 65535 f\n0000000009 00000 n\n0000000058 00000 n\n0000000115 00000 n\ntrailer\n<< /Size 4 /Root 1 0 R >>\nstartxref\n190\n%%EOF"
        val stream = ByteArrayInputStream(minimalPdf.toByteArray())
        assertThrows<EmptyDocumentException> {
            service.extractText(stream, "empty.pdf")
        }
    }
}
