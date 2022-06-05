package com.manenkov.flow

object ScalaVersionCompat {
  type LazyList[+A] = scala.collection.immutable.LazyList[A]
  val LazyList = scala.collection.immutable.LazyList
}
