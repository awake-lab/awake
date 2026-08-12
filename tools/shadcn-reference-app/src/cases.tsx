import type { ReactNode } from "react"
import { Button } from "./ui/button"
import { Badge } from "./ui/badge"
import { Checkbox } from "./ui/checkbox"
import { RadioGroup, RadioGroupItem } from "./ui/radio-group"
import { Switch } from "./ui/switch"
import { Progress } from "./ui/progress"
import { Input } from "./ui/input"
import { Tabs, TabsList, TabsTrigger } from "./ui/tabs"
import { Slider } from "./ui/slider"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "./ui/select"
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card"
import { Label } from "./ui/label"
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "./ui/tooltip"
import {
  Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle,
} from "./ui/dialog"

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
  "radio-group-states": {
    render: () => (
      <RadioGroup defaultValue="comfortable">
        {[
          ["default", "Default"],
          ["comfortable", "Comfortable"],
          ["compact", "Compact"],
        ].map(([value, label]) => (
          <div className="flex items-center gap-2" key={value}>
            <RadioGroupItem value={value} id={`radio-${value}`} />
            <Label htmlFor={`radio-${value}`}>{label}</Label>
          </div>
        ))}
      </RadioGroup>
    ),
  },
  "progress-states": {
    render: () => (
      <div className="flex w-[212px] flex-col gap-4">
        <Progress value={25} />
        <Progress value={65} />
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
  "select-closed": {
    render: () => (
      <Select>
        <SelectTrigger className="w-[172px]">
          <SelectValue placeholder="Select a fruit" />
        </SelectTrigger>
        <SelectContent>
          {["Apple", "Banana", "Blueberry", "Grapes", "Pineapple"].map((f) => (
            <SelectItem key={f} value={f.toLowerCase()}>{f}</SelectItem>
          ))}
        </SelectContent>
      </Select>
    ),
  },
  "card-login": {
    render: () => (
      <Card className="w-[288px]">
        <CardHeader>
          <CardTitle>Login to your account</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col gap-3">
          <Label htmlFor="email">Email</Label>
          <Input id="email" placeholder="Email" />
          <Button className="w-full">Login</Button>
        </CardContent>
      </Card>
    ),
  },
  // Tooltip and dialog render open via `defaultOpen`/`open` rather than needing the capture to
  // hover or click. A docs page can only be scraped in whatever state it happens to be in;
  // owning the page means an open overlay is just another case.
  "tooltip-open": {
    render: () => (
      <TooltipProvider>
        <Tooltip defaultOpen>
          <TooltipTrigger asChild>
            <Button variant="outline">Hover</Button>
          </TooltipTrigger>
          <TooltipContent side="bottom">Add to library</TooltipContent>
        </Tooltip>
      </TooltipProvider>
    ),
  },
  "dialog-open": {
    render: () => (
      <Dialog open>
        <DialogContent showCloseButton={false} className="w-[320px]">
          <DialogHeader>
            <DialogTitle>Edit profile</DialogTitle>
            <DialogDescription>
              Make changes to your profile here. Click save when you're done.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button>Save changes</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
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
