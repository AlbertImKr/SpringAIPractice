package com.albert.springaipractice.service

import org.springframework.ai.document.Document
import org.springframework.ai.reader.ExtractedTextFormatter
import org.springframework.ai.reader.TextReader
import org.springframework.ai.reader.pdf.PagePdfDocumentReader
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.io.File

@Service
class EtlService(
    private val vectorStore: VectorStore,
) {

    fun processAndStoreFromFile(
        filePath: String,
        metadata: Map<String, Any> = emptyMap(),
        chunkSize: Int = 800,
        chunkOverlap: Int = 100,
    ): Int {
        val file = File(filePath)
        require(file.exists()) { "파일을 찾을 수 없습니다: $filePath" }

        // Extract: 파일에서 문서 읽기
        val documents = when {
            filePath.endsWith(".pdf", ignoreCase = true) -> {
                // PDF 파일 처리
                val pdfReader = PagePdfDocumentReader(
                    FileSystemResource(file),
                    PdfDocumentReaderConfig.builder()
                        .withPageTopMargin(0)
                        .withPageExtractedTextFormatter(
                            ExtractedTextFormatter.builder()
                                .withNumberOfTopTextLinesToDelete(0)
                                .build()
                        )
                        .withPagesPerDocument(1)
                        .build()
                )
                pdfReader.get()
            }

            else -> {
                // 텍스트 파일 처리
                loadDocumentsFromFile(filePath)
            }
        }

        // Transform: 문서 분할 및 메타데이터 추가
        val splitDocuments = splitDocuments(documents, chunkSize, chunkOverlap)
        val documentsWithMetadata = if (metadata.isNotEmpty()) {
            addMetadata(splitDocuments, metadata)
        } else {
            splitDocuments
        }

        // Load: 벡터 스토어에 저장
        saveToVectorStore(documentsWithMetadata)

        return documentsWithMetadata.size
    }

    fun initializeDocuments(): Map<String, Int> {
        val results = mutableMapOf<String, Int>()

        try {
            // Spring AI 소개 문서 처리
            val springAiCount = processAndStoreDocuments(
                "documents/spring-ai-intro.txt", mapOf("type" to "Spring AI", "category" to "introduction")
            )
            results["spring-ai-intro.txt"] = springAiCount

            // RAG 개념 문서 처리
            val ragCount = processAndStoreDocuments(
                "documents/rag-concepts.txt", mapOf("type" to "RAG", "category" to "concepts")
            )
            results["rag-concepts.txt"] = ragCount
        } catch (e: Exception) {
            throw RuntimeException("문서 초기화 중 오류 발생: ${e.message}", e)
        }

        return results
    }

    fun searchSimilarDocuments(query: String, topK: Int = 5): List<Document> {
        val request = SearchRequest.builder().query(query).topK(topK).build()
        return vectorStore.similaritySearch(request)
    }

    private fun processAndStoreDocuments(
        resourcePath: String,
        metadata: Map<String, Any> = emptyMap(),
        chunkSize: Int = 800,
        chunkOverlap: Int = 100,
    ): Int {
        // Extract: 문서 읽기
        val documents = loadDocumentsFromClasspath(resourcePath)

        // Transform: 문서 분할 및 메타데이터 추가
        val splitDocuments = splitDocuments(documents, chunkSize, chunkOverlap)
        val documentsWithMetadata = if (metadata.isNotEmpty()) {
            addMetadata(splitDocuments, metadata)
        } else {
            splitDocuments
        }

        // Load: 벡터 스토어에 저장
        saveToVectorStore(documentsWithMetadata)

        return documentsWithMetadata.size
    }

    private fun loadDocumentsFromFile(filePath: String): List<Document> {
        val file = File(filePath)
        require(file.exists()) { "파일을 찾을 수 없습니다: $filePath" }

        val resource: Resource = FileSystemResource(file)
        val textReader = TextReader(resource)
        return textReader.get()
    }

    private fun splitDocuments(
        documents: List<Document>,
        chunkSize: Int = 800,
        chunkOverlap: Int = 100,
    ): List<Document> {
        val textSplitter = TokenTextSplitter.builder().withChunkSize(chunkSize).withMaxNumChunks(chunkOverlap).build()

        return textSplitter.apply(documents)
    }

    private fun loadDocumentsFromClasspath(resourcePath: String): List<Document> {
        val resource: Resource = ClassPathResource(resourcePath)
        val textReader = TextReader(resource)
        return textReader.get()
    }

    private fun addMetadata(documents: List<Document>, metadata: Map<String, Any>): List<Document> {
        return documents.map { document ->
            val updatedMetadata = document.metadata.toMutableMap()
            updatedMetadata.putAll(metadata)
            Document(document.text ?: "", updatedMetadata)
        }
    }

    private fun saveToVectorStore(documents: List<Document>) {
        vectorStore.add(documents)
    }
}
