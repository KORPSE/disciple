package io.k0rp53.disciple.util

import io.ics.disciple.dep._

import scala.reflect.ClassTag

object Util {
  def classTag[T: ClassTag] = implicitly[ClassTag[T]]
  def getId[P: ClassTag](name: Option[Symbol]) = name match {
    case Some(n) => NamedId(n, classTag[P])
    case None    => CTId(classTag[P])
  }

  type CT[T] = ClassTag[T]
}
