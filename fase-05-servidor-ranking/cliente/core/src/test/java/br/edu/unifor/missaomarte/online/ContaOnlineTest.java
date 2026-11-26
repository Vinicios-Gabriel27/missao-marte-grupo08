package br.edu.unifor.missaomarte.online;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.unifor.missaomarte.ranking.RegistroRanking;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Regras da conta testadas com um servidor falso (sem rede). */
class ContaOnlineTest {
    static class ServidorFalso implements ServicoOnline {
        final List<String> contas = new ArrayList<>();
        final List<RegistroRanking> enviadas = new ArrayList<>();
        boolean fora;
        boolean tokenExpirado;

        public void cadastrar(String nome, String senha, Resposta<Void> r) {
            if (fora) {
                r.falha("Servidor indisponível");
            } else if (contas.contains(nome)) {
                r.falha("Esse nome de jogador já está em uso.");
            } else {
                contas.add(nome);
                r.sucesso(null);
            }
        }

        public void entrar(String nome, String senha, Resposta<Sessao> r) {
            if (fora) {
                r.falha("Servidor indisponível");
            } else if (contas.contains(nome)) {
                r.sucesso(new Sessao(nome, "tok-" + nome));
            } else {
                r.falha("Nome ou senha incorretos.");
            }
        }

        public void enviarPartida(Sessao s, RegistroRanking p, Resposta<Long> r) {
            if (tokenExpirado) {
                r.falha("Sessão expirada. Entre novamente.");
                return;
            }
            enviadas.add(p);
            r.sucesso((long) enviadas.size());
        }

        public void rankingGlobal(Resposta<List<RegistroRanking>> r) {
            r.sucesso(enviadas);
        }
    }

    static class Captura<T> implements Resposta<T> {
        T valor;
        String erro;

        public void sucesso(T v) {
            valor = v;
        }

        public void falha(String m) {
            erro = m;
        }
    }

    private final RegistroRanking partida = new RegistroRanking("x", 100, "Médio", 50, 3, 5, 0, false, "agora");

    @Test
    void convidadoNaoEnviaNada() {
        ServidorFalso s = new ServidorFalso();
        ContaOnline conta = new ContaOnline(s);
        assertFalse(conta.logado());
        assertFalse(conta.enviar(partida, new Captura<>()));
        assertTrue(s.enviadas.isEmpty());
    }

    @Test
    void cadastrarEntraAutomaticamenteEEnvia() {
        ServidorFalso s = new ServidorFalso();
        ContaOnline conta = new ContaOnline(s);
        conta.cadastrarEEntrar("Vini", "senha123", new Captura<>());
        assertTrue(conta.logado());
        assertEquals("Vini", conta.nome().orElseThrow());
        Captura<Long> envio = new Captura<>();
        assertTrue(conta.enviar(partida, envio));
        assertEquals(1L, envio.valor);
    }

    @Test
    void falhaDeLoginMantemConvidado() {
        ContaOnline conta = new ContaOnline(new ServidorFalso());
        Captura<Sessao> login = new Captura<>();
        conta.entrar("Ninguem", "x", login);
        assertFalse(conta.logado());
        assertEquals("Nome ou senha incorretos.", login.erro);
    }

    @Test
    void servidorForaDoArNaoQuebraOJogo() {
        ServidorFalso s = new ServidorFalso();
        s.fora = true;
        ContaOnline conta = new ContaOnline(s);
        Captura<Sessao> login = new Captura<>();
        conta.cadastrarEEntrar("A", "senha123", login);
        assertFalse(conta.logado());
        assertEquals("Servidor indisponível", login.erro);
    }

    @Test
    void tokenExpiradoVoltaParaConvidado() {
        ServidorFalso s = new ServidorFalso();
        ContaOnline conta = new ContaOnline(s);
        conta.cadastrarEEntrar("B", "senha123", new Captura<>());
        s.tokenExpirado = true;
        Captura<Long> envio = new Captura<>();
        conta.enviar(partida, envio);
        assertFalse(conta.logado());
        assertTrue(envio.erro.startsWith("Sessão expirada"));
    }
}
