package com.manenkov.flow

import java.time.{DayOfWeek, LocalDate}

class Flow(val config: Configuration) {
  def flow(tasks: Seq[Task]): Seq[Task] = {
    tasks.sortBy(_.due)
  }

  private def dayLimit(date: LocalDate): Long = {
    val optWeekdayLimit = date.getDayOfWeek match {
      case DayOfWeek.MONDAY => config.mondayLimit
      case DayOfWeek.TUESDAY => config.tuesdayLimit
      case DayOfWeek.WEDNESDAY => config.wednesdayLimit
      case DayOfWeek.THURSDAY => config.thursdayLimit
      case DayOfWeek.FRIDAY => config.fridayLimit
      case DayOfWeek.SATURDAY => config.saturdayLimit
      case DayOfWeek.SUNDAY => config.sundayLimit
    }
    config.dailyLimit.getOrElse(optWeekdayLimit.getOrElse(Long.MaxValue))
  }
}

object Flow {
  def apply() = new Flow(Configuration())
  def apply(config: Configuration) = new Flow(config)
}
