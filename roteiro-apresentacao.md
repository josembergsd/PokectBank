# Roteiro de Apresentação — PokectBank (10 minutos)

| Tempo | Conteúdo |
|-------|----------|
| 1 min | Problema e proposta do app. |
| 1 min | Equipe, papéis e tecnologias utilizadas. |
| 4 min | Demonstração do fluxo principal do aplicativo, com cada integrante falando sobre sua tela. |
| 2 min | Arquitetura MVVM, API consumida e persistência local. |
| 1 min | Diferencial do projeto e decisões de escopo. |
| 1 min | GitHub/README, aprendizados e encerramento. |

---

## Distribuição das Telas

| Integrante | Tela |
|------------|------|
| Gustavo | Login |
| Edimar | Ranking |
| Thiago | Prêmios |
| Clebson | Home e Missões |
| Josemberg | Metas |

---

## Falas por Bloco

---

### 1. Problema e Proposta (0:00–1:00) — Apresentador

> "Boa noite a todos. Nosso projeto é o **PokectBank**, um aplicativo Android de banco infantil voltado para educação financeira de crianças.
>
> A gente identificou que educação financeira infantil ainda é pouco abordada de forma lúdica e digital — a maioria das crianças não tem contato com conceitos como poupar, definir metas ou entender o valor do dinheiro.
>
> O PokectBank resolve isso simulando um banco de verdade, mas num ambiente gamificado e seguro: a criança tem um cofrinho virtual, pode depositar moedas, criar metas de economia — como comprar um video game ou um livro —, cumprir missões, ganhar recompensas e competir num ranking familiar. Tudo isso sem dinheiro real, 100% offline e com uma interface colorida e animada feita pra criança."

---

### 2. Equipe, Papéis e Tecnologias (1:00–2:00) — Apresentador

> "A equipe é formada por cinco integrantes e cada um ficou responsável por uma tela do aplicativo:
>
> - **Gustavo** desenvolveu a tela de **Login**,
> - **Edimar** ficou com a tela de **Ranking**,
> - **Thiago** cuidou da tela de **Prêmios**,
> - **Clebson** fez as telas de **Home** e **Missões**,
> - **Josemberg** desenvolveu a tela de **Metas**.
>
> As tecnologias que usamos foram: **Kotlin** como linguagem principal, **Jetpack Compose** com Material 3 pra interface, **Room** como banco de dados local SQLite e **DataStore Preferences** pra persistir a sessão de login. A arquitetura é **MVVM** — Model-View-ViewModel —, e a injeção de dependência é feita de forma manual, sem frameworks como Hilt ou Koin. O app roda em Android 8.0 ou superior, apenas em modo retrato."

---

### 3. Demonstração do Fluxo Principal (2:00–6:00)

#### 3.1 Login (2:00–2:45) — Gustavo

> *(Com o app aberto na tela de login)*
>
> "Eu desenvolvi a tela de **Login**. Como o app é voltado pra crianças, a gente optou por uma autenticação bem simples: um wizard de duas etapas. Na primeira etapa a criança digita o nome e escolhe um avatar entre várias opções ilustradas. Na segunda etapa ela confirma os dados e entra no app.
>
> O estado de login é persistido com **DataStore Preferences** — então quando a criança abre o app de novo, ela já cai direto na Home, sem precisar fazer login outra vez. Também tem a opção de sair da conta, que limpa os dados locais."

#### 3.2 Home (2:45–3:30) — Clebson

> *(Navegando para a tela Home)*
>
> "A **Home** é a tela principal do app. Aqui a criança vê o cofrinho com o saldo atual — o usuário de demonstração é o Lucas, que começa com R$ 47,50 —, o nível e a barra de XP. Mais abaixo tem as missões em andamento, pra criança ver rápido o que ela precisa fazer, e o extrato de transações com tudo que entrou e saiu.
>
> O botão de depósito é o principal da tela: a criança escolhe um valor, confirma, e na hora o saldo atualiza, caem confetes na tela e a transação aparece no extrato. Cada depósito também dá XP, então a criança vai subindo de nível conforme usa o app."

#### 3.3 Missões (3:30–4:10) — Clebson

> *(Navegando para Missões)*
>
> "Na tela de **Missões** a criança encontra tarefas que ela pode cumprir pra ganhar moedas e XP. As missões são divididas por categoria — Diária, Semanal e Especial — e por dificuldade — Fácil, Média e Difícil. Dá pra filtrar por categoria no topo da tela.
>
> Tem missões como 'Arrumar o quarto', 'Ajudar na louça', 'Ler um livro'... cada uma tem uma recompensa em moedas e XP. A criança pode iniciar uma missão depois de aceitar o desafio, e quando concluída o progresso atualiza e mostra a animação de conclusão. Isso estimula a criança a transformar tarefas do dia a dia em conquistas."

#### 3.4 Metas (4:10–4:55) — Josemberg

> *(Navegando para Metas)*
>
> "Na tela de **Metas** a criança aprende a poupar com objetivos concretos. Ela pode criar uma meta de economia — por exemplo 'Comprar um Video Game' por R$ 200,00 — escolher um emoji e uma cor pra representar a meta.
>
> Conforme a criança deposita moedas no cofrinho, ela pode alocar valores pras metas. A barra de progresso mostra visualmente quanto já foi guardado e quanto falta. Quando a meta é atingida, ela fica marcada como concluída. O app já vem com 4 metas de exemplo: Video Game, Guitarra, Livros e Material de Arte."

#### 3.5 Prêmios (4:55–5:40) — Thiago

> *(Navegando para Prêmios)*
>
> "A tela de **Prêmios** funciona como uma loja de recompensas. A criança acumula moedas fazendo depósitos e cumprindo missões, e pode trocar essas moedas por prêmios cadastrados — por exemplo '30 min de TV extra' por 50 moedas, ou 'Escolher o jantar' por 80 moedas.
>
> Quando a criança clica num prêmio, o app verifica se ela tem saldo suficiente. Se tiver, abre um modal de confirmação mostrando o custo e o saldo restante. Ao confirmar, o valor é debitado, o prêmio é marcado como resgatado e uma animação de confete comemora a conquista. Se não tiver saldo, aparece uma mensagem explicando que faltam moedas."

#### 3.6 Ranking (5:40–6:00) — Edimar

> *(Navegando para Ranking)*
>
> "O **Ranking** traz uma competição saudável dentro da família. Cada membro aparece num card com avatar, nível e XP. A lista é ordenada do maior pro menor XP, então a criança vê em que posição ela está e pode se motivar a subir no ranking.
>
> Além do ranking, temos o **Desafio Semanal** — uma meta coletiva que aparece no topo da tela com uma barra de progresso. Todo mundo contribui junto e, quando bate a meta, todos ganham uma recompensa. Isso estimula a colaboração, não só a competição."

---

### 4. Arquitetura MVVM e Persistência (6:00–8:00) — Apresentador

> "Agora vou explicar como o app funciona por dentro. Usamos a arquitetura **MVVM**: cada tela tem uma ViewModel que expõe um `StateFlow` com o estado da interface. A tela, que é uma função Composable, coleta esse estado com `collectAsStateWithLifecycle` e renderiza a interface.
>
> O fluxo de dados é单向: a ViewModel chama métodos do **AppRepository**, que é a camada central de regras de negócio. O repositório escreve no banco, e o Room emite automaticamente novos valores via `Flow`, que o repositório coleta e repassa pras ViewModels.
>
> Pra persistência, usamos duas ferramentas oficiais do Android Jetpack:
>
> - **Room**: banco SQLite com 7 entidades — Usuário, Transação, Meta, Missão, Prêmio, Membro da Família e Desafio Semanal. Cada entidade tem seu DAO com consultas reativas usando `Flow`. São 7 DAOs no total.
>
> - **DataStore Preferences**: guarda o estado de login como chave-valor — se a criança está logada e qual avatar ela escolheu. É isso que permite abrir o app e já cair direto na Home.
>
> O **AppRepository** atua como ponte entre os Flows do Room e os StateFlows consumidos pelas ViewModels. Toda operação de escrita no repositório retorna `Result<Unit>`, então a ViewModel sempre sabe se deu certo ou se precisa mostrar um erro. As mensagens de erro são em português, pensando no público infantil.
>
> Por fim, não usamos nenhuma biblioteca de injeção de dependência — Hilt, Koin, nada disso. Tudo é feito com um singleton manual chamado `AppContainer`, que cria o banco, o repositório e as preferências uma única vez. Isso deixa o projeto mais simples e com menos mágica."

---

### 5. Diferencial e Decisões de Escopo (8:00–9:00) — Apresentador

> "O que diferencia o PokectBank de outros apps?
>
> Primeiro, ele é **100% offline** — a criança não precisa de internet pra usar. Isso é importante porque muitos pais não querem que crianças fiquem expostas à rede o tempo todo.
>
> Segundo, a **gamificação completa**: o app não é só um cofrinho digital. Tem sistema de níveis com XP, missões com categorias e dificuldades, ranking familiar com competição saudável, desafio semanal colaborativo, recompensas resgatáveis e animações de confete e progresso que dão feedback visual imediato.
>
> Terceiro, o foco é **educação, não transação real**: não tem dinheiro de verdade envolvido. A criança aprende conceitos como poupar, planejar, definir metas e merecer recompensas pelo esforço — tudo num ambiente seguro e controlado pelos pais.
>
> Algumas decisões de escopo que tomamos: optamos por não implementar autenticação real com senha, porque o público é infantil. Os dados ficam 100% no dispositivo — sem servidor, sem nuvem. O banco de dados usa `fallbackToDestructiveMigration`, ou seja, se a estrutura mudar, os dados são recriados do zero — como é um app de estudo, isso é aceitável e simplifica o desenvolvimento."

---

### 6. GitHub, Aprendizados e Encerramento (9:00–10:00) — Apresentador

> "O código completo está no GitHub, no link que está no README do projeto. O repositório inclui instruções de como clonar, buildar e rodar os testes.
>
> Os principais aprendizados do grupo foram:
>
> - Integrar **Room com Jetpack Compose** usando Flows reativos e `collectAsStateWithLifecycle`.
> - Implementar arquitetura **MVVM do zero**, sem frameworks de DI, entendendo cada camada na prática.
> - Escrever **testes automatizados** com Robolectric, que roda o Android na JVM sem precisar de emulador — temos mais de 30 testes cobrindo DAOs, repositório e ViewModels.
> - Trabalhar com **Version Catalog** do Gradle pra centralizar dependências e manter o build organizado.
>
> Era isso que tínhamos pra apresentar. Obrigado pela atenção e estamos abertos a perguntas!"

---

## Resumo de Cronometragem

| Minuto | Quem fala | Conteúdo |
|--------|-----------|----------|
| 0:00–1:00 | Apresentador | Problema e proposta |
| 1:00–2:00 | Apresentador | Equipe, papéis e tecnologias |
| 2:00–2:45 | Gustavo | Demonstração: Login |
| 2:45–3:30 | Clebson | Demonstração: Home |
| 3:30–4:10 | Clebson | Demonstração: Missões |
| 4:10–4:55 | Josemberg | Demonstração: Metas |
| 4:55–5:40 | Thiago | Demonstração: Prêmios |
| 5:40–6:00 | Edimar | Demonstração: Ranking |
| 6:00–8:00 | Apresentador | Arquitetura MVVM e persistência |
| 8:00–9:00 | Apresentador | Diferencial e decisões |
| 9:00–10:00 | Apresentador | GitHub, aprendizados e encerramento |
