import * as React from "react"
import { cn } from "../lib/utils"

function Kbd({ className, ...props }: React.ComponentProps<"kbd">) {
  return (
    <kbd
      data-slot="kbd"
      className={cn(
        "inline-flex items-center justify-center rounded border border-input bg-muted px-1.5 font-mono text-[10px] font-medium text-foreground select-none",
        className
      )}
      {...props}
    />
  )
}

export { Kbd }
