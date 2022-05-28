package com.manenkov.flow

import org.scalatest.funsuite.AnyFunSuite

import java.time.{LocalDate, LocalDateTime}

class FlowSuite extends AnyFunSuite {
  test("No split") {
    object PerDay {
      val limit = 8
      def next: LocalDate => LocalDate = _.plusDays(1)
      def keyF: Event => LocalDate = _.due.toLocalDate
      def updF: (Event, LocalDate) => Event = (t, key) => t.copy(due = key.atTime(t.due.toLocalTime))
    }
    val original = Seq(
      Event("CA1", isPin = true,  LocalDateTime.of(2022, 1, 1, 0, 1)),
      Event("CA2", isPin = true,  LocalDateTime.of(2022, 1, 1, 1, 1)),
      Event("CA3", isPin = true,  LocalDateTime.of(2022, 1, 1, 2, 1)),
    )
    val actual = Flow.flow(original, PerDay.limit)(PerDay.keyF, PerDay.next, PerDay.updF)
    val expected = List(
      Event("CA1",isPin = true, LocalDateTime.parse("2022-01-01T00:01")),
      Event("CA2",isPin = true, LocalDateTime.parse("2022-01-01T01:01")),
      Event("CA3",isPin = true, LocalDateTime.parse("2022-01-01T02:01")),
    )
    assertResult(expected.length)(actual.length)
    expected.zip(actual).foreach(res => assertResult(res._1)(res._2))
  }

  test("Per day") {
    object PerDay {
      val limit = 2
      def next: LocalDate => LocalDate = _.plusDays(1)
      def keyF: Event => LocalDate = _.due.toLocalDate
      def updF: (Event, LocalDate) => Event = (t, key) => t.copy(due = key.atTime(t.due.toLocalTime))
    }
    val original = Seq(
      Event("CA1", isPin = true,  LocalDateTime.of(2022, 1, 1, 0, 1)),
      Event("CA2", isPin = true,  LocalDateTime.of(2022, 1, 1, 1, 1)),
      Event("CA3", isPin = true,  LocalDateTime.of(2022, 1, 1, 2, 1)),
      Event("CB1", isPin = false, LocalDateTime.of(2022, 1, 2, 0, 1)),
      Event("CB2", isPin = false, LocalDateTime.of(2022, 1, 2, 1, 1)),
      Event("CB3", isPin = true,  LocalDateTime.of(2022, 1, 2, 2, 1)),
      Event("CC1", isPin = false, LocalDateTime.of(2022, 1, 3, 0, 1)),
      Event("CC2", isPin = false, LocalDateTime.of(2022, 1, 3, 1, 1)),
      Event("CC3", isPin = false, LocalDateTime.of(2022, 1, 3, 2, 1)),
      Event("CC4", isPin = false, LocalDateTime.of(2022, 3, 3, 2, 1)),
    )
    val actual = Flow.flow(original, PerDay.limit)(PerDay.keyF, PerDay.next, PerDay.updF)
    val expected = Seq(
      Event("CA1",isPin = true,  LocalDateTime.parse("2022-01-01T00:01")),
      Event("CA2",isPin = true,  LocalDateTime.parse("2022-01-01T01:01")),
      Event("CA3",isPin = true,  LocalDateTime.parse("2022-01-01T02:01")),
      Event("CB1",isPin = false, LocalDateTime.parse("2022-01-02T00:01")),
      Event("CB3",isPin = true,  LocalDateTime.parse("2022-01-02T02:01")),
      Event("CB2",isPin = false, LocalDateTime.parse("2022-01-03T01:01")),
      Event("CC1",isPin = false, LocalDateTime.parse("2022-01-03T00:01")),
      Event("CC2",isPin = false, LocalDateTime.parse("2022-03-03T01:01")),
      Event("CC3",isPin = false, LocalDateTime.parse("2022-03-03T02:01")),
      Event("CC4",isPin = false, LocalDateTime.parse("2022-03-04T02:01")),
    )
    assertResult(expected.length)(actual.length)
    expected.zip(actual).foreach(res => assertResult(res._1)(res._2))
  }

  test("Per day then per month") {
    val c = Seq(
      Event("CA1", isPin = true,  LocalDateTime.of(2022, 1, 1, 0, 1)),
      Event("CA2", isPin = true,  LocalDateTime.of(2022, 1, 1, 1, 1)),
      Event("CA3", isPin = true,  LocalDateTime.of(2022, 1, 1, 2, 1)),
      Event("CB1", isPin = false, LocalDateTime.of(2022, 1, 2, 0, 1)),
      Event("CB2", isPin = false, LocalDateTime.of(2022, 1, 2, 1, 1)),
      Event("CB3", isPin = true,  LocalDateTime.of(2022, 1, 2, 2, 1)),
      Event("CC1", isPin = false, LocalDateTime.of(2022, 1, 3, 0, 1)),
      Event("CC2", isPin = false, LocalDateTime.of(2022, 1, 3, 1, 1)),
      Event("CC3", isPin = false, LocalDateTime.of(2022, 1, 3, 2, 1)),
      Event("CC4", isPin = false, LocalDateTime.of(2022, 3, 3, 2, 1)),
    )

    object PerDay {
      val limit = 2
      def next: LocalDate => LocalDate = _.plusDays(1)
      def keyF: Event => LocalDate = _.due.toLocalDate
      def updF: (Event, LocalDate) => Event = (t, key) => t.copy(due = key.atTime(t.due.toLocalTime))
    }

    object PerMonth {
      val limit = 4
      def keyF: Event => (Int, Int) =
        t => (t.due.getYear, t.due.getMonth.getValue)
      def next: ((Int, Int)) => (Int, Int) =
        ym => {
          val (year, month) = (ym._1, ym._2)
          if (month == 12) Tuple2(year + 1, 1) else Tuple2(year, month + 1)
        }
      def updF: (Event, (Int, Int)) => Event =
        (t, ym) => t.copy(due = t.due.withYear(ym._1).withMonth(ym._2))
    }

    val stage1 = Flow.flow(c, PerMonth.limit)(PerMonth.keyF, PerMonth.next, PerMonth.updF)
    val stage2 = Flow.flow(stage1, PerDay.limit)(PerDay.keyF, PerDay.next, PerDay.updF)

    val expected = Seq(
      Event("CA1", isPin = true,  LocalDateTime.parse("2022-01-01T00:01")),
      Event("CA2", isPin = true,  LocalDateTime.parse("2022-01-01T01:01")),
      Event("CA3", isPin = true,  LocalDateTime.parse("2022-01-01T02:01")),
      Event("CB3", isPin = true,  LocalDateTime.parse("2022-01-02T02:01")),
      Event("CB1", isPin = false, LocalDateTime.parse("2022-03-02T00:01")),
      Event("CB2", isPin = false, LocalDateTime.parse("2022-03-02T01:01")),
      Event("CC1", isPin = false, LocalDateTime.parse("2022-03-03T00:01")),
      Event("CC2", isPin = false, LocalDateTime.parse("2022-03-03T01:01")),
      Event("CC3", isPin = false, LocalDateTime.parse("2022-04-03T02:01")),
      Event("CC4", isPin = false, LocalDateTime.parse("2022-04-03T02:01")),
    )
    assertResult(expected.length)(stage2.length)
    expected.zip(stage2).foreach(pair => assertResult(pair._1)(pair._2))
  }
}
