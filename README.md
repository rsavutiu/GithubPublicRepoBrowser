# GitHub Public Repository Browser

## Architecture

Clean Architecture with three layers:

```
domain/          Pure Kotlin — models, repository interfaces, use cases
data/            Apollo GraphQL client, mappers, paging source, repository implementations
ui/              Jetpack Compose screens, ViewModels, navigation, components
di/              Hilt modules (NetworkModule, RepositoryModule)
```

**Patterns used:**
- **MVI** (Model-View-Intent) on both Search and Detail screens — intents, UI state, side effects via Channel
- **Atomic Design** for UI components — atoms (SearchTextField, GithubAvatar, LanguageBadge), molecules (SearchBar, RepoStats), organisms (RepoCard, RepoList). I also tried making previews and keeping functions small and stateless as much as possible.
- **Repository pattern** with domain interfaces and data implementations

I normally use REST APIs, Koin and MVVM, but decided to use the GraphQL Github API since it was available, as well as Hilt since it's a pure Android exercise, not KMP/CMP.

## Tech Stack

| Library | Version | Purpose |
|---|---|---|
| Jetpack Compose + Material 3 | BOM 2026.03.00 | UI framework |
| Apollo GraphQL | 4.4.1 | GitHub API client |
| Hilt | 2.58 | Dependency injection |
| Paging 3 | 3.3.6 | Infinite scroll pagination |
| Navigation Compose | 2.9.0 | Type-safe navigation with shared element transitions |
| Coil 3 | 3.4.0 | Image loading |
| CommonMark | 0.24.0 | Markdown to HTML rendering |
| MockK | 1.14.9 | Unit testing |
| Turbine | 1.2.1 | Flow testing |

## Setup

1. Generate a [GitHub Personal Access Token](https://github.com/settings/tokens) with `public_repo` scope
2. Add it to `local.properties` in the project root:
   ```properties
   github.token=ghp_your_token_here
   ```
3. Build and run from Android Studio
The token is read at build time via `BuildConfig.GITHUB_TOKEN` and sent as a Bearer token in the Authorization header.
This is mandatory for GraphQL GitHub API

## Testing
Unit tests are incomplete and mostly AI-generated.


## AI Use

Assistance from Claude (Anthropic) for code review, architecture guidance, and implementation of shared element transitions, MVI pattern, Markdown rendering, and GraphQL scalar mapping.
Also used it to set up the project initially and connect the 3rd party dependencies.
