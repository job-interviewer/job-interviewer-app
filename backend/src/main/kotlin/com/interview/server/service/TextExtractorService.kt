package com.interview.server.service

import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.springframework.stereotype.Service
import java.io.InputStream

class UnsupportedFileException(message: String) : RuntimeException(message)
class EmptyDocumentException(message: String) : RuntimeException(message)

@Service
class TextExtractorService {

    fun extractText(inputStream: InputStream, fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "pdf" -> extractPdf(inputStream)
            "docx" -> extractDocx(inputStream)
            else -> throw UnsupportedFileException("지원하지 않는 파일 형식입니다: .$ext (PDF 또는 DOCX만 지원)")
        }
    }

    private fun extractPdf(inputStream: InputStream): String {
        return PDDocument.load(inputStream, MemoryUsageSetting.setupMixed(50L * 1024 * 1024)).use { doc ->
            val stripper = PDFTextStripper().apply {
                sortByPosition = true
            }
            val text = stripper.getText(doc).trim()
            if (text.length < 50) {
                throw EmptyDocumentException("PDF에서 텍스트를 추출할 수 없습니다. 스캔 PDF이거나 내용이 없는 파일입니다.")
            }
            text
        }
    }

    private fun extractDocx(inputStream: InputStream): String {
        XWPFDocument(inputStream).use { doc ->
            val text = doc.paragraphs.joinToString("\n") { it.text }.trim()
            if (text.length < 50) {
                throw EmptyDocumentException("DOCX에서 충분한 텍스트를 추출할 수 없습니다.")
            }
            return text
        }
    }
}
