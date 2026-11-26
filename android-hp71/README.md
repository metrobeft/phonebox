# PhoneBox HP 7.1 — Aplicativo Android 100% Nativo (API 17 / v2.1.0)

Aplicativo Android **100% nativo em Java puro** (zero WebView, zero AndroidX/Jetpack, zero Kotlin stdlib, zero bibliotecas externas), projetado sob medida para o tablet **HP 7.1 1201** rodando **Android 4.2.2 Jelly Bean (API 17)** (`Quad-core Cortex-A7 1 GHz`, `1 GB RAM`, tela `7" 1024×600`, com a barra preta fixa de navegação do sistema em paisagem e retrato).

Conecta-se diretamente ao `PhoneBoxAgent` pela rede local privada (`HTTP`, porta `8788`), utilizando o endpoint unificado `/api/legacy/sync`, verificação automática de atualizações via `/api/legacy/apk-info` e **polling dedicado de 5s para cotas de IA apenas enquanto a aba IA estiver visível**.

> Baseado no projeto open-source [ersinkoc/phonebox](https://github.com/ersinkoc/phonebox) (Licença MIT). Este módulo nativo em Java foi desenvolvido especificamente neste fork para reviver tablets Android 4.2.2, com o **Theme Studio v2.0** inspirado em [metrobeft/designer-temas-animados](https://github.com/metrobeft/designer-temas-animados).

---

## Destaques desta versão (`v2.1.0` / `versionCode=11`)

- **Theme Studio v2.1 — 6 Mundos Animados em Galeria Visual (2×3) + Default Navy em Configurações:**
  - **Sakura Storm (`sakura` / alias `anime`):** céu crepuscular, lua crescente, montanhas, galho de cerejeira com 2 lanternas japonesas (`chōchin`) balançando, pétalas 3D com rajadas de vento, estrelas cadentes e cards em cristal translúcido.
  - **Lofi Room (`lofi`):** janela noturna chuvosa com skyline, fio de pisca-pisca quente, chuva inclinada + gotas lentas no vidro, abajur pêssego, vapor de chá, relâmpago distante suave, entrada com vinil girando e mini equalizador no topo.
  - **Starfall Aurora (`starfall`):** cortinas ondulantes de Aurora Boreal (esmeralda/violeta), constelações em ciano autodesenhadas (Orion, Cassiopeia, Cygnus), portal Torii nas colinas, vaga-lumes dourados e meteoros.
  - **Hatsune Miku 01 (`miku`):** palco vocaloid em ciano `#39C5BB` + magenta `#FF2E88`, ondas synth, equalizador de 22 bandas com picos, notas musicais desenhadas em Canvas, holograma translúcido da mascote (`miku_mascot.png`, ~58 KB seguro para 1 GB RAM) e entrada de palco dedicada. *(Artes `miku_mascot.png` / `theme_miku.png`: uso não-comercial de fã `© Crypton Future Media, INC. www.piapro.net`, excluídas da MIT.)*
  - **Arcane Fantasy (antasy):** reino arcano em ouro #F4C858 + esmeralda mana #4CF2B2, lua cheia com círculo mágico giratório (hexagrama rúnico), castelo com torres iluminadas, ilha flutuante, cristais de mana e fadas de luz subindo.
  - **Hack 2.0 (*Zero-Day Cyberdeck* — 	heme/hack/):** chuva Matrix em 3 camadas, scanlines CRT + cortes cromáticos RGB + jitter horizontal, boot interativo typewriter, terminal vivo na barra superior e engine global de glitch legível `h4ck3r`.
  - **Default Navy (`default`):** tema base clássico limpo.
- **Auto-Updater de APK integrado (`core/ApkUpdater.java`):** compara `versionCode` e `SHA-256` com o agente no PC, exibe banner/diálogo quando há atualização, baixa com barra de progresso, valida o `SHA-256` e aciona o instalador nativo do Android 4.2.2 (`ACTION_VIEW`).
- **Compatibilidade real com Android 4.2.2 (API 17):** zero chamadas API 18+, glifos/notas/estrelas/lanternas/Torii desenhados nativamente com `Canvas Path` (zero quadrados `□` na fonte `DroidSans`), zero alocações em `onDraw()`, assinatura v1-only RSA 2048.

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
