package com.rsav.githubPublicRepoBrowser.domain.model

data class ProgrammingLanguage(
    val name: String,
    val queryValue: String = name,
)

val PROGRAMMING_LANGUAGES: List<ProgrammingLanguage> = listOf(
    "Assembly", "Astro", "C", "C#", "C++", "Clojure", "CMake", "COBOL",
    "CoffeeScript", "CSS", "Cuda", "D", "Dart", "Dockerfile", "Elixir",
    "Elm", "Emacs Lisp", "Erlang", "F#", "Fortran", "GDScript", "Go",
    "Groovy", "Haskell", "HCL", "HTML", "Java", "JavaScript", "Julia",
    "Jupyter Notebook", "Kotlin", "Lua", "Makefile", "MATLAB", "Nim",
    "Nix", "Objective-C", "OCaml", "Pascal", "Perl", "PHP", "PowerShell",
    "Prolog", "Python", "R", "Racket", "Ruby", "Rust", "Scala", "Shell",
    "Solidity", "SQL", "Svelte", "Swift", "TypeScript", "V", "Vala",
    "Verilog", "VHDL", "Vim Script", "Vue", "Zig",
).map { ProgrammingLanguage(it) }
