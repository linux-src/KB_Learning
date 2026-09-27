<div align="center">

# KB Wallet

**A Kotlin Multiplatform crypto portfolio & paper-trading app**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-Multiplatform-4285F4?logo=jetpackcompose&logoColor=white)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Platform](https://img.shields.io/badge/platform-Android%20%7C%20iOS%20%7C%20Desktop-lightgrey)]()
[![Ktor](https://img.shields.io/badge/backend-Ktor-orange?logo=ktor&logoColor=white)](https://ktor.io)

</div>

> **⚠️ Educational project.** KB Wallet is a learning/demo app. Nothing in it is financial advice, and the trading simulator uses fictional balances — no real money or real exchange is involved.

## What it is

KB Wallet is a Compose Multiplatform app (Android, iOS, and Desktop from one Kotlin codebase) for tracking a crypto portfolio, practicing trades in a simulated market, and learning the fundamentals of crypto — from "what is a blockchain" to reading candlestick charts.

## Features

- **Portfolio tracking** — holdings, live prices, P&L, transaction history
- **Buy / Sell flow** with a simulated balance — no real funds ever move
- **Trading simulator** — a background market engine that fills limit/stop orders against live-ish price ticks, so you can practice order types risk-free
- **Charts** — Japanese candlesticks by default, with an OHLC crosshair readout, SMA overlay, pan/zoom and multiple timeframes; a simplified line view is one tap away
- **Watchlist** — track coins you don't hold yet
- **Crypto Library** — a 17-topic reference built into the app, from beginner basics (wallets, private keys, exchanges, stablecoins) to advanced topics (DeFi, tokenomics, leverage, taxes)
- **Notifications** — a purchase raises an animated in-app confirmation, a foreground banner, an entry in the in-app notification centre, and a real OS notification (Android status bar, iOS notification centre, desktop tray), each gated by the switches in notification settings
- **Biometric login** and a lightweight auth server (Ktor) for account security
- **Analytics** — realized/unrealized P&L breakdown

## Tech stack

| Layer | Choice |
|---|---|
| UI | Compose Multiplatform (Material 3) |
| Language | Kotlin Multiplatform (Android + iOS + Desktop) |
| DI | Koin |
| Networking | Ktor Client |
| Persistence | Room (KMP) |
| Images | Coil 3 |
| Backend | Ktor server (auth, hashing, tokens) |
| Serialization | kotlinx.serialization / kotlinx.datetime |

## Project structure

```
KB_Learning/
├── composeApp/            # Shared KMP client
│   └── src/
│       ├── commonMain/    # Shared UI + business logic (feature packages under app/)
│       ├── androidMain/   # Android-specific implementations
│       ├── iosMain/       # iOS-specific implementations
│       └── desktopMain/   # Desktop (JVM) specific implementations
├── iosApp/                # iOS app entry point (SwiftUI shell hosting Compose)
└── server/                # Ktor backend: auth, user accounts, token issuing
```

Client code under `composeApp/src/commonMain/kotlin/dev/kbwallet/app/` is organized by feature (`portfolio`, `trade`, `chart`, `coins`, `watchlist`, `history`, `library`, `analytics`, `simulator`, `notifications`, `profile`, `dashboard`), each following a rough data → domain → presentation layering, wired together with Koin.

## Getting started

**Prerequisites**
- JDK 21
- Android Studio (latest stable) for Android
- Xcode + a Mac for the iOS target (Kotlin/Native's iOS targets can't be built on Linux/Windows)
- Desktop has no extra prerequisite beyond JDK 21 — it builds and runs on Linux, macOS, and Windows alike, from any of those OSes

**Build & run**

```bash
# Android debug build
./gradlew :composeApp:assembleDebug

# Run backend server locally
./gradlew :server:run

# Run tests
./gradlew :composeApp:allTests
```

For iOS, open `iosApp/iosApp.xcodeproj` in Xcode and run — it hosts the shared Compose UI via the `ComposeApp` framework produced by the `:composeApp` module.

### Desktop

Run it straight from Gradle, no packaging needed:

```bash
./gradlew :composeApp:run
```

To produce a redistributable native app, build the installer for your OS (each must be built on that OS — Compose's `jpackage`-based packaging doesn't cross-compile):

| OS | Command | Output |
|---|---|---|
| Linux (Debian/Ubuntu/Mint) | `./gradlew :composeApp:packageDeb` | `composeApp/build/compose/binaries/main/deb/*.deb` — install with `sudo apt install ./kb-wallet_*.deb` |
| Linux (Fedora/RHEL) | `./gradlew :composeApp:packageRpm` | `composeApp/build/compose/binaries/main/rpm/*.rpm` (needs `rpmbuild` installed) |
| macOS | `./gradlew :composeApp:packageDmg` | `composeApp/build/compose/binaries/main/dmg/*.dmg` |
| Windows | `./gradlew :composeApp:packageMsi` | `composeApp/build/compose/binaries/main/msi/*.msi` (needs the [WiX Toolset](https://wixtoolset.org/) installed) |

Or skip installing altogether and just unpack a portable app image:

```bash
./gradlew :composeApp:createDistributable
# → composeApp/build/compose/binaries/main/app/KB Wallet/
```

All native distributions bundle their own JRE, so end users don't need Java installed. Every format is declared in `composeApp/build.gradle.kts` under `compose.desktop.application.nativeDistributions`.

## Configuration & secrets

Nothing sensitive is committed to the repo. Local dev works out of the box with safe defaults; override them for anything beyond local testing:

| Setting | Where | Default | Override |
|---|---|---|---|
| CoinRanking API key | `composeApp` (client) | shared demo key, code-generated at build time | `coinRanking.apiKey` in `local.properties`, or `COINRANKING_API_KEY` env var |
| JWT signing secret | `server` | dev-only insecure placeholder | `JWT_SECRET` env var — **required** before any real deployment |
| CORS allowed hosts | `server` | `localhost:8080,10.0.2.2:8080` | `CORS_ALLOWED_HOSTS` env var (comma-separated `host:port`) |

Note the CoinRanking key is embedded in the compiled client either way (any API key shipped in a mobile app can be extracted from the binary) — keeping it out of source control is about repo hygiene, not making it a real secret. The JWT secret is a real server-side secret and must be set via env var in any environment handling real accounts.

**Release signing (Android):** copy `keystore.properties.example` to `keystore.properties` (gitignored), generate your own keystore (`keytool -genkey -v -keystore release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias kbwallet`), and fill in the paths/passwords. Without it, `:composeApp:assembleRelease` still builds but produces an unsigned APK.

## Learn more

- [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
