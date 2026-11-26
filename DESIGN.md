# Design

## Identidade

Azul-marinho para o fundo, azul médio para separar superfícies e azul-claro para seleção e controles. Texto quase branco no tablet. O editor do PC segue a mesma família, com fundo azul-gelo no modo claro.

Além do tema padrão, o painel oferece dois modos visuais completos com paridade entre APK e Web:
- **Hack 2.0 (Zero-Day Cyberdeck):** fundo preto puro `#000000` integrado à barra preta do Android 4.2.2, chuva Matrix em camadas, scanlines CRT, cortes cromáticos RGB, terminal typewriter vivo e engine de texto glitch legível (`h4ck3r`).
- **Anime 2.0 (Sakura Midnight / Neon Tokyo HUD):** céu crepuscular estrelado com lua crescente, pétalas de cerejeira e estrelas cadentes, cards em cristal translúcido cel-shaded, gemas estelares douradas e faixa superior animada.

## Hierarquia e componentes

- O relógio e o clima lideram Início. Os controles de mídia ficam ao lado em paisagem e abaixo em retrato. As métricas ficam em uma faixa separada.
- As superfícies têm cantos discretos e contornos finos. A aba selecionada se distingue pelo preenchimento, texto e indicador. Cores de alerta mantêm seu significado.
- Priorize dígitos grandes para o relógio, texto de navegação legível e áreas de toque generosas.
- Tons principais do painel são #0b1427, #152641, #203c63, #f2f7ff e #8bd0ff. O tema e o destaque configurados pelo usuário continuam disponíveis.

## Composição por aba

- Sistema reserva mais espaço para especificações ao lado dos discos.
- Atalhos mantém a área inteira para os ícones configuráveis e deixa o mixer abaixo.
- Processos coloca a lista primeiro, com indicadores ao lado em paisagem e abaixo em retrato.
- IA separa as cotas da atividade, que reúne sessões, GitHub e webhooks.
- Discord dá mais largura ao diretório de canais em paisagem.
- Clima/Hora usa um plano mais claro para o clima atual e o relógio de mesa.

Na prévia do PC, a barra preta do Android fica representada fora da área útil. A prévia usa 1024 × 552 em paisagem e 600 × 976 em retrato. O APK e o navegador usam a área medida em execução.

## Implementação

- O APK concentra as cores e medidas em `android-hp71/src/net/oxog/phonebox/hp71/theme/`. A navegação usa Canvas e Views da API 17.
- O painel web em `agent/PhoneBox.Agent/legacy/` usa cores estáticas no CSS e aplica as preferências recebidas do agente em ES5.
- O editor do Windows em `agent/PhoneBox.Agent/settings.html` usa tokens CSS e os temas claro e escuro do sistema.
