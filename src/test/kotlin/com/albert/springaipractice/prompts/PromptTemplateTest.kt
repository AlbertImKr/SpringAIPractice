package com.albert.springaipractice.prompts

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.template.st.StTemplateRenderer

class PromptTemplateTest {

    @Test
    fun testStPromptTemplate() {
        val template = PromptTemplate.builder()
            .template("다음 {text} 문장을 영어로 번역해줘")
            .build()

        val prompt = template.render(mapOf("text" to "안녕, 어떻게 지내?"))

        assertThat(prompt).isEqualTo("다음 안녕, 어떻게 지내? 문장을 영어로 번역해줘")
    }

    @Test
    fun testStPromptTemplateWithOtherDelimiter() {
        val template = PromptTemplate.builder()
            .template("다음 <text> 문장을 영어로 번역해줘")
            .build()

        val prompt = template.render(mapOf("text" to "안녕, 어떻게 지내?"))

        assertThat(prompt).isEqualTo("다음 <text> 문장을 영어로 번역해줘")
    }

    @Test
    fun testCustomDelimiterPromptTemplate() {
        val template = PromptTemplate.builder()
            .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
            .template("다음 <text> 문장을 영어로 번역해줘")
            .build()

        val prompt = template.render(mapOf("text" to "안녕, 어떻게 지내?"))

        assertThat(prompt).isEqualTo("다음 안녕, 어떻게 지내? 문장을 영어로 번역해줘")
    }
}
