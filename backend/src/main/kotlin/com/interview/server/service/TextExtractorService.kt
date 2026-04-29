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

    /**
     * Selects a text extraction strategy based on the file extension and returns the extracted text.
     *
     * @param inputStream The uploaded file's input stream to be consumed by the extractor.
     * @param fileName The original file name used to determine the extension (e.g., "doc.pdf").
     * @return The extracted and trimmed textual content of the document.
     * @throws UnsupportedFileException If the file extension is not "pdf" or "docx".
     */
    fun extractText(inputStream: InputStream, fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "pdf" -> extractPdf(inputStream)
            "docx" -> extractDocx(inputStream)
            else -> throw UnsupportedFileException("지원하지 않는 파일 형식입니다: .$ext (PDF 또는 DOCX만 지원)")
        }
    }

    /**
     * Extracts and returns the trimmed plain text content from a PDF input stream.
     *
     * @return The extracted text trimmed of leading and trailing whitespace.
     * @throws EmptyDocumentException If the extracted text length is less than 50 characters (e.g., a scanned or effectively empty PDF).
     */
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

    /**
     * Extracts and returns plain text from a DOCX input stream.
     *
     * @param inputStream The input stream containing a DOCX file.
     * @return The extracted text trimmed of surrounding whitespace.
     * @throws EmptyDocumentException if the extracted text is shorter than 50 characters.
     */
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
