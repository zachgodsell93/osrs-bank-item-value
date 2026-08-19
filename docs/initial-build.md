# Claude Code Prompt — RuneLite Bank Value Overlay Plugin

> Paste the whole of this file into Claude Code as the opening message, from an empty
> cloned repo. Replace `<github-username>` and `<plugin-name>` before you send it.

---

Build a RuneLite plugin from scratch in this empty repository. It overlays the bank
interface with per-item values and colour-coded highlighting, modelled on the behaviour
of RuneLite's built-in Ground Items plugin but applied to the bank.

## Step 0 — Verify the API before writing any code

Your training data on RuneLite's API is likely stale. Before writing code:

1. Fetch `https://github.com/runelite/example-plugin` and read the README,
   `build.gradle`, `settings.gradle`, and the example plugin/config/panel classes.
   Use its structure, Gradle config, and dependency versions verbatim as the baseline.
2. Read the current source of these classes in `runelite/runelite` on GitHub:
   - `net.runelite.client.ui.overlay.WidgetItemOverlay`
   - `net.runelite.client.plugins.grounditems.GroundItemsPlugin` and its config +
     overlay (the tier/colour model is the reference implementation)
   - `net.runelite.client.util.QuantityFormatter`
   - `net.runelite.client.game.ItemManager`
3. Confirm the current names for bank widget/component constants. These have been
   refactored more than once (`WidgetInfo` → `ComponentID` → `InterfaceID.Bankmain.*`).
   Use whatever the current source says.

**The live source wins over anything written in this prompt.** If a class or method
named below does not exist, find the current equivalent and tell me what changed
instead of inventing an API or stubbing it out.

## Step 1 — Repository scaffolding

- Package: `com.<github-username>.<plugin-name>`
- Java 11 target unless the example plugin specifies otherwise
- Gradle build matching the example plugin, including the `https://repo.runelite.net`
  repository and `compileOnly` dependency on `net.runelite:client`
- `LICENSE` — BSD 2-Clause (RuneLite ecosystem convention)
- `.gitignore` for Gradle/IntelliJ
- `plugin.properties` at repo root for eventual Plugin Hub submission:
  `displayName`, `author`, `description`, `tags`, `plugins` (fully-qualified plugin class)
- A `PluginTest` class under `src/test/java` with a `main` that boots RuneLite with this
  plugin loaded via `ExternalPluginManager.loadBuiltin(...)` — this is the dev loop
- `README.md` with a screenshot placeholder, feature list, and config reference

The plugin name must not contain the string "runelite".

## Step 2 — Core behaviour

An overlay extending `WidgetItemOverlay`, registered with `showOnBank()` in its
constructor. For each rendered bank item, compute a value, resolve a colour tier from
that value, and draw according to config.

### Value calculation

- `quantity = itemWidget.getQuantity()`; **if quantity is 0 the slot is a bank
  placeholder — render nothing and return early.** This is important; placeholders will
  otherwise light up the whole bank.
- Unit price comes from the configured price source (below).
- Total value = unit price × quantity. Use `long` arithmetic throughout — a full bank
  tab of stacked items overflows `int` easily.
- Items with no meaningful price (untradeables with no GE price and no alch value)
  render nothing.

### Price source (config enum `priceSource`)

| Option | Behaviour |
|---|---|
| `GRAND_EXCHANGE` (default) | `itemManager.getItemPrice(itemId)` |
| `HIGH_ALCHEMY` | high alch value from the item composition |
| `HIGHEST` | max of the two |

Note: `getItemPrice` already resolves noted items to their unnoted counterpart in
current RuneLite versions — verify this and do not double-handle it.

### Tier resolution

Five bands, mirroring Ground Items. Each has a configurable threshold and colour.
The winning tier is the **highest threshold whose value is less than or equal to the
item's computed value**. Items below the lowest threshold use `defaultColor`.

Suggested defaults (all user-editable):

| Tier | Default threshold | Default colour |
|---|---|---|
| default (below all tiers) | — | `#FFFFFF` |
| low | 20,000 | `#66B2FF` |
| medium | 100,000 | `#99FF99` |
| high | 1,000,000 | `#FF9600` |
| insane | 10,000,000 | `#FF66B2` |

Each tier gets an individual enable toggle. A disabled tier is skipped during
resolution and falls through to the next one down.

### Number formatting (config enum `valueFormat`)

| Option | 73,542 renders as | 1,000,000 renders as |
|---|---|---|
| `SHORT` | `73K` | `1M` |
| `DECIMAL` (default) | `73.5K` | `1M` |
| `EXACT` | `73,542` | `1,000,000` |

Use `QuantityFormatter` rather than hand-rolling this — it already implements the
game's own stack-size and decimal-stack conventions. Values under 1,000 always render
as plain digits.

## Step 3 — Configuration

A `@ConfigGroup`-annotated interface, organised with `@ConfigSection`. Use `@Alpha` on
every `Color` so alpha is editable, and `@Range` on numeric inputs.

**Section: General**
- `showValueText` (boolean, default true)
- `priceSource` (enum, above)
- `valueFormat` (enum, above)
- `hideUnderValue` (int, default 0) — items whose total value is below this render
  nothing at all
- `applyToInventory` (boolean, default false) — also overlay the inventory side panel
  while the bank is open, via `showOnInventory()`
- `applyToBankInventory` — only if the above is not already sufficient once you check
  how `WidgetItemOverlay` scopes the bank's inventory panel; drop this option if
  redundant

**Section: Display**
- `textPosition` (enum: `TOP_LEFT`, `TOP_RIGHT`, `BOTTOM_LEFT`, `BOTTOM_RIGHT`,
  `CENTER`; default `BOTTOM_LEFT`) — the game draws stack counts at top-left, so the
  default must avoid colliding with them
- `textShadow` (boolean, default true) — one-pixel offset shadow for legibility
- `useSmallFont` (boolean, default true) — bank item cells are roughly 36×32px, so the
  Runescape small font is usually the only thing that fits

**Section: Highlighting**
- `highlightMode` (enum: `TEXT_ONLY` (default), `BOX_OUTLINE`, `BOX_FILL`,
  `OUTLINE_AND_TEXT`, `NONE`)
- `fillOpacity` (int, 0–255, default 40) — applies to `BOX_FILL`

**Section: Value tiers**
- Per tier: `<tier>Enabled`, `<tier>Threshold`, `<tier>Color`
- `defaultColor`

Give every `@ConfigItem` an explicit `position` so the panel ordering is stable, and
write descriptions that would make sense to someone who has not read this spec.

## Step 4 — Rendering details

- Clip drawing to the bank item container's bounds. Partially-scrolled items at the
  top and bottom edge of the bank must not paint outside the container. Check whether
  `WidgetItemOverlay` already handles this in the current version; if it does not, clip
  explicitly against the container widget's canvas bounds.
- Text must be readable against both light and dark item sprites — hence the shadow.
- `BOX_OUTLINE` draws a 1px rectangle on `itemWidget.getCanvasBounds()`.
- Restore any mutated `Graphics2D` state (font, colour, clip, composite) before
  returning.

## Step 5 — Performance

A bank can hold well over 800 items and `renderItemOverlay` runs per item per frame.

- Cache the computed `(unit price, tier colour, formatted string)` keyed on
  `itemId + quantity`. A `HashMap` or Guava cache is fine.
- Invalidate the cache on config change (`@Subscribe onConfigChanged`, filtered to this
  config group) and when the bank closes.
- Do not call `getItemComposition` or build strings unconditionally in the render path.
- Resolve config values into fields on config change rather than reading the config
  interface per item per frame.

## Step 6 — Definition of done

- `./gradlew build` passes clean
- The `PluginTest` main launches RuneLite with the plugin active
- Every config option demonstrably changes behaviour at runtime with no restart
- Placeholders render nothing
- Tier boundaries are correct at exact threshold values (an item worth exactly 1,000,000
  is `high`, not `medium`)
- No allocation-heavy work in the render loop
- README documents every config option

## Non-goals

Do not implement: bank tab totals, a side panel, a summary of total bank worth, price
history, or anything that writes to disk. Keep it to the overlay.

## Working style

Work in stages and stop for my review after each: (1) scaffolding + build green,
(2) config interface, (3) overlay with value text only, (4) tiers and highlighting,
(5) caching and polish. Do not write the whole thing in one pass.

Flag anywhere you are unsure about the current RuneLite API rather than guessing.
