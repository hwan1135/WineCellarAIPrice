# WineCellarAIPrice
BLUF: Below is a GitHub-ready repository bundle you can paste into a local repo and push. It includes a recommended file architecture, README.md, .gitignore, and the complete starter source for the current app scope with AI price estimation scaffolding. It is structured for maintainability, but I am not claiming it has been built or tested in this environment.

Repository architecture
WineCellarAIPrice/
├─ .gitignore
├─ README.md
├─ settings.gradle
├─ build.gradle
├─ gradle.properties
└─ app/
   ├─ build.gradle
   └─ src/
      └─ main/
         ├─ AndroidManifest.xml
         ├─ java/
         │  └─ com/
         │     └─ example/
         │        └─ winecellar/
         │           ├─ MainActivity.kt
         │           ├─ WineCellarApp.kt
         │           ├─ ai/
         │           │  ├─ AiPriceModels.kt
         │           │  ├─ AiPricePromptBuilder.kt
         │           │  ├─ AiPriceService.kt
         │           │  └─ MockAiPriceService.kt
         │           ├─ data/
         │           │  └─ InMemoryRepository.kt
         │           ├─ model/
         │           │  └─ Models.kt
         │           ├─ repository/
         │           │  └─ AiPriceRepository.kt
         │           ├─ ui/
         │           │  ├─ screens/
         │           │  │  ├─ AddBottleScreen.kt
         │           │  │  ├─ AddCellarScreen.kt
         │           │  │  ├─ BottleDetailScreen.kt
         │           │  │  ├─ CellarDetailScreen.kt
         │           │  │  ├─ CellarListScreen.kt
         │           │  │  └─ RecommendationsScreen.kt
         │           │  └─ theme/
         │           │     ├─ Theme.kt
         │           │     └─ Type.kt
         │           └─ viewmodel/
         │              └─ WineCellarViewModel.kt
         └─ res/
            └─ values/
               ├─ strings.xml
               └─ themes.xml


# WineCellarAIPrice

Android wine cellar management app prototype built with Kotlin and Jetpack Compose.

## Current Features

- Multi-cellar support
- Shelf-based visual cellar layout
- Manual bottle entry
- Bottle move workflow between shelf slots
- Drink window and peak estimation
- "Drink first" recommendation view
- AI price estimation scaffolding
- Single-bottle AI price refresh
- Cellar-wide AI price refresh
- GitHub-ready project structure

## Current Limitations

This repository currently uses:
- in-memory storage only
- mock AI price estimation service
- no real network API integration
- no Room persistence yet
- no camera/photo recognition yet
- no drag-and-drop gesture yet
- no real-time verified pricing feed

## AI Price Estimation Design

The app sends structured bottle metadata to an AI-price service interface and expects structured JSON-like results:
- estimated price
- low/high range
- confidence
- explanation

In this repo, the service is mocked through `MockAiPriceService`.

## Setup

1. Clone or create this repository locally
2. Open in Android Studio
3. Sync Gradle
4. Run on emulator or Android device

## Minimum Environment

- Android Studio Hedgehog or newer recommended
- Kotlin 1.9.24
- Compile SDK 34
- Min SDK 26

## How to Push to GitHub

```bash
git init
git add .
git commit -m "Initial commit - WineCellarAIPrice"
git branch -M main
git remote add origin <your-github-repo-url>
git push -u origin main