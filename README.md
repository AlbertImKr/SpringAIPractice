# Spring AI Practice

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

## Reference Documentation

- [Spring AI Reference Documentation](https://docs.spring.io/spring-ai/reference/index.html)


