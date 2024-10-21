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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Classe responsável por interagir com a API do OpenAI.
 */
@Component
public class OpenAIClient {

    private final String apiKey;
    private final String assistantId;
    private String threadId;
    private final OpenAiService service;

    /**
     * Construtor que inicializa o cliente OpenAI com as chaves de API e assistente.
     *
     * @param apiKey      Chave da API do OpenAI.
     * @param assistantId ID do assistente do OpenAI.
     */
    public OpenAIClient(@Value("${app.openai.api.key}") String apiKey,
                        @Value("${app.openai.api.assistant.id}") String assistantId) {
        this.apiKey = apiKey;
        this.assistantId = assistantId;
        this.service = new OpenAiService(apiKey, Duration.ofSeconds(60));
    }

    /**
     * Envia uma requisição de conclusão de chat para o OpenAI.
     *
     * @param dados Dados da requisição de chat.
     * @return Resposta do assistente.
     */
    public String enviarRequisicaoChatCompletion(DadosRequisicaoChatCompletion dados) {
        // Cria a Mensagem
        var messageRequest = MessageRequest
                .builder()
                .role(ChatMessageRole.USER.value())
                .content(dados.promptUsuario())
                .build();

        // Cria a Thread ou utiliza caso ela já tenha sido criada
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

        // Cria o objeto run, passando o id do assistant e o id da thread
        var runRequest = RunCreateRequest
                .builder()
                .assistantId(assistantId)
                .build();
        var run = service.createRun(threadId, runRequest);

        // Loop while para continuar enquanto o status de Run não for igual a "completed"
        try {
            while (!run.getStatus().equalsIgnoreCase("completed")) {
                Thread.sleep(1000 * 10);
                run = service.retrieveRun(threadId, run.getId());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Obtendo a resposta do Run
        var mensagens = service.listMessages(threadId);
        var respostaAssistente = mensagens
                .getData()
                .stream()
                .sorted(Comparator.comparingInt(Message::getCreatedAt).reversed())
                .findFirst().get().getContent().get(0).getText().getValue();

        return respostaAssistente;
    }

    /**
     * Carrega o histórico de mensagens da thread atual.
     *
     * @return Lista de mensagens do histórico.
     */
    public List<String> carregarHistoricoDeMensagens() {
        var mensagens = new ArrayList<String>();

        if (this.threadId != null) {
            mensagens.addAll(
                    service.listMessages(this.threadId)
                            .getData()
                            .stream()
                            .sorted(Comparator.comparingInt(Message::getCreatedAt))
                            .map(m -> m.getContent().get(0).getText().getValue())
                            .collect(Collectors.toList())
            );
        }
        return mensagens;
    }

    public void apagarThread() {
        if (threadId != null) {
            service.deleteThread(threadId);
            threadId = null;
        }
    }
}