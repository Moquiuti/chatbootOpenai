package br.com.alura.ecomart.chatbot;

import br.com.alura.ecomart.chatbot.domain.DadosCalculoFrete;
import br.com.alura.ecomart.chatbot.domain.UF;
import br.com.alura.ecomart.chatbot.domain.service.CalculadorDeFrete;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.math.BigDecimal;

@SpringBootApplication
public class ChatbotApplication {

	public static void main(String[] args) {

		SpringApplication.run(ChatbotApplication.class, args);
		CalculadorDeFrete calculador = new CalculadorDeFrete();
		DadosCalculoFrete dados = new DadosCalculoFrete(10, UF.BA); // Quantidade de produtos

		BigDecimal resultado = calculador.calcular(dados);
		System.out.println("Frete calculado: " + resultado); // Deve imprimir o valor esperado
	}

}
