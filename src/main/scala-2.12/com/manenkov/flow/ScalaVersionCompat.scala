package com.manenkov.flow

object ScalaVersionCompat {
  type LazyList[+A] = scala.collection.immutable.Stream[A]
  val LazyList = scala.collection.immutable.Stream
}
