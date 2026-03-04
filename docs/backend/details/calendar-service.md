# Calendar Service Module - Detail

**Path:** `/backend/atlas-project-backend/atlas-project-backend-calendar-service/`  
**Module:** [Backend Index](../INDEX.md)  
**Last Updated:** 2026-03-03

## Purpose

The Calendar Service module provides work calendar functionality for calculating working days, handling holidays, and scheduling tasks around non-working days.

## Directory Structure

```
src/main/kotlin/com/khan366kos/atlas/project/backend/calendar/
└── service/
    └── CacheCalendarProvider.kt
```

---

## CacheCalendarProvider

### Purpose

Implements the work calendar service with caching capabilities. Provides methods for determining working days and calculating dates.

### File: CacheCalendarProvider.kt

---

### Key Responsibilities

1. **Working Day Detection:** Determine if a date is a working day
2. **Date Calculation:** Add working days to a date (skip weekends/holidays)
3. **Current/Next Working Day:** Find the next working day from a given date
4. **Calendar Configuration:** Store holidays and working day patterns

---

### Interface/Class Definition

```kotlin
class CacheCalendarProvider {
    private val holidays: Set<LocalDate>
    private val workingDays: Set<Int>  // 1=Monday, 7=Sunday
    
    // ... initialization
}
```

---

### Key Methods

#### isWorkingDay(date: LocalDate): Boolean

**Purpose:** Check if a date is a working day.

**Logic:**
- Returns `false` if date is in holidays
- Returns `false` if date is weekend (not in workingDays)
- Returns `true` otherwise

```kotlin
fun isWorkingDay(date: LocalDate): Boolean {
    if (date in holidays) return false
    if (date.dayOfWeek.value !in workingDays) return false
    return true
}
```

---

#### currentOrNextWorkingDay(date: LocalDate): LocalDate

**Purpose:** Return the date if it's a working day, or the next working day.

**Logic:**
- If `isWorkingDay(date)` → return date
- Otherwise → iterate forward until a working day is found

```kotlin
fun currentOrNextWorkingDay(date: LocalDate): LocalDate {
    var current = date
    while (!isWorkingDay(current)) {
        current = current.plusDays(1)
    }
    return current
}
```

---

#### addWorkingDays(date: LocalDate, days: Duration): LocalDate

**Purpose:** Add a number of working days to a date.

**Logic:**
- Start from the given date
- For each working day to add, move forward skipping non-working days

```kotlin
fun addWorkingDays(date: LocalDate, days: Duration): LocalDate {
    var current = currentOrNextWorkingDay(date)
    var daysToAdd = days.asInt()
    
    while (daysToAdd > 0) {
        current = current.plusDays(1)
        if (isWorkingDay(current)) {
            daysToAdd--
        }
    }
    return current
}
```

---

#### getWorkingDaysInRange(start: LocalDate, end: LocalDate): Int

**Purpose:** Count working days between two dates.

**Logic:**
- Iterate from start to end
- Count only working days

```kotlin
fun getWorkingDaysInRange(start: LocalDate, end: LocalDate): Int {
    var count = 0
    var current = start
    while (current <= end) {
        if (isWorkingDay(current)) count++
        current = current.plusDays(1)
    }
    return count
}
```

---

### Configuration

#### Holidays

Stored as a `Set<LocalDate>` for O(1) lookup.

```kotlin
val holidays = setOf(
    LocalDate(2026, 1, 1),   // New Year's Day
    LocalDate(2026, 12, 25), // Christmas
    // ... more holidays
)
```

#### Working Days

Stored as a `Set<Int>` where 1=Monday through 7=Sunday.

```kotlin
val workingDays = setOf(1, 2, 3, 4, 5)  // Monday-Friday
```

---

### Dependencies

**Imports:**
- `kotlinx.datetime.LocalDate`
- `kotlinx.datetime.DayOfWeek`

**Imported by:**
- Ktor App module (via `AppConfig`)
- Domain models (for schedule calculations)

---

## Usage in Schedule Calculations

### Task Scheduling

When a task is created or rescheduled:

```kotlin
val startDate = calendar.currentOrNextWorkingDay(today)
val endDate = calendar.addWorkingDays(startDate, task.duration)
```

### Dependency Recalculation

When a predecessor task's end date changes:

```kotlin
val successorStart = calendar.addWorkingDays(
    predecessorEnd, 
    Duration(lagDays + 1)
)
```

---

## Integration with Ktor App

### Configuration in AppConfig

```kotlin
class AppConfig(environment: ApplicationEnvironment) {
    val calendarService: CalendarService = CacheCalendarProvider()
    // ...
}
```

### Usage in Routing

```kotlin
get("/work-calendar") {
    val timelineCalendar = config.calendarService.current()
    call.respond(timelineCalendar.toTransport())
}
```

---

## Related Files

- [Ktor App Module](./ktor-app.md)
- [Common Module](./common.md)
- [Transport Module](./transport.md)
