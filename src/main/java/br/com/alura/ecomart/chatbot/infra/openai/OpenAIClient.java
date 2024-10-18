package br.com.alura.ecomart.chatbot.infra.openai;

import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.messages.Message;
import com.theokanning.openai.messages.MessageRequest;
import com.theokanning.openai.runs.RunCreateRequest;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.threads.ThreadRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;

@Component
public class OpenAIClient {

    private final String apiKey;
    private final String assistantId;
    private String threadId;
    private final OpenAiService service;

    public OpenAIClient(@Value("${app.openai.api.key}") String apiKey,
                        @Value("${app.openai.api.assistant.id}") String assistantId) {
        this.apiKey = apiKey;
        this.assistantId = assistantId;
        this.service = new OpenAiService(apiKey, Duration.ofSeconds(60));
    }

    public String enviarRequisicaoChatCompletion(DadosRequisicaoChatCompletion dados) {
        /**
         * Cria a Mensagem
         */
        var messageRequest = MessageRequest
                .builder()
                .role(ChatMessageRole.USER.value())
                .content(dados.promptUsuario())
                .build();

        /**
         * Cria a Thread ou utiliza caso ela já tenha sido criada
         */
        if (this.threadId == null) {
            var threadRequest = ThreadRequest
                    .builder()
                    .messages(Arrays.asList(messageRequest))
                    .build();

            var thread = service.createThread(threadRequest);
            this.threadId = thread.getId();
        } else {
            service.createMessage(this.threadId, messageRequest);
        }
        /**
         * Por fim criado o objeto run, passando o id do assistant e o id da thread
         */
        var runRequest = RunCreateRequest
                .builder()
                .assistantId(assistantId)
                .build();
        var run = service.createRun(threadId, runRequest);

        /**
         * loop while para continuar enquanto o status de Run não foi igual a "completed".
         * Thread.sleep() para um tempos de espera de 10 segundos
         * depois desses 10 segundos, é chamada a service e enviando buscar novamente o Run.
         * é feito uma consulta para carregar novamente o Run e verificar se o status mudou para completed.
         * Enquanto não estiver completed, ele vai ficar nesse loop.
         *
         * Isso gera um erro de compilação no sleep(), porque ele lança uma exception.
         * por isso esse while está dentro de um try/catch. No catch(), eu busco uma Exception e, se acontecer,
         * será interrompido aqui o programa com throw new RuntimeException(e).
         */
        try {
            while (!run.getStatus().equalsIgnoreCase("completed")) {
                Thread.sleep(1000 * 10);
                run = service.retrieveRun(threadId, run.getId());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        /**
         * Obtendo a resposta do Run
         */
        var mensagens = service.listMessages(threadId);
        var respostaAssistente = mensagens
                .getData()
                .stream()
                .sorted(Comparator.comparingInt(Message::getCreatedAt).reversed())
                .findFirst().get().getContent().get(0).getText().getValue();

        return respostaAssistente;
    }

}
