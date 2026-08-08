import type { ReactNode } from "react"
import { Button } from "./ui/button"
import { Badge } from "./ui/badge"
import { Checkbox } from "./ui/checkbox"
import { Switch } from "./ui/switch"
import { Input } from "./ui/input"
import { Tabs, TabsList, TabsTrigger } from "./ui/tabs"
import { Slider } from "./ui/slider"

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
  "checkbox-states": {
    render: () => (
      <div className="flex items-center gap-4">
        <Checkbox />
        <Checkbox defaultChecked />
        <Checkbox disabled />
      </div>
    ),
  },
  "switch-states": {
    render: () => (
      <div className="flex items-center gap-4">
        <Switch />
        <Switch defaultChecked />
        <Switch disabled />
      </div>
    ),
  },
  "input-states": {
    render: () => (
      <div className="flex flex-col gap-3 w-64">
        <Input placeholder="Placeholder" />
        <Input defaultValue="Typed text" />
        <Input placeholder="Disabled" disabled />
      </div>
    ),
  },
  "tabs-states": {
    render: () => (
      <Tabs defaultValue="account">
        <TabsList>
          <TabsTrigger value="account">Account</TabsTrigger>
          <TabsTrigger value="password">Password</TabsTrigger>
        </TabsList>
      </Tabs>
    ),
  },
  "slider-states": {
    render: () => (
      <div className="w-64">
        <Slider defaultValue={[50]} max={100} step={1} />
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
