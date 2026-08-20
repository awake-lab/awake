# 📦 Master Library Documentation & Visualization Blueprint

This manifest establishes a strict, unified standard for writing documentation, configuring
build-level guardrails, and using interactive notebooks for algorithm visualization in our Kotlin
library.

---

## 🧭 Section 1: The KDoc Style Reference Manual

All public APIs, interfaces, extension properties, and core algorithmic classes must adhere to these
formatting layout protocols.

### 1. The Three-Tier Structural Hierarchy

Every KDoc block must strictly follow this vertical sequence with a single empty line separating
each segment:

1. **Summary Line:** A crisp, single sentence written in the imperative mood. (e.g.,
   `"Executes..."`, `"Computes..."`).
2. **Detailed Paragraph (Optional):** Context detailing lifecycle phases, thread safety, scaling
   constraints, or coroutine context assumptions.
3. **Block Tags:** Standardized tracking attributes (`@param`, `@return`, `@throws`) sorted
   sequentially.

```kotlin
/**
 * Executes a single bubble sort step by swapping adjacent mismatched values.
 *
 * This function handles the in-place state mutation of the target collection.
 * It tracks the iteration count to compute the overall time complexity matrix.
 *
 * @param list The mutable collection currently undergoing evaluation.
 * @param index The active pointer reference position.
 * @throws IndexOutOfBoundsException If the index steps past the boundary threshold.
 */
fun bubbleStep(list: MutableList<Int>, index: Int) {
    if (index >= list.size - 1) throw IndexOutOfBoundsException("Pointer out of bounds")
    if (list[index] > list[index + 1]) {
        val temp = list[index]
        list[index] = list[index + 1]
        list[index + 1] = temp
    }
}
```

### 2. Strict Type Anchoring (`[...]`)

Never type class names, variables, or functional endpoints as unlinked text strings. Wrap them in
bracket indicators so compiler engines can generate hyperlinked context trees:

* **Local Scope Types:** Use `[MyClass]` inside the same package boundary.
* **Foreign Scope Types:** Use fully-qualified paths like `[com.library.network.Client]`.
* **Functional Hooks:** Reference direct methods or fields using `[Client.connect]` or `[index]`.

### 3. Visual Noise Reductions & Anti-Patterns

* **Skip Self-Explanatory Context:** Avoid writing redundant code comments whose naming architecture
  makes its objective completely explicit (e.g., do not write
  `/** Sets the name */ fun setName(name: String)`).
* **No Hyphen Interferences:** Do not include structural hyphens inside block tags. Separate using
  single blank spaces:
    * ❌ `@param id - The identifier.`
    * `@param id The identifier.`
* **Proper Sentence Casing:** Every text block appended to a tag descriptor must begin with an
  uppercase character and terminate with a period.

---

## 🛠️ Section 2: Dokka Gradle Configuration (Convention Plugin Format)

To prevent code drift and enforce these rules on our compilation pipelines, we use the Dokka Gradle
Plugin (v2). This setup must be defined in your build-logic convention plugin file using
`configure<DokkaExtension>` to safely bypass missing type accessors.

```kotlin
import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("org.jetbrains.dokka")
}

configure<DokkaExtension> {
    // 1. Configure global generation endpoints
    dokkaPublications.html {
        outputDirectory.set(layout.buildDirectory.dir("docs/api-reference"))
    }

    // 2. Map properties across all active source sets
    dokkaSourceSets.configureEach {
        moduleName.set("Awake Core Engine Library")
        includes.from(project.files("README.md"))

        // --- COMPILER VALIDATION GUARDRAILS ---
        reportUndocumented.set(true) // Breaks compilation if a public method lacks KDocs
        failOnWarning.set(true)      // Upgrades documentation warnings to hard failures

        // Filter documented code scopes
        documentedVisibilities.set(
            setOf(
                org.jetbrains.dokka.DokkaConfiguration.Visibility.PUBLIC,
                org.jetbrains.dokka.DokkaConfiguration.Visibility.PROTECTED
            )
        )

        skipDeprecated.set(true) // Keeps reference pages clean of legacy features
    }
}
```

### 🚀 Running the Dokka v2 Tasks

Because Dokka v2 leverages an updated task naming configuration, use the exact generation endpoint
below:

```bash
./gradlew dokkaGeneratePublicationHtml
```

*Note: Your compiled interactive HTML reference pages will output cleanly
into `/build/docs/api-reference/index.html`.*

---

## 📊 Section 3: Kotlin Notebook & Kandy Usage Guide

Instead of dumping internal library code, notebooks must act as **Interactive Tutorials**. They
should guide developers through importing your library, configuring a client, and visualizing output
metrics using Kandy.

### 📓 Notebook Cell Structuring Plan

#### Cell 1 (Markdown: Initializing the Library Dependency)

```markdown
### 🚀 Step 1: Add the Library and Visualization Tools

To begin using our library in your workspace, load our library alongside the Kandy plotting
framework into your notebook instance.
```

#### Cell 2 (Executable Code: Repository and Dependency Setup)

```kotlin
// Load visualization and dataframe plugins into the notebook
%use kandy
%use dataframe

// Add your Maven coordinate to import the library
        @file:Repository("https://github.com")
        @file:DependsOn("com.yourdomain:awake-core:1.0.0")

        import com . yourdomain . awake . AwakeClient
        import com . yourdomain . awake . models . MatrixConfig
```

#### Cell 3 (Markdown: Basic Client Configuration)

```markdown
### ⚙️ Step 2: Configure and Initialize the Client

Create a basic runtime client instance. This object orchestrates your algorithm queries and captures
performance profiles during execution loops.
```

#### Cell 4 (Executable Code: Client Setup)

```kotlin
// Initialize your library client with custom parameters
val client = AwakeClient.configure {
    timeoutMillis = 5000L
    enableMetrics = true
}

println("Client status: \${client.checkHealth()}")
```

#### Cell 5 (Markdown: Executing and Visualizing the Output Data)

```markdown
### 📊 Step 3: Run the Algorithm and Visualize the Result Curve

Call your primary entry-point function. We will then pass the library's return data straight into
Kandy to plot a performance timeline.
```

#### Cell 6 (Executable Code: Running the Function and Plotting with Kandy)

```kotlin
// 1. Execute your core algorithm logic using the public API
val executionResults = client.runAnalysisDataset(size = 50)

// 2. Map the library properties directly into a Kandy chart canvas
plot {
    line {
        x(executionResults.datasetSizes) { name = "Input Elements (N)" }
        y(executionResults.latenciesMs) { name = "Latency Time (ms)" }
        color = Color.BLUE
    }
    layout.title = "Library Benchmark Matrix: Algorithm Scaling Curve"
}
```

---

### 💡 Why this Ecosystem Strategy Works

* **Pre-Computed Renderings on GitHub:** When you save your Kotlin Notebook with executed cells,
  GitHub reads the `.ipynb` file cleanly, showing humans and AI agents your structural rules
  alongside full-color, rich graph outputs right in their browser.
* **Universal Context for Coding Models:** Combined with an `llms.txt` file at your repository root,
  AI code tools (like Cursor or Copilot) will look at your strict KDoc rules and live notebook
  examples to generate perfect, un-hallucinated code blocks for your users.
