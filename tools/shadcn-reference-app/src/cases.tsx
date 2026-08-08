import type { ReactNode } from "react"
import { Button } from "./ui/button"
import { Badge } from "./ui/badge"

/**
 * The reference cases. Each id is the single source of truth shared with the Awake side:
 * the capture renders `?case=<id>`, and the Kotlin preview of the same id renders the
 * equivalent Awake components. Pairing is therefore by construction rather than hand-matched.
 *
 * States that a docs demo cannot show -- focus, disabled, hover -- are ordinary cases here,
 * which is the whole reason for owning the reference page.
 */
export const CASES: Record<string, { render: () => ReactNode }> = {
  "button-variants": {
    render: () => (
      <div className="flex items-center gap-2">
        <Button>Default</Button>
        <Button variant="secondary">Secondary</Button>
        <Button variant="outline">Outline</Button>
        <Button variant="ghost">Ghost</Button>
        <Button variant="destructive">Destructive</Button>
        <Button variant="link">Link</Button>
      </div>
    ),
  },
  "button-sizes": {
    render: () => (
      <div className="flex items-center gap-2">
        <Button size="sm">Small</Button>
        <Button>Default</Button>
        <Button size="lg">Large</Button>
      </div>
    ),
  },
  "button-disabled": {
    render: () => (
      <div className="flex items-center gap-2">
        <Button disabled>Default</Button>
        <Button variant="outline" disabled>Outline</Button>
        <Button variant="destructive" disabled>Destructive</Button>
      </div>
    ),
  },
  "badge-variants": {
    render: () => (
      <div className="flex items-center gap-2">
        <Badge>Default</Badge>
        <Badge variant="secondary">Secondary</Badge>
        <Badge variant="destructive">Destructive</Badge>
        <Badge variant="outline">Outline</Badge>
      </div>
    ),
  },
}
