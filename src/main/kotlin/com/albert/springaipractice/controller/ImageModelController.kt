package com.albert.springaipractice.controller

import com.albert.springaipractice.service.ImageModelService
import org.springframework.ai.image.Image
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class ImageModelController(private val imageModelService: ImageModelService) {

    @PostMapping("/api/image/analyze")
    fun analyzeImage(@RequestPart image: MultipartFile, @RequestPart input: UserInput): String {
        return imageModelService.analyzeImage(image, input.question)
    }

    @PostMapping("/api/image/analyze-url")
    fun analyzeImage(@RequestBody imageUrlAnalyzeRequest: ImageUrlAnalyzeRequest): String {
        return imageModelService.analyzeImageUrl(imageUrlAnalyzeRequest.imageUrl, imageUrlAnalyzeRequest.question)
    }

    @PostMapping("/api/image/generate")
    fun generateImage(@RequestBody input: UserInput): Image {
        return imageModelService.generateImage(input.question)
    }
}

data class ImageUrlAnalyzeRequest(val imageUrl: String, val question: String)
