package com.manenkov.flow

import org.scalatest.funsuite.AnyFunSuite

import java.time.{LocalDate, LocalDateTime}

class FlowSuite extends AnyFunSuite {
  test("No split") {
    val original = Seq(
      Event("CA1", isPin = true,  LocalDateTime.of(2022, 1, 1, 0, 1)),
      Event("CA2", isPin = true,  LocalDateTime.of(2022, 1, 1, 1, 1)),
      Event("CA3", isPin = true,  LocalDateTime.of(2022, 1, 1, 2, 1)),
    )

    val perDay = PerDay(8)
    val actual = Flow.flow(original)(perDay.limit, perDay.keyF, perDay.next, perDay.updF)

    val expected = List(
      Event("CA1",isPin = true, LocalDateTime.parse("2022-01-01T00:01")),
      Event("CA2",isPin = true, LocalDateTime.parse("2022-01-01T01:01")),
      Event("CA3",isPin = true, LocalDateTime.parse("2022-01-01T02:01")),
    )
    assertResult(expected.length)(actual.length)
    expected.zip(actual).foreach(res => assertResult(res._1)(res._2))
  }

  test("Per day") {
    val perDay = PerDay(2)
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
    val actual = Flow.flow(original)(perDay.limit, perDay.keyF, perDay.next, perDay.updF)
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

  test("Per week") {
    val perWeek = PerWeek(2)
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
    val actual = Flow.flow(original)(perWeek.limit, perWeek.keyF, perWeek.next, perWeek.updF)
    val expected = Seq(
      Event("CA1", isPin = true,  LocalDateTime.parse("2022-01-01T00:01")),
      Event("CA2", isPin = true,  LocalDateTime.parse("2022-01-01T01:01")),
      Event("CA3", isPin = true,  LocalDateTime.parse("2022-01-01T02:01")),
      Event("CB3", isPin = true,  LocalDateTime.parse("2022-01-02T02:01")),
      Event("CB1", isPin = false, LocalDateTime.parse("2022-01-09T00:01")),
      Event("CB2", isPin = false, LocalDateTime.parse("2022-01-09T01:01")),
      Event("CC1", isPin = false, LocalDateTime.parse("2022-02-28T00:01")),
      Event("CC2", isPin = false, LocalDateTime.parse("2022-02-28T01:01")),
      Event("CC3", isPin = false, LocalDateTime.parse("2022-03-07T02:01")),
      Event("CC4", isPin = false, LocalDateTime.parse("2022-03-10T02:01"))
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

    val perDay = PerDay(2)
    val perMonth = PerMonth(4)

    val stage1 = Flow.flow(c)(perMonth.limit, perMonth.keyF, perMonth.next, perMonth.updF)
    val stage2 = Flow.flow(stage1)(perDay.limit, perDay.keyF, perDay.next, perDay.updF)

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
