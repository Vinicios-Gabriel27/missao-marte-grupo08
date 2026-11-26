package br.edu.unifor.missaomarte.servidor.seguranca;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * API sem sessão (stateless): rotas públicas para cadastro, login, ranking, documentação e
 * saúde; o resto exige "Authorization: Bearer <token>". O token é validado aqui, pelo
 * resource server do Spring Security, antes de chegar ao controller.
 */
@Configuration
public class ConfiguracaoSeguranca {

    @Bean
    SecurityFilterChain filtros(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.POST, "/api/jogadores", "/api/sessoes").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ranking").permitAll()
                        .requestMatchers("/actuator/health/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        return http.build();
    }

    @Bean
    PasswordEncoder codificadorDeSenha() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    JwtEncoder codificadorJwt(ConfiguracaoJwt config) {
        return NimbusJwtEncoder.withSecretKey(config.chave()).algorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    JwtDecoder decodificadorJwt(ConfiguracaoJwt config) {
        return NimbusJwtDecoder.withSecretKey(config.chave()).macAlgorithm(MacAlgorithm.HS256).build();
    }
}
