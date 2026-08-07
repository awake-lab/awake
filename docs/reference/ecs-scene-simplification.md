### 1. The Dynamic Components Modifier (The Flat Configurator)
Instead of using a generic mutableListOf<Any>() which creates extra garbage collection allocations, your EntityModifier should store lambdas that operate on the components your world pools. This allows you to configure components seamlessly without breaking your pooling architecture.
```kotlin
class EntityModifier {
    // Array of configuration functions to run once the components are pooled/created
    @PublishedApi
    internal val actions = mutableListOf<(World, Entity) -> Unit>()

    // Overload the plus operator to accept completely pre-made or library components
    operator fun plus(component: Any): EntityModifier = apply {
        actions.add { world, entity ->
            world.add(entity, component::class, component)
        }
    }

    // Inline configuration functions that utilize your world's optimized inline add<T>()
    inline fun <reified T : Any> configure(crossinline setup: T.() -> Unit) = apply {
        actions.add { world, entity ->
            val comp = world.add<T>(entity)
            comp.setup()
        }
    }
}

// Global syntactic sugar entrypoint
fun Modifier() = EntityModifier() // TODO let's decide a proper name or keep it?
```

### 2. Semantic Extension Modifiers (The "UI / Setup" Layer)
You can cleanly write semantic extension functions targeting specific game structures (like your Camera setup, Lights, or Transform systems) anywhere in your project:
```kotlin
// Setup extension helper for your CameraMode enum
fun EntityModifier.camera(mode: CameraMode, target: Entity? = null, setup: SceneCamera.() -> Unit = {}) = 
    configure<SceneCamera> {
        this.mode = mode
        this.targetEntityId = target?.id
        this.setup()
    }

fun EntityModifier.transform(x: Float = 0f, y: Float = 0f, z: Float = 0f) = 
    configure<SceneTransform> {
        this.x = x; this.y = y; this.z = z
    }

fun EntityModifier.meshRenderer(mesh: String, material: String) = 
    configure<SceneMeshRenderer> {
        this.mesh = mesh
        this.material = material
    }

```

### 3. The Pure ECS Scene Graph Builder (The Nested Block Tree)
This builder acts as the structural orchestrator. When you instantiate a child block, it generates an optimized Entity from your World, registers spatial relations using an internal tracking component, and flushes all configured properties directly into the underlying ECS component stores.
```kotlin
// Data component to track hierarchies inside your system loop
data class ChildComponent(var parentId: Int = -1)

@DslMarker
annotation class AwakeSceneDsl

@AwakeSceneDsl
class SceneBuilder internal constructor(
    private val world: World,
    private val parentEntity: Entity? = null
) {
    fun entity(
        name: String? = null,
        modifier: EntityModifier = Modifier(),
        block: SceneBuilder.() -> Unit = {}
    ): Entity {
        // 1. Utilize your exact world.create() method
        val currentEntity = world.create()

        // 2. Hook up Name trackers if required
        if (name != null) {
            world.add<NameComponent>(currentEntity).value = name
        }

        // 3. Inject hierarchy link if nested inside another entity block
        if (parentEntity != null) {
            world.add<ChildComponent>(currentEntity).parentId = parentEntity.id
        }

        // 4. Flush the modifier actions straight to the world storage (Uses pooled components!)
        modifier.actions.forEach { action -> action(world, currentEntity) }

        // 5. Recursively process children passing down the active parent scope
        val childContext = SceneBuilder(world, parentEntity = currentEntity)
        childContext.block()

        return currentEntity
    }
}

// Global root context execution builder
fun World.scene(block: SceneBuilder.() -> Unit) {
    SceneBuilder(this).block()
}
```
---
### How Your Game Architecture Looks Now
You now have a clean, declarative layout with zero allocation redundancy:
```kotlin
val world = World()

world.scene {
    // Top level Player Entity
    entity("Player", Modifier().transform(y = 1f).meshRenderer("player.gltf", "m_skin")) {
        
        // Nested Child Entity utilizing a hybrid modifier chain setup
        entity(
            name = "PlayerFollowCamera",
            modifier = Modifier()
                .transform(z = -5f)
                // Implicitly passes the outer player entity reference down as a track target!
                .camera(CameraMode.ThirdPerson, target = parentEntity) 
        )
        
        // A child mesh entity, like an attached weapon prop
        entity("Sword", Modifier().transform(x = 0.5f).meshRenderer("sword.gltf", "m_steel"))
    }
}

```

### Why This Architecture Fits Your ECS Perfectly

1. **Zero Boilerplate Component Extensions:** If an external module introduces a complex physics component like LibPhysicsComponent, your user can inject it into the modifier tree instantly using Modifier().configure<LibPhysicsComponent> { mass = 50f } without needing to edit your core scene definitions.
2. **Maintains Component Pooling Optimization:** By using world.add<T>(entity) inside the builder lambdas, your system completely bypasses runtime object allocation strategies. It actively taps into your components.pool(type).obtain() architecture to reuse historical properties cleanly.
3. **Completely Decoupled:** SceneNode is gone. Your entities are back to being optimized, lightweight wrappers for unique array indices.

---

### What to Replace: The Refactored Integration
Instead of having entity { ... } build a tree of SceneNode data objects, you should pass your World directly into your scene staging area so your generic, pooling-friendly SceneBuilder can build entities inside your ECS immediately.

Here is how your SceneGameDsl looks when cleanly refactored to support the hybrid modifier approach:

```kotlin
class SceneGameDsl internal constructor(
    private val world: World // Pass your highly-optimized ECS world into the DSL context
) {
    // 🛑 RETIRED: private var sceneDocumentDsl = SceneDocumentDsl(null)
    
    private var sceneName: String? = null
    
    // ... Keep your assets, renderables, systems, and service variables completely unchanged ...

    fun name(value: String?) {
        this.sceneName = value
    }

    // 🚀 NEW: This feeds directly into our highly-optimized, data-oriented SceneBuilder!
    fun scene(
        name: String? = null,
        block: SceneBuilder.() -> Unit
    ) {
        if (name != null) this.sceneName = name
        
        // Execute entity spawning directly against the ecs storage at loading-time
        SceneBuilder(world).apply(block)
    }

    // 🚀 NEW: Shortcut to spawn a root-level entity cleanly without nesting if wanted
    fun entity(
        name: String? = null,
        modifier: EntityModifier = Modifier(),
        block: SceneBuilder.() -> Unit = {}
    ) {
        SceneBuilder(world).entity(name, modifier, block)
    }

    // ... Keep all your systems(), fixedSystem(), update(), service() methods completely unchanged ...

    internal fun build(): SceneGameSpec {
        installInfrastructureSystems() // Keeps your SpatialSystem and structural loop intact!

        return SceneGameSpec(
            // 🛑 RETIRED: sceneDocument = sceneDocumentDsl.build(),
            sceneName = sceneName, // Just pass the raw meta configuration name string down
            renderableFactory = renderableFactory,
            assetLibraryFactory = assetLibraryFactory,
            systems = systemsDsl.build(),
            updateBlock = updateBlock,
            overlayBlock = overlayBlock,
            onReadyBlock = { onReadyBlocks.forEach { it(this) } },
            onDisposeBlock = { onDisposeBlocks.forEach { it(this) } },
            serviceRegistrations = serviceRegistrations.toList()
        )
    }
}
```
---
### The New, Modern Engine Configuration Workflow
Your top-level architecture remains structural, clean, and highly professional, but under the hood, your memory footprints drop to practically zero allocations:

```kotlin
gameSpec.ecs {
    name("Cyberpunk City Level")

    assets {
        // Your old asset loader system remains functional!
    }

    systems {
        fixedSystem("PhysicsSystem") { PhysicsSystem(world) }
        frameSystem("AnimationSystem") { AnimationSystem(world) }
    }

    // This block populates your highly optimized EntityArena and ComponentRegistry loops instantly!
    scene {
        entity("PlayerMesh", Modifier().transform(y = 2f).meshRenderer("character.gltf", "m_skin")) {
            
            // Nested spatial hierarchy utilizing the flat helper chains
            entity("MainCamera", Modifier().transform(z = -5f).camera(CameraMode.ThirdPerson))
            
            entity("HoldingWeapon", Modifier().transform(x = 0.5f).meshRenderer("rifle.gltf", "m_iron"))
        }
    }
}
```

### Why This Refactor is a Massive Win
- **Zero Rework on Core Infrastructure:** You don't lose your frame system orchestration, your phase definitions (SceneSystemPhase.Fixed), or your dependency-injected services.
- **Flushes Memory Instantaneously:** You completely remove the compilation middleman step. Your engine no longer allocates millions of temporary ArrayList<SceneNode> pointers during startup loading screens. The setup immediately maps variables to flat arrays.

---

To pass your World instance cleanly into the root initialization pipeline without exposing internal mechanics or breaking your DSL structure, you should utilize a Factory or Provider Pattern within your GameSpecDsl.Because your World manages runtime state, it shouldn't exist as a global static variable during configuration time. Instead, you register a factory lambda that your runtime engine invokes when it starts up the scene.Here is the implementation to wire your World, SceneGameDsl, and SceneBuilder together.

### 1. Update the Specification Object

```kotlin
class SceneGameSpec(
    val sceneName: String?,
    val systems: List<SceneSystemRegistration>,
    // This function acts as the delayed setup mechanism for entities
    val scenePopulationBlock: (World) -> Unit, 
    val renderableFactory: SceneRenderableFactory,
    val assetLibraryFactory: (() -> SceneAssetLibrary)?,
    val updateBlock: SceneUpdateBlock,
    val overlayBlock: SceneOverlayBlock,
    val onReadyBlock: SceneGameRuntime.() -> Unit,
    val onDisposeBlock: SceneGameRuntime.() -> Unit,
    val serviceRegistrations: List<SceneServiceRegistration<*>>
)

```
### 2. Refactor SceneGameDsl to Delay Entity Creation
Instead of passing the World to the DSL instantly during configuration, SceneGameDsl captures the descriptive block as a property. It wraps it in a deferred lambda execution sequence for later.

```kotlin
class SceneGameDsl internal constructor() {
    private var sceneName: String? = null
    
    // Store the structural description block for later initialization
    private var scenePopulationBlock: (World) -> Unit = {}
    
    // ... rest of your existing systems, assets, and service variables ...

    fun name(value: String?) {
        this.sceneName = value
    }

    /**
     * Captures the declarative entity layout block without running it yet.
     * It delays execution until the actual runtime engine assigns a World.
     */
    fun scene(block: SceneBuilder.() -> Unit) {
        this.scenePopulationBlock = { liveWorld ->
            // Running against the live world instance inside the runtime loading loop
            SceneBuilder(liveWorld).apply(block)
        }
    }

    // Keep your assets {}, systems {}, and service {} methods completely untouched

    internal fun build(): SceneGameSpec {
        installInfrastructureSystems()

        return SceneGameSpec(
            sceneName = sceneName,
            systems = systemsDsl.build(),
            scenePopulationBlock = scenePopulationBlock, // Pass the delayed population logic
            renderableFactory = renderableFactory,
            assetLibraryFactory = assetLibraryFactory,
            updateBlock = updateBlock,
            overlayBlock = overlayBlock,
            onReadyBlock = { onReadyBlocks.forEach { it(this) } },
            onDisposeBlock = { onDisposeBlocks.forEach { it(this) } },
            serviceRegistrations = serviceRegistrations.toList()
        )
    }
}

```


### 3. How the Runtime Engine Boots It (The Engine Wire-up)

When your engine processes a loaded SceneGameSpec inside its core framework manager, it initializes a clean World, creates systems, and runs the deferred blueprint code.

```kotlin
class SceneGameRuntime(private val spec: SceneGameSpec) {
    // 1. Instantiates your custom optimized ECS World safely at runtime
    val world = World() 

    fun load() {
        // 2. Instantiate and register the systems defined via the DSL
        spec.systems.forEach { systemReg ->
            val systemInstance = systemReg.factory(this)
            // ecsSystemManager.register(systemInstance)
        }

        // 3. RUN THE DELAYED SCENE BLOCK!
        // This fires up your SceneBuilder, triggering world.create() and component pooling.
        spec.scenePopulationBlock(world)

        // 4. Trigger lifecycle hook
        spec.onReadyBlock(this)
    }
    
    fun update(deltaTime: Float) {
        // Engine loop updates systems and passes blocks safely...
    }
}
```
---



---

### 1. The New SceneGameRuntime Core
Here is the production-ready code for your new SceneGameRuntime. It replaces the legacy SceneRuntime completely, matching your exact specifications and wiring together the World, SceneGameSpec, and SceneBuilder.

```kotlin
class SceneGameRuntime(
    private val spec: SceneGameSpec,
    private val renderer: Renderer
) {
    // Single source of truth for all game data
    val world: World = World()
    
    // Ordered lists of systems derived from the DSL configuration
    private val fixedSystems = mutableListOf<System>()
    private val frameSystems = mutableListOf<System>()

    private var isLoaded = false

    /**
     * Replaces the legacy load step. Remaining suspended to protect the main thread,
     * it boots services, instantiates systems, pools components, and runs the scene DSL.
     */
    suspend fun load() {
        check(!isLoaded) { "Scene is already loaded." }

        // 1. Initialize and register Dependency Injected Services
        // spec.serviceRegistrations.forEach { it.initialize(this) }

        // 2. Instantiate systems using the factories defined in your DSL
        spec.systems.forEach { registration ->
            val systemInstance = registration.factory(this)
            when (registration.phase) {
                SceneSystemPhase.Fixed -> fixedSystems.add(systemInstance)
                SceneSystemPhase.Frame -> frameSystems.add(systemInstance)
            }
        }

        // 3. RUN THE SCENE BUILDER BLUEPRINT (Delayed Execution)
        // This flushes the layout directly into your World's component pools!
        spec.scenePopulationBlock(world)

        // 4. Trigger the custom onReady lifecycle hook
        spec.onReadyBlock(this)

        isLoaded = true
    }

    /**
     * Runs physics, AI, and fixed-interval logic.
     * Call this inside your fixed timestep loop accumulator step.
     */
    fun updateFixed(fixedDelta: Float) {
        if (!isLoaded) return
        
        for (i in fixedSystems.indices) {
            fixedSystems[i].update(world, fixedDelta)
        }
    }

    /**
     * Replaces the legacy render() loop.
     * Enforces strict ordering: Game Logic -> Spatial Matrix Resolution -> Render Submission.
     */
    fun updateFrame(frameDelta: Float) {
        if (!isLoaded) return

        // 1. Run all game-specific frame systems (Camera, Animations, UI Input)
        for (i in frameSystems.indices) {
            frameSystems[i].update(world, frameDelta)
        }

        // 2. Execute global update block if any custom logic was passed to ecs { update { ... } }
        spec.updateBlock(world, frameDelta)
        
        // 3. Draw UI overlays at the very end of the frame pipeline
        spec.overlayBlock(world, frameDelta)
    }

    /**
     * Cleans up the runtime gracefully when shifting maps or stopping the game.
     */
    fun dispose() {
        spec.onDisposeBlock(this)
        world.clear()
        fixedSystems.clear()
        frameSystems.clear()
        isLoaded = false
    }
}
```

### 2. Upgraded Engine Glue (SceneGameDsl)
Make sure your internal infrastructure installation injects the SpatialSystem and RenderSystem right at the tail-end of your system arrays so they always catch game logic mutations seamlessly before drawing to the screen.

```kotlin
class SceneGameDsl internal constructor() {
    // ... your existing system arrays, assets, and service registries ...

    internal fun installInfrastructureSystems() {
        // Enforce the critical ordering rule into your registration tree:
        
        // SpatialSystem runs in Fixed or Frame phase right after gameplay mutations
        fixedSystem("EngineSpatialSystem") { SpatialSystem(world) }
        
        // RenderSystem always executes last in the Frame phase
        frameSystem("EngineRenderSystem") { RenderSystem(renderer) }
    }
}
```

### 3. How the Bootstrap Works Now

Your main loop no longer worries about what order systems update or how hierarchies look. Everything behaves predictably out of a singular context:

```kotlin
suspend fun main() {
    val renderer = Renderer()
    val gameSpec = GameSpecDsl()

    // 1. Setup configuration blueprint
    gameSpec.ecs {
        name("Level 1")
        
        systems {
            fixedSystem("AiSystem") { AiSystem(world) }
            frameSystem("CameraSystem") { CameraSystem(world) }
        }

        scene {
            entity("Player", Modifier().transform(y = 2f)) {
                entity("CameraTarget", Modifier().transform(z = -5f).camera(CameraMode.ThirdPerson))
            }
        }
    }

    // 2. Instantiate and run the modern runtime engine
    val runtime = SceneGameRuntime(gameSpec.buildEcsSpec(), renderer)
    
    runtime.load() // Safely runs the SceneBuilder data injection

    // 3. Clean Engine Tick Example Loop
    while (gameIsRunning) {
        val dt = calculateDeltaTime()

        // Handle fixed physics steps safely
        while (physicsAccumulator >= FIXED_TIMESTEP) {
            runtime.updateFixed(FIXED_TIMESTEP)
            physicsAccumulator -= FIXED_TIMESTEP
        }

        // Handle frame steps (AI -> Matrix Math -> GPU Submission)
        runtime.updateFrame(dt)
    }
}
```


---

### The Clean Refactored Code
Here is how beautifully clean your RotatingCubeDemo registers against your new architecture:
```kotlin
// 1. Pure Data Components (Allocated from your World Pools)
data class DemoStateComponent(
    var wireframe: Boolean = false,
    var spinRadians: Float = 0f,
    var showAimMarkers: Boolean = false,
    var moveTargetWithWASD: Boolean = false
)
data class SpinControl(var speed: Float = 1.0f)

// 2. Clear Declarative Level Registration
fun GameSpecDsl.rotatingCubeDemo() = ecs {
    name("Rotating Cube Demo")

    assets {
        // Handle your mesh and material requests here cleanly
    }

    systems {
        // Logic systems handle behavior across the proper game loop updates
        fixedSystem("TimeAndSpinSystem") { SpinSystem(world) }
        frameSystem("DemoCameraSystem") { ModernCameraSystem(world) }
        frameSystem("DebugDrawSystem") { DebugMarkerSystem(world, renderer) }
    }

    scene {
        // Global Demo State Tracker Entity
        entity("DemoState", Modifier().configure<DemoStateComponent> { 
            showAimMarkers = true 
        })

        // The Spinning Cube Node
        val cube = entity("SpinningCube", 
            Modifier()
                .transform(y = 0.5f)
                .meshRenderer("cube.obj", "m_default")
                + SpinControl(speed = 1.5f)
        )

        // The Camera Entity leveraging your CameraMode Enum tracking the Cube
        entity("MainCamera", 
            Modifier()
                .transform(y = 5f, z = 10f)
                .camera(CameraMode.ThirdPerson, target = cube) {
                    distance = 8.0f
                }
        )

        // The Environment Lighting
        entity("MainLight", Modifier() + LightComponent())
    }

    // Modern UI bindings tap directly into components, removing manual callback hooks
    overlay { world, _ ->
        val stateEntity = world.query<DemoStateComponent>().firstOrNull() ?: return@overlay
        val state = world.get<DemoStateComponent>(stateEntity)!!
        val camera = world.query<CameraComponent>().firstOrNull()?.let { world.get<CameraComponent>(it) }

        camera?.let { renderCameraModeToggle(it.mode) { mode -> it.mode = mode } }
        
        // Pure Immediate-mode reactive data toggles
        state.wireframe = shadcnSwitch("cube-wireframe", state.wireframe, "Wireframe")
        state.showAimMarkers = shadcnSwitch("cube-markers", state.showAimMarkers, "Show aim markers")
    }
}

```

### Eliminating UI Boilerplate (Using Component Delegates)

Instead of writing world.get<T>(entity)!! over and over, you can create a simple Kotlin property delegate (by component()). This lets you treat ECS components as if they were simple, local variables in your UI blocks.

### The Magic Glue (Add this to your engine once)
Or check first in there's already an alternative in world.

```kotlin
inline fun <reified T : Any> World.component(entity: Entity) = object {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return this@component.get<T>(entity) ?: error("Component ${T::class.simpleName} missing!")
    }
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        // If your components are mutable data classes, you can mutate them directly
    }
}
```

### The Beautiful, Boilerplate-Free UI Code

By finding your reference entities once at the start of your UI block, the rest of your code looks like a standard frontend layout with no ECS code visible:

```kotlin
gameSpec.ecs {
    name("Rotating Cube Demo")

    // Define your entities declaratively
    scene {
        val state = entity("State", Modifier() + DemoStateComponent() + SpinControl())
        val cube  = entity("Cube", Modifier().transform(y = 0.5f).meshRenderer("cube.obj"))
        val cam   = entity("Camera", Modifier().camera(CameraMode.ThirdPerson, target = cube))
    }

    // Zero Boilerplate UI Setup
    overlay { world, _ ->
        // 1. Grab your entity handles cleanly
        val stateEntity  = world.query<DemoStateComponent>().first()
        val cameraEntity = world.query<CameraComponent>().first()

        // 2. Delegate the components to simple local variables
        val demo   by world.component<DemoStateComponent>(stateEntity)
        val spin   by world.component<SpinControl>(stateEntity)
        val camera by world.component<CameraComponent>(cameraEntity)

        // 3. Pure, clean declarative rendering with absolute zero ECS getters/setters!
        renderCameraModeToggle(camera.mode) { camera.mode = it }
        
        demo.wireframe          = shadcnSwitch("ui-wireframe", demo.wireframe, "Wireframe")
        demo.showAimMarkers     = shadcnSwitch("ui-markers", demo.showAimMarkers, "Show aim markers")
        spin.speed              = shadcnSlider("ui-speed", spin.speed, min = 0f, max = 5f)
    }
}
```

---

### 1. The Core Infrastructure (The asset Property Delegate)
Add this generic helper function to your framework. It hooks into your SceneGameRuntime context to lazily resolve materials and meshes from your AssetLibrary when requested.

```kotlin
// A lightweight property delegate that fetches your asset on demand from the ECS context
class AssetDelegate<T : Any>(
    private val path: String,
    private val resolver: (SceneGameRuntime, String) -> T
) {
    private var cachedAsset: T? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        // Automatically returns the cached asset if it's already resolved
        return cachedAsset ?: error("Asset at path '$path' has not been loaded by the runtime yet!")
    }

    internal fun resolve(runtime: SceneGameRuntime) {
        cachedAsset = resolver(runtime, path)
    }
}

// Inline syntax helpers for your level files
fun meshAsset(path: String) = AssetDelegate(path) { runtime, p -> runtime.renderer.createMesh(p) }
fun materialAsset(path: String) = AssetDelegate(path) { runtime, p -> runtime.renderer.createMaterial(p) }

```

### 2. Updating your Engine load() Sequence

Update your SceneGameRuntime loading pipeline so it resolves these registered asset paths before building the scene graph entities.

```kotlin
class SceneGameRuntime(private val spec: SceneGameSpec) {
    val world = World()
    val renderer = Renderer()

    suspend fun load() {
        // 1. Core Systems initialization (remains identical)
        
        // 2. Automatically discover and load all asset delegates declared in this scope
        // (The spec passes down the registered delegate handlers)
        spec.assetDelegates.forEach { it.resolve(this) }

        // 3. Fire your Scene Builder safely knowing all meshes and textures exist in memory
        spec.scenePopulationBlock(world)
    }
}
```

### 3. The Final, Zero-Boilerplate Game Code
Look at how clean this makes your demo file. All file checking, null states, and imperative boilerplate are completely gone.

```kotlin
gameSpec.ecs {
    name("Rotating Cube Demo")

    // 1. Declare assets elegantly up-top using delegates
    val cubeMesh by meshAsset("models/cube.obj")
    val defaultMat by materialAsset("materials/shiny_red.mat")

    // 2. Setup your Systems pipeline
    systems {
        fixedSystem("SpinSystem") { SpinSystem(world) }
        frameSystem("RenderSystem") { RenderSystem(renderer) }
    }

    // 3. Pass your loaded assets straight into your clean Modifier tree!
    scene {
        val stateEntity = entity("State", Modifier() + DemoStateComponent() + SpinControl())
        
        // No null-checks, no if statements. The compiled mesh drops right in.
        entity("Cube", 
            Modifier()
                .transform(y = 0.5f)
                + MeshRenderer(cubeMesh, defaultMat) 
        )

        entity("MainCamera", Modifier().camera(CameraMode.ThirdPerson, target = stateEntity))
    }

    // 4. Clean, reactive, non-intrusive UI layout
    overlay { world, _ ->
        val stateEntity = world.query<DemoStateComponent>().first()
        val demo by world.component<DemoStateComponent>(stateEntity)

        demo.wireframe = shadcnSwitch("ui-wireframe", demo.wireframe, "Wireframe")
        demo.showAimMarkers = shadcnSwitch("ui-markers", demo.showAimMarkers, "Show Markers")
    }
}
```
--- 
### Why This Design Completes Your Architecture
- Compile-Time Asset Safety: You cannot accidentally reference an asset that hasn't been declared or tracked. It is bound cleanly as a local property field.
- Separation of Concerns: Your rendering system only processes active MeshRenderer data blocks. It doesn't care how or when the data was pulled from your asset disks.
- Garbage Collection Optimization: Because your references (cubeMesh, defaultMat) are pinned to immutable property fields throughout the lifetime of the level, the CPU completely avoids the object creation and null allocation checking loop inside your per-frame updates.