package br.edu.unifor.missaomarte.servidor.seguranca;

import br.edu.unifor.missaomarte.servidor.dominio.Jogador;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

/** Emite o token JWT (HS256) com o id do jogador no "subject". */
@Component
public class EmissorDeToken {
    public record TokenEmitido(String valor, Instant expiraEm) {
    }

    private final JwtEncoder codificador;
    private final ConfiguracaoJwt config;

    public EmissorDeToken(JwtEncoder codificador, ConfiguracaoJwt config) {
        this.codificador = codificador;
        this.config = config;
    }

    public TokenEmitido emitir(Jogador jogador) {
        Instant agora = Instant.now();
        Instant expira = agora.plus(config.validade());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("missao-marte")
                .subject(String.valueOf(jogador.getId()))
                .claim("nome", jogador.getNome())
                .issuedAt(agora)
                .expiresAt(expira)
                .build();
        String token = codificador.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
        return new TokenEmitido(token, expira);
    }
}
