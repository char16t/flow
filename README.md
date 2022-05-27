# flow for [Assistant](https://github.com/char16t/assistant)

`flow` allows you to automatically reschedule events in the flow depending on the constraints at different time intervals. An alternative experimental implementation of the [Assistant](https://github.com/char16t/assistant) core.

## Usage

Suppose there is an ordered sequence of events with the date and time for which they are scheduled. At the same time, there are restrictions:

* No more than 2 events per day
* No more than 4 events per month

It is necessary to reorganize the sequence so that the sequence meets the requirements.

Defining the sequence:

```scala
val original = Seq(
    T(name = "CA1", isPin = true,  due = LocalDateTime.of(2022, 1, 1, 0, 1)),
    T(name = "CA2", isPin = true,  due = LocalDateTime.of(2022, 1, 1, 1, 1)),
    T(name = "CA3", isPin = true,  due = LocalDateTime.of(2022, 1, 1, 2, 1)),
    T(name = "CB1", isPin = false, due = LocalDateTime.of(2022, 1, 2, 0, 1)),
    T(name = "CB2", isPin = false, due = LocalDateTime.of(2022, 1, 2, 1, 1)),
    T(name = "CB3", isPin = true,  due = LocalDateTime.of(2022, 1, 2, 2, 1)),
    T(name = "CC1", isPin = false, due = LocalDateTime.of(2022, 1, 3, 0, 1)),
    T(name = "CC2", isPin = false, due = LocalDateTime.of(2022, 1, 3, 1, 1)),
    T(name = "CC3", isPin = false, due = LocalDateTime.of(2022, 1, 3, 2, 1)),
    T(name = "CC4", isPin = false, due = LocalDateTime.of(2022, 3, 3, 2, 1)),
)
```

Defining restrictions:

```scala
object PerDay {
  val limit = 2
  def next: LocalDate => LocalDate = _.plusDays(1)
  def keyF: T => LocalDate = _.due.toLocalDate
  def updF: (T, LocalDate) => T = (t, key) => t.copy(due = key.atTime(t.due.toLocalTime))
}
object PerMonth {
  val limit = 4
  def keyF: T => (Int, Int) =
    t => (t.due.getYear, t.due.getMonth.getValue)
  def next: ((Int, Int)) => (Int, Int) =
    ym => {
      val (year, month) = (ym._1, ym._2)
      if (month == 12) Tuple2(year + 1, 1) else Tuple2(year, month + 1)
    }
  def updF: (T, (Int, Int)) => T =
    (t, ym) => t.copy(due = t.due.withYear(ym._1).withMonth(ym._2))
}
```

Apply:

```scala
val stage1 = Flow.flow(c, PerDay.limit)(PerDay.keyF, PerDay.next, PerDay.updF)
val stage2 = Flow.flow(stage1, PerMonth.limit)(PerMonth.keyF, PerMonth.next, PerMonth.updF)
```

Get result (`stage2`):

```scala
Seq(
      T("CA1", isPin = true,  LocalDateTime.parse("2022-01-01T00:01")),
      T("CA2", isPin = true,  LocalDateTime.parse("2022-01-01T01:01")),
      T("CA3", isPin = true,  LocalDateTime.parse("2022-01-01T02:01")),
      T("CB3", isPin = true,  LocalDateTime.parse("2022-01-02T02:01")),
      T("CB1", isPin = false, LocalDateTime.parse("2022-03-02T00:01")),
      T("CB2", isPin = false, LocalDateTime.parse("2022-03-02T01:01")),
      T("CC1", isPin = false, LocalDateTime.parse("2022-03-03T00:01")),
      T("CC2", isPin = false, LocalDateTime.parse("2022-03-03T01:01")),
      T("CC3", isPin = false, LocalDateTime.parse("2022-04-03T02:01")),
      T("CC4", isPin = false, LocalDateTime.parse("2022-04-03T02:01")),
)
```
