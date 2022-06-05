package com.manenkov.flow

import java.time.temporal.WeekFields
import java.time.{LocalDate, LocalDateTime}
import java.util.UUID
import scala.collection.mutable
import ScalaVersionCompat.LazyList

case class Event(
                  id: String = UUID.randomUUID.toString,
                  name: String,
                  isPin: Boolean = false,
                  due: LocalDateTime = LocalDateTime.now(),
                  order: Int = 0,
                )

trait Restriction[K] {
  val limit: Int

  def next: K => K

  def keyF: Event => K

  def keysRangeF: (K, K) => Seq[K]

  def updF: (Event, K) => Event
}

class PerDay(val limit: Int) extends Restriction[LocalDate] {
  def next: LocalDate => LocalDate = _.plusDays(1)

  def keyF: Event => LocalDate = _.due.toLocalDate

  def keysRangeF: (LocalDate, LocalDate) => Seq[LocalDate] =
    (fromDate, toDate) => next(fromDate).toEpochDay.until(toDate.toEpochDay).map(LocalDate.ofEpochDay)

  def updF: (Event, LocalDate) => Event = (t, key) => t.copy(due = key.atTime(t.due.toLocalTime))
}

object PerDay {
  def apply(limit: Int): PerDay = new PerDay(limit)
}

class PerMonth(val limit: Int) extends Restriction[(Int, Int)] {
  def keyF: Event => (Int, Int) =
    t => (t.due.getYear, t.due.getMonth.getValue)

  def keysRangeF: ((Int, Int), (Int, Int)) => Seq[(Int, Int)] =
    (from, to) => {
      LazyList.iterate(from)(next).takeWhile(_ != to).drop(1).toList
    }

  def next: ((Int, Int)) => (Int, Int) =
    ym => {
      val (year, month) = (ym._1, ym._2)
      if (month == 12) Tuple2(year + 1, 1) else Tuple2(year, month + 1)
    }

  def updF: (Event, (Int, Int)) => Event =
    (t, ym) => t.copy(due = t.due.withYear(ym._1).withMonth(ym._2))
}

object PerMonth {
  def apply(limit: Int): PerMonth = new PerMonth(limit)
}

class PerWeek(val limit: Int) extends Restriction[(Int, Int)] {
  def keyF: Event => (Int, Int) =
    event => (event.due.getYear, event.due.get(WeekFields.ISO.weekOfYear))

  def keysRangeF: ((Int, Int), (Int, Int)) => Seq[(Int, Int)] = (from, to) => {
    LazyList.iterate(from)(next).takeWhile(_ != to).drop(1).toList
  }

  def next: ((Int, Int)) => (Int, Int) =
    yw => {
      val (year, week) = (yw._1, yw._2)
      val lastWeek = LocalDate.of(year, 12, 31).get(WeekFields.ISO.weekOfYear)
      if (week == lastWeek) (year + 1, 0) else (year, week + 1)
    }

  def updF: (Event, (Int, Int)) => Event =
    (event, yw) => {
      val (year, week) = (yw._1, yw._2)
      event.copy(due = event.due
        .withYear(year)
        .`with`(WeekFields.ISO.weekOfYear, week)
        .`with`(WeekFields.ISO.dayOfWeek, event.due.getDayOfWeek.getValue))
    }
}

object PerWeek {
  def apply(limit: Int): PerWeek = new PerWeek(limit)
}

class PerYear(val limit: Int) extends Restriction[Int] {
  override def next: Int => Int = _ + 1

  override def keyF: Event => Int = _.due.getYear

  override def keysRangeF: (Int, Int) => Seq[Int] = (from, to) => (from + 1) until to

  override def updF: (Event, Int) => Event = (e, year) => e.copy(due = e.due.withYear(year))
}

object PerYear {
  def apply(limit: Int) = new PerYear(limit)
}

trait Change {
  def id: String
}

case class ChangeOrder(id: String, from: Int, to: Int) extends Change

case class ChangeDue(id: String, from: LocalDateTime, to: LocalDateTime) extends Change

object Flow {

  def diff(a: Seq[Event], b: Seq[Event]): Seq[Change] = {
    def toMap(a: Seq[Event]): Map[String, Event] = {
      a.foldLeft(Map[String, Event]())((hmap, evt) => {
        hmap.updated(evt.id, evt)
      })
    }

    val ha = toMap(a)
    val hb = toMap(b)

    val changes = ha.foldLeft(Seq[Change]())((changes, pair) => pair._2 match {
      case evt: Event if hb.contains(evt.id) && evt.order != hb(evt.id).order =>
        changes :+ ChangeOrder(
          id = evt.id,
          from = evt.order,
          to = hb(evt.id).order
        )

      case evt: Event if hb.contains(evt.id) && evt.due != hb(evt.id).due =>
        changes :+ ChangeDue(
          id = evt.id,
          from = evt.due,
          to = hb(evt.id).due
        )
      case _ => changes
    })
    changes
  }

  def flow[K](c: Seq[Event])(restriction: Restriction[K]): Seq[Event] = {
    if (c.isEmpty) {
      return c
    }
    val sortedOriginal = c.sortBy(_.due)
    val m = sortedOriginal.foldLeft((mutable.LinkedHashMap[K, Seq[Event]](), restriction.keyF(sortedOriginal.head)))((acc, t) => {
      val map = acc._1
      val prevKey = acc._2
      val currentKey = restriction.keyF(t)

      val rangeOfKeys = restriction.keysRangeF(prevKey, currentKey)

      map ++= rangeOfKeys.map((_, Seq[Event]()))

      map.put(currentKey, map.getOrElse(currentKey, Seq[Event]()) :+ t)
      (map, currentKey)
    })._1
    val keys = m.keys.toSeq
    val paired = pairs(m.values.toSeq)
    val vals =
      if (paired.nonEmpty)
        f2(paired, restriction.limit)
      else
        f2(Seq((m.values.toSeq.head, Nil)), restriction.limit)

    val rest = vals.drop(keys.length)

    val updated = keys.zip(vals.take(keys.length)) ++ rest.foldLeft((restriction.next(keys.last), Seq[K]()))((acc, _) => {
      val date = acc._1
      val res = acc._2
      (restriction.next(date), res :+ date)
    })._2.zip(rest)

    updated.flatMap(pair => {
      val key = pair._1
      val value = pair._2
      value.map(restriction.updF(_, key))
    }).zipWithIndex.map(eventWithIndex => eventWithIndex._1.copy(order = eventWithIndex._2))
  }

  private def pairs[A](a: Seq[A]): Seq[(A, A)] =
    (0 until a.length - 1).foldLeft(Seq[(A, A)]())((acc, idx) => {
      acc :+ Tuple2(a(idx), a(idx + 1))
    })

  private def f(p: (Seq[Event], Seq[Event]), limit: Int): (Seq[Event], Seq[Event]) = {
    var p1 = p._1
    var p2 = p._2

    var save = Seq[Event]()
    var rest = Seq[Event]()
    for (e <- p1) {
      if (save.length < limit - p1.count(_.isPin) || e.isPin) {
        save = save :+ e
      } else {
        rest = rest :+ e
      }
    }
    p1 = save
    p2 = rest ++ p2
    (p1, p2)
  }

  private def f2(ps: Seq[(Seq[Event], Seq[Event])], limit: Int): Seq[Seq[Event]] = {
    val pair = ps.indices.foldLeft(Tuple2(Seq[Seq[Event]](), ps.head))((acc, idx) => {
      val i1 = idx
      val i2 = idx + 1
      val tuple = acc._2
      //    println(s"ps($i1) = $tuple")
      //    println(s"elem = ${f(tuple)._1}")
      //    println(s"rest = ${f(tuple)._2}")
      if (i2 < ps.length)
        (acc._1 :+ f(tuple, limit)._1, (f(tuple, limit)._2, ps(i2)._2))
      else
        (acc._1 :+ f(tuple, limit)._1, (f(tuple, limit)._2, Nil))
    })
    pair._1 ++ pair._2._1.sliding(limit, limit)
  }
}
