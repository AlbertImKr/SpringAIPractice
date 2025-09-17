package com.albert.springaipractice.service

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class AudioModelService(val transcriptionModel: OpenAiAudioTranscriptionModel) {

    fun transcribeAudio(audioFile: MultipartFile): String {
        val audioInput = audioFile.resource

        val transcriptionPrompt = AudioTranscriptionPrompt(audioInput)

        val response = transcriptionModel.call(transcriptionPrompt)
        return response.result.output
    }
}
