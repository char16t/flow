package com.manenkov.flow

import java.time.LocalDateTime
import scala.collection.mutable

case class Event(
              name: String,
              isPin: Boolean = false,
              due: LocalDateTime = LocalDateTime.now(),
            )

object Flow {

  def flow[K](c: Seq[Event], limit: Int)(keyF: Event => K, next: K => K, updF: (Event, K) => Event): Seq[Event] = {
    val m = c.sortBy(_.due).foldLeft(mutable.LinkedHashMap[K, Seq[Event]]())((map, t) => {
      map.put(keyF(t), map.getOrElse(keyF(t), Seq[Event]()).appended(t))
      map
    })
    val keys = m.keys.toSeq
    val paired = pairs(m.values.toSeq)
    val vals = if (paired.nonEmpty) f2(paired, limit) else m.values.toSeq

    val rest = vals.drop(keys.length)

    val updated = keys.zip(vals.take(keys.length)) ++ rest.foldLeft((next(keys.last), Seq[K]()))((acc, elems) => {
      val date = acc._1
      val res = acc._2
      (next(date), res.appended(date))
    })._2.zip(rest)

    updated.flatMap(pair => {
      val key = pair._1
      val value = pair._2
      value.map(updF(_, key))
    })
  }

  private def pairs[A](a: Seq[A]): Seq[(A, A)] =
    (0 until a.length - 1).foldLeft(Seq[(A, A)]())((acc, idx) => {
      acc.appended(Tuple2(a(idx), a(idx + 1)))
    })

  private def f(p: (Seq[Event], Seq[Event]), limit: Int): (Seq[Event], Seq[Event]) = {
    var p1 = p._1
    var p2 = p._2

    var save = Seq[Event]()
    var rest = Seq[Event]()
    for (e <- p1) {
      if (save.length < limit - p1.count(_.isPin) || e.isPin) {
        save = save.appended(e)
      } else {
        rest = rest.appended(e)
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
        (acc._1.appended(f(tuple, limit)._1), (f(tuple, limit)._2, ps(i2)._2))
      else
        (acc._1.appended(f(tuple, limit)._1), (f(tuple, limit)._2, Nil))
    })
    pair._1 ++ pair._2._1.sliding(limit,limit)
  }
}
