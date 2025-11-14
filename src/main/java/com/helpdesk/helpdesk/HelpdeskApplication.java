package com.helpdesk.helpdesk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.context.WebServerInitializedEvent; // Importe este
import org.springframework.context.event.EventListener; // Importe este
import org.springframework.stereotype.Component; // Importe este

@SpringBootApplication
public class HelpdeskApplication {

	public static void main(String[] args) {
		SpringApplication.run(HelpdeskApplication.class, args);
	}

	// --- COLE ESTE BLOCO DE CÓDIGO ---
	/**
	 * Este componente "ouve" o evento de quando o servidor web
	 * (Tomcat) está pronto e inicializado.
	 */
	@Component
	class WebServerListener {

		/**
		 * Este método é acionado assim que o servidor está no ar.
		 * @param event O evento que contém os dados do servidor, incluindo a porta.
		 */
		@EventListener
		public void onWebServerInitialized(WebServerInitializedEvent event) {
			// Pegamos a porta dinamicamente, caso ela mude no futuro
			int port = event.getWebServer().getPort();

			// Imprimimos a mensagem customizada no console
			System.out.println("==================================================");
			System.out.println("🚀 SERVIDOR HELP DESK NO AR! 🚀");
			System.out.println("Disponível em: http://localhost:" + port);
			System.out.println("==================================================");
		}
	}
	// --- FIM DO BLOCO ---

}