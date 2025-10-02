# Spring AI Practice

- [Chat Model API](#chat-model-api)
- [Chat Client API](#chat-client-api)
- [Prompts](#prompts)
- [Structured Output Converter](#structured-output-converter)
- [Audio Models](#audio-models)

## Chat Model API

Spring AI는 다양한 Chat Model AI와 연동하여 개발자에게 AI 기반 채팅 완성 기능을 애플리케이션에 통합할 수 있는 기능을 제공합니다. 간편하고 휴대성이 뛰어난 인터페이스로 설계되어 개발자는 최소한의
코드 변경으로 여러 모델 간을 전환할 수 있습니다.

### 기본 Flow

```Mermaid
flowchart LR
    A[Prompt] -- 사용자 입력 --- B[ChatModel]
    A -- 사용자 입력 --- C[StreamingChatModel]
    B -- 모델 응답 --- D[ChatResponse]
    C -- 모델 응답 --- D
```

### Prompt

Spring AI에서 Prompt는 아래와 같이 구성됩니다:

- List<Message> messages: Message 객체의 리스트
- ChatOptions options: 모델에 대한 추가 옵션

#### Message

Message는 Content Interface를 extend하고 MessageType Enum를 추가하여 아래와 같이 구성됩니다:

```java

public interface Content {
    String getText();

    Map<String, Object> getMetadata();
}

public interface Message extends Content {

    MessageType getMessageType();
}
```

Message는 MessageType을 통해 Role을 구분합니다:

```java
public enum MessageType {
    SYSTEM,
    USER,
    ASSISTANT,
    TOOL_RESPONSE
}
```

기본적으로 제공되는 Message 구현체는 다음과 같습니다:

- SystemMessage: 시스템 메시지
- UserMessage: 사용자 메시지
- AssistantMessage: 어시스턴트 메시지
- ToolResponseMessage: 도구 응답 메시지

#### ChatOptions

ChatOptions는 모델에 대한 추가 옵션을 설정하는데 사용됩니다. 예를 들어, 사용 모델, 최대 토큰수, 빈도수 등을 설정할 수 있습니다.

```java
public interface ChatOptions extends ModelOptions {

    String getModel();

    Float getFrequencyPenalty();

    Integer getMaxTokens();

    Float getPresencePenalty();

    List<String> getStopSequences();

    Float getTemperature();

    Integer getTopK();

    Float getTopP();

    ChatOptions copy();

}
```

#### Prompt 예시 (kotlin)

```kotlin
    val systemMessage: SystemMessage = SystemMessage.builder()
    .text("사용자 질문에 대해 한국어로 답변을 해야 합니다.")
    .build()

val userMessage: UserMessage = UserMessage.builder()
    .text(question)
    .build()

val chatOptions: ChatOptions = ChatOptions.builder()
    .model("gpt-4o-mini")
    .temperature(0.3)
    .maxTokens(1000)
    .build()

val prompt: Prompt = Prompt.builder()
    .messages(listOf(systemMessage, userMessage))
    .chatOptions(chatOptions)
    .build()
```

### ChatResponse

ChatResponse는 모델의 응답을 나타내며 아래와 같이 구성됩니다:

- chatResponseMetadata(metadata): 응답 메타데이터
- List<Generation> generations: 모델이 생성한 응답 리스트

```java
public class ChatResponse implements ModelResponse<Generation> {

    private final ChatResponseMetadata chatResponseMetadata;
    private final List<Generation> generations;

    @Override
    public ChatResponseMetadata getMetadata() {...}

    @Override
    public List<Generation> getResults() {...}

    // 기타 메서드...
}
```

### Chat Model Interface

Chat Model Interface는 call 메서드를 통해 Prompt를 입력받아 ChatResponse를 반환합니다. 또한, 편의를 위해 단일 메시지 또는 메시지 배열을 입력받는 오버로드된 call 메서드를
제공합니다.

```java
public interface ChatModel extends Model<Prompt, ChatResponse>, StreamingChatModel {

    default String call(String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        Generation generation = call(prompt).getResult();
        return (generation != null) ? generation.getOutput().getText() : "";
    }

    default String call(Message... messages) {
        Prompt prompt = new Prompt(Arrays.asList(messages));
        Generation generation = call(prompt).getResult();
        return (generation != null) ? generation.getOutput().getText() : "";
    }

    @Override
    ChatResponse call(Prompt prompt);

    default ChatOptions getDefaultOptions() {
        return ChatOptions.builder().build();
    }

    default Flux<ChatResponse> stream(Prompt prompt) {
        throw new UnsupportedOperationException("streaming is not supported");
    }

}
```

### Chat Model 사용 예시 (kotlin)

```kotlin
    val chatResponse: ChatResponse = chatModel.call(prompt).result.output.text ?: "No response generated."
```

### 마무리

Spring AI의 Chat Model API를 사용하면 다양한 AI 모델과 쉽게 통합하여 강력한 채팅 기능을 쉽게 애플리케이션에 추가할 수 있습니다.

## Chat Client API

ChatClient는 AI Model과 통신하는 fluent API를 제공합니다. 여기서 fluent API란 메서드 체이닝을 통해 직관적이고 가독성 높은 코드를 작성할 수 있는 스타일을 의미합니다.

### Creating a ChatClient

#### ChatClient.Builder

ChatClient는 ChatClient.Builder 객체를 통해 생성할 수 있습니다. Builder는 API 키, 모델, 옵션 등을 설정하는 메서드를 제공합니다. Spring AI는 기본 자동 설정은
`ChatClient.Builder` 빈을 제공합니다.

```Kotlin
@Configuration
class AiConfig {

    @Bean
    fun chatClient(chatClientBuilder: ChatClient.Builder): ChatClient {
        return chatClientBuilder
            .defaultSystem("사용자 질문에 대해 한국어로 답변을 해야 합니다.")
            .defaultOptions(
                ChatOptions.builder()
                    .model("gpt-4o-mini")
                    .temperature(0.3)
                    .maxTokens(1000)
                    .build()
            )
            .build()
    }
}

@Service
class AiService(private val chatClientBuilder: ChatClient.Builder) {

    private val chatClient = chatClientBuilder
        .defaultSystem("사용자 질문에 대해 한국어로 답변을 해야 합니다.")
        .defaultOptions(
            ChatOptions.builder()
                .model("gpt-4o-mini")
                .temperature(0.3)
                .maxTokens(1000)
                .build()
        )
        .build()

    fun askQuestion(question: String): String {
        return chatClient.prompt()
            .user(question)
            .call()
            .content() ?: "No response generated."
    }
}
```

#### Working with Multiple Chat Models

단일 애플리케이션에서 여러 Chat Model을 사용하는 경우도 있습니다. Spring AI에서는 이 문제도 간단하게 해결할 수 있습니다.

> [!Important]
> 먼저 설정을 통해 기본 ChatClient 빈 생성을 비활성화 해야 합니다. 그렇지 않으면 동일한 타입의 빈이 두 개 이상 존재하게 되어 애플리케이션 컨텍스트 로딩에 실패합니다.
>
> ```properties
> spring.ai.chat.client.enabled=false
> ```


여러 ChatClient 빈을 생성하는 예시는 다음과 같습니다:

```Kotlin
// gradle build.gradle.kts
dependencies {
    // ...
    implementation("org.springframework.ai:spring-ai-starter-model-openai")
    implementation("org.springframework.ai:spring-ai-starter-model-anthropic")
    // ...
}

// application.properties
spring.ai.chat.client.enabled = false
spring.ai.model.openai.api - key = your - openai - api - key
spring.ai.model.anthropic.api - key = your - anthropic - api - key

// AiConfig.kt
@Configuration
class AiConfig {
    @Bean
    fun openAi(openAiModel: OpenAiChatModel): ChatClient {
        return ChatClient.create(openAiModel)
    }

    @Bean
    fun authropicChatClient(authropicChatModel: AnthropicChatModel): ChatClient {
        return ChatClient.create(authropicChatModel)
    }
}

// Service.kt
@Service
class AiService(
    @Qualifier("openAi") private val openAiChatClient: ChatClient,
    @Qualifier("authropicChatClient") private val authropicChatClient: ChatClient
) {
    // ...
}
```

### ChatClient Fluent API

ChatClient는 fluent API를 제공하여 직관적이고 가독성 높은 코드를 작성할 수 있습니다. 주로 prompt 빌더를 통해 메시지를 구성합니다.

- `prompt()`: 새로운 프롬프트 빌더를 생성합니다.
- `prompt(prompt: Prompt)`: 기존 프롬프트를 사용합니다.
- `prompt(String content)`: 사용자 메시지로 시작하는 프롬프트를 생성합니다.

### ChatClient Responses

ChatClient는 다양한 응답 형식을 지원합니다:

- ChatResponse: 기본 응답 형식
- Entity: 엔티티 추출 응답 형식
- Streaming Responses: 스트리밍 응답 형식

```kotlin
val chatResponse = chatClient.prompt()
    .user(question)
    .call()
    .chatResponse()

val entity = chatClient.prompt()
    .user(question)
    .call()
    .entity(CustomMessage::class.java)

val streamingResponses = chatClient.prompt()
    .user(question)
    .stream()
    .content()
```

### Prompt Templates

ChatClient Fluent API는 프롬프트 템플릿 기능을 제공합니다.

```kotlin
chatClient.prompt()
    .user { u ->
        u
            .text("{동적변수}를 포함한 질문입니다.")
            .param("동적변수", variableExample)
    }
    .call()
    .content()
```

> [!Note]
> ChatClient에서 직접 구성한 TemplateRenderer는 ChatClient 빌더 체인에 직접 정의한 `prompt` 콘텐츠에만 적용됩니다.
> `QuestionAnswerAdivisor`와 같은 사전 정의된 템플릿에는 적용되지 않습니다.


ChatClient에 다른 템플릿 엔진을 사용하려면 TemplateRenderer 인터페이스의 사용자 지정 구현을 제공할 수 있습니다.

```Kotlin
// 예시: prompt에 JSON을 포함되고 싶고 JSON 구문과 충돌을 피하고 싶다면 다음과 같이 기본 구분 기호 `{}` 대신 `<` 및 `>`를 사용할 수 있습니다.
chatClient.prompt()
    .user { u ->
        u
            .text("<variable>를 포함한 질문입니다.")
            .param("variable", variableExample)
    }
    .templateRenderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
    .call()
    .content() ?: NO_RESPONSE
```

### `call()` return values

`call()` 메서드는 다양한 형식의 응답을 반환합니다.

- `String content()`: 응답의 텍스트 콘텐츠를 반환합니다.
- `ChatResponse chatResponse()`: 여러 생성 결과와 메타데이터(토큰 사용량 등)를 포함하는 ChatResponse 객체를 반환합니다.
- `ChatClientResponse chatClientResponse()`: ChatResponse 객체와 ChatClient 실행 컨텍스트를 포함하는 ChatClientResponse 객체를 반환하여
  advisors 실행 중에 사용되는 추가 데이터(예: RAG flow의 문서 등)에 접근할 수 있습니다.
- `ResponseEntity<?> responseEntity()`: HTTP 응답 엔티티를 반환합니다.
- `entity(Class<T> clazz)`: 지정된 클래스 유형의 엔티티를 반환합니다.

### `stream()` return values

`stream()` 메서드는 스트리밍 응답을 반환합니다.

- `Flux<String> content()`: 응답의 텍스트 콘텐츠를 스트리밍합니다.
- `Flux<ChatResponse> chatResponse()`: 추가 메타데이터(토큰 사용량 등)를 포함하는 ChatResponse 객체를 스트리밍합니다.
- `Flux<ChatClientResponse> chatClientResponse()`: `chatClientResponse` 객체를 스트리밍합니다.

### Using defaults

`Configuration` 클래스에서 `ChatClient.Builder` 빈을 구성할 때 기본 시스템 메시지와 옵션을 설정할 수 있습니다.

- `defaultSystem(String systemMessage)`: 모든 프롬프트에 적용할 기본 시스템 메시지를 설정합니다.
- `defaultOptions(ChatOptions options)`: 모든 프롬프트에 적용할 기본 옵션을 설정합니다.
- `defaultFunction(String name, String description, java.util.function.Function<I, O> function)`
    - name: 함수 이름
    - description: 함수 설명
    - function: 함수 구현
- `defaultFunctions(String… functionNames)`: 애플리케이션 컨텍스트에 정의된 `java.util.Function` 빈의 이름을 사용하여 함수를 등록합니다.
- `defaultUser(String text), defaultUser(Resource text), defaultUser(Consumer<UserSpec> userSpecConsumer)`: 사용자 메시지를
  설정합니다.
- `defaultAdvisors(Advisor… advisor)`: 어드바이저를 추가합니다.
- `defaultAdvisors(Consumer<AdvisorSpec> advisorSpecConsumer)`: `AdvisorSpec`을 사용하여 어드바이저를 추가합니다.

```Kotlin
@Configuration
class AiConfig {

    @Bean
    fun chatClient(builder: ChatClient.Builder): ChatClient {
        return builder.defaultSystem("한국어로 대답해줘").build()
    }
}
```

### Advisors

`Advisors API`는 Spring AI에서 가로채고 수정하고 확장할 수 있는 강력한 메커니즘을 제공합니다. RAG(Retrieval-Augmented Generation) 패턴 구현, 로깅, 프롬프트 보강 등
다양한 용도로 사용할 수 있습니다.

사용자 텍스트로 AI 모델을 호출할 때 일반적인 패턴은 프롬포트에 데이터를 추가하거나 보강하는 것입니다. 이러한 데이터는 일반적으로 다음과 같은 형태를 취합니다:

- 단독 데이터: AI 모델이 학습하지 않은 데이터
- 대화 기록: 채팅 모델의 API는 `stateless`이므로 이전 대화 내용이 포함되지 않습니다. 따라서 이전 대화 내용을 프롬프트에 추가할 필요가 있을 수 있습니다.

### 마무리

ChatClient API를 사용하면 Fluent API를 통해 직관적이고 가독성 높은 코드를 작성할 수 있습니다.

## Prompts

Prompts는 AI 모델이 특정 출력을 생성하도록 안내하는 데 사용하는 입력 텍스트입니다. 그래서 효과적인 프롬프트를 작성하는 것은 원하는 결과를 얻는 데 매우 중요합니다. Spring AI는 개발자가 쉽게
프롬프트를 생성하고 관리할 수 있도록 지원합니다.

### Prompt

Spring AI에서 Prompt는 아래와 같이 구성됩니다:

- List<Message> messages: Message 객체의 리스트
- ChatOptions options: 모델에 대한 추가 옵션

```java
public class Prompt implements ModelRequest<List<Message>> {

    private final List<Message> messages;

    private ChatOptions chatOptions;
}
```

### Message

Message는 Prompt의 구성 요소로 Content + Role을 포함합니다.

```java
public interface Content {
    String getText();

    Map<String, Object> getMetadata();
}

public interface Message extends Content {

    MessageType getMessageType();
}
```

Message는 MessageType을 통해 Role을 구분합니다:

```java
public enum MessageType {

    USER("user"),

    ASSISTANT("assistant"),

    SYSTEM("system"),

    TOOL("tool");

    // ...
}
```

### PromptTemplate

Spring AI에서 구조화 된 프롬프트를 생성하기 위해 `PromptTemplate` 인터페이스를 제공합니다.

```java
public class PromptTemplate implements PromptTemplateActions, PromptTemplateMessageActions {

    // ...
}
```

`PromptTemplate`은 `TemplateRenderer`를 사용하여 템플릿을 렌더링합니다. Default로 `StTemplateRenderer`를 사용합니다.

```java
public interface TemplateRenderer extends BiFunction<String, Map<String, Object>, String> {

    @Override
    String apply(String template, Map<String, Object> variables);

}
```

Custom StringTemplate Renderer 예시

```Kotlin
        val template = PromptTemplate.builder()
    .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
    .template("다음 <text> 문장을 영어로 번역해줘")
    .build()

val prompt = template.render(mapOf("text" to "안녕, 어떻게 지내?"))

assertThat(prompt).isEqualTo("다음 안녕, 어떻게 지내? 문장을 영어로 번역해줘")
```

PromptTemplate 구현하는 interface는 다른 역할을 합니다:

- `PromptTemplateActions`: Prompt 생성에 사용되는 메서드를 제공합니다.

```java
public interface PromptTemplateActions extends PromptTemplateStringActions {

    Prompt create();

    Prompt create(ChatOptions modelOptions);

    Prompt create(Map<String, Object> model);

    Prompt create(Map<String, Object> model, ChatOptions modelOptions);

}
```

- `PromptTemplateStringActions`: 최종 문자열을 렌더링하는 메서드를 제공합니다.

```Java
public interface PromptTemplateStringActions {

    String render();

    String render(Map<String, Object> model);

}
```

- `PromptTemplateMessageActions`: Message 객체를 생성하는 메서드를 제공합니다.

```java
public interface PromptTemplateMessageActions {

    Message createMessage();

    Message createMessage(List<Media> mediaList);

    Message createMessage(Map<String, Object> model);

}
```

### Creating effective prompts

명확하고 효과적인 프롬프트를 보장하기 위해 몇 가지 핵심 구성 요소를 통합하는 것이 중요합니다:

- Instructions: 모델이 수행할 작업에 대한 명확한 지침
- External Context: 필요의 따라 관련 정보나 데이터
- User Input: 유저의 직접적인 요청
- Output Indicator: 모델이 생성해야 하는 출력 유형을 지정하는 지시문

#### Simple Techniques Keywords

- Text Summarization: 텍스트 요약
- Question Answering: 질문 답변
- Text Classification: 텍스트 분류
- Conversation: 대화 생성
- Code Generation: 코드 생성

#### Advanced Techniques Keywords

- Zero-shot Learning: 사전 학습된 지식만을 사용하여 새로운 작업 수행
- Few-shot Learning: 몇 가지 예시를 제공하여 모델이 새로운 작업을 수행하도록 학습
- Chain-of-Thought Prompting: 복잡한 문제 해결을 위해 모델이 단계별로 사고하도록 유도
- Role-playing Prompts: 모델이 특정 역할을 맡아 행동하도록 유도
- Microsoft Guidance: Framework for Prompt Creation and Optimization

### 마무리

효과적인 프롬프트 작성은 원하는 결과를 얻는 데 매우 중요합니다. Spring AI의 Prompt 및 PromptTemplate 기능을 활용하여 명확하고 구조화된 프롬프트를 생성할 수 있습니다.

## Structured Output Converter

일반적으로 AI 모델의 결과를 JSON, XML 또는 Java 클래스와 같은 구조화된 형식으로 변환하는 것이 필요합니다. Spring AI는 이러한 변환을 쉽게 처리할 수 있는 Structured Output
Converter 기능을 제공합니다.

### Structured Output API

`StructuredOutputConverter` 인터페이스는 AI 모델의 출력의 최상위의 인터페이스입니다. 그리고 Spring의 `Converter<String, T>` 인터페이스와 `FormatProvider`
인터페이스를 확장합니다.

```java
public interface StructuredOutputConverter<T> extends Converter<String, T>, FormatProvider {

}
```

다음 다이어그램은 흐름을 보여줍니다:

![Structured Output Converter](assets/structuredoutputconverter.png)
> 출처: https://docs.spring.io/spring-ai/reference/api/structured-output-converter.html

- FormatProvider: AI 모델에 특정 출력 형식을 지정하는 데 사용됩니다.
- Converter<String, T>: AI 모델의 문자열 출력을 원하는 형식 T로 변환하는 데 사용됩니다.

### Available Converters

Spring AI는 다양한 구조화된 출력 형식을 지원하는 여러 기본 제공 변환기를 제공합니다:

![Available Converters](assets/availableconverters.png)
> 출처: https://docs.spring.io/spring-ai/reference/api/structured-output-converter.html

- `AbstractConversionServiceOutputConverter<T>`: 사전 정의된 `GenericConversionService`만 제공합니다.
- `AbstractMessageOutputConverter<T>`: 사전 정의된 `MessageConverter`만 제공합니다.
- `BeanOutputConverter<T>`: 지정한 Java 클래스으로 파생된 `DRAFT_2020_12` JSON 스키마를 생성할 수 있습니다. AI 모델에 이 스키마를 제공하여 구조화된 출력을 생성하도록 할
  수 있습니다. 또 이 컨버터는 AI 모델의 JSON 출력을 지정한 Java 클래스로 변환할 수 있습니다.
- `MapOutputConverter`: RFC82959 호환 JSON 응답을 생성하도록 AI 모델에 지시할 수 있습니다. 또한 AI 모델의 JSON 출력을 `Map<String, Object>`로 변환할 수
  있습니다.
- `ListOutputConverter`: 쉼표로 구분된 목록 출력을 생성하도록 AI 모델에 지시할 수 있습니다. 또한 AI 모델의 쉼표로 구분된 목록 출력을 `java.util.List`로 변환할 수 있습니다.

### Test with Converters

```kotlin
class StructuredOutputConverterTest {

    @Test
    fun beanOutputConverterTest() {
        val converter = BeanOutputConverter(Person::class.java)
        val input = """
            {
              "name": "John Doe",
              "age": 30
            }
        """.trimIndent()

        val person = converter.convert(input) ?: throw IllegalArgumentException("Conversion failed")

        assertThat(person.name).isEqualTo("John Doe")
        assertThat(person.age).isEqualTo(30)

        val format = converter.getFormat()

        println(format)

        val schemaMap = converter.jsonSchemaMap

        println(schemaMap)
    }

    @Test
    fun mapOutputConverterTest() {
        val converter = MapOutputConverter()

        val input = """
            {
              "name": "Jane Doe",
              "age": 25
            }
        """.trimIndent()

        val resultMap = converter.convert(input) ?: throw IllegalArgumentException("Conversion failed")

        println(resultMap)
        assertThat(resultMap["name"]).isEqualTo("Jane Doe")
        assertThat(resultMap["age"]).isEqualTo(25)

        val format = converter.getFormat()
        println(format)
    }

    @Test
    fun listOutputConverterTest() {
        val converter = ListOutputConverter()

        val input = """
            Allice, Albert, Bob, Carol
        """.trimIndent()

        val resultList = converter.convert(input) ?: throw IllegalArgumentException("Conversion failed")

        println(resultList)
        assertThat(resultList).hasSize(4)

        val format = converter.format
        println(format)
    }

    class Person(val name: String, val age: Int)
}
```

### 마무리

Structured Output Converter 기능을 사용하면 AI 모델의 출력을 다양한 구조화된 형식으로 쉽게 변환할 수 있습니다.

## Audio Models

Spring AI는 Transcription 및 Text-to-Speech(TTS) API를 지원합니다.

### Transcription API

Spring AI(1.0.2) 기준 OpenAI Audio Transcription과 Azure OpenAIAudio Transcription를 간편하게 사용할 수 있습니다. OpenAI Audio
Transcription과 Azure OpenAIAudio Transcription은 비슷하기 때문에 OpenAI Audio Transcription 예시를 통해 설명합니다.

#### Spring AI Starter OpenAI Audio Transcription

OpenAI Audio Transcription 지원하는 파일 형식: flac, mp3, mp4, mpeg, mpga, m4a, ogg, wav, or webm.

기본 설정은 `application.properties` 또는 `application.yml`에서 수정할 수 있습니다.

```properties
# 현재 OpenAI에서 whisper-1 모델만 지원, default=whisper-1
spring.ai.openai.audio.transcription.options.model=whisper-1
# json, text, srt, verbose_json, or vtt, default=text
spring.ai.openai.audio.transcription.options.responseFormat=TEXT
# ISO 639-1 언어 코드, null이면 자동 감지, default=null
spring.ai.openai.audio.transcription.options.language=null
# 0.0 ~ 1.0 사이의 값, default=0.7
spring.ai.openai.audio.transcription.options.temperature=0.7
# word 또는 sentence, null이면 자동 설정, default=null
spring.ai.openai.audio.transcription.options.granularity-type=null
# OpenAI API 기본 URL, default=api.openai.com
spring.ai.openai.audio.transcription.base-url=api.openai.com
```

개별 설정을 하려면 `OpenAiAudioTranscriptionOptions`를 사용하여 `Prompt`에 전달할 수 있습니다.

```kotlin
val audioOptions = OpenAiAudioTranscriptionOptions.builder()
    .model("whisper-1")
    .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.VERBOSE_JSON)
    .temperature(0.7f)
    .language("ko")
    .build()

val transcriptionPrompt = AudioTranscriptionPrompt(audioInput, audioOptions)
```

> 참조: OpenAI Audio Transcription API
> https://platform.openai.com/docs/api-reference/audio/createTranscription

OpenAI Audio Transcription 사용 예시

```kotlin
fun transcribeAudio(audioFile: MultipartFile): String {
    val audioInput = audioFile.resource

    val transcriptionPrompt = AudioTranscriptionPrompt(audioInput)

    val response = transcriptionModel.call(transcriptionPrompt)
    return response.result.output
}
```

### Text-to-Speech (TTS) API

기본 설정은 application.properties 또는 application.yml에서 수정할 수 있습니다.

```Properties

spring.ai.model.audio.speech=openai
spring.ai.openai.audio.speech.base-url=api.openai.com
# tts-1, tts-1-hd; default=tts-1
spring.ai.openai.audio.speech.options.model=tts-1
# alloy, echo, fable, onyx, nova, and shimmer; default=alloy
spring.ai.openai.audio.speech.options.voice=alloy
# mp3, opus, aac, flac, wav, and pcm; default=mp3
spring.ai.openai.audio.speech.options.response-format=mp3
# 0.25 (slowest) to 4.0 (fastest); default=1.0
spring.ai.openai.audio.speech.options.speed=1.0
```

개별 설정을 하려면 `OpenAiAudioSpeechOptions`를 사용하여 `Prompt`에 전달할 수 있습니다.

```kotlin
val speedOptions = OpenAiAudioSpeechOptions.builder()
    .model("tts-1")
    .voice(OpenAiAudioApi.SpeechRequest.Voice.ALLOY)
    .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
    .speed(1.0f)
    .build()

val speechPrompt = SpeechPrompt(text, speedOptions)
```

> 참조: OpenAI Create Speech API
> https://platform.openai.com/docs/api-reference/audio/createSpeech

OpenAI Text-to-Speech 사용 예시

```kotlin
fun textToSpeech(text: String): Resource {
    val speechPrompt = SpeechPrompt(text)

    val response = speechModel.call(speechPrompt)
    return ByteArrayResource(response.result.output)
}
```

Streaming TTS 사용 예시

```kotlin
fun streamTextToSpeech(text: String): Flux<ByteArray> {
    val speechPrompt = SpeechPrompt(text)

    return speechModel.stream(speechPrompt)
        .map { it.result.output }
}
```

### 마무리

Spring AI의 Audio Models 기능은 아직 그렇게 드라마 틱하지는 않는 것 같고 주로 OpenAI 만 지원해서 기능이 제한적인것 같습니다. ChatClient API와 같이 상위 Interface를
제공하지도 않아 Spring 스럽지 않은 느낌이 듭니다. 이후 버전에서 개선되길 기대합니다.

## Image Model API

Spring AI는 Image Model API를 통해 다양한 AI 기반 이미지 생성 모델과 통합할 수 있는 기능을 제공합니다.

### Image Model Interface

Image Model Interface는 call 메서드를 통해 ImagePrompt를 입력받아 ImageResponse를 반환합니다. Model 인터페이스를 확장하여 일관된 구조를 유지합니다.

```java

@FunctionalInterface
public interface ImageModel extends Model<ImagePrompt, ImageResponse> {

    ImageResponse call(ImagePrompt request);

}
```

### ImagePrompt

ImagePrompt는 ModelRequest 인터페이스를 구현하며, 이미지 생성에 필요한 메시지와 옵션을 포함합니다.

```Java
public class ImagePrompt implements ModelRequest<List<ImageMessage>> {

    private final List<ImageMessage> messages;

    private ImageOptions imageModelOptions;

    @Override
    public List<ImageMessage> getInstructions() {...}

    @Override
    public ImageOptions getOptions() {...}

    // constructors and utility methods omitted
}
```

### ImageMessage

ImageMessage는 이미지 생성에 필요한 텍스트와 가중치를 포함합니다.

```Java
public class ImageMessage {

    private String text;

    private Float weight;

    public String getText() {...}

    public Float getWeight() {...}

    // constructors and utility methods omitted
}
```

### ImageOptions

ImageOptions는 ModelOptions 인터페이스를 확장하며, 이미지 생성에 필요한 다양한 옵션을 제공합니다.

```Java
public interface ImageOptions extends ModelOptions {

    Integer getN();

    String getModel();

    Integer getWidth();

    Integer getHeight();

    String getResponseFormat(); // openai - url or base64 : stability ai byte[] or base64

}
```

### ImageResponse

ImageResponse는 ModelResponse 인터페이스를 구현하며, 이미지 생성 결과와 메타데이터를 포함합니다.

```Java
public class ImageResponse implements ModelResponse<ImageGeneration> {

    private final ImageResponseMetadata imageResponseMetadata;

    private final List<ImageGeneration> imageGenerations;

    @Override
    public ImageGeneration getResult() {
        // get the first result
    }

    @Override
    public List<ImageGeneration> getResults() {...}

    @Override
    public ImageResponseMetadata getMetadata() {...}

    // other methods omitted

}
```

### ImageGeneration

ImageGeneration는 ModelResult 인터페이스를 구현하며, 개별 이미지 생성 결과와 메타데이터를 포함합니다.

```Java
public class ImageGeneration implements ModelResult<Image> {

    private ImageGenerationMetadata imageGenerationMetadata;

    private Image image;

    @Override
    public Image getOutput() {...}

    @Override
    public ImageGenerationMetadata getMetadata() {...}

    // other methods omitted

}
```

### Available Image Models

Spring AI（1.0.2）기준 공식적으로 지원하는 모델은 아래와 같습니다:

- OpenAI Image Generation
- Azure OpenAI Image Generation
- QianFan Image Generation
- Stability AI Image Generation
- ZhiPuAI Image Generation

### OpenAI Image Model Analysis Example

OpenAI 이미지 분석은 OpenAI Chat Model API의 멀티모달 기능을 활용하여 이미지와 텍스트를 함께 처리할 수 있습니다. Spring AI에서는 OpenAI Chat Model 혹은
ChatClient API를 사용하여 이미지 분석을 수행할 수 있습니다.

```kotlin
fun analyzeImage(image: MultipartFile, question: String): String {

    val resource = InputStreamResource(image.inputStream)
    val mimeType = MimeType.valueOf(image.contentType ?: MediaType.IMAGE_JPEG_VALUE)

    return openAiGPT4OMini.prompt()
        .user { userSpec ->
            userSpec
                .text(question)
                .media(mimeType, resource)
        }
        .call()
        .content() ?: "No response generated."
}

fun analyzeImageUrl(imageUrl: String, question: String): String {

    val systemMessage = SystemMessage("한국어로 대답해줘")
    val url = URI(imageUrl)

    val userMessage = UserMessage.builder()
        .text(question)
        .media(Media(MimeTypeUtils.IMAGE_PNG, url))
        .build()

    val prompt = Prompt(systemMessage, userMessage)

    return openAiModel.call(prompt).result.output.text ?: "No response generated."
}
```

### OpenAI Image Generation Example

```kotlin
fun generateImage(question: String): Image {
    val prompt = ImagePrompt(
        question,
        OpenAiImageOptions.builder()
            .height(1024)
            .width(1024)
            .build()
    )

    val result = imageModel.call(prompt).result

    return result.output
}
```

### 마무리

Spring AI에서 공식적으로 지원하는 기능은 Image Genration과 멀티모달 기능을 활용한 이미지 분석입니다. 이미지 편집, 변환 등은 아직 지원하지 않는 것 같습니다. 이후 버전에서 개선되길 기대합니다.

## Adivisors API

Spring AI Advisors API는 Spring AI에서 가로채고 수정하고 확장할 수 있는 강력한 메커니즘을 제공합니다.

### Core Components

![advisors-core-components.png](assets/advisors-core-components.png)
> 출처: https://docs.spring.io/spring-ai/reference/api/advisors.html#_core_components

- advisorCall(), advisorStream(): Advisor의 핵심 메서드로 플롬프트를 보내기전에 검사, 정의, 확장하고 응답을 받은 후 응답 검사, 처리 오류 처리 등을 수행합니다.
- getOrder(): Advisor의 실행 순서를 정의합니다. 낮은 값이 먼저 실행됩니다.
- getName(): Advisor의 이름을 반환합니다.

### Advisors Flow

![advisors-flow.png](assets/advisors-flow.png)
> 출처: https://docs.spring.io/spring-ai/reference/api/advisors.html#_core_components

### Default Advisors

Spring AI는 몇 가지 기본 Advisors를 제공합니다:

- SimpleLoggerAdvisor: 요청 및 응답을 로깅합니다.

    ```Java
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        // 1. 요청 로깅
        // AI 모델 호출 전, 사용자 요청 내용을 로그에 기록
        // (디버깅, 모니터링, 감사 목적)
        logRequest(chatClientRequest);
    
        // 2. 다음 Advisor 또는 실제 AI 모델 호출
        // Chain of Responsibility 패턴: 다음 체인으로 요청 전달
        // 여러 Advisor가 있다면 순차적으로 실행되고, 마지막에는 실제 AI 모델이 호출됨
        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);
    
        // 3. 응답 로깅
        // AI 모델 호출 후, 생성된 응답 내용을 로그에 기록
        // (성능 측정, 응답 품질 분석, 문제 추적 목적)
        logResponse(chatClientResponse);
    
        // 4. 응답 반환
        // 로깅 완료 후 원본 응답을 그대로 반환 (응답 수정 없음)
        return chatClientResponse;
    }
    ```

- VectorStoreChatMemoryAdvisor: 벡터 DB에 대화를 저장하고 의미 유사도 검색으로 관련 과거 대화만 선택적으로 가져와 프롬프트에 추가합니다.
    - before(): 프롬프트를 보내기 전에 벡터 저장소에서 관련 문서를 검색하고 시스템 메시지에 추가합니다.
    - after(): 모델의 응답을 받은 후, 새로운 사용자 메시지를 벡터 저장소에 저장합니다.

        ```Java
        @Override
        public ChatClientRequest before(ChatClientRequest request, AdvisorChain advisorChain) {
            // 1. 대화 컨텍스트 정보 추출
            // 현재 대화 세션의 고유 ID 획득 (없으면 기본값 사용)
            String conversationId = getConversationId(request.context(), this.defaultConversationId);
        
            // 사용자 메시지 텍스트 추출 (null인 경우 빈 문자열 사용)
            String query = request.prompt().getUserMessage() != null ?
                    request.prompt().getUserMessage().getText() : "";
        
            // 검색할 메모리 개수(topK) 결정 (컨텍스트에서 가져오거나 기본값 사용)
            int topK = getChatMemoryTopK(request.context());
        
            // 2. 벡터 저장소에서 관련 대화 검색
            // 같은 대화 세션의 메모리만 검색하도록 필터 설정
            String filter = DOCUMENT_METADATA_CONVERSATION_ID + "=='" + conversationId + "'";
        
            // 검색 요청 객체 생성: 사용자 질문과 의미적으로 유사한 과거 대화 검색
            var searchRequest = org.springframework.ai.vectorstore.SearchRequest.builder()
                    .query(query)              // 검색 쿼리 (현재 사용자 메시지)
                    .topK(topK)                // 상위 K개 결과만 반환
                    .filterExpression(filter)  // 대화 ID 필터 적용
                    .build();
        
            // 벡터 유사도 검색 실행 - 관련성 높은 과거 대화 가져오기
            java.util.List<org.springframework.ai.document.Document> documents = this.vectorStore
                    .similaritySearch(searchRequest);
        
            // 3. Long-term Memory 문자열 생성
            // 검색된 과거 대화들을 하나의 문자열로 결합 (각 대화는 줄바꿈으로 구분)
            String longTermMemory = documents == null ? ""
                    : documents.stream()
                    .map(org.springframework.ai.document.Document::getText)  // 각 문서의 텍스트 추출
                    .collect(java.util.stream.Collectors.joining(System.lineSeparator()));  // 줄바꿈으로 연결
        
            // 4. 시스템 프롬프트에 메모리 추가 (RAG 패턴의 "증강" 단계)
            // 기존 시스템 메시지 가져오기
            org.springframework.ai.chat.messages.SystemMessage systemMessage = request.prompt().getSystemMessage();
        
            // 템플릿을 사용해 시스템 메시지와 long-term memory를 결합
            // 예: "지시사항: {instructions}\n과거 대화: {long_term_memory}"
            String augmentedSystemText = this.systemPromptTemplate
                    .render(java.util.Map.of("instructions", systemMessage.getText(),
                            "long_term_memory", longTermMemory));
        
            // 5. 증강된 시스템 메시지로 새로운 요청 객체 생성
            // mutate()를 사용해 기존 요청을 복사하고 프롬프트만 수정
            ChatClientRequest processedChatClientRequest = request.mutate()
                    .prompt(request.prompt().augmentSystemMessage(augmentedSystemText))
                    .build();
        
            // 6. 현재 사용자 메시지를 벡터 저장소에 저장 (향후 검색을 위해)
            org.springframework.ai.chat.messages.UserMessage userMessage = processedChatClientRequest.prompt()
                    .getUserMessage();
        
            if (userMessage != null) {
                // 사용자 메시지를 Document로 변환하여 벡터 DB에 저장
                // 나중에 이 대화를 검색할 수 있도록 인덱싱
                this.vectorStore.write(toDocuments(java.util.List.of(userMessage), conversationId));
            }
        
            // 7. 메모리가 증강된 요청 객체 반환
            // 이제 AI는 과거 대화 컨텍스트를 포함한 프롬프트로 응답을 생성함
            return processedChatClientRequest;
        }

        @Override
        public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
            // 1. AI 어시스턴트 응답 메시지 추출
            // 빈 리스트로 초기화 (응답이 없는 경우 대비)
            List<Message> assistantMessages = new ArrayList<>();
        
            // chatResponse가 null이 아닌 경우에만 처리 (null 안전성 체크)
            if (chatClientResponse.chatResponse() != null) {
                // 2. 응답 결과에서 모든 어시스턴트 메시지 추출
                assistantMessages = chatClientResponse.chatResponse()
                        .getResults()                           // 모든 생성 결과 가져오기 (스트리밍의 경우 여러 개일 수 있음)
                        .stream()                               // 스트림으로 변환
                        .map(g -> (Message) g.getOutput())      // 각 결과에서 실제 메시지 객체 추출
                        .toList();                              // List로 수집
            }
        
            // 3. AI 응답을 벡터 저장소에 저장 (Long-term Memory 구축)
            // 현재 대화의 conversationId와 함께 저장하여 나중에 검색 가능하도록 함
            this.vectorStore.write(
                    toDocuments(assistantMessages,  // 메시지들을 Document 형식으로 변환
                            this.getConversationId(chatClientResponse.context(), this.defaultConversationId))  // 대화 ID 추출
            );
        
            // 4. 원본 응답 객체를 그대로 반환
            // after() 메서드는 응답을 수정하지 않고, 저장만 수행함
            return chatClientResponse;
        }
        ```

- PromptChatMemoryAdvisor: 이는 VectorStoreChatMemoryAdvisor와 유사하지만, 벡터 저장소 대신 메모리 내에서 대화 기록을 관리합니다.
    - before(): 프롬프트를 보내기 전에 메모리에서 관련 대화를 검색하고 시스템 메시지에 추가합니다.
    - after(): 모델의 응답을 받은 후, 새로운 사용자 메시지를 메모리에 저장합니다.

        ```java
        @Override
        public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
            // 현재 대화 세션의 고유 ID 획득 (없으면 기본값 사용)
            String conversationId = getConversationId(chatClientRequest.context(), this.defaultConversationId);
                  
            // 1. 현재 대화의 메모리(대화 히스토리) 조회
            // ChatMemory에서 이 대화 세션의 모든 과거 메시지 가져오기
            List<Message> memoryMessages = this.chatMemory.get(conversationId);
                  
            // 디버그 로그: 메모리 처리 전 상태 기록
            logger.debug("[PromptChatMemoryAdvisor.before] Memory before processing for conversationId={}: {}",
                  conversationId, memoryMessages);
              
            // 2. 메모리 메시지를 문자열 형식으로 변환
            // USER와 ASSISTANT 메시지만 필터링하여 "역할:내용" 형식으로 변환
            String memory = memoryMessages.stream()
               .filter(m -> m.getMessageType() == MessageType.USER || 
                            m.getMessageType() == MessageType.ASSISTANT)  // 시스템 메시지 등 제외
               .map(m -> m.getMessageType() + ":" + m.getText())          // "USER:안녕하세요" 형식으로 변환
               .collect(Collectors.joining(System.lineSeparator()));      // 각 메시지를 줄바꿈으로 구분
              
            // 3. 시스템 메시지에 대화 메모리 추가 (컨텍스트 증강)
            // 기존 시스템 메시지 가져오기
            SystemMessage systemMessage = chatClientRequest.prompt().getSystemMessage();
                  
            // 템플릿을 사용해 시스템 지시사항과 대화 히스토리 결합
            // 예: "지시사항: {instructions}\n대화 기록: {memory}"
            String augmentedSystemText = this.systemPromptTemplate
               .render(Map.of("instructions", systemMessage.getText(), 
                              "memory", memory));
              
            // 4. 증강된 시스템 메시지로 새로운 요청 객체 생성
            // mutate()를 사용해 기존 요청을 복사하고 프롬프트만 수정
            ChatClientRequest processedChatClientRequest = chatClientRequest.mutate()
               .prompt(chatClientRequest.prompt().augmentSystemMessage(augmentedSystemText))
               .build();
              
            // 5. 현재 사용자 메시지를 대화 메모리에 추가
            // (시스템 메시지 생성 후에 추가 - 현재 메시지는 다음 요청에서 사용됨)
            UserMessage userMessage = processedChatClientRequest.prompt().getUserMessage();
                  
            // ChatMemory에 현재 사용자 메시지 저장 (다음 대화에서 컨텍스트로 활용)
            this.chatMemory.add(conversationId, userMessage);
              
            // 6. 메모리가 증강된 요청 객체 반환
            // AI는 이제 전체 대화 히스토리를 포함한 컨텍스트로 응답 생성
            return processedChatClientRequest;
        }

      @Override
      public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
          // 1. AI 어시스턴트 응답 메시지를 담을 리스트 초기화
          List<Message> assistantMessages = new ArrayList<>();
          
          // 2. 스트리밍 응답 처리 (단일 결과가 있는 경우)
          // 스트리밍 모드에서는 하나의 결과만 반환되므로 별도 처리
          if (chatClientResponse.chatResponse() != null && 
              chatClientResponse.chatResponse().getResult() != null &&
              chatClientResponse.chatResponse().getResult().getOutput() != null) {
             // 단일 결과를 리스트로 래핑
             assistantMessages = List.of((Message) chatClientResponse.chatResponse().getResult().getOutput());
          }
          // 3. 일반(비스트리밍) 응답 처리 (여러 결과가 있을 수 있는 경우)
          else if (chatClientResponse.chatResponse() != null) {
             // 모든 생성 결과에서 어시스턴트 메시지 추출
             assistantMessages = chatClientResponse.chatResponse()
                .getResults()                           // 모든 결과 가져오기
                .stream()                               // 스트림 변환
                .map(g -> (Message) g.getOutput())      // 각 결과에서 메시지 추출
                .toList();                              // List로 수집
          }
      
          // 4. 추출된 어시스턴트 메시지가 있으면 메모리에 추가
          if (!assistantMessages.isEmpty()) {
             // 현재 대화 세션의 메모리에 AI 응답 저장
             this.chatMemory.add(
                   this.getConversationId(chatClientResponse.context(), this.defaultConversationId),
                   assistantMessages
             );
      
             // 5. 디버그 로깅 (디버그 레벨이 활성화된 경우에만)
             if (logger.isDebugEnabled()) {
                // 메모리에 추가된 ASSISTANT 메시지 로그
                logger.debug(
                      "[PromptChatMemoryAdvisor.after] Added ASSISTANT messages to memory for conversationId={}: {}",
                      this.getConversationId(chatClientResponse.context(), this.defaultConversationId),
                      assistantMessages
                );
                
                // 메모리에 추가 후 전체 대화 히스토리 조회
                List<Message> memoryMessages = this.chatMemory
                   .get(this.getConversationId(chatClientResponse.context(), this.defaultConversationId));
                
                // 현재 메모리 상태 전체 로그 (누적된 모든 대화)
                logger.debug(
                      "[PromptChatMemoryAdvisor.after] Memory after ASSISTANT add for conversationId={}: {}",
                      this.getConversationId(chatClientResponse.context(), this.defaultConversationId),
                      memoryMessages
                );
             }
          }
          
          // 6. 원본 응답 객체를 그대로 반환
          // after() 메서드는 응답을 수정하지 않고, 메모리 저장과 로깅만 수행
          return chatClientResponse;
      }
      ```   

- MessageChatMemoryAdvisor: 과거 대화를 Message 객체 배열 형태로 유지하며 현재 요청의 메시지 리스트에 직접 병합하여 전체 대화 히스토리를 AI에게 전달합니다.
    - Before: 과거 대화를 메모리에서 조회하여 현재 요청의 메시지 리스트에 추가합니다.
    - After: 모델의 응답을 받은 후, 새로운 어시스턴트 메시지를 메모리에 저장합니다.

        ```Java
        @Override
        public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
            // 현재 대화 세션의 고유 ID 획득 (없으면 기본값 사용)
            String conversationId = getConversationId(chatClientRequest.context(), this.defaultConversationId);
      
            // 1. 현재 대화의 메모리(대화 히스토리) 조회
            // ChatMemory에서 이 대화 세션의 모든 과거 메시지 가져오기
            // (USER, ASSISTANT, SYSTEM 등 모든 타입의 메시지 포함)
            List<Message> memoryMessages = this.chatMemory.get(conversationId);
      
            // 2. 메시지 리스트 재구성 (메모리 + 현재 요청)
            // 과거 대화 히스토리를 포함하는 새로운 메시지 리스트 생성
            List<Message> processedMessages = new ArrayList<>(memoryMessages);  // 기존 메모리 복사
          
            // 현재 요청의 지시사항(instructions) 메시지들을 추가
            // instructions에는 SystemMessage, UserMessage 등이 포함될 수 있음
            processedMessages.addAll(chatClientRequest.prompt().getInstructions());
      
            // 3. 재구성된 메시지 리스트로 새로운 요청 객체 생성
            // mutate()를 중첩 사용하여 요청과 프롬프트를 모두 수정
            ChatClientRequest processedChatClientRequest = chatClientRequest.mutate()
               .prompt(
                  chatClientRequest.prompt()
                     .mutate()
                     .messages(processedMessages)  // 과거 대화 + 현재 요청으로 메시지 교체
                     .build()
               )
               .build();
      
            // 4. 현재 사용자 메시지를 대화 메모리에 추가
            // 다음 대화 턴에서 컨텍스트로 활용하기 위해 저장
            UserMessage userMessage = processedChatClientRequest.prompt().getUserMessage();
            this.chatMemory.add(conversationId, userMessage);
      
            // 5. 메모리가 포함된 요청 객체 반환
            // AI는 이제 전체 대화 히스토리가 포함된 메시지 리스트로 응답 생성
            return processedChatClientRequest;
        }

        @Override
        public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
            // 1. AI 어시스턴트 응답 메시지를 담을 리스트 초기화
            List<Message> assistantMessages = new ArrayList<>();
      
            // 2. 응답에서 모든 어시스턴트 메시지 추출 (null 안전성 체크)
            if (chatClientResponse.chatResponse() != null) {
                // 모든 생성 결과에서 어시스턴트 메시지 추출
                assistantMessages = chatClientResponse.chatResponse()
                        .getResults()                           // 모든 결과 가져오기 (여러 개일 수 있음)
                        .stream()                               // 스트림으로 변환
                        .map(g -> (Message) g.getOutput())      // 각 결과에서 실제 메시지 객체 추출
                        .toList();                              // List로 수집
            }
      
            // 3. AI 응답을 대화 메모리에 추가
            // 현재 대화 세션(conversationId)의 메모리에 어시스턴트 메시지 저장
            // 다음 대화 턴에서 컨텍스트로 활용하기 위함
            this.chatMemory.add(
                    this.getConversationId(chatClientResponse.context(), this.defaultConversationId),
                    assistantMessages
            );
      
            // 4. 원본 응답 객체를 그대로 반환
            // after() 메서드는 응답을 수정하지 않고, 메모리 저장만 수행 (Pass-through)
            return chatClientResponse;
        }
        ```

- ChatModelCallAdvisor: 응답 형식 지시사항(JSON, XML 등)을 프롬프트에 추가하고 Advisor 체인을 우회하여 ChatModel을 직접 호출합니다.

    ```Java
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        // 1. 입력 유효성 검증
        // chatClientRequest가 null이면 IllegalArgumentException 발생
        Assert.notNull(chatClientRequest, "the chatClientRequest cannot be null");
    
        // 2. 포맷 지시사항 추가 (응답 구조화)
        // 요청에 출력 형식(JSON, XML 등)에 대한 지시사항을 프롬프트에 추가
        // 예: "다음 JSON 형식으로 응답하세요: {...}"
        ChatClientRequest formattedChatClientRequest = augmentWithFormatInstructions(chatClientRequest);
    
        // 3. ChatModel 직접 호출
        // Advisor 체인을 우회하고 ChatModel을 직접 호출하여 응답 생성
        // (callAdvisorChain.nextCall()을 사용하지 않음에 주목!)
        ChatResponse chatResponse = this.chatModel.call(formattedChatClientRequest.prompt());
        
        // 4. ChatClientResponse 생성 및 반환
        // AI 응답과 요청 컨텍스트를 포함한 응답 객체 구성
        return ChatClientResponse.builder()
            .chatResponse(chatResponse)                              // AI 모델의 응답
            .context(Map.copyOf(formattedChatClientRequest.context())) // 요청 컨텍스트 복사 (불변)
            .build();
    }
    ```

- QuestionAnswerAdvisor: 사용자 질문과 관련된 문서를 벡터 검색으로 찾아 프롬프트에 추가하고(RAG), 검색된 문서를 응답 메타데이터로 제공합니다.
    - Before: 벡터 검색으로 관련 문서를 찾아 사용자 질문에 컨텍스트로 추가하는 RAG의 Retrieval + Augmentation 단계 수행합니다.
    - After: 검색된 문서들을 응답 메타데이터에 포함시켜 AI 답변의 출처를 추적 가능하게 합니다.

        ```Java
        @Override
        public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
          // 1. 벡터 저장소에서 유사한 문서 검색 (RAG의 "Retrieval" 단계)
          // 기본 검색 설정을 복사하고 현재 사용자 질문으로 쿼리 설정
          var searchRequestToUse = SearchRequest.from(this.searchRequest)
             .query(chatClientRequest.prompt().getUserMessage().getText())  // 사용자 질문을 검색 쿼리로 사용
             .filterExpression(doGetFilterExpression(chatClientRequest.context()))  // 컨텍스트 기반 필터 적용
             .build();
      
          // 벡터 유사도 검색 실행 - 질문과 관련된 문서들 찾기
          List<Document> documents = this.vectorStore.similaritySearch(searchRequestToUse);
      
          // 2. 검색된 문서들을 컨텍스트에 추가
          // 요청 컨텍스트 복사 후 검색된 문서 저장 (나중에 메타데이터로 활용)
          Map<String, Object> context = new HashMap<>(chatClientRequest.context());
          context.put(RETRIEVED_DOCUMENTS, documents);
      
          // 검색된 문서들의 텍스트를 하나의 문자열로 결합
          // 각 문서는 줄바꿈으로 구분
          String documentContext = documents == null ? ""
                : documents.stream()
                   .map(Document::getText)                      // 각 문서의 텍스트 추출
                   .collect(Collectors.joining(System.lineSeparator()));  // 줄바꿈으로 연결
      
          // 3. 사용자 프롬프트를 문서 컨텍스트로 증강 (RAG의 "Augmentation" 단계)
          UserMessage userMessage = chatClientRequest.prompt().getUserMessage();
          
          // 템플릿을 사용해 원본 질문과 검색된 문서 컨텍스트를 결합
          // 예: "다음 문서를 참고하여 '{query}'에 답변하세요:\n{question_answer_context}"
          String augmentedUserText = this.promptTemplate
             .render(Map.of("query", userMessage.getText(), 
                            "question_answer_context", documentContext));
      
          // 4. 증강된 프롬프트로 새로운 요청 생성
          // 원본 사용자 메시지를 문서 컨텍스트가 포함된 버전으로 교체
          return chatClientRequest.mutate()
             .prompt(chatClientRequest.prompt().augmentUserMessage(augmentedUserText))  // 증강된 메시지로 교체
             .context(context)  // 검색된 문서를 컨텍스트에 포함
             .build();
        }
    
      
        @Override
        public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
            // 1. ChatResponse 빌더 준비
            ChatResponse.Builder chatResponseBuilder;
          
            // 기존 응답이 없으면 새로운 빌더 생성, 있으면 기존 응답에서 복사
            if (chatClientResponse.chatResponse() == null) {
               chatResponseBuilder = ChatResponse.builder();
            }
            else {
               chatResponseBuilder = ChatResponse.builder().from(chatClientResponse.chatResponse());
            }
          
            // 2. 검색된 문서를 응답 메타데이터에 추가
            // before()에서 검색한 문서들을 응답의 메타데이터로 포함
            // 사용자가 AI 답변의 출처(source documents)를 확인할 수 있도록 함
            chatResponseBuilder.metadata(RETRIEVED_DOCUMENTS, 
                                          chatClientResponse.context().get(RETRIEVED_DOCUMENTS));
          
            // 3. 메타데이터가 추가된 응답 객체 반환
            return ChatClientResponse.builder()
               .chatResponse(chatResponseBuilder.build())  // 메타데이터 포함된 응답
               .context(chatClientResponse.context())       // 원본 컨텍스트 유지
               .build();
        }
        ```

- SafeGuardAdvisor: 민감한 단어가 포함된 요청을 사전에 차단하여 부적절한 콘텐츠가 AI 모델에 전달되는 것을 방지하는 필터링합니다.

    ```Java
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        // 1. 민감한 단어 검사 및 차단
        // 민감한 단어 목록이 존재하고, 프롬프트에 민감한 단어가 포함되어 있는지 확인
        if (!CollectionUtils.isEmpty(this.sensitiveWords)  // 민감 단어 목록이 비어있지 않은 경우
              && this.sensitiveWords.stream()               // 스트림으로 변환
                 .anyMatch(w -> chatClientRequest.prompt()  // 하나라도 매치되면 true
                    .getContents()                          // 프롬프트의 전체 내용 가져오기
                    .contains(w))) {                        // 민감 단어 포함 여부 확인
           
           // 민감한 단어가 발견되면 AI 호출 없이 실패 응답 반환
           // (비용 절감 + 보안 강화 + 빠른 응답)
           return createFailureResponse(chatClientRequest);
        }
    
        // 2. 민감한 단어가 없으면 다음 Advisor 또는 AI 모델 호출
        // 정상적인 요청은 체인을 따라 계속 진행
        return callAdvisorChain.nextCall(chatClientRequest);
    }
    ```

### 마무리

Advisors API는 Prompt 요청과 응답을 가로채고 수정할 수 있는 유연한 메커니즘을 제공합니다. 이를 통해 로깅, 메모리 관리, RAG, 포맷 지시사항 추가, 민감한 단어 필터링 등 다양한 기능을 구현할
수 있습니다.

## Reference Documentation

- [Spring AI Reference Documentation](https://docs.spring.io/spring-ai/reference/index.html)


