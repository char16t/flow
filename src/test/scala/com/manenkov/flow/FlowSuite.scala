package com.manenkov.flow

import com.manenkov.flow.Flow.{ChangeDue, ChangeOrder}
import org.scalatest.funsuite.AnyFunSuite

import java.time.LocalDateTime
import java.util.UUID

class FlowSuite extends AnyFunSuite {
  test("No split") {
    val original = Seq(
      Event(id = "1", name = "CA1", isPin = true, due = LocalDateTime.of(2022, 1, 1, 0, 1)),
      Event(id = "2", name = "CA2", isPin = true, due = LocalDateTime.of(2022, 1, 1, 1, 1)),
      Event(id = "3", name = "CA3", isPin = true, due = LocalDateTime.of(2022, 1, 1, 2, 1)),
    )

    val actual = Flow.flow(original)(PerDay(8))

    val expected = List(
      Event(id = "1", name = "CA1", isPin = true, due = LocalDateTime.parse("2022-01-01T00:01")),
      Event(id = "2", name = "CA2", isPin = true, due = LocalDateTime.parse("2022-01-01T01:01")),
      Event(id = "3", name = "CA3", isPin = true, due = LocalDateTime.parse("2022-01-01T02:01")),
    )
    assertResult(expected.length)(actual.length)
    expected.zip(actual).foreach(res => assertResult(res._1)(res._2))
  }

  test("Per day") {
    val original = Seq(
      Event(id = "1", name = "CA1", isPin = true, due = LocalDateTime.of(2022, 1, 1, 0, 1)),
      Event(id = "2", name = "CA2", isPin = true, due = LocalDateTime.of(2022, 1, 1, 1, 1)),
      Event(id = "3", name = "CA3", isPin = true, due = LocalDateTime.of(2022, 1, 1, 2, 1)),
      Event(id = "4", name = "CB1", due = LocalDateTime.of(2022, 1, 2, 0, 1)),
      Event(id = "5", name = "CB2", due = LocalDateTime.of(2022, 1, 2, 1, 1)),
      Event(id = "6", name = "CB3", isPin = true, due = LocalDateTime.of(2022, 1, 2, 2, 1)),
      Event(id = "7", name = "CC1", due = LocalDateTime.of(2022, 1, 3, 0, 1)),
      Event(id = "8", name = "CC2", due = LocalDateTime.of(2022, 1, 3, 1, 1)),
      Event(id = "9", name = "CC3", due = LocalDateTime.of(2022, 1, 3, 2, 1)),
      Event(id = "10", name = "CC4", due = LocalDateTime.of(2022, 3, 3, 2, 1)),
    )
    val actual = Flow.flow(original)(PerDay(2))
    val expected = Seq(
      Event(id = "1", name = "CA1", isPin = true, due = LocalDateTime.parse("2022-01-01T00:01")),
      Event(id = "2", name = "CA2", isPin = true, due = LocalDateTime.parse("2022-01-01T01:01")),
      Event(id = "3", name = "CA3", isPin = true, due = LocalDateTime.parse("2022-01-01T02:01")),
      Event(id = "4", name = "CB1", due = LocalDateTime.parse("2022-01-02T00:01")),
      Event(id = "6", name = "CB3", isPin = true, due = LocalDateTime.parse("2022-01-02T02:01")),
      Event(id = "5", name = "CB2", due = LocalDateTime.parse("2022-01-03T01:01")),
      Event(id = "7", name = "CC1", due = LocalDateTime.parse("2022-01-03T00:01")),
      Event(id = "8", name = "CC2", due = LocalDateTime.parse("2022-01-04T01:01")),
      Event(id = "9", name = "CC3", due = LocalDateTime.parse("2022-01-04T02:01")),
      Event(id = "10", name = "CC4", due = LocalDateTime.parse("2022-03-03T02:01")),
    )
    assertResult(expected.length)(actual.length)
    expected.zip(actual).foreach(res => assertResult(res._1)(res._2))
  }

  test("Per week") {
    val original = Seq(
      Event(id = "CA1", name = "CA1", isPin = true, due = LocalDateTime.of(2022, 1, 1, 0, 1)),
      Event(id = "CA2", name = "CA2", isPin = true, due = LocalDateTime.of(2022, 1, 1, 1, 1)),
      Event(id = "CA3", name = "CA3", isPin = true, due = LocalDateTime.of(2022, 1, 1, 2, 1)),
      Event(id = "CB1", name = "CB1", due = LocalDateTime.of(2022, 1, 2, 0, 1)),
      Event(id = "CB2", name = "CB2", due = LocalDateTime.of(2022, 1, 2, 1, 1)),
      Event(id = "CB3", name = "CB3", isPin = true, due = LocalDateTime.of(2022, 1, 2, 2, 1)),
      Event(id = "CC1", name = "CC1", due = LocalDateTime.of(2022, 1, 3, 0, 1)),
      Event(id = "CC2", name = "CC2", due = LocalDateTime.of(2022, 1, 3, 1, 1)),
      Event(id = "CC3", name = "CC3", due = LocalDateTime.of(2022, 1, 3, 2, 1)),
      Event(id = "CC4", name = "CC4", due = LocalDateTime.of(2022, 3, 3, 2, 1)),
    )
    val actual = Flow.flow(original)(PerWeek(2))
    val expected = Seq(
      Event(id = "CA1", name = "CA1", isPin = true, due = LocalDateTime.parse("2022-01-01T00:01")),
      Event(id = "CA2", name = "CA2", isPin = true, due = LocalDateTime.parse("2022-01-01T01:01")),
      Event(id = "CA3", name = "CA3", isPin = true, due = LocalDateTime.parse("2022-01-01T02:01")),
      Event(id = "CB3", name = "CB3", isPin = true, due = LocalDateTime.parse("2022-01-02T02:01")),
      Event(id = "CB1", name = "CB1", due = LocalDateTime.parse("2022-01-09T00:01")),
      Event(id = "CB2", name = "CB2", due = LocalDateTime.parse("2022-01-09T01:01")),
      Event(id = "CC1", name = "CC1", due = LocalDateTime.parse("2022-01-10T00:01")),
      Event(id = "CC2", name = "CC2", due = LocalDateTime.parse("2022-01-10T01:01")),
      Event(id = "CC3", name = "CC3", due = LocalDateTime.parse("2022-01-17T02:01")),
      Event(id = "CC4", name = "CC4", due = LocalDateTime.parse("2022-03-03T02:01")),
    )
    assertResult(expected.length)(actual.length)
    expected.zip(actual).foreach(res => assertResult(res._1)(res._2))
  }

  test("Per day then per month") {
    val c = Seq(
      Event(id = "CA1", name = "CA1", isPin = true, due = LocalDateTime.of(2022, 1, 1, 0, 1)),
      Event(id = "CA2", name = "CA2", isPin = true, due = LocalDateTime.of(2022, 1, 1, 1, 1)),
      Event(id = "CA3", name = "CA3", isPin = true, due = LocalDateTime.of(2022, 1, 1, 2, 1)),
      Event(id = "CB1", name = "CB1", due = LocalDateTime.of(2022, 1, 2, 0, 1)),
      Event(id = "CB2", name = "CB2", due = LocalDateTime.of(2022, 1, 2, 1, 1)),
      Event(id = "CB3", name = "CB3", isPin = true, due = LocalDateTime.of(2022, 1, 2, 2, 1)),
      Event(id = "CC1", name = "CC1", due = LocalDateTime.of(2022, 1, 3, 0, 1)),
      Event(id = "CC2", name = "CC2", due = LocalDateTime.of(2022, 1, 3, 1, 1)),
      Event(id = "CC3", name = "CC3", due = LocalDateTime.of(2022, 1, 3, 2, 1)),
      Event(id = "CC4", name = "CC4", due = LocalDateTime.of(2022, 3, 3, 2, 1)),
    )

    val stage1 = Flow.flow(c)(PerDay(2))
    val stage2 = Flow.flow(stage1)(PerMonth(4))

    val expected = Seq(
      Event(id = "CA1", name = "CA1", isPin = true, due = LocalDateTime.parse("2022-01-01T00:01")),
      Event(id = "CA2", name = "CA2", isPin = true, due = LocalDateTime.parse("2022-01-01T01:01")),
      Event(id = "CA3", name = "CA3", isPin = true, due = LocalDateTime.parse("2022-01-01T02:01")),
      Event(id = "CB3", name = "CB3", isPin = true, due = LocalDateTime.parse("2022-01-02T02:01")),
      Event(id = "CB1", name = "CB1", due = LocalDateTime.parse("2022-02-02T00:01")),
      Event(id = "CC1", name = "CC1", due = LocalDateTime.parse("2022-02-03T00:01")),
      Event(id = "CB2", name = "CB2", due = LocalDateTime.parse("2022-02-03T01:01")),
      Event(id = "CC2", name = "CC2", due = LocalDateTime.parse("2022-02-04T01:01")),
      Event(id = "CC3", name = "CC3", due = LocalDateTime.parse("2022-03-04T02:01")),
      Event(id = "CC4", name = "CC4", due = LocalDateTime.parse("2022-03-03T02:01")),
    )
    assertResult(expected.length)(stage2.length)
    expected.zip(stage2).foreach(pair => assertResult(pair._1)(pair._2))
  }

  test("Per year") {
    val original = Seq(
      Event(id = "CA1", name = "CA1", due = LocalDateTime.of(2022, 1, 1, 0, 1)),
      Event(id = "CA2", name = "CA2", due = LocalDateTime.of(2022, 1, 1, 1, 1)),
      Event(id = "CA3", name = "CA3", due = LocalDateTime.of(2022, 1, 1, 2, 1)),
    )

    val actual = Flow.flow(original)(PerYear(2))

    val expected = List(
      Event(id = "CA1", name = "CA1", due = LocalDateTime.parse("2022-01-01T00:01")),
      Event(id = "CA2", name = "CA2", due = LocalDateTime.parse("2022-01-01T01:01")),
      Event(id = "CA3", name = "CA3", due = LocalDateTime.parse("2023-01-01T02:01")),
    )
    assertResult(expected.length)(actual.length)
    expected.zip(actual).foreach(res => assertResult(res._1)(res._2))
  }

  test("diff") {
    val from = Seq(
      Event(id = UUID.fromString("0-0-0-1-1").toString, name = "CA1", isPin = true, due = LocalDateTime.of(2022, 1, 1, 0, 1)),
      Event(order = 1, id = UUID.fromString("0-0-0-1-2").toString, name = "CA2", isPin = true, due = LocalDateTime.of(2022, 1, 1, 1, 1)),
      Event(order = 2, id = UUID.fromString("0-0-0-1-3").toString, name = "CA3", isPin = true, due = LocalDateTime.of(2022, 1, 1, 2, 1)),
      Event(order = 3, id = UUID.fromString("0-0-0-2-1").toString, name = "CB1", due = LocalDateTime.of(2022, 1, 2, 0, 1)),
      Event(order = 4, id = UUID.fromString("0-0-0-2-2").toString, name = "CB2", due = LocalDateTime.of(2022, 1, 2, 1, 1)),
      Event(order = 5, id = UUID.fromString("0-0-0-2-3").toString, name = "CB3", isPin = true, due = LocalDateTime.of(2022, 1, 2, 2, 1)),
      Event(order = 6, id = UUID.fromString("0-0-0-3-1").toString, name = "CC1", due = LocalDateTime.of(2022, 1, 3, 0, 1)),
      Event(order = 7, id = UUID.fromString("0-0-0-3-2").toString, name = "CC2", due = LocalDateTime.of(2022, 1, 3, 1, 1)),
      Event(order = 8, id = UUID.fromString("0-0-0-3-3").toString, name = "CC3", due = LocalDateTime.of(2022, 1, 3, 2, 1)),
      Event(order = 9, id = UUID.fromString("0-0-0-3-4").toString, name = "CC4", due = LocalDateTime.of(2022, 3, 3, 2, 1)),
    )
    val to = Seq(
      Event(id = UUID.fromString("0-0-0-1-1").toString, name = "CA1", isPin = true, due = LocalDateTime.parse("2022-01-01T00:01")),
      Event(order = 1, id = UUID.fromString("0-0-0-1-2").toString, name = "CA2", isPin = true, due = LocalDateTime.parse("2022-01-01T01:01")),
      Event(order = 2, id = UUID.fromString("0-0-0-1-3").toString, name = "CA3", isPin = true, due = LocalDateTime.parse("2022-01-01T02:01")),
      Event(order = 3, id = UUID.fromString("0-0-0-2-1").toString, name = "CB1", due = LocalDateTime.parse("2022-01-02T00:01")),
      Event(order = 4, id = UUID.fromString("0-0-0-2-3").toString, name = "CB3", isPin = true, due = LocalDateTime.parse("2022-01-02T02:01")),
      Event(order = 5, id = UUID.fromString("0-0-0-2-2").toString, name = "CB2", due = LocalDateTime.parse("2022-01-03T01:01")),
      Event(order = 6, id = UUID.fromString("0-0-0-3-1").toString, name = "CC1", due = LocalDateTime.parse("2022-01-03T00:01")),
      Event(order = 7, id = UUID.fromString("0-0-0-3-2").toString, name = "CC2", due = LocalDateTime.parse("2022-01-04T01:01")),
      Event(order = 8, id = UUID.fromString("0-0-0-3-3").toString, name = "CC3", due = LocalDateTime.parse("2022-01-04T02:01")),
      Event(order = 9, id = UUID.fromString("0-0-0-3-4").toString, name = "CC4", due = LocalDateTime.parse("2022-03-03T02:01")),
    )
    val actual = Flow.diff(from, to)
    val expected = Seq(
      ChangeDue(
        UUID.fromString("00000000-0000-0000-0003-000000000003").toString,
        LocalDateTime.parse("2022-01-03T02:01"),
        LocalDateTime.parse("2022-01-04T02:01")
      ),
      ChangeOrder(UUID.fromString("00000000-0000-0000-0002-000000000002").toString, 4, 5),
      ChangeOrder(UUID.fromString("00000000-0000-0000-0002-000000000003").toString, 5, 4),
      ChangeDue(
        UUID.fromString("00000000-0000-0000-0003-000000000002").toString,
        LocalDateTime.parse("2022-01-03T01:01"),
        LocalDateTime.parse("2022-01-04T01:01")
      )
    )
    assertResult(expected.length)(actual.length)
    expected.zip(actual).foreach(res => assertResult(res._1)(res._2))
  }
}
