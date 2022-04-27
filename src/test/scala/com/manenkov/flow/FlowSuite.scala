package com.manenkov.flow

import org.scalatest.funsuite.AnyFunSuite

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class FlowSuite extends AnyFunSuite {
  test("Simple sort by due date") {
    val now = LocalDateTime.now()
    val input = Seq(
      Task("A", now.minus(1, ChronoUnit.HOURS)),
      Task("B", now.minus(2, ChronoUnit.HOURS)),
    )
    val actual = Flow().flow(input)
    val expected = Seq(
      Task("B", now.minus(2, ChronoUnit.HOURS)),
      Task("A", now.minus(1, ChronoUnit.HOURS)),
    )
    assert(actual == expected)
  }
}
