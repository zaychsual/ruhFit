---
name: Ruh & Raga System
colors:
  surface: '#0b1326'
  surface-dim: '#0b1326'
  surface-bright: '#31394d'
  surface-container-lowest: '#060e20'
  surface-container-low: '#131b2e'
  surface-container: '#171f33'
  surface-container-high: '#222a3d'
  surface-container-highest: '#2d3449'
  on-surface: '#dae2fd'
  on-surface-variant: '#bbcabf'
  inverse-surface: '#dae2fd'
  inverse-on-surface: '#283044'
  outline: '#86948a'
  outline-variant: '#3c4a42'
  surface-tint: '#4edea3'
  primary: '#4edea3'
  on-primary: '#003824'
  primary-container: '#10b981'
  on-primary-container: '#00422b'
  inverse-primary: '#006c49'
  secondary: '#ffb690'
  on-secondary: '#552100'
  secondary-container: '#ec6a06'
  on-secondary-container: '#4a1c00'
  tertiary: '#ffb3af'
  on-tertiary: '#650911'
  tertiary-container: '#fc7c78'
  on-tertiary-container: '#711419'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#6ffbbe'
  primary-fixed-dim: '#4edea3'
  on-primary-fixed: '#002113'
  on-primary-fixed-variant: '#005236'
  secondary-fixed: '#ffdbca'
  secondary-fixed-dim: '#ffb690'
  on-secondary-fixed: '#341100'
  on-secondary-fixed-variant: '#783200'
  tertiary-fixed: '#ffdad7'
  tertiary-fixed-dim: '#ffb3af'
  on-tertiary-fixed: '#410005'
  on-tertiary-fixed-variant: '#842225'
  background: '#0b1326'
  on-background: '#dae2fd'
  surface-variant: '#2d3449'
typography:
  display-hero:
    fontFamily: Inter
    fontSize: 40px
    fontWeight: '700'
    lineHeight: 48px
    letterSpacing: -0.02em
  display-hero-mobile:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: -0.015em
  headline-md:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  metric-num-lg:
    fontFamily: Inter
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.01em
  metric-num-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 28px
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.05em
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 14px
    letterSpacing: 0.04em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  margin: 1.25rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

This design system manifests the synthesis of inner discipline and physical vigor: the contemplation of spiritual duty (*Ruh*) balanced against the kinetic drive of bodily strength (*Raga*). The visual tone is structured, focused, and meditative, built to eliminate cognitive clutter during predawn prayers, intense calisthenics sessions, and late-night reflections.

The design philosophy adopts a refined **Dark-Mode Modern Minimalist** approach augmented with controlled luminescence:
- **Atmospheric Dark Precision:** Rich slate-toned backgrounds preserve circadian comfort and visual depth across variable lighting environments.
- **Dual Energy Channels:** Functional color duality provides immediate, subconscious role separation—soothing, reverent emerald tones guide spiritual routines, while vibrant, kinetic amber-orange accents drive physical exertion.
- **Ergonomic Tactility:** Designed around a bottom-up thumb-zone architecture for mobile usage during active training sessions or one-handed tasbih counts.

## Colors

The palette employs deep atmospheric foundations punctuated by high-contrast intentional accents. 

### Palette Architecture
- **Base Canvas (`#0F172A`):** The ground layer across all root viewports.
- **Surface Elevation (`#1E293B`):** Structural card layer. Elevates interactive components against the base.
- **Subtle Stroke (`#334155`):** Structural 1px separation lines defining card bounds without cluttering screen hierarchy.
- **Ruh Domain (Emerald):**
  - Primary Accent: `#10B981` (Emerald 500) for primary actionable targets, prayer completions, active tasbih states, and progress rings.
  - Interactive Hover/Active: `#059669` (Emerald 600).
  - Ambient Glow/Sub-surface: `#065F46` (Emerald 800) at 12–24% opacity.
- **Raga Domain (Kinetic Orange):**
  - Primary Accent: `#F97316` (Orange 500) for set completions, timer countdowns, heart-rate zones, and workout triggers.
  - Interactive Hover/Active: `#EA580C` (Orange 600).
  - Deep Anchor: `#C2410C` (Orange 700).
- **Typography & Content Hierarchy:**
  - High Emphasis: `#F8FAFC` (Slate 50) for primary metrics, titles, and body-heavy instructions.
  - Medium Emphasis: `#94A3B8` (Slate 400) for timestamps, secondary tags, and set/rep labels.
  - Low Emphasis: `#64748B` (Slate 500) for inactive toggles, divider marks, and background ring tracks.

## Typography

Typography centers exclusively on **Inter** to ensure maximum legibility, compact line-height economy, and technical neutrality.

### Key Rules
- **Tabular Figures (`tnum`):** All timers, rep counters, rest periods, prayer schedules, and tasbih totals must render with CSS `font-variant-numeric: tabular-nums;` (or OpenType feature `"tnum"`). This prevents jittering during dynamic updates.
- **Labels:** Metric categories (e.g., `FAJR`, `PULL-UPS`, `AMRAP`) use uppercase styling paired with tracked letter spacing (`0.04em`–`0.05em`) to balance optical density against dark backgrounds.
- **Visual Weight:** Avoid thin weights (`< 400`) to guarantee legibility against dark slate surfaces across outdoor and low-light environments.

## Layout & Spacing

The layout is built for a 390x844px mobile viewport using an 8pt baseline rhythm and a fluid 4-column system.

### Form Factor Behavior
- **Screen Margins:** Fixed `1.25rem` (20px) horizontal inset keeps content clear of the device bevel and thumb perimeter.
- **Vertical Spacing Rhythm:** Component separation strictly follows an 8px modular scale (`space-xs` = 4px, `space-sm` = 8px, `space-md` = 16px, `space-lg` = 24px, `space-xl` = 32px).
- **Thumb-Zone Optimization:** High-frequency interactions (workout logging buttons, tasbih taps, routine toggles) remain pinned within the lower 42% of the viewport height. Persistent headers and status summaries occupy the top viewport zone.
- **Safe Area Insets:** Account for standard mobile top notches (44px) and home indicators (34px) as absolute padding baselines.

## Elevation & Depth

Visual depth combines structural borders, translucent overlays, and tinted ambient glows rather than traditional cast drop shadows.

### Elevation Hierarchy
1. **Level 0 (Base Layer):** Canvas `#0F172A`. Flat, non-interactive backdrop.
2. **Level 1 (Card & Module Layer):** Solid `#1E293B` or frosted surface with 85% opacity backdrop-filter (`blur(16px)`). Bound by a crisp `1px solid #334155` border.
3. **Level 2 (Active/Floating Layer):** Modals, sticky bottom action sheets, and floating tally buttons. Background `#1E293B` backed by a 1px border at `#475569`.
4. **State-Driven Glows:**
   - **Ruh Ambient Glow:** Applied to active prayer cards and completed worship rings using `box-shadow: 0 0 20px -4px rgba(16, 185, 129, 0.25);`.
   - **Raga Kinetic Glow:** Applied to active timers and logging buttons using `box-shadow: 0 0 20px -4px rgba(249, 115, 22, 0.3);`.

## Shapes

The design system uses a defined curvature model that pairs structural cards with pill-shaped interactive anchors:
- **Cards & Enclosures:** Standard components use a continuous 16px radius (`rounded-lg`), scaling to 20px (`rounded-xl`) for full-width grouped dashboard containers.
- **Badges, Tags, & Buttons:** Primary action buttons, segment pills, and tags leverage complete capsule shapes (`rounded-full` / 9999px) to establish clear visual affordance.
- **Counters & Progress Trackers:** Circular geometries reinforce cycle tracking (e.g., circular prayer progress arcs and circular rest timers).

## Components

### Buttons
- **Primary Ruh Action:** Background `#10B981`, foreground `#0F172A` (bold), height 52px, pill-shaped, emerald glow on press.
- **Primary Raga Action:** Background `#F97316`, foreground `#0F172A` (bold), height 52px, pill-shaped, orange glow on press.
- **Secondary / Ghost:** Transparent background, border `1px solid #334155`, text `#F8FAFC`, active scale down (0.98).

### Dual-Themed Status Chips & Badges
- **Spiritual (Ruh):** Background `rgba(16, 185, 129, 0.12)`, text `#10B981`, border `1px solid rgba(16, 185, 129, 0.25)`.
- **Physical (Raga):** Background `rgba(249, 115, 22, 0.12)`, text `#F97316`, border `1px solid rgba(249, 115, 22, 0.25)`.
- Height 24px, padding 0 10px, uppercase `label-sm` font.

### Cards & Grouped Containers
- Background `#1E293B`, border `1px solid #334155`, radius `16px` (`rounded-lg`), padding `16px` (`space-md`).
- Active items (e.g., current active prayer or active exercise set) shift border color to their respective thematic accent (`#10B981` or `#F97316`).

### Input Fields & Set/Rep Steppers
- Height 48px, background `#0F172A`, border `1px solid #334155`, radius `12px`.
- Active focus ring: `1px solid #10B981` (Ruh context) or `1px solid #F97316` (Raga context).
- Counter inputs feature oversized minus/plus tap regions (minimum 44x44px touch targets) with tabular font readouts.

### Checkboxes & Segment Toggles
- Custom circular check controls: Inactive `#334155` stroke; active Ruh fills with `#10B981` and white SVG check; active Raga fills with `#F97316`.

### Domain-Specific Components
- **Tasbih & Rest Tap-Plate:** Large bottom-anchored circular or pill target (minimum 88px height) designed for rapid blind thumb-tapping with haptic feedback.
- **Dual-Ring Header Summary:** Interlocking or concentric radial meters displaying daily spiritual milestones alongside workout volume.