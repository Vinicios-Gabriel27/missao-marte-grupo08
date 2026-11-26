# Fase 6 — Android

O mesmo jogo (módulo `core`) roda no desktop e no Android. O módulo `android` só tem o ponto de
entrada (`AndroidLauncher`) e a configuração do aparelho.

| Pasta | Conteúdo |
|---|---|
| `cliente/` | `core` (jogo, compartilhado) · `lwjgl3` (desktop) · `android` (celular) · `assets` (modelos, fontes, sons — usados pelos dois) |
| `servidor/` | Servidor da Fase 5 (sem mudanças de API) |

## Baixar o APK pronto

O CI gera o APK de teste a cada envio ao GitHub e publica em **Releases → "APK Android (debug)"**.
No celular: baixe o `.apk`, permita "instalar apps de fontes desconhecidas" e instale.

## Gerar o APK localmente

Requer Android SDK (Android Studio) — o SDK é detectado por `local.properties` (`sdk.dir=...`) ou `ANDROID_HOME`.

```bash
cd fase-06-android/cliente
./gradlew :android:assembleDebug      # APK em android/build/outputs/apk/debug/
./gradlew :android:installDebug       # instala no aparelho/emulador conectado
./gradlew :lwjgl3:run                 # o desktop continua funcionando (sem SDK o módulo android é ignorado)
```

## Controles de toque

| Ação | Como |
|---|---|
| Mover | **joystick virtual**: toque e arraste na metade esquerda da tela (intensidade proporcional, zona morta) |
| Atirar | automático |
| Upgrade | tocar no cartão |
| Pausar | tocar no **relógio** (topo) ou botão **Voltar** do Android; na pausa, botão "Menu principal" |
| Menu | todos os botões funcionam por toque; campos de texto abrem o teclado do celular |

O teclado/mouse do desktop continua valendo (o jogo usa o que estiver ativo).

## Servidor no celular

O endereço é configurável no menu **CONTA → Servidor** (nenhum segredo fica no app).

| Situação | Endereço |
|---|---|
| Emulador Android | `http://10.0.2.2:8080` (padrão no Android) |
| Celular via USB | `adb reverse tcp:8080 tcp:8080` e use `http://localhost:8080` |
| Celular no Wi-Fi | servidor com `MISSAOMARTE_ENDERECO=0.0.0.0` e use `http://IP-DO-PC:8080` |

HTTP sem criptografia é permitido **só no build debug** (`usesCleartextTraffic`); o release exige HTTPS.

## Decisões

- **OpenGL ES 3.0** (`useGL30`): exigido pela renderização PBR do gdx-gltf.
- **Mapa de sombras menor no celular** (1024 em vez de 2048) para manter o desempenho.
- **Mesmos assets** do desktop (`assets.srcDirs = ['../assets']`), sem duplicar arquivos.
- **Bibliotecas nativas** (libGDX e FreeType) extraídas dos `.jar` para as 4 arquiteturas (arm, arm64, x86, x86_64).
- Modo imersivo (tela cheia) e orientação paisagem.

## Limitações

- Testado em compilação (CI) e no desktop; o desempenho real depende do aparelho (PBR + sombras + bloom são exigentes).
