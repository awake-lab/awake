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
  // `w-fit` so the captured element hugs its content -- the crop-coverage problem that made
  // ten of eleven scraped pairs unmeasurable was framing, not fidelity.
  return (
    <div id="case" className="w-fit p-2" data-case={id}>
      {entry.render()}
    </div>
  )
}
