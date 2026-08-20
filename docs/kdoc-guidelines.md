# 📝 KDoc Reference Manual & Rules

This document outlines the strict guidelines and best practices for writing public-facing KDoc
strings in our Kotlin library. Adhering to these rules ensures that documentation is clean, readable
for human developers, and highly optimized for AI coding assistants.

---

## 1. Structural Hierarchy Matrix

Every KDoc block must strictly follow this exact vertical ordering with a single blank line
separating each section:

1. **Summary Sentence:** A single, high-level line written in the imperative mood.
2. **Detailed Description (Optional):** Paragraphs detailing thread-safety, architectural workflows,
   or specific behaviors.
3. **Block Tags:** Metadata attributes (`@param`, `@return`, `@throws`) sorted sequentially.

### Template

```kotlin
/**
 * Executes a high-level command sequence.
 *
 * This process is non-blocking and executes entirely on the background I/O dispatcher. 
 * Ensure the client engine is fully initialized prior to invocation.
 *
 * @param command The explicit sequence schema to pass to the engine core.
 * @return A sealed [ExecutionResult] detailing success or failure matrices.
 * @throws IllegalStateException If the core engine instance is not active.
 */
suspend fun execute(command: CommandSchema): ExecutionResult
```

---

## 2. Strict Grounding & Hyperlinking (`[...]`)

Never leave class names, properties, or functions as raw text. Wrap them in square brackets to allow
compilers and document generators (like Dokka) to construct semantic lookup graphs.

* **Local Scopes:** Use `[MyClass]` for classes within the same package boundary.
* **Foreign Scopes:** Use fully-qualified paths `[com.library.network.Client]` for external modules.
* **Functional Hooks:** Reference direct methods or fields using `[Client.connect]` or `[userId]`.

---

## 3. Formatting & Markdown Syntax

KDoc strings naturally support Markdown. Keep formatting simple to maintain raw code scannability:

* **Inline Identifiers:** Wrap variable names, literals, and parameters in backticks: \`null\`,
  \`true\`, \`false\`, or \`timeoutMillis\`.
* **Lists:** Use the asterisks symbol (`*`) for unnumbered lists of preconditions or lifecycle
  stages.
* **Code blocks:** Avoid massive code blocks inside code documentation. Relocate detailed guides to
  the root `README.md` or a dedicated `/samples` folder.

---

## 4. Kotlin Idioms & Features

Document Kotlin features using explicit syntactic patterns tailored for the language structure:

### A. Extension Functions

Always clarify who the explicit receiver is, rather than focusing purely on the output value.

```kotlin
/**
 * Validates whether this [String] instance contains a safely formatted email address syntax.
 */
fun String.isValidEmail(): Boolean
```

### B. Property Accessors (`@property`)

Do not write separate getters and setters. Document constructor fields cleanly at the class-header
level.

```kotlin
/**
 * Configuration payload for the global cache engine.
 *
 * @property maxEntries The absolute limit of reference nodes retained in memory.
 * @property evictOnLowMemory Automatically purge secondary indices if the system reports low JVM memory.
 */
data class CacheConfig(val maxEntries: Int, val evictOnLowMemory: Boolean)
```

### C. Sealed Classes & Enums

Document the top-level parent class context, and provide short, one-liner summaries for every
underlying subclass to describe its specific mutation or state.

---

## 5. Anti-Patterns & Visual Noise Reductions

* **Skip Self-Explanatory Context:** Do not document code whose naming convention makes its purpose
  universally clear. (e.g., Avoid writing `/** Sets the name */ fun setName(name: String)`).
* **No Hyphen Noise:** Avoid using hyphens inside block tags. Use a clean space instead:
    * ❌ `@param id - The identifier.`
    * `@param id The identifier.`
* **Sentence Cases:** Ensure every description field appended to a block tag starts with an
  uppercase letter and concludes with a period.