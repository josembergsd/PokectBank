# Roadmap: PokectBank — Persistência

## Overview

Refatoração da camada de persistência do PokectBank: substituir o armazenamento 100% em memória (MutableStateFlow + Mutex) por Room Database + DataStore Preferences, mantendo a API pública do AppRepository inalterada e as 6 telas Compose funcionando identicamente. O milestone entrega dados que sobrevivem a reinicializações do app, login persistente, e um botão de reset para estado de primeira instalação.

## Phases

- [ ] **Phase 1: Persistência Room + DataStore** - AppRepository migrado para Room/DataStore mantendo API pública inalterada, com seed data, reset e testes

## Phase Details

### Phase 1: Persistência Room + DataStore
**Goal**: Criança consegue simular depósitos, metas e recompensas com os dados persistindo entre sessões — fecha o app, reabre, tudo continua como deixou. Login state persiste. Botão de reset restaura seed data.
**Depends on**: Nothing (first phase)
**Requirements**: BUILD-01, BUILD-02, ROOM-01, ROOM-02, ROOM-03, ROOM-04, ROOM-05, ROOM-06, ROOM-07, ROOM-08, ROOM-09, ROOM-10, ROOM-11, ROOM-12, ROOM-13, ROOM-14, DATA-01, DATA-02, DATA-03, REPO-01, REPO-02, REPO-03, REPO-04, REPO-05, REPO-06, RESET-01, RESET-02, RESET-03, RESET-04, TEST-01, TEST-02, TEST-03
**Success Criteria** (what must be TRUE):
  1. Usuário faz login com avatar selecionado, fecha e reabre o app — permanece logado na mesma conta (login state persiste via DataStore Preferences)
  2. Usuário realiza depósitos, cria/edita metas e resgata recompensas, fecha e reabre o app — todos os dados (saldo, histórico de transações, metas com valores alocados, recompensas resgatadas, ranking familiar) preservados exatamente como deixou
  3. Usuário pressiona "Resetar Dados" na HomeScreen, confirma o diálogo de confirmação — app volta ao estado de primeira instalação com seed data recriado (usuário demo com R$47.50, 4 metas, 5 missões, 6 recompensas, 5 membros da família)
  4. Todas as 6 telas (Home, Goals, Missions, Rewards, Ranking, Login) exibem os mesmos dados e comportamentos de antes da migração — navegação, CRUD e progresso visual sem regressões
**Plans**: 4 plans — **Status: PLANNED**

Plans:
- [x] **01-01-PLAN.md** — Build setup + DataStore Login: KSP 2.1.0-1.0.29 + Room 2.8.4 + DataStore 1.2.1 no Gradle, LoginPreferencesManager com Flow<LoginPrefsData>, LoginViewModel consome DataStore, MainActivity auth gate de 3 estados
- [x] **01-02-PLAN.md** — Room Foundation: PokectBankConverters (14 TypeConverters), 7 modelos anotados com @Entity(tableName), 7 DAOs com Flow<T> observe, PokectBankDatabase singleton com seed via execSQL() em onCreate()
- [x] **01-03-PLAN.md** — AppRepository Refactor: Substituir MutableStateFlow + 5 Mutex por DAO Flow→StateFlow bridge (repositoryScope.launch+collect), @Transaction write methods, remover createSeed*(), preservar API pública idêntica
- [x] **01-04-PLAN.md** — Reset + Tests + Wiring: ResetSection + AlertDialog M3 na HomeScreen, AppRepository.resetData(), AppContainer.resetRepository(), distinctUntilChanged nos ViewModels, Testes de TypeConverter (round-trip) + DAO (inMemoryDatabase) + AppRepositoryTest migrado

## Progress

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Persistência Room + DataStore | 0/4 | Planned | 2026-05-21 |
