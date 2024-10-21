package br.com.alura.ecomart.chatbot.web.controller;

import br.com.alura.ecomart.chatbot.domain.service.ChatBootService;
import br.com.alura.ecomart.chatbot.web.dto.PerguntaDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsável por gerenciar as requisições relacionadas ao chatbot.
 */
@Controller
@RequestMapping({"/", "chat"})
public class ChatController {

    private static final String PAGINA_CHAT = "chat";

    private final ChatBootService chatBootService;

    /**
     * Construtor que injeta a dependência do serviço de chatbot.
     *
     * @param chatBootService Serviço de chatbot.
     */
    public ChatController(ChatBootService chatBootService) {
        this.chatBootService = chatBootService;
    }

    /**
     * Método que carrega a página do chatbot com o histórico de mensagens.
     *
     * @param model Modelo para adicionar atributos à view.
     * @return Nome da página do chatbot.
     */
    @GetMapping
    public String carregarPaginaChatbot(Model model) {
        final var mensagens = chatBootService.carregarHistorico();
        model.addAttribute("historico", mensagens);
        return PAGINA_CHAT;
    }

    /**
     * Método que responde a uma pergunta enviada pelo usuário.
     *
     * @param dto Objeto que contém a pergunta do usuário.
     * @return Resposta do chatbot.
     */
    @PostMapping
    @ResponseBody
    public String responderPergunta(@RequestBody PerguntaDto dto) {
        return chatBootService.responderPergunta(dto.pergunta());
    }

    /**
     * Método que limpa a conversa do chatbot.
     *
     * @return Nome da página do chatbot.
     */
    @GetMapping("limpar")
    public String limparConversa() {
        chatBootService.limparHistorico();
        return "redirect:/chat";
    }
}