# pacenote — product workspace

> product-wide source of truth for the android interface. page overrides, when
> present under `pages/`, take precedence only for that page.

## direction

- native compose interpretation of the výpisflow workspace system.
- dark, monochrome, compact and content-first: technical rather than ornamental.
- inter typography, zinc-toned layers, a 48 dp grid, quiet borders and tactile
  state changes establish one visual family with the source product.
- every card carries a subtle deterministic noise texture adapted from the
  high-frequency fractal-noise treatment in výpisflow.
- static composition stays calm; interaction supplies the personality.
- avoid saturated feature colors, cursive display faces, oversized sports
  typography, ornamental rings, animated sheen, fake blur and motion without
  feedback value.

## design dials

- variance: 3/10 — centered, quiet, highly consistent.
- motion: 6/10 — live but purposeful, with short reactions and spatial continuity.
- density: 8/10 — compact dashboard spacing with accessible touch targets.

## color roles

| role | hex | compose token | meaning |
|---|---:|---|---|
| canvas | `#09090B` | `Midnight` | main background |
| raised canvas | `#0D0D0F` | `MidnightRaised` | workspace chrome |
| material | `#131316` | `Graphite` | cards and navigation |
| raised material | `#1A1A1E` | `GraphiteHigh` | inputs and preview |
| soft material | `#1B1B1F` | `GraphiteSoft` | selected and nested layers |
| divider | `#232327` | `Hairline` | quiet structure |
| strong divider | `#34343A` | `outline` | focus and selected outlines |
| primary | `#D4D4D8` | `Frost` | action, text and selection |
| secondary | `#A1A1AA` | `Steel` | metadata |
| local | solid light gray | `LocalAccent` | device-backed result |
| remote | mid-gray outline | `RemoteAccent` | in-memory remote result |
| error | `#FF6961` | `Danger` | semantic exception only |

storage meaning never relies on color. local uses a solid gray inverse pill plus
a phone icon; remote uses an outlined mid-gray pill plus a cloud icon.

## typography

- bundled inter variable font for every screen, control and brand treatment.
- no runtime font download and no platform-dependent fallback in normal use.
- display: semibold, 36–44 sp, tight tracking; only totals and durations.
- headline: semibold, 21–30 sp; one title per screen.
- body: regular, 12–15 sp with comfortable line height.
- labels: semibold, 10–13 sp; sentence case unless the brand mark requires caps.
- wordmark: inter bold, 17 sp, compact negative tracking.
- script, calligraphic and decorative faces are intentionally excluded.
- numerals remain glanceable but no longer dominate the entire viewport.
- license attribution lives in `third_party/inter/LICENSE.txt`.

## shape and spacing

- radius scale: 10 / 12 / 16 / 20 / 24 dp.
- screen margin: 16 dp compact, 18 dp expanded.
- core gap scale: 3 / 5 / 8 / 10 / 14 / 18 dp.
- interactive rows and controls: 48 dp minimum; primary fab: 52 dp.
- one-pixel low-contrast borders and a restrained 2 dp shadow define content
  surfaces without floating bubble geometry.

## material and noise

- surfaces use near-black translucency, a hairline, and static fine grain.
- grain is generated in `drawWithCache`, drawn behind content and capped at a
  fixed point count so it does not allocate on every animation frame.
- no continuous sheen is used. the home summary and primary action remain
  visually static apart from interaction and value transitions.
- the canvas uses a static 48 dp grid and two low-contrast radial tonal fields;
  there are no drifting circles or ornamental rings.
- text is never blurred or placed on a busy high-contrast texture.

## motion language

- navigation: 180–260 ms fade plus 0.985→1 scale continuity.
- filters: 140–220 ms selection fill plus spring press response.
- storage cards: spring press/selection and a short check reveal.
- results: 45 ms capped stagger, 260–340 ms fade/slide/scale entry.
- summary: animated value replacement only; no status dot, sheen or ornamental chart.
- preview: crossfade/scale when title, duration or destination changes.
- refresh: short rotation plus a one-pixel progress line.
- no infinite ambient motion is required. compose motion duration scaling is
  left intact so android accessibility animation settings are respected.

## components

### summary

- compact 176 dp summary in portrait and a fixed 188 dp card in landscape.
- shows total minutes, session count and local/remote split.
- metrics use a simple text grid with no circular decoration.

### result cards

- compact horizontal hierarchy: destination/date first, then title/location and
  duration on one row.
- each card gets a unique deterministic grain seed from its result id.
- local and remote use solid versus outlined destination pills.

### form

- visible labels and adjacent validation remain mandatory.
- three inputs share one grouped grain surface. expanded layouts place location
  and duration side by side so the entire form and save action fit the viewport.
- destination cards are compact, springy and use the same selected treatment.
- live preview animates data changes and mirrors the final card semantics.

### navigation

- compact: 66 dp floating bottom material with icon and label.
- expanded: slim full-width workspace chrome with a bottom hairline, the `pn`
  mark, inter wordmark and a compact two-item segmented destination control.
- selected state is a quiet light-gray material, not a saturated accent.

## adaptive behavior

- compact (<600 dp): bottom navigation and one scrollable column.
- expanded (>=600 dp): top navigation; at >=700 dp content becomes a true
  two-pane composition.
- results: fixed 320–390 dp summary/filter panel left, adaptive result grid right.
- create: compact two-row details panel left, destination/preview/action right;
  all primary controls fit the initial landscape viewport.
- keyboard, system bars and display cutouts must not obscure actions.

## accessibility and delivery checks

- body contrast targets at least 4.5:1.
- all actions remain at least 48 dp even as visual density increases.
- icons without adjacent labels have localized descriptions.
- local/remote meaning combines copy, icon and surface treatment.
- loading, empty, filtered-empty, error, validation and saved states stay visible.
- verify font scale 1.3, disabled animations, portrait and landscape.
- check that grain remains subordinate at normal and increased contrast settings.
