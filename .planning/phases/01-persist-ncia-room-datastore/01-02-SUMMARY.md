---
phase: 01-persist-ncia-room-datastore
plan: 02
subsystem: data/local/
tags: ["room", "entity", "dao", "database", "seed-data", "type-converter"]
requires: ["01-01"]
provides: ["PokectBankDatabase", "PokectBankConverters", "7 DAO interfaces"]
affects: ["app/src/main/java/com/pokect/bank/kids/data/models/*.kt"]
tech-stack:
  added: ["Room @Entity", "Room @Dao", "Room @Database", "Room @TypeConverter"]
  patterns: ["Dual-key pattern (uid + id)", "Flow<T> observe methods", "Suspend write methods", "execSQL seed callback"]
key-files:
  created:
    - app/src/main/java/com/pokect/bank/kids/data/local/PokectBankConverters.kt
    - app/src/main/java/com/pokect/bank/kids/data/local/PokectBankDatabase.kt
    - app/src/main/java/com/pokect/bank/kids/data/local/dao/UserDao.kt
    - app/src/main/java/com/pokect/bank/kids/data/local/dao/TransactionDao.kt
    - app/src/main/java/com/pokect/bank/kids/data/local/dao/SavingsGoalDao.kt
    - app/src/main/java/com/pokect/bank/kids/data/local/dao/MissionDao.kt
    - app/src/main/java/com/pokect/bank/kids/data/local/dao/RewardDao.kt
    - app/src/main/java/com/pokect/bank/kids/data/local/dao/FamilyMemberDao.kt
    - app/src/main/java/com/pokect/bank/kids/data/local/dao/WeeklyChallengeDao.kt
  modified:
    - app/src/main/java/com/pokect/bank/kids/data/models/User.kt
    - app/src/main/java/com/pokect/bank/kids/data/models/Transaction.kt
    - app/src/main/java/com/pokect/bank/kids/data/models/SavingsGoal.kt
    - app/src/main/java/com/pokect/bank/kids/data/models/Mission.kt
    - app/src/main/java/com/pokect/bank/kids/data/models/Reward.kt
    - app/src/main/java/com/pokect/bank/kids/data/models/FamilyMember.kt
    - app/src/main/java/com/pokect/bank/kids/data/models/WeeklyChallenge.kt
  unchanged:
    - app/src/main/java/com/pokect/bank/kids/data/models/AvatarOption.kt
decisions:
  - "D-01: Anotar models existentes com @Entity diretamente (implementado)"
  - "D-02: Dual-key pattern — uid (@PrimaryKey autoGenerate) + id (String, unique index)"
  - "D-03: TypeConverters em PokectBankConverters classe única"
  - "D-04: Seed via execSQL() em onCreate() callback"
metrics:
  duration: "~3 min"
  completed_at: "2026-05-21"
  tasks: 2
  commits: 2
  files_created: 9
  files_modified: 7
---

# Phase 01 Plan 02: Room Persistence Layer — Complete

**One-liner:** Complete Room persistence infrastructure: 7 @Entity-annotated models with dual-key pattern, 14 TypeConverter methods, 7 DAO interfaces with Flow<T> reactive queries, and PokectBankDatabase singleton with execSQL seed callback.

## Execution Summary

Executed all 2 tasks as type="auto" (fully autonomous, no checkpoints). Both tasks compiled successfully with `./gradlew assembleDebug` — Room KSP generated DAO implementation classes without errors.

## Tasks Completed

| Task | Name | Commit | Files |
|------|------|--------|-------|
| 1 | Create PokectBankConverters + annotate all 7 models with @Entity | `2beaed8` | PokectBankConverters.kt + 7 model files |
| 2 | Create all 7 DAO interfaces + PokectBankDatabase with seed callback | `e66cc4a` | 7 DAOs + PokectBankDatabase.kt |

## Task Details

### Task 1: PokectBankConverters + @Entity annotations

**PokectBankConverters** (14 methods, 7 converter pairs):
- 5 enum pairs: MissionStatus, MissionDifficulty, MissionCategory, TransactionType, GoalColor — using `.name` / `.valueOf()`
- 1 List<String> pair: `joinToString(",")` / `split(",")` — for User.badges and WeeklyChallenge.participants
- 1 Instant pair: `toEpochMilli()` / `ofEpochMilli()` — for WeeklyChallenge.endDate

**Model annotations** (per D-01, D-02):
- All 7 models annotated with `@Entity(tableName = "...", indices = [Index(value = ["id"], unique = true)])`
- Each gains `@PrimaryKey(autoGenerate = true) val uid: Long = 0` as first constructor parameter
- Original `id: String` kept as regular column (unique index) — dual-key pattern per D-02
- User gains both `uid` and `id: String = "user-1"` (previously had neither)
- AvatarOption.kt left unchanged (not persisted)
- All instantiations use named parameters — zero breakage at call sites

### Task 2: DAOs + PokectBankDatabase

**7 DAO interfaces** with consistent pattern:
- `observe*()` → `Flow<T>` (reactive, non-suspend)
- `insert*()` → `suspend`, `OnConflictStrategy.REPLACE`
- `update*()` → `suspend` (where applicable)
- `*ByOriginalId()` → lookup by seed UUID (`id` String)
- `deleteAll()` → `suspend`
- FamilyMemberDao: `syncCurrentUser(xp, coins, level)` UPDATE method

**PokectBankDatabase:**
- `@Database` with all 7 entities, `@TypeConverters(PokectBankConverters::class)`, version = 1, exportSchema = false
- 7 abstract DAO accessor methods
- Singleton via `@Volatile INSTANCE` + double-checked `synchronized` locking
- `resetInstance(context)`: close → null → deleteDatabase → rebuild
- `.fallbackToDestructiveMigration()` configured
- `SeedDatabaseCallback.onCreate()` populates all 7 tables via execSQL()

**Seed data** matches AppRepository.kt createSeed*() methods exactly:
- User "Lucas" — R$47.50, level 5, XP 750, 85 coins, 8 badges
- 5 transactions (t1–t5)
- 3 savings goals (g1–g3)
- 4 missions (m1–m4)
- 4 rewards (r1–r4)
- 5 family members (fm1–fm5)
- 1 weekly challenge (wc1) — endDate computed at creation time via `Instant.now().plus(7 days)`

## Deviations from Plan

None — plan executed exactly as written. The 6 entities (Transaction, SavingsGoal, Mission, Reward, FamilyMember, WeeklyChallenge) had no pre-existing `@PrimaryKey` annotations on their `id` field (the plan described this as something to "remove", but the actual codebase had no Room annotations at all). This made the migration even simpler: just add annotations without needing to remove any.

## Threat Surface

No new threat surface introduced beyond what the plan's threat model covers. All files are in the data/local/ layer (Room infrastructure) with no network or user-facing entry points.

## Known Stubs

None identified — all created files are real implementations.

## Requirements Satisfied

- ROOM-01: PokectBankConverters with TypeConverters for 5 enums + List<String> + Instant
- ROOM-02: 7 entities annotated with @Entity
- ROOM-03: Entities maintain same fields and types as original models
- ROOM-04: UserDao with @Insert, @Update, @Query returning Flow<User>
- ROOM-05: TransactionDao with @Insert, @Query returning Flow<List<Transaction>>
- ROOM-06: SavingsGoalDao with @Insert, @Update, @Query returning Flow<List<SavingsGoal>>
- ROOM-07: MissionDao with @Query returning Flow<List<Mission>>
- ROOM-08: RewardDao with @Insert, @Update, @Query returning Flow<List<Reward>>
- ROOM-09: FamilyMemberDao with @Insert, @Update, @Query returning Flow<List<FamilyMember>>
- ROOM-10: WeeklyChallengeDao with @Insert, @Update, @Query returning Flow<List<WeeklyChallenge>>
- ROOM-11: FamilyMemberDao.syncCurrentUser() for cross-entity consistency
- ROOM-12: PokectBankDatabase with @Database listing all 7 entities + TypeConverters
- ROOM-13: Singleton via companion object with synchronized getInstance()
- ROOM-14: RoomDatabase.Callback.onCreate() populates seed data

## Verification

- [x] `./gradlew assembleDebug` compiles — all entities annotated, all DAOs implemented, database class compiles
- [x] 7 DAO files in data/local/dao/ (verified via `find ... | wc -l`)
- [x] 7 model files have @Entity annotation with indices (verified via grep)
- [x] 14 @TypeConverter methods in PokectBankConverters (verified via grep)
- [x] execSQL contains "Lucas" and "47.5" in PokectBankDatabase.kt (verified via grep)
- [x] All DAO write methods are suspend — no allowMainThreadQueries()
- [x] fallbackToDestructiveMigration() configured
- [x] AvatarOption.kt unchanged (no @Entity annotation)

## Self-Check: PASSED
