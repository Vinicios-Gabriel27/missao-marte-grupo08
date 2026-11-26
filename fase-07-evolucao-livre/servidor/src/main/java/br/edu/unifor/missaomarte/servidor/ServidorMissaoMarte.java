package br.edu.unifor.missaomarte.servidor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Servidor do ranking global do Missão Marte. Independente do cliente (não usa código dele). */
@SpringBootApplication
public class ServidorMissaoMarte {
    public static void main(String[] args) {
        SpringApplication.run(ServidorMissaoMarte.class, args);
    }
}
