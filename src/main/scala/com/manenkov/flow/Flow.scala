package com.manenkov.flow

import java.time.LocalDateTime
import scala.collection.mutable

case class T(
              name: String,
              isPin: Boolean = false,
              due: LocalDateTime = LocalDateTime.now(),
            )

object Flow {

  def flow[K](c: Seq[T], limit: Int)(keyF: T => K, next: K => K, updF: (T, K) => T): Seq[T] = {
    val m = c.sortBy(_.due).foldLeft(mutable.LinkedHashMap[K, Seq[T]]())((map, t) => {
      map.put(keyF(t), map.getOrElse(keyF(t), Seq[T]()).appended(t))
      map
    })
    val keys = m.keys.toSeq
    val vals = f2(pairs(m.values.toSeq), limit)

    val rest = vals.drop(keys.length)

    val updated = keys.zip(vals.take(keys.length)) ++ rest.foldLeft((next(keys.last), Seq[K]()))((acc, elems) => {
      val date = acc._1
      val res = acc._2
      (next(date), res.appended(date))
    })._2.zip(rest)

    updated.flatMap(pair => {
      val key = pair._1
      val value = pair._2
      //(key, value.map(t => t.copy(due = key.atTime(t.due.toLocalTime))))
      value.map(updF(_, key))
    })
  }

  private def pairs[A](a: Seq[A]): Seq[(A, A)] =
    (0 until a.length - 1).foldLeft(Seq[(A, A)]())((acc, idx) => {
      acc.appended(Tuple2(a(idx), a(idx + 1)))
    })

  private def f(p: (Seq[T], Seq[T]), limit: Int): (Seq[T], Seq[T]) = {
    var p1 = p._1
    var p2 = p._2

    var save = Seq[T]()
    var rest = Seq[T]()
    for (e <- p1) {
      if (save.length < limit - p1.count(_.isPin) || e.isPin) {
        save = save.appended(e)
      } else {
        rest = rest.appended(e)
      }
    }
    // move pinned back
    p1 = save.filter(!_.isPin) ++ p1.filter(_.isPin)
    p2 = rest ++ p2
    (p1, p2)
  }

  private def f2(ps: Seq[(Seq[T], Seq[T])], limit: Int): Seq[Seq[T]] = {
    val pair = ps.indices.foldLeft(Tuple2(Seq[Seq[T]](), ps(0)))((acc, idx) => {
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
