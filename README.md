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


## Reference Documentation

- [Spring AI Reference Documentation](https://docs.spring.io/spring-ai/reference/index.html)


