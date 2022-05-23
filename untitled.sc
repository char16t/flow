import java.time.{LocalDate, LocalDateTime}
import scala.collection.mutable

def pairs[A](a: Seq[A]): Seq[(A, A)] =
  (0 until a.length - 1).foldLeft(Seq[(A, A)]())((acc, idx) => {
    acc.appended(Tuple2(a(idx), a(idx + 1)))
  })

case class T(
              name: String,
              isPin: Boolean = false,
              due: LocalDateTime = LocalDateTime.now(),
            )

def f(p: (Seq[T], Seq[T]), limit: Int): (Seq[T], Seq[T]) = {
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

def f2(ps: Seq[(Seq[T], Seq[T])], limit: Int): Seq[Seq[T]] = {
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

val b = Seq(
  Seq(T("A1", isPin = true), T("A2", isPin = true), T("A3", isPin = true)),
  Seq(T("B1"), T("B2"), T("B3")),
  Seq(T("C1"), T("C2", isPin = true), T("C3")),
  Seq(T("D1"), T("D2"), T("D3"), T("D4")),
)
val ps = pairs(b)
f2(ps, 2)

// -----

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
object PerWeek {
  val limit = 3
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

val stage1 = flow(c, PerDay.limit)(PerDay.keyF, PerDay.next, PerDay.updF)
val stage2 = flow(stage1, PerMonth.limit)(PerMonth.keyF, PerMonth.next, PerMonth.updF)
