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

### Available models (IPEX-LLM Ollama 0.9.3 compatible)
- `qwen2.5:3b` — fast default for summaries, drafts, Q&A
- `llama3.2:3b` — translations, simple tasks
- `codellama:7b` — code-focused boilerplate and completions
- `mistral:7b` — best general quality, use for important drafts
- `granite3.2:8b` — structured extraction, tool-style output

Note: IPEX-LLM Ollama is v0.9.3. Models requiring newer Ollama (like qwen3-vl) will NOT work.
Always pull models using the IPEX-LLM binary: `C:\Users\rsavu\ipex-llm-ollama\ollama.exe pull <model>`
Keep models under 8B for snappy responses on Intel ARC 16GB.
