import { useEffect } from "react"
import { CASES } from "./cases"

/**
 * Renders exactly one reference case, selected by query string, with nothing else on the page.
 *
 * One case per load rather than a gallery: the capture screenshots `#case` and a gallery would
 * force the tiling that made the previous scraped references only as trustworthy as our own
 * arrangement choices.
 */
export function App() {
  const params = new URLSearchParams(window.location.search)
  const id = params.get("case") ?? "button-variants"
  const dark = params.get("theme") === "dark"
  const radius = params.get("radius")

  useEffect(() => {
    document.documentElement.classList.toggle("dark", dark)
    document.documentElement.style.colorScheme = dark ? "dark" : "light"
    if (radius) document.documentElement.style.setProperty("--radius", radius)
  }, [dark, radius])

  const entry = CASES[id]
  if (!entry) {
    return <div id="case" data-error="unknown-case">{`unknown case: ${id}`}</div>
  }
  // `w-fit` with no padding: the captured element must be exactly the component, since any
  // wrapper padding is 16px of reference that the Awake preview has no counterpart for and
  // would show up as a fidelity difference it isn't.
  return (
    <div id="case" className="w-fit" data-case={id}>
      {entry.render()}
    </div>
  )
}
