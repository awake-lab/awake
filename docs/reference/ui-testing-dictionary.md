# UI Testing Dictionary

Plain-English meanings for terms used in Awake UI code and tests. This page is for people who
prefer clear technical English; it is not a language test.

| Term | Plain meaning | In Awake |
|---|---|---|
| **fixture** | A small, repeatable UI example used by a test. | A button rendered at a known size. |
| **frame** | One moment of UI input and drawing. | One call to `renderUiComponent` or `UiTestSession.frame`. |
| **gesture** | A user action made of one or more frames. | `click`, `drag`, `hover`, or `longPress`. |
| **semantics** | Meaningful UI information for tools, tests, and accessibility. | A node id, role, label, selected state, and bounds. It is not visual pixels. |
| **semantic node** | One item in that meaningful UI tree. | The node for a button, menu item, or text field. |
| **bounds** | A rectangle: x position, y position, width, and height. | `frame.bounds("save")`. |
| **primitive** | A low-level thing the UI renderer draws. | A quad, rounded rectangle, glyph, or path. |
| **snapshot / golden** | A saved expected image or output used to detect change. | It proves output changed or stayed the same; it does not alone prove it is correct. |
| **baseline** | The saved value that a snapshot test compares against. | A PNG or signature map. |
| **parity** | How closely Awake matches a reference implementation. | Geometry parity compares real sizes/positions with shadcn. |
| **probe** | A focused, temporary or permanent test used to answer one uncertain question with real measurements. | A test checking whether a popup overlaps another control. Name permanent probes after the invariant, not the incident. |
| **regression test** | A test that prevents a fixed bug from coming back. | It must fail if the fix is removed. |
| **matrix test** | One data-driven test that covers combinations systematically. | A layout sizing table covering parent and child sizing modes. |
| **lifecycle** | The setup, render, and finish sequence of a UI frame. | `UiContext.beginFrame` → render → `finishFrame`. Shared helpers own this for normal component tests. |
| **boilerplate** | Repeated setup that does not explain what the test proves. | Repeating context, font, theme, and frame setup in every test. |
| **low-level test** | A test that checks the UI engine mechanism itself, not only a component result. | A cache, clip-stack, renderer, or direct frame-lifecycle test. It needs `@UiLowLevelTest("reason")`. |

## Simple rule for choosing a test helper

- One static frame: use `renderUiComponent(...)`.
- Several frames or user input: use `uiTestSession(...)`.
- Use `hover`, `click`, `doubleClick`, `longPress`, `rightClick`, or `drag` instead of manually
  writing the normal press/release frames.
- Use an exact `UiInputState` frame only for input the helpers cannot express, such as wheel or
  a specific keyboard state.
- Use raw `UiContext` only when the test is directly about Core/runtime lifecycle, layout cache,
  renderer/backend output, or another mechanism that the helper would hide.

See [UI Validation](ui-validation.md) for the required proof and test ownership rules.
