package com.manenkov

package object flow {
  type AsComparable[A] = A => Comparable[_ >: A]
  implicit def ordered[A: AsComparable]: Ordering[A] = (x: A, y: A) => x compareTo y
}
