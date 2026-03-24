# GitHub Public Repo Browser — Improvement Suggestions
**Date: 2026-03-24**

## Already Implemented
- Saved Searches (DataStore Preferences)
- Repository Activity Sparkline (detail page, trimmed to active weeks)
- Topic/Tag-Based Browsing (tappable topics on cards + detail, topic search filter)
- User Profile Page (owner click → all repos)
- Heuristic Badges (popularity, activity, staleness, license, fork ratio)
- Ask AI (multi-provider: Claude, ChatGPT, Gemini, Copilot, Perplexity — app or web fallback)
- License Badge (SPDX ID on cards + detail)
- README parsing (6 filename variants)
- StrictMode integration (network + custom slow call detection)

## Suggested Next Features

### 1. Health Score (High Impact)
A single 0-100 composite score per repo, displayed as a colored ring or badge on list cards.

**Inputs:**
- Days since last commit (recency)
- Stars-per-year ratio (growth velocity)
- Fork-to-star ratio (community engagement)
- Has README (documentation)
- Has license (legal clarity)
- Has topics (discoverability)
- Open issues trend (maintenance responsiveness)

**Why:** Instant "should I even look at this?" signal. One number replaces mentally evaluating 5+ metrics.

### 2. Similar Repos (Detail Page)
When viewing a repo, show 3-4 related repos based on shared topics.

**Implementation:** Run a `topic:X` search behind the scenes using the first topic of the current repo. Filter out the current repo from results.

**Why:** Keeps users discovering without leaving the app. Natural "what else is out there?" flow.

### 3. Contributor Count + Bus Factor Warning
Fetch `/repos/{owner}/{repo}/contributors` from REST API (paginated, just need count).

**Badges:**
- "👥 200+ contributors" — healthy community
- "⚠️ Single maintainer" — if 10k+ stars but ≤2 contributors

**Why:** Critical for evaluating library risk. A project with one maintainer can disappear overnight.

### 4. Issue/PR Pulse
Fetch open/closed issues count via GraphQL:
```graphql
issues(states: OPEN) { totalCount }
issues(states: CLOSED, filterBy: { since: "2026-02-24" }) { totalCount }
```

**Display:** "Issues: 45 open / 12 closed this month"
**Badge:** If open issues growing with no closes → "⚠️ Unresponsive maintenance"

**Why:** Stars and forks show popularity, but issue response shows maintainer commitment.

### 5. Search History with Frequency
Track what the user searches in DataStore. Show "Recent" and "Frequent" sections above the saved searches.

**Implementation:** Store `Map<String, SearchHistoryEntry>` with query string, timestamp, and count. Show top 5 frequent + last 5 recent.

**Why:** Zero API cost, pure local intelligence. Users often repeat searches.

### 6. Offline Favorites
Let users star repos locally (separate from GitHub stars, no auth needed).

**Implementation:**
- Room database for full repo data + README markdown
- "Save offline" button on detail page
- Separate "Favorites" tab/section on main screen
- Sync indicator showing when data was last cached

**Why:** Useful on planes, commutes, or when evaluating repos without internet.

### 7. Release Tracker
Fetch latest release via GraphQL:
```graphql
releases(first: 1, orderBy: { field: CREATED_AT, direction: DESC }) {
    nodes { tagName, publishedAt, name }
}
```

**Display on detail page:** "v2.3.1 — 3 days ago" vs "v1.0.0 — 2 years ago"
**Badge on card:** "📦 v2.3.1" for repos with recent releases

**Why:** Release recency tells the maintenance story at a glance. A repo with commits but no releases may be unstable.

### 8. Dependency Insights (Advanced)
For repos with `package.json`, `build.gradle.kts`, `Cargo.toml`, or `requirements.txt`:
- Fetch the file via GraphQL `object(expression: "HEAD:package.json")`
- Parse dependencies
- Flag known outdated or deprecated packages

**Why:** Deep contextual intelligence. Helps evaluate code quality without reading source.

## Priority Recommendation
1. **Health Score** — highest bang for buck, uses data already fetched
2. **Release Tracker** — one extra GraphQL field, high signal
3. **Search History** — zero API cost, improves daily UX
4. **Similar Repos** — reuses existing search infrastructure
5. **Contributor Count** — one REST call, high value signal
6. **Issue Pulse** — two extra GraphQL fields
7. **Offline Favorites** — requires Room setup, but valuable
8. **Dependency Insights** — most complex, save for later
