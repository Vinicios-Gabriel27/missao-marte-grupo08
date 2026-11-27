# Retrospectiva da equipe — Grupo 08

**Projeto:** Evolução do Missão Marte Unifor (T200) · **Equipe:** Vinicios Gabriel, José Matheus e Marcelo Fong

## Maiores desafios

1. **A ideia do jogo.** Definir para onde levar o Missão Marte foi a parte mais difícil no começo. Queríamos algo
   bem diferente do console, então escolhemos o estilo *survivors* em 3D (inspirado em Vampire Survivors, Megabonk
   e The Deep Horde) e pedimos autorização ao professor. O desafio foi mudar o gênero **sem perder a essência** do
   original: passageiros com bônus (Professor, Engenheiro, Astronauta), plataforma de pouso, vida, pontuação,
   dificuldades e ranking.
2. **Encontrar os modelos 3D.** Precisávamos de modelos bonitos, gratuitos e com licença que permitisse o uso.
   Depois de procurar, chegamos aos pacotes da Kenney (licença CC0). Mesmo assim deu trabalho: alguns personagens
   referenciavam texturas em arquivos separados e o jogo travava ao carregar; foi preciso embutir as texturas nos
   arquivos `.glb` e ajustar posição, escala e iluminação para o visual ficar bom.
3. **Conectar o ranking online.** Fazer o jogo conversar com o servidor foi o desafio técnico mais trabalhoso:
   cadastro e login com senha em hash, token JWT, requisições sem travar o jogo (outra thread), o que fazer quando
   o servidor cai, e depois fazer o celular e outros computadores enxergarem o servidor na rede (endereço, porta e
   firewall).

## O que deu certo

- Separar as regras do jogo da interface logo na Fase 1. Isso permitiu trocar console → 3D → Android sem reescrever
  as regras, e testar tudo sem abrir janela.
- Testes automáticos e o GitHub Actions: cada mudança era compilada, testada e o jogo era aberto sozinho para tirar
  capturas de tela, o que pegou erros cedo.
- Manter cada fase numa pasta própria deixou a evolução fácil de mostrar.

## O que faríamos diferente

- Distribuir melhor o trabalho e os commits entre os integrantes ao longo das sprints, com o Kanban acompanhando de
  verdade desde o início.
- Testar no celular mais cedo (o APK só foi instalado num aparelho real no fim, e apareceram ajustes de toque e de
  teclado).
- Publicar o servidor na nuvem, para o ranking funcionar de qualquer lugar sem depender de um PC ligado na rede.

## Limitações conhecidas

- O servidor não executa o jogo; ele só recusa pontuações impossíveis (não prova que uma pontuação é legítima).
- Rodando local, a comunicação é HTTP; em produção seria HTTPS.
- O APK é de teste, assinado com a chave de depuração padrão do Android (senha pública `android`), que não é um
  segredo; uma versão de loja usaria uma chave própria fora do repositório.
