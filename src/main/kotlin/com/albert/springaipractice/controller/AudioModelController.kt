package com.albert.springaipractice.controller

import com.albert.springaipractice.service.AudioModelService
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class AudioModelController(private val audioModelService: AudioModelService) {

    @PostMapping("/api/audio/transcribe")
    fun transcribeAudio(@RequestPart audio: MultipartFile): String {
        return audioModelService.transcribeAudio(audio)
    }

    @PostMapping("/api/audio/speech")
    fun textToSpeech(@RequestBody content: UserInput): ResponseEntity<Resource> {
        val resource = audioModelService.textToSpeech(content.question)
        MediaType.MULTIPART_FORM_DATA_VALUE
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("audio/mp3"))
            .header(HttpHeaders.CONTENT_DISPOSITION)
            .body(resource)
    }
}
