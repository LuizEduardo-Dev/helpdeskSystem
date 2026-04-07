package com.helpdesk.helpdesk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class HelpdeskApplication {

	public static void main(String[] args) {
		SpringApplication.run(HelpdeskApplication.class, args);
	}



	@Component
	class WebServerListener {

		/**
		 * Este método é acionado assim que o servidor está no ar.
		 * @param event O evento que contém os dados do servidor, incluindo a porta.
		 */
		@EventListener
		public void onWebServerInitialized(WebServerInitializedEvent event) {

			int port = event.getWebServer().getPort();

			// Imprimimos a mensagem customizada no console
			System.out.println("==================================================");
			System.out.println("SERVIDOR HELP DESK NO AR!");
			System.out.println("Disponível em: http://localhost:" + port);
			System.out.println("==================================================");
		}
	}


}