package com.albert.springaipractice.controller

import com.albert.springaipractice.service.AudioModelService
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class AudioModelController(private val audioModelService: AudioModelService) {

    @PostMapping("/api/audio/transcribe")
    fun transcribeAudio(@RequestParam("audio") audioFile: MultipartFile): String {
        return audioModelService.transcribeAudio(audioFile)
    }

    @PostMapping("/api/audio/speech")
    fun textToSpeech(@RequestParam("text") text: String): ResponseEntity<Resource?> {
        val resource = audioModelService.textToSpeech(text)
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("audio/mp3"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"speech.mp3\"")
            .body(resource)
    }
}
