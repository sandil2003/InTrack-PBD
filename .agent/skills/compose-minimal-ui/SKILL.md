---
name: compose-minimal-ui
description: Design and implement smooth, minimal, and modern Jetpack Compose UIs following Material Design 3. Use when building or styling Jetpack Compose screens, components, animations, bottom sheets, cards, or dashboards for Android apps.
---
# Compose Minimal UI

Guidelines and patterns for implementing fluid, minimalist, and accessible Android user interfaces with Jetpack Compose and Material Design 3.

## When to Use
- Building or refactoring Jetpack Compose screens and composables.
- Implementing minimal cards, gauges, charts, or transaction lists.
- Designing frictionless bottom sheets and micro-interactions.
- Adding smooth animations, transitions, and state-driven motion.

## Core Design Principles
1. Restraint Over Clutter: Eliminate unnecessary borders, heavy dividers, and harsh shadows. Rely on tonal elevation, surface container roles, and subtle alpha variations.
2. 8dp Spatial System: Standardize all paddings and margins to multiples of 4dp and 8dp (4.dp, 8.dp, 12.dp, 16.dp, 24.dp). Never use arbitrary hardcoded values.
3. Ergonomic Thumb Zones: Keep primary interactive elements, quick-add triggers, and confirmation buttons within the bottom 40% of the screen.
4. Seamless Fluidity: Every list change, state toggle, and modal sheet should transition smoothly with spring or easing animations.

## Material Design 3 Standards
- Use standard M3 surface tokens: `MaterialTheme.colorScheme.surfaceContainerLow` for background cards, `surfaceContainer` for elevated components, and `primary` / `secondary` for high-value accents.
- Enforce strict typography hierarchy:
  - `headlineMedium` / `headlineSmall` for primary metric amounts.
  - `titleMedium` / `titleSmall` for section headers and card titles.
  - `bodyMedium` for transaction descriptions and primary metadata.
  - `labelSmall` / `labelMedium` for status pills, category chips, and timestamps.
- Maintain WCAG AA contrast ratios (minimum 4.5:1 for normal text).

## Layout & Component Patterns

### Minimal Metric Card
Wrap primary metrics in rounded tonal cards (`RoundedCornerShape(20.dp)`) without heavy border strokes:

```kotlin
Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ),
    modifier = Modifier.fillMaxWidth()
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = formattedAmount,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}