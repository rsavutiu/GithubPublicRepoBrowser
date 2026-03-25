# GitHub Public Repo Browser — Improvement Suggestions
**Date: 2026-03-25**

## Already Implemented
- Saved Searches (DataStore Preferences)
- Repository Activity Sparkline (detail page, trimmed to active weeks)
- Topic/Tag-Based Browsing (tappable topics on cards + detail, topic search filter)
- User Profile Page (owner click → all repos)
- Heuristic Badges (popularity, activity, staleness, license, fork ratio, issue pulse)
- Ask AI (multi-provider: Claude, ChatGPT, Gemini, Copilot, Perplexity — app or web fallback)
- License Badge (SPDX ID on cards + detail)
- README parsing (6 filename variants: README.md, readme.md, README.MD, Readme.md, README, README.rst)
- StrictMode integration (network + custom slow call detection)
- Health Score (0-100 composite ring on each card, 7 weighted signals)
- Issue/PR Pulse (open/closed issue counts via GraphQL, factored into health score and badges)
- Dependency Insights (12 ecosystems: npm, Gradle, pip, Cargo, Go, RubyGems, pub, Maven, CocoaPods, Composer, Swift PM, Podfile)
- GitHub-inspired theme (custom Material 3 color scheme, light + dark, no dynamic color)
- REST API alternative (repository pattern encapsulating GraphQL/REST swap)
- Per-app language support (106 languages, locale_config.xml)
- Last update time (updatedAt already fetched and displayed on cards)
- "This Year" trending period option (default: This Week)

## Suggested Next Features

### 1. Similar Repos (Detail Page)
When viewing a repo, show 3-4 related repos based on shared topics.

**Implementation:** Run a `topic:X` search behind the scenes using the first topic of the current repo. Filter out the current repo from results.

**Why:** Keeps users discovering without leaving the app. Natural "what else is out there?" flow.

### 2. Contributor Count + Bus Factor Warning
Fetch `/repos/{owner}/{repo}/contributors` from REST API (paginated, just need count).

**Badges:**
- "200+ contributors" — healthy community
- "Single maintainer" — if 10k+ stars but ≤2 contributors

**Why:** Critical for evaluating library risk. A project with one maintainer can disappear overnight.

### 3. Search History with Frequency
Track what the user searches in DataStore. Show "Recent" and "Frequent" sections above the saved searches.

**Implementation:** Store `Map<String, SearchHistoryEntry>` with query string, timestamp, and count. Show top 5 frequent + last 5 recent.

**Why:** Zero API cost, pure local intelligence. Users often repeat searches.

### 4. Offline Favorites
Let users star repos locally (separate from GitHub stars, no auth needed).

**Implementation:**
- Room database for full repo data + README markdown
- "Save offline" button on detail page
- Separate "Favorites" tab/section on main screen
- Sync indicator showing when data was last cached

**Why:** Useful on planes, commutes, or when evaluating repos without internet.

### 5. Release Tracker
Fetch latest release via GraphQL:
```graphql
releases(first: 1, orderBy: { field: CREATED_AT, direction: DESC }) {
    nodes { tagName, publishedAt, name }
}
```

**Display on detail page:** "v2.3.1 — 3 days ago" vs "v1.0.0 — 2 years ago"
**Badge on card:** "v2.3.1" for repos with recent releases

**Why:** Release recency tells the maintenance story at a glance. A repo with commits but no releases may be unstable.

### 6. Fork Network Visualization
Show a visual tree of the most popular forks for a repo.

**Implementation:** Fetch `/repos/{owner}/{repo}/forks?sort=stargazers` and display top 5 forks with their star counts.

**Why:** Helps discover actively maintained forks of abandoned projects.

### 7. Commit Frequency Heatmap
Replace or augment the sparkline with a GitHub-style contribution calendar grid.

**Implementation:** Use the same `/stats/participation` data but render as a 52×7 grid with color intensity.

**Why:** More intuitive than a line chart for spotting patterns (weekday vs weekend, seasonal activity).

## Priority Recommendation
1. **Release Tracker** — one extra GraphQL field, high signal
2. **Search History** — zero API cost, improves daily UX
3. **Similar Repos** — reuses existing search infrastructure
4. **Contributor Count** — one REST call, high value signal
5. **Offline Favorites** — requires Room setup, but valuable
6. **Fork Network** — one REST call, niche but unique
7. **Commit Heatmap** — visual polish, reuses existing data
