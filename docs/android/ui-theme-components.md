# UI, Theme, and Components

The UI layer keeps visual styling and reusable building blocks in one place.

## Theme Files

```text
ui/theme/Color.kt
ui/theme/Shape.kt
ui/theme/Theme.kt
ui/theme/Type.kt
```

## Color.kt

Contains ModuleLens color tokens.

Why:

- avoids hardcoded colors across screens
- makes design updates easier
- keeps the app visually consistent

## Type.kt

Defines typography styles.

Why:

- avoids random `.sp` values in feature screens
- creates consistent hierarchy for titles, body text, labels, and captions

## Shape.kt

Defines app shape tokens.

Why:

- cards and controls feel consistent
- screen components avoid one-off corner radius values

## Theme.kt

Applies the Material 3 theme.

Why:

- centralizes colors, typography, and shapes
- lets screens use `MaterialTheme`

## Shared Components

```text
BottomNavigationBar.kt
ModuleLensCard.kt
ModuleLensTopBar.kt
ProgressBar.kt
ScreenState.kt
SectionHeader.kt
StatusChip.kt
```

## ModuleLensCard

Reusable card wrapper.

Why:

- keeps card shape, color, and elevation consistent
- avoids repeating Material card setup

## ModuleLensTopBar

Reusable top app bar.

Why:

- keeps page headers consistent
- supports navigation icons and actions

## BottomNavigationBar

Reusable bottom tab bar.

Why:

- keeps top-level navigation consistent
- uses `BottomNavItem` definitions

## ProgressBar

Reusable progress display.

Why:

- used for course/module progress
- keeps progress styling consistent

## ScreenState

Shared screen state components:

- `ModuleLensLoadingState`
- `ModuleLensErrorState`
- `ModuleLensInlineError`
- `ModuleLensEmptyState`
- `ModuleLensRefreshingIndicator`

Why:

- avoids repeating loading and error UI in every screen
- makes the app feel consistent
- reduces code size

## SectionHeader

Simple heading row for grouped screen sections.

Why:

- keeps screen sections easy to scan
- optional action text can be added when needed

## StatusChip

Small label for statuses and categories.

Used for:

- review status
- difficulty
- source type
- quiz item count

Why:

- compact status display
- reusable visual pattern
