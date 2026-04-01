# Project: GitHub Public Repo Browser

Android app built with Kotlin, Jetpack Compose, MVVM, Hilt, Room.

## Local LLM Delegation (Ollama via MCP)

A local Ollama instance runs on Intel ARC A770 16GB (IPEX-LLM). Claude MUST delegate mechanical tasks to it automatically — do NOT wait for the user to ask. This is the default behavior.

### When to use local LLM (via mcp__local-llm__ or mcp__ollama__ tools)
RULE: Only delegate when input is ALREADY in context. NEVER read files just to relay them.
- `local_summarize` — condense text/diffs already in context
- `local_draft` — generate boilerplate, docs, or PR descriptions from short specs
- `local_classify` — categorize items from lists in context
- `local_extract` — pull structured data from unstructured text in context
- `local_transform` — reformat or translate text in context
- `local_complete` — raw completion for any mechanical task
- `chat_completion` — direct Ollama chat for simple Q&A

### When NOT to use local LLM (keep on Claude)
- Anything requiring reading files first (code review, refactoring, debugging)
- Complex reasoning or architectural decisions
- Multi-step tool orchestration
- Large or nuanced code generation
- Anything where relaying content costs MORE tokens than doing it directly

### Signal rule
Every time you use the local Ollama LLM, you MUST prefix the relevant output with:

**[LOCAL-LLM]** (model: <model_name>)

This tells the user that the response came from the local model, not Claude.

### Model selection (IPEX-LLM Ollama 0.9.3 compatible)

This is a coding project. **Default model is `codellama:7b`** for all code tasks. Use `mcp__ollama__chat_completion` with explicit model param for code — the `mcp__local-llm__*` tools default to `qwen2.5:3b` which is weaker at code.

| Model | When to use |
|---|---|
| **`codellama:7b`** | **DEFAULT** — previews, boilerplate, data classes, tests, XML, any code |
| `mistral:7b` | Important prose — PR descriptions, README sections, architecture docs |
| `qwen2.5:3b` | Quick throwaway — commit messages, one-line summaries |
| `granite3.2:8b` | Structured extraction — parsing logs, structured output |
| `llama3.2:3b` | Translations, string resources for i18n |

### 10-line rule
Before writing ≥10 lines of repetitive/templated code or text, STOP and delegate to local LLM. No exceptions. Claude reviews and fixes the output (expect ~30% rework), but that's still cheaper than generating 100% on Opus.

Note: IPEX-LLM Ollama is v0.9.3. Models requiring newer Ollama (like qwen3-vl) will NOT work.
Always pull models using the IPEX-LLM binary: `C:\Users\rsavu\ipex-llm-ollama\ollama.exe pull <model>`
Keep models under 8B for snappy responses on Intel ARC 16GB.
