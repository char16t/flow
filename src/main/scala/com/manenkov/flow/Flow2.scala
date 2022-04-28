package com.manenkov.flow

import java.time.LocalDateTime
import java.util.UUID

/** Task in flow.
 *
 * @param id    Unique identifier of task in current flow.
 * @param order Order of task in current flow.
 * @param task  Task definition.
 * @param due   Due date of current task.
 */
case class TaskOut[T](
                       id: UUID,
                       order: Long,
                       task: T,
                       due: LocalDateTime,
                     )

object Flow2 {
  def flow[T](tasks: Seq[TaskOut[T]]): Seq[TaskOut[T]] = ???
}
