package com.manenkov.flow

class Flow(val config: Configuration) {
  def flow(tasks: Seq[Task]): Seq[Task] = {
    tasks.sortBy(_.due)
  }
}

object Flow {
  def apply() = new Flow(Configuration())
  def apply(config: Configuration) = new Flow(config)
}
