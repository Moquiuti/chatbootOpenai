package br.com.alura.ecomart.chatbot.domain.service;

import br.com.alura.ecomart.chatbot.infra.openai.DadosRequisicaoChatCompletion;
import br.com.alura.ecomart.chatbot.infra.openai.OpenAIClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serviço responsável por interagir com o cliente OpenAI para responder perguntas e carregar o histórico de mensagens.
 */
@Service
public class ChatBootService {

    private final OpenAIClient client;

    /**
     * Construtor que inicializa o serviço com o cliente OpenAI.
     *
     * @param client Cliente OpenAI.
     */
    public ChatBootService(OpenAIClient client) {
        this.client = client;
    }

    /**
     * Responde a uma pergunta enviada pelo usuário.
     *
     * @param pergunta Pergunta do usuário.
     * @return Resposta do chatbot.
     */
    public String responderPergunta(@NotNull final String pergunta) {
        final var promptSistema = "Você é um chatbot de atendimento a clientes de um ecommerce e deve responder apenas perguntas relacionadas com o ecommerce";
        final var dados = new DadosRequisicaoChatCompletion(promptSistema, pergunta);
        return client.enviarRequisicaoChatCompletion(dados);
    }

    /**
     * Carrega o histórico de mensagens do chatbot.
     *
     * @return Lista de mensagens do histórico.
     */
    public List<String> carregarHistorico() {
        return client.carregarHistoricoDeMensagens();
    }

    public void limparHistorico() {
        client.apagarThread();
    }
}