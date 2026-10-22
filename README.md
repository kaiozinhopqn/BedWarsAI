# BedWars AI (Minecraft Forge 1.8.9) - v0.3.0

Bots que existem de verdade no seu mundo (servidor integrado/LAN ou servidor seu com o mod).
O codigo compila e foi testado em servidor dedicado (ver "Teste automatizado").

## Compilar

**Requisito: JDK 8.** O Gradle 2.14 / ForgeGradle 2.1 nao rodam em Java 9+.
O JDK 8 (Temurin) esta em `C:\Users\Kaio\.jdks\temurin-8`.

    build.bat

Ou manualmente, com `JAVA_HOME` apontando para o JDK 8:

    set JAVA_HOME=C:\Users\Kaio\.jdks\temurin-8
    gradlew.bat -p . build

O wrapper (`gradlew.bat` + `gradle/`) ja esta no projeto (Gradle 2.14, versao do MDK 1.8.9).
`gradle.properties` fixa `org.gradle.java.home` para o JDK 8 desta maquina.

Jar gerado: `build/libs/BedWarsAI-0.3.0.jar` -> pasta `mods` de um perfil Forge 1.8.9.
Teste rapido sem instalar: `gradlew.bat -p . runClient` (ou `runServer` para servidor dedicado).

## Comandos
    /bwai help | version | status
    /bwai bot add <nome> [EASY|NORMAL|HARD|INSANE] [AGGRESSIVE|BALANCED|DEFENSIVE]
    /bwai bot remove <nome> | removeall | list
    /bwai bot difficulty <nome> <nivel>
    /bwai bot personality <nome> <personalidade>

## O que ja existe
- Bot como jogador real (EntityPlayerMP + conexao falsa), com nome, vida, inventario proprio.
- IA por estados: IDLE, CHASE, FIGHT, EAT, BRIDGE. Dificuldade muda CPS, mira e reacao.
- PvP: perseguicao, mira suave, ataque respeitando a invulnerabilidade do 1.8.9, strafe, sprint.
- Evita cair no vazio (checa chao a frente). Sem chao: constroi ponte agachada colocando blocos reais.
- Come maca dourada quando a vida esta baixa e nao ha inimigo perto.
- Kit inicial (espada, 128 la, 3 macas) configuravel em `config/bedwarsai.cfg`.

## Correcoes aplicadas nesta build
- `EnumFacing.getHorizontalAngle()` nao existe no 1.8.9: o yaw da direcao agora e calculado
  em `WorldUtil.yawForFacing` (NORTH=180, EAST=270, SOUTH=0, WEST=90).
- `FakeNetworkManager` agora monta o mesmo pipeline do servidor 1.8.9 (`MessageDeserializer2`,
  `MessageDeserializer`, `MessageSerializer2`, `MessageSerializer`). Sem isso o campo `channel`
  do `NetworkManager` ficava `null` e o FML (`makeEntitySpawnAdjustment`) causava
  NullPointerException no primeiro tick do EntityTracker.
- Bot criado pelo console/comando nasce no ponto de spawn do mundo (antes nascia em 0,0,0,
  dentro do terreno, e morria na hora).

## Teste automatizado (servidor dedicado)
`gradlew.bat -p . runServer` com `run/eula.txt` e `run/server.properties` ja configurados
(mundo flat, offline). Comandos usados no console:

    bwai version
    bwai status
    bwai bot add Bot1 NORMAL AGGRESSIVE
    bwai bot add Bot2 HARD BALANCED
    bwai bot list
    bwai bot removeall
    bwai bot list
    stop

Resultado observado: mod carrega ("BedWars AI 0.3.0 carregado"), bots nascem, se atacam
("Bot1 was slain by Bot2"), bots mortos sao removidos e nao ha excecao no log.

## Limitacoes conhecidas (v0.3.0)
- NAO ha: arenas, camas, equipes, lojas, geradores, partida BedWars, interface grafica, pathfinding A*.
- Todos os jogadores/bots sao inimigos entre si (sem equipes).
- Bot que morre e removido (sem respawn). Bot sem skin propria (aparece Steve/Alex).
- A ponte vai em linha reta no eixo dominante; nao desvia de obstaculos.
- Funciona so onde o mod esta instalado no servidor. NAO funciona em servidores publicos.

## Roteiro de teste no jogo
1. `/bwai bot add Teste` -> bot aparece na sua frente e na lista de jogadores (TAB).
2. Ele deve correr ate voce e bater. `/bwai bot list` mostra estado.
3. Va para uma ilha flutuante: ao ficar sem chao ele deve construir ponte ate voce.
4. Deixe a vida baixa e afaste-se: ele deve comer maca.
5. `/bwai bot removeall` e saia do mundo: nao deve haver erro no log.
