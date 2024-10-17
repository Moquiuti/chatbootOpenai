package br.com.alura.ecomart.chatbot.web.controller;

import br.com.alura.ecomart.chatbot.domain.service.ChatBootService;
import br.com.alura.ecomart.chatbot.web.dto.PerguntaDto;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import io.reactivex.Flowable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

@Controller
@RequestMapping({"/", "chat"})
public class ChatController {

    private static final String PAGINA_CHAT = "chat";

    private final ChatBootService chatBootService;

    public ChatController(ChatBootService chatBootService) {
        this.chatBootService = chatBootService;
    }

    @GetMapping
    public String carregarPaginaChatbot() {
        return PAGINA_CHAT;
    }

    @PostMapping
    @ResponseBody
    public ResponseBodyEmitter responderPergunta(@RequestBody PerguntaDto dto) {
        var fluxoResposta = chatBootService.responderPergunta(dto.pergunta());
        var emitter = new ResponseBodyEmitter();
        fluxoResposta.subscribe(chunk -> {
            var token = chunk.getChoices().get(0).getMessage().getContent();
            if (token != null) {
                emitter.send(token);
            }
        }, emitter::completeWithError, emitter::complete);

        return emitter;
    }

    @GetMapping("limpar")
    public String limparConversa() {
        return PAGINA_CHAT;
    }

}
