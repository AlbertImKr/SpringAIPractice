package com.albert.springaipractice.service

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt
import org.springframework.ai.openai.OpenAiAudioSpeechModel
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel
import org.springframework.ai.openai.audio.speech.SpeechPrompt
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class AudioModelService(
    private val transcriptionModel: OpenAiAudioTranscriptionModel,
    private val speechModel: OpenAiAudioSpeechModel,
) {

    fun transcribeAudio(audioFile: MultipartFile): String {
        val audioInput = audioFile.resource

        val transcriptionPrompt = AudioTranscriptionPrompt(audioInput)

        val response = transcriptionModel.call(transcriptionPrompt)
        return response.result.output
    }

    fun textToSpeech(text: String): Resource {

        val speechPrompt = SpeechPrompt(text)

        val response = speechModel.call(speechPrompt)
        return ByteArrayResource(response.result.output)
    }
}
