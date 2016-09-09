package io.ics

import io.ics.disciple.labels._

import scala.language.implicitConversions

package object disciple {
  implicit def symbolToLabel(s: Symbol): Label = Label(s)
  val * = NoLabel
}
