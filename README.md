# PokectBank 🏦

App Android de banco infantil desenvolvido em **Kotlin** + **Jetpack Compose** para simular educação financeira para crianças — com cofrinho, metas de economia, missões, recompensas e ranking familiar.

## Funcionalidades

| Tela | Descrição |
|------|-----------|
| **Home** | Resumo do cofrinho (saldo + progresso da meta), nível/XP, missões em andamento, extrato de transações e depósito com confete |
| **Metas** | Criação e acompanhamento de metas de economia com alocação de moedas, emojis customizados e barra de progresso |
| **Missões** | Lista de missões com filtro por categoria (Diária/Semanal/Especial), barra de progresso e confete ao concluir |
| **Recompensas** | Loja de prêmios para resgatar com moedas acumuladas, verificação de saldo e modal de confirmação |
| **Ranking** | Leaderboard familiar ordenado por XP com desafio semanal |
| **Login** | Wizard de 2 etapas com seleção de avatar — estado de login persistido via DataStore |

## Tecnologias

- **Linguagem:** Kotlin 2.1.0 (Java 17 como target)
- **UI:** Jetpack Compose (BOM 2024.12.01) com Material 3
- **Persistência:** Room 2.8.4 (SQLite) para dados do usuário + DataStore Preferences para estado de login
- **Navegação:** Navigation Compose 2.8.5 (6 telas)
- **Injeção de Dependência:** Manual via singleton `AppContainer`
- **Build:** Gradle 9.3.1 (Kotlin DSL) com Version Catalog (`libs.versions.toml`)

### Dependências principais

| Biblioteca | Versão | Uso |
|---|---|---|
| Compose BOM | 2024.12.01 | Gerencia versões das bibliotecas Compose |
| Room (runtime + ktx) | 2.8.4 | Banco SQLite local com DAOs reativos (Flow) |
| Room Compiler (KSP) | 2.8.4 | Geração de código Room via KSP |
| DataStore Preferences | 1.2.1 | Persistência chave-valor do login |
| Navigation Compose | 2.8.5 | Navegação entre telas |
| Lifecycle ViewModel | 2.8.7 | Integração ViewModel com Compose |

**Testes:**
- JUnit 4 + Kotlin Test + kotlinx-coroutines-test
- Robolectric 4.14.1 (Android no JVM, sem emulador)
- Banco Room em memória nos testes

## Requisitos para execução

- **JDK 17+**
- **Android SDK Platform 36** (instalado via Android Studio ou `sdkmanager`)
- **Gradle 9.3.1** (baixado automaticamente pelo wrapper)
- Dispositivo/emulador **Android 8.0+** (API 26+)

## Como executar

```bash
# 1. Clonar o projeto
git clone <repo-url>
cd PokectBank

# 2. Build do projeto
./gradlew assembleDebug

# 3. Instalar em dispositivo/emulador conectado
./gradlew installDebug

# 4. Rodar testes
./gradlew test
```

Para abrir no **Android Studio**: importe a pasta raiz como projeto Gradle existente. O Android Studio detectará automaticamente as configurações do `build.gradle.kts`.

## Estrutura do projeto

```
app/src/main/java/com/pokect/bank/kids/
├── MainActivity.kt              # Activity única (entry point)
├── data/
│   ├── local/
│   │   ├── PokectBankDatabase.kt    # Room database (7 entidades)
│   │   ├── PokectBankConverters.kt  # Type converters
│   │   └── dao/                     # 7 DAOs (User, Transaction, etc.)
│   ├── models/                      # Entidades Room + enums
│   ├── preferences/
│   │   └── LoginPreferences.kt      # DataStore wrapper
│   └── repository/
│       ├── AppRepository.kt         # Lógica de negócio central
│       └── RepositoryEvent.kt       # Eventos cross-screen
├── di/
│   └── AppContainer.kt             # DI manual (singleton)
├── ui/
│   ├── components/                  # Componentes reutilizáveis
│   ├── navigation/                  # NavGraph + Screen routes
│   ├── screens/                     # 6 telas (home/goals/missions/rewards/ranking/login)
│   └── theme/                       # Material 3 theme (cores, fontes, animações)
└── util/                            # Formatadores (moeda pt-BR, datas)
```

## Dados de exemplo

O app inclui dados de demonstração populados automaticamente no primeiro acesso:
- Usuário **Lucas** com R$ 47,50 de saldo e nível 5
- **4 metas** de economia (`🎮 Video Game`, `🎸 Guitarra`, `📚 Livros`, `🎨 Material de Arte`)
- **5 missões** com diferentes categorias e dificuldades
- **6 recompensas** para resgatar com moedas
- **5 membros** da família no ranking (`Lucas`, `Ana`, `Pedro`, `Sofia`, `Miguel`)
- **1 desafio semanal** com recompensa em XP e moedas

## Integrantes

Os integrantes do grupo são:
- Clebson Pires Lira | [GitHub](https://github.com/Clebson-Lira)
- Edimar Barbosa Miranda Junior | [GitHub](https://github.com/edimarbmjunior)
- Gustavo de Oliveira Bossolani | [GitHub](https://github.com/gustavo-bossolani)
- Josemberg Sousa Duarte | [Github](https://github.com/josembergsd)
- Thiago de Morais Severo | [GitHub](https://github.com/ThiagoEncode)

## Licença

Projeto de estudo pessoal — sem licença definida.
