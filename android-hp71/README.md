# PhoneBox HP 7.1 — Aplicativo Android 100% Nativo (API 17 / v1.5.0)

Aplicativo Android **100% nativo em Java puro** (zero WebView, zero AndroidX/Jetpack, zero Kotlin stdlib, zero bibliotecas externas), projetado sob medida para o tablet **HP 7.1 1201** rodando **Android 4.2.2 Jelly Bean (API 17)** (`Quad-core Cortex-A7 1 GHz`, `1 GB RAM`, tela `7" 1024×600`, com a barra preta fixa de navegação do sistema em paisagem e retrato).

Conecta-se diretamente ao `PhoneBoxAgent` pela rede local privada (`HTTP`, porta `8788`), utilizando o endpoint unificado `/api/legacy/sync`, verificação automática de atualizações via `/api/legacy/apk-info` e **polling dedicado de 5s para cotas de IA apenas enquanto a aba IA estiver visível**.

> Baseado no projeto open-source [ersinkoc/phonebox](https://github.com/ersinkoc/phonebox) (Licença MIT). Este módulo nativo em Java foi desenvolvido especificamente neste fork para reviver tablets Android 4.2.2.

---

## Destaques desta versão (`v1.5.0` / `versionCode=9`)

- **Auto-Updater de APK integrado (`core/ApkUpdater.java`):** compara `versionCode` e `SHA-256` com o agente no PC, exibe banner/diálogo quando há atualização, baixa com barra de progresso, valida o `SHA-256` e aciona o instalador nativo do Android 4.2.2 (`ACTION_VIEW`).
- **Tema Hack 2.0 (*Zero-Day Cyberdeck* — `theme/hack/`):** chuva Matrix em 3 camadas, scanlines CRT + cortes cromáticos RGB + jitter horizontal, boot interativo typewriter, terminal vivo na barra superior e engine global de glitch legível `h4ck3r`.
- **Tema Anime 2.0 (*Sakura Midnight / Neon Tokyo HUD* — `theme/anime/`):** céu crepuscular animado com lua crescente, 42 estrelas cintilantes, 20 pétalas de cerejeira e estrelas cadentes, cards cristal translúcidos cel-shaded, overlay de brilhos + varredura na troca de aba, animação de entrada *Sakura Midnight Bloom* e faixa estelar na barra superior.
- **Compatibilidade real com Android 4.2.2 (API 17):** zero chamadas API 18+, glifos desenhados nativamente com `Canvas Path` (sem quadrados `□` na fonte `DroidSans`), assinatura v1-only RSA 2048.

---

## Arquitetura Modular

```text
android-hp71/
├── AndroidManifest.xml             # minSdk=17, targetSdk=17, fullscreen sem recriar Activity ao girar
├── build.ps1                       # Compila, gera DEX (API 17), zipalign, assina v1 RSA 2048 e gera apk-meta.json
├── local.config.example.ps1        # Exemplo de overrides locais (copie para local.config.ps1, ignorado no Git)
├── res/                            # Strings, tema Holo Fullscreen, ícones PNG e artes theme_hack.png / theme_anime.png
└── src/net/oxog/phonebox/hp71/
    ├── MainActivity.java           # Shell principal: fullscreen, brilho hardware + overlay, swipe, volume, temas, updater
    ├── core/                       # Rede, estado, cache LRU, polling inteligente e auto-updater
    │   ├── AppPrefs.java           # SharedPreferences (host/porta, tema, efeitos, auto-check APK, brilho, volume)
    │   ├── AppState.java           # Estado imutável (PC, clima, mídia, áudio, discos, processos, IA, Discord, APK)
    │   ├── ApiClient.java          # HttpURLConnection leve em ExecutorService (GET/POST sem bloquear UI)
    │   ├── ApkUpdater.java         # Verificação remota, download com progresso, validação SHA-256 e instalador nativo
    │   ├── BitmapCache.java        # LruCache com downsampling automático para ícones e avatares
    │   └── SyncManager.java        # Polling adaptativo + timer exclusivo de 5s liveAi=1 na aba IA
    ├── theme/                      # Design System centralizado
    │   ├── ThemeColors.java        # Paletas Default, Hack 2.0, Anime 2.0, AMOLED, Midnight, Warm, Graphite
    │   ├── ThemeDimens.java        # Escala dp/sp, raios de borda, espaçamentos e áreas de toque (>=44dp)
    │   ├── AppTheme.java           # Estado reativo do tema sincronizado com o agente e overrides locais
    │   ├── DrawableFactory.java    # Fábricas de drawables normais, Hack cyberdeck e Anime cristal
    │   ├── hack/                   # Zero-Day Cyberdeck (MatrixRain, HackGlitchOverlay, HackBootOverlay, HackHeaderTerminal, HackTextGlitchManager, drawables)
    │   └── anime/                  # Sakura Midnight (AnimeSkyBackground, AnimeSparkleOverlay, AnimeEntranceOverlay, AnimeHeaderRibbon, drawables)
    ├── layouts/                    # Contêineres responsivos Horizontal (1024×552) / Vertical (600×976)
    ├── components/                 # Primitivos reutilizáveis em Canvas (Card, SectionHeader, ProgressBar, LineChart, TopBar, BottomNav, Modal)
    ├── widgets/                    # Blocos funcionais autocontidos (Relógio/Clima, Mídia, Volume, Mixer, Processos, IA, Discord, Relógio Noturno)
    └── screens/                    # Compositores de layout das 7 abas (Home, System, Deck, Procs, Ai, Discord, Night)
```

---

## Como Compilar o APK

Requisitos:
- **JDK** (`javac` + `keytool`)
- **Android SDK Build-Tools** (`aapt2`, `d8`, `zipalign`, `apksigner`) e uma plataforma (`android.jar`)

Execute na pasta `android-hp71/`:

```powershell
.\build.ps1
```

Se quiser personalizar caminhos ou senha da keystore localmente sem alterar arquivos versionados, copie `local.config.example.ps1` para `local.config.ps1` (ignorado no `.gitignore`).

O script executa automaticamente:
1. Geração de keystore RSA 2048 local (`phonebox-hp71-release.keystore`, ignorado no Git) se ainda não existir;
2. `aapt2 compile` + `aapt2 link` (`--min-sdk-version 17 --target-sdk-version 17`);
3. `javac -source 8 -target 8` em todos os fontes Java;
4. `d8 --min-api 17` gerando `classes.dex` compatível com Dalvik do Android 4.2.2;
5. Empacotamento + `zipalign -f 4`;
6. Assinatura **exclusivamente v1 (JAR / RSA 2048)** com `apksigner`;
7. Validação com `apksigner verify` e `aapt2 dump badging`;
8. Cópia automática de `dist/PhoneBox-HP71.apk` e `dist/apk-meta.json` para `agent/PhoneBox.Agent/legacy/`.

---

## Como Instalar no HP 7.1 (Android 4.2.2)

### Opção A — Direto pelo Wi-Fi (mais simples)
1. No tablet HP 7.1, ative **Configurações → Segurança → Fontes desconhecidas**.
2. Abra o navegador do tablet e acesse:
   - `http://<IP-LOCAL-DO-PC>:8788/legacy/PhoneBox-HP71.apk`
   *(ou clique no botão **↓ BAIXAR APK** na barra superior do painel web `/legacy/` ou em `http://127.0.0.1:8765/#pages`)*
3. Toque no arquivo baixado e confirme em **Instalar**.
4. Na primeira abertura, toque em **Config.** → **Conexão do PC** e informe o IP local do seu PC (porta padrão `8788`).
5. Nas próximas versões, o próprio app detecta a atualização no servidor e baixa/abre o instalador direto pela rede local.

### Opção B — Via cabo USB (ADB)
```powershell
adb install -r .\dist\PhoneBox-HP71.apk
```
