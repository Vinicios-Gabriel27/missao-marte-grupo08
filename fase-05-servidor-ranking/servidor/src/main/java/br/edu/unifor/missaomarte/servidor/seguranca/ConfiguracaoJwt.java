package br.edu.unifor.missaomarte.servidor.seguranca;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Chave e validade do token. O segredo vem da variável de ambiente MISSAOMARTE_JWT_SEGREDO
 * (mínimo 32 caracteres). Sem ela, gera uma chave aleatória a cada início (tokens antigos
 * deixam de valer) — bom para desenvolvimento, nunca é gravada em disco nem em log.
 */
@Component
public class ConfiguracaoJwt {
    private static final Logger LOG = LoggerFactory.getLogger(ConfiguracaoJwt.class);

    private final SecretKey chave;
    private final Duration validade;

    public ConfiguracaoJwt(@Value("${missaomarte.jwt.segredo:}") String segredo,
                           @Value("${missaomarte.jwt.validade-minutos:120}") long minutos) {
        byte[] bytes;
        if (segredo == null || segredo.isBlank()) {
            bytes = new byte[32];
            new SecureRandom().nextBytes(bytes);
            LOG.warn("MISSAOMARTE_JWT_SEGREDO não definido: usando chave temporária (tokens expiram ao reiniciar).");
        } else if (segredo.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("MISSAOMARTE_JWT_SEGREDO precisa ter pelo menos 32 caracteres.");
        } else {
            bytes = segredo.getBytes(StandardCharsets.UTF_8);
        }
        this.chave = new SecretKeySpec(bytes, "HmacSHA256");
        this.validade = Duration.ofMinutes(minutos);
        LOG.info("Tokens JWT válidos por {} minutos.", minutos);
    }

    public SecretKey chave() {
        return chave;
    }

    public Duration validade() {
        return validade;
    }
}
