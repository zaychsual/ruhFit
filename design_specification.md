# Product Design Specification & Prompt: "Ruh & Raga" Tracker

Personal Daily Tracker for Spiritual Routine (Ibadah) & Physical Conditioning (Workout). Built for high-frequency daily logging, clean minimalist UI, and intuitive progress visualization.

---

## 1. Design System & Visual Guidelines

* **Target Device:** Mobile (iOS & Android) — 390x844px baseline (iPhone standard viewport).
* **Theme & Style:** Modern Minimalist, Calm Dark Mode default (with high-contrast accent colors), subtle rounded cards (`rounded-2xl`), clean micro-interactions.
* **Color Palette:**
  * **Background Base:** `#0F172A` (Slate 900)
  * **Surface / Cards:** `#1E293B` (Slate 800) with subtle borders `#334155` (Slate 700)
  * **Spiritual Accent (Ibadah):** `#10B981` (Emerald Green) & `#065F46` (Deep Emerald)
  * **Workout Accent (Raga):** `#F97316` (Energetic Orange) & `#EA580C` (Warm Amber)
  * **Neutral Text:** `#F8FAFC` (Primary Text), `#94A3B8` (Muted / Secondary Text)
* **Typography:** Clean Sans-serif (Inter / SF Pro Display). Big readable numbers for counters, stats, and timers.
* **Layout Paradigm:** Single-hand reachability (action buttons placed in thumb zone / bottom half).

---

## 2. Core Navigation Structure (Bottom Navigation Bar)

* **Tab 1: Today (Dashboard / Fast Log)** — Icon: `CalendarToday` / `CheckCircle`
* **Tab 2: Ibadah Tracker** — Icon: `Moon` / `Mosque`
* **Tab 3: Workout Logger** — Icon: `Dumbbell` / `Flame`
* **Tab 4: Progress & Heatmap** — Icon: `BarChart3` / `Grid`

---

## 3. Screen-by-Screen Specifications & Prompts

### Screen 1: Today's Overview (Home Dashboard)
* **Screen Purpose:** At-a-glance status of today's spiritual completion and physical workout targets with quick-action toggles.
* **Key Components:**
  * **Top Bar:** Date selector (Today, Mon, 16 Oct), current active streak indicator (`🔥 14 Days Streak`).
  * **Dual Progress Rings / Bars:**
    * *Ruh (Ibadah):* Radial progress ring (e.g., 5/5 Shalat, 1 Tilawah completed).
    * *Raga (Workout):* Radial progress ring (e.g., 120/150 push-up target completed, 5 km run logged).
  * **Quick Log Cards (Carousel / Stack):**
    * Next upcoming prayer card with remaining countdown.
    * Active workout quick-start button ("Continue Push-up Session: Set 3/5").
  * **Contribution Heatmap Strip:** Mini 7-day horizontal habit tracker (green & orange dots representing completed goals).

---

### Screen 2: Ibadah Tracker (Spiritual Checklist)
* **Screen Purpose:** Clear, distraction-free logging of obligatory and voluntary spiritual activities.
* **Key Components:**
  * **Segmented Control:** "Fardhu (Obligatory)" | "Sunnah" | "Tilawah & Dzikir".
  * **Prayer Tracker List Items:**
    * Subuh, Dzuhur, Ashar, Maghrib, Isya.
    * Each card has: Prayer name, calculated time, checkbox/toggle pill, and an indicator for "Jama'ah" toggle.
  * **Sunnah Accordion:**
    * Qiyamul Lail / Tahajjud, Dhuha, Rawatib (before/after counters), Witir.
  * **Tilawah Progress Card:**
    * Book icon, current Surah/Juz slider or stepper (`Surah Al-Kahf: Ayat 1 - 30`).
    * "Completed for today" one-tap button.

---

### Screen 3: Workout Logger & Rep Counter
* **Screen Purpose:** Fast, low-friction logging during high-intensity workouts without getting distracted by complex forms.
* **Key Components:**
  * **Header:** Workout title selector (e.g., "Daily Calisthenics Routine" or "Cardio & Run").
  * **Repetition Counter Hub (Large Display):**
    * Giant circular counter showing total reps (e.g., "Push-ups: 75").
    * Quick incremental pills: `+5`, `+10`, `+20`, `-1` for instant logging.
    * Set breakdown table: `Set 1: 25 reps | Set 2: 25 reps | Set 3: 25 reps`.
  * **Built-in Rest Interval Timer:**
    * Digital countdown pill (e.g., `00:45 remaining`).
    * Large "Start Rest" / "Next Set" thumb button at the bottom.
  * **Cardio Quick-Entry Card:**
    * Numeric inputs for Distance (km) and Duration (hh:mm).

---

### Screen 4: Analytics, Heatmap & Monthly Review
* **Screen Purpose:** Long-term motivation through data visualization.
* **Key Components:**
  * **GitHub-style Consistency Heatmap:**
    * 52-week or 30-day matrix grid with color intensity based on completion score (gray = skipped, light green/orange = partial, vibrant = full completion).
  * **Volume Metrics:**
    * Total Reps logged this month (e.g., `2,450 Reps`).
    * Shalat on-time adherence rate (e.g., `96%`).
    * Total Quran pages completed (e.g., `45 Pages / 2.2 Juz`).
  * **Weekly Comparison Chart:** Grouped bar chart comparing this week vs. last week's workout volume and prayer consistency.

---

## 4. Ready-to-Use Google Stitch Prompt

```text
Design a cohesive, premium dark-mode mobile UI for a daily routine and habit tracking application called "Ruh & Raga" (combining Islamic spiritual worship and calisthenics/workout logging). 

Style & Aesthetic:
- Dark slate background (#0F172A) with elevated card surfaces (#1E293B).
- Accent Colors: Emerald Green (#10B981) for Ibadah/prayer elements, Vibrant Orange (#F97316) for workout/fitness elements.
- Clean typography with high legibility, rounded card corners (16px), subtle inner strokes, and thumb-friendly bottom action controls.

Please generate the following 4 core screens:
1. Home Dashboard: Displaying daily streak counter, dual completion rings (Spiritual vs Physical), next upcoming prayer countdown, and a quick-log workout card.
2. Ibadah Checklist Screen: Clean segmented checklist for 5 Daily Prayers (with Jama'ah badge toggle), Sunnah prayers, and a Quran reading (Tilawah) page progress slider.
3. Workout Counter & Timer Screen: Focused active exercise mode showing large digital rep counters for bodyweight movements (Push-up/Pull-up/Squat) with quick-add buttons (+5, +10), set history list, and an embedded rest-interval countdown timer.
4. Consistency & Progress Screen: Showing a full 30-day GitHub-style habit contribution heatmap, monthly volume metrics, and bar charts comparing weekly physical and spiritual output.
```