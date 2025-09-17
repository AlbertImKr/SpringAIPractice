package com.albert.springaipractice.controller

import com.albert.springaipractice.service.AudioModelService
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
}
