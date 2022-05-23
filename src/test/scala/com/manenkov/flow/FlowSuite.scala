package com.manenkov.flow

import org.scalatest.funsuite.AnyFunSuite

import java.time.{LocalDate, LocalDateTime}

class FlowSuite extends AnyFunSuite {
  test("Empty test") {
    assert(2 * 2 == 4)
  }

  test("Test2") {
    val c = Seq(
      T("CA1", isPin = true, LocalDateTime.of(2022, 1, 1, 0, 1)),
      T("CA2", isPin = true, LocalDateTime.of(2022, 1, 1, 1, 1)),
      T("CA3", isPin = true, LocalDateTime.of(2022, 1, 1, 2, 1)),
      T("CB1", isPin = false, LocalDateTime.of(2022, 1, 2, 0, 1)),
      T("CB2", isPin = false, LocalDateTime.of(2022, 1, 2, 1, 1)),
      T("CB3", isPin = true, LocalDateTime.of(2022, 1, 2, 2, 1)),
      T("CC1", isPin = false, LocalDateTime.of(2022, 1, 3, 0, 1)),
      T("CC2", isPin = false, LocalDateTime.of(2022, 1, 3, 1, 1)),
      T("CC3", isPin = false, LocalDateTime.of(2022, 1, 3, 2, 1)),
      T("CC4", isPin = false, LocalDateTime.of(2022, 3, 3, 2, 1)),
    )

    object PerDay {
      val limit = 2
      def next: LocalDate => LocalDate = _.plusDays(1)
      def keyF: T => LocalDate = _.due.toLocalDate
      def updF: (T, LocalDate) => T = (t, key) => t.copy(due = key.atTime(t.due.toLocalTime))
    }

    object PerMonth {
      val limit = 4
      def keyF: T => ((Int, Int)) =
        t => (t.due.getYear, t.due.getMonth.getValue)
      def next: ((Int, Int)) => (Int, Int) =
        ym => {
          val (year, month) = (ym._1, ym._2)
          if (month == 12) Tuple2(year + 1, 1) else Tuple2(year, month + 1)
        }
      def updF: (T, (Int, Int)) => T =
        (t, ym) => t.copy(due = t.due.withYear(ym._1).withMonth(ym._2))
    }

    val stage1 = Flow.flow(c, PerDay.limit)(PerDay.keyF, PerDay.next, PerDay.updF)
    val stage2 = Flow.flow(stage1, PerMonth.limit)(PerMonth.keyF, PerMonth.next, PerMonth.updF)
  }
}
