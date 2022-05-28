# flow for [Assistant](https://github.com/char16t/assistant)

`flow` allows you to automatically reschedule events in the flow depending on the constraints at different time
intervals. An alternative experimental implementation of the [Assistant](https://github.com/char16t/assistant) core.

## Usage

Suppose there is an ordered sequence of events with the date and time for which they are scheduled. At the same time,
there are restrictions:

* No more than 2 events per day
* No more than 4 events per month
* It is preferable to keep the events in the original order
* If an event cannot be scheduled on the specified date, it must be scheduled as soon as possible

_(Supported restrictions is `PerDay`, `PerWeek`, `PerMonth`, `PerYear`)_

It is necessary to reorganize the sequence so that the sequence meets the requirements.

Defining the sequence:

```scala
val original = Seq(
  Event(name = "CA1", isPin = true,  due = LocalDateTime.of(2022, 1, 1, 0, 1)),
  Event(name = "CA2", isPin = true,  due = LocalDateTime.of(2022, 1, 1, 1, 1)),
  Event(name = "CA3", isPin = true,  due = LocalDateTime.of(2022, 1, 1, 2, 1)),
  Event(name = "CB1", isPin = false, due = LocalDateTime.of(2022, 1, 2, 0, 1)),
  Event(name = "CB2", isPin = false, due = LocalDateTime.of(2022, 1, 2, 1, 1)),
  Event(name = "CB3", isPin = true,  due = LocalDateTime.of(2022, 1, 2, 2, 1)),
  Event(name = "CC1", isPin = false, due = LocalDateTime.of(2022, 1, 3, 0, 1)),
  Event(name = "CC2", isPin = false, due = LocalDateTime.of(2022, 1, 3, 1, 1)),
  Event(name = "CC3", isPin = false, due = LocalDateTime.of(2022, 1, 3, 2, 1)),
  Event(name = "CC4", isPin = false, due = LocalDateTime.of(2022, 3, 3, 2, 1)),
)
```

Apply:

```scala
val stage1 = Flow.flow(original)(PerDay(2))
val stage2 = Flow.flow(stage1)(PerMonth(4))
```

Get result:

```scala
stage2.toString
//List(
//  Event(CA1,true,2022-01-01T00:01), 
//  Event(CA2,true,2022-01-01T01:01), 
//  Event(CA3,true,2022-01-01T02:01), 
//  Event(CB3,true,2022-01-02T02:01), 
//  Event(CB1,false,2022-02-02T00:01),
//  Event(CC1,false,2022-02-03T00:01),
//  Event(CB2,false,2022-02-03T01:01),
//  Event(CC2,false,2022-02-04T01:01),
//  Event(CC3,false,2022-03-04T02:01),
//  Event(CC4,false,2022-03-03T02:01)
//)
```
