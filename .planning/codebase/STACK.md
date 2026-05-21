# Technology Stack

**Analysis Date:** 2026-05-21

## Languages

**Primary:**
- Kotlin 2.1.0 — All application source code under `app/src/main/java/com/pokect/bank/kids/`
- Java 17 (compile target) — JVM target for Kotlin compilation (`jvmTarget = "17"` in `app/build.gradle.kts`); no hand-written Java source files

**Build Scripts:**
- Gradle Kotlin DSL — `build.gradle.kts`, `app/build.gradle.kts`, `settings.gradle.kts`

## Runtime

**Environment:**
- Android — `minSdk = 26` (Android 8.0 Oreo), `targetSdk = 36`, `compileSdk = 36`
- Portrait-only orientation (`android:screenOrientation="portrait"` in `AndroidManifest.xml`)

**Package Manager:**
- Gradle 9.3.1 (wrapper — `gradle/wrapper/gradle-wrapper.properties`)
- Version Catalog: `gradle/libs.versions.toml` (centralized dependency management)
- Lockfile: Not applicable (Gradle uses version catalog; dependency verification not configured)

## Frameworks

**Core UI:**
- Jetpack Compose BOM 2024.12.01 — UI framework (manages all Compose library versions)
  - `androidx.compose.material3:material3` — Material 3 design components
  - `androidx.compose.material:material-icons-extended` — Extended icon set
  - `androidx.compose.ui:ui-text` 1.11.1 — Text rendering

**Navigation:**
- AndroidX Navigation — `navigation-compose` 2.8.5 (`NavHost`-based screen navigation, 6 routes defined in `app/src/main/java/com/pokect/bank/kids/ui/navigation/NavGraph.kt`)

**Lifecycle & ViewModel:**
- AndroidX Lifecycle 2.8.7 — `lifecycle-runtime-compose`, `lifecycle-viewmodel-compose`
- AndroidX Activity — `activity-compose` 1.9.3 (Compose integration via `ComponentActivity`)

**Persistence:**
- Room 2.8.4 — SQLite ORM (`room-runtime`, `room-ktx`, `room-compiler`)
  - 7 entities: `User`, `Transaction`, `SavingsGoal`, `Mission`, `Reward`, `FamilyMember`, `WeeklyChallenge`
  - 7 DAOs with Flow-based reactive observation
  - Database file: `pokectbank.db` (single-file SQLite, version 1, no export schema)
  - Migration strategy: `fallbackToDestructiveMigration()` (destroys and recreates on schema change)
- DataStore Preferences 1.2.1 — Key-value storage for login state (`login_state`)

**Code Generation:**
- KSP 2.1.0-1.0.29 (`com.google.devtools.ksp`) — Kotlin Symbol Processing for Room annotation processing

**Build:**
- Android Gradle Plugin (AGP) 8.7.3
- Kotlin Compose Compiler Plugin — `org.jetbrains.kotlin.plugin.compose` 2.1.0 (bundled in Kotlin 2.0+)
- Gradle Toolchain Resolver — `org.gradle.toolchains.foojay-resolver-convention` 1.0.0

**Testing:**
| Framework | Version | Purpose |
|---|---|---|
| JUnit | 4.13.2 | Unit test runner |
| Kotlin Test | 2.1.0 | `org.jetbrains.kotlin:kotlin-test` |
| Kotlinx Coroutines Test | 1.8.1 | `kotlinx-coroutines-test` for coroutine testing |
| Compose UI Test JUnit4 | (via BOM) | Compose UI testing assertions |
| Compose UI Test Manifest | (via BOM) | `debugImplementation` — test manifest for Compose UI tests |
| AndroidX Test Core | 1.6.1 | `androidx.test:core` — Android instrumentation test support |
| Robolectric | 4.14.1 | Android unit testing framework (runs Android code on JVM) |

## Key Dependencies

**Critical (build-time):**
| Package | Version | Purpose |
|---|---|---|
| `compose-bom` | 2024.12.01 | Single version manager for all Compose libraries |
| `room-compiler` | 2.8.4 | KSP-based annotation processor for Room entities/DAOs |
| `ksp` plugin | 2.1.0-1.0.29 | Kotlin Symbol Processing for Room code generation |

**Persistence (runtime):**
| Package | Version | Purpose |
|---|---|---|
| `room-runtime` | 2.8.4 | Room database framework core |
| `room-ktx` | 2.8.4 | Kotlin coroutines extensions for Room |
| `datastore-preferences` | 1.2.1 | DataStore key-value storage for login preferences |

**Infrastructure (runtime):**
| Package | Version | Purpose |
|---|---|---|
| `navigation-compose` | 2.8.5 | Screen-based in-app navigation |
| `lifecycle-runtime-compose` | 2.8.7 | Lifecycle-aware composition |
| `lifecycle-viewmodel-compose` | 2.8.7 | ViewModel integration with Compose |
| `activity-compose` | 1.9.3 | Compose host activity setup |

## Configuration

**Environment:**
- No `.env` files or environment variable configuration — app is fully self-contained
- No API keys, no external service configuration required

**Build Configuration:**
| File | Purpose |
|---|---|
| `gradle/libs.versions.toml` | Centralized version catalog — all dependency versions and plugin definitions |
| `build.gradle.kts` (root) | Plugin declarations: AGP, Kotlin, Compose, KSP (all `apply false`) |
| `app/build.gradle.kts` | App module: SDK versions, Compose feature flag, dependencies, compile options |
| `settings.gradle.kts` | Repository config (Google, Maven Central, Gradle Plugin Portal), project name "PokectBank" |
| `gradle.properties` | JVM args (2048m), AndroidX enabled, Kotlin official style, parallel builds disabled (max 2 workers) |
| `gradle/wrapper/gradle-wrapper.properties` | Gradle 9.3.1 distribution URL |
| `app/proguard-rules.pro` | ProGuard rules — keeps `@Composable` methods for release builds |
| `app/src/main/AndroidManifest.xml` | App manifest — single Activity, portrait, singleTask launch mode |

**Room Configuration:**
- Database class: `app/src/main/java/com/pokect/bank/kids/data/local/PokectBankDatabase.kt`
- Database version: 1, `exportSchema = false`
- Type converters: `PokectBankConverters.kt` — handles enums, `List<String>`, and `Instant` ↔ `Long`
- Seed data: `SeedDatabaseCallback` — SQL insert statements on database creation (pre-populates demo data)
- In-memory variant available for tests via `getTestInstance()`

**DataStore Configuration:**
- Store name: `login_state`
- File: `app/src/main/java/com/pokect/bank/kids/data/preferences/LoginPreferences.kt`
- Properties: `is_logged_in` (boolean), `selected_avatar` (int)
- Exposed as `Flow<LoginPrefsData>` from `LoginPreferences`

## Platform Requirements

**Development:**
- Android Studio (or compatible IDE with Android SDK)
- JDK 17+ (JVM target in `app/build.gradle.kts`: `jvmTarget = "17"`)
- Android SDK Platform 36
- Gradle 9.3.1 (auto-downloaded via wrapper)
- KSP plugin for Room annotation processing

**Production:**
- Android 8.0+ (API 26+)
- Portrait orientation only
- No network permission required (fully offline)
- No Google Play Services dependency
- No cloud backend — all data stored locally in SQLite + DataStore

---

*Stack analysis: 2026-05-21*
