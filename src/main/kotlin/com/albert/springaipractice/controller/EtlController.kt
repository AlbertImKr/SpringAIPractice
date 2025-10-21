package com.albert.springaipractice.controller

import com.albert.springaipractice.service.EtlService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@RestController
@RequestMapping("/api/etl")
class EtlController(private val etlService: EtlService) {

    @PostMapping("/initialize")
    fun initializeDocuments(): EtlResponse {
        return try {
            val results = etlService.initializeDocuments()
            EtlResponse(
                success = true,
                message = "문서 초기화 완료",
                details = results
            )
        } catch (e: Exception) {
            EtlResponse(
                success = false,
                message = "문서 초기화 실패: ${e.message}"
            )
        }
    }

    @PostMapping("/upload")
    fun uploadAndProcessDocument(
        @RequestPart("file") file: MultipartFile,
        @RequestParam("type", defaultValue = "document") type: String,
        @RequestParam("category", defaultValue = "general") category: String,
        @RequestParam("chunkSize", defaultValue = "800") chunkSize: Int,
        @RequestParam("chunkOverlap", defaultValue = "100") chunkOverlap: Int,
    ): EtlResponse {
        return try {
            // 임시 파일로 저장
            val tempFile = File.createTempFile("upload", ".${file.originalFilename?.substringAfterLast('.')}")
            Files.copy(file.inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            require(file.originalFilename != null) { "파일 이름이 없습니다." }

            val metadata: Map<String, String> = mapOf(
                "type" to type,
                "category" to category,
                "filename" to file.originalFilename!!
            )

            val documentCount = etlService.processAndStoreFromFile(
                tempFile.absolutePath,
                metadata,
                chunkSize,
                chunkOverlap
            )

            // 임시 파일 삭제
            val deleted = tempFile.delete()
            if (!deleted) {
                println("임시 파일 삭제 실패: ${tempFile.absolutePath}")
            }

            EtlResponse(
                success = true,
                message = "파일 처리 완료",
                details = mapOf("processed_chunks" to documentCount)
            )
        } catch (e: Exception) {
            EtlResponse(
                success = false,
                message = "파일 처리 실패: ${e.message}"
            )
        }
    }

    @PostMapping("/search")
    fun searchDocuments(@RequestBody request: SearchRequest): SearchResponse {
        return try {
            val documents = etlService.searchSimilarDocuments(request.query, request.topK)
            SearchResponse(
                success = true,
                query = request.query,
                results = documents.map { doc ->
                    DocumentResult(
                        content = doc.text!!,
                        metadata = doc.metadata
                    )
                }
            )
        } catch (e: Exception) {
            SearchResponse(
                success = false,
                query = request.query,
                message = "검색 실패: ${e.message}"
            )
        }
    }

    @PostMapping("/process-text")
    fun processTextDirectly(@RequestBody request: ProcessTextRequest): EtlResponse {
        return try {
            // 텍스트를 임시 파일로 저장 후 처리
            val tempFile = File.createTempFile("text", ".txt")
            tempFile.writeText(request.text)

            val metadata = mapOf(
                "type" to request.type,
                "category" to request.category,
                "source" to "direct_input"
            )

            val documentCount = etlService.processAndStoreFromFile(
                tempFile.absolutePath,
                metadata,
                request.chunkSize,
                request.chunkOverlap
            )

            // 임시 파일 삭제
            val deleted = tempFile.delete()
            if (!deleted) {
                println("임시 파일 삭제 실패: ${tempFile.absolutePath}")
            }

            EtlResponse(
                success = true,
                message = "텍스트 처리 완료",
                details = mapOf("processed_chunks" to documentCount)
            )
        } catch (e: Exception) {
            EtlResponse(
                success = false,
                message = "텍스트 처리 실패: ${e.message}"
            )
        }
    }
}

data class EtlResponse(
    val success: Boolean,
    val message: String,
    val details: Map<String, Any>? = null,
)

data class SearchRequest(
    val query: String,
    val topK: Int = 5,
)

data class SearchResponse(
    val success: Boolean,
    val query: String,
    val results: List<DocumentResult>? = null,
    val message: String? = null,
)

data class DocumentResult(
    val content: String,
    val metadata: Map<String, Any>,
)

data class ProcessTextRequest(
    val text: String,
    val type: String = "document",
    val category: String = "general",
    val chunkSize: Int = 800,
    val chunkOverlap: Int = 100,
)
