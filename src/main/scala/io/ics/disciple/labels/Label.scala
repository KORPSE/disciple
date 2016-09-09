package io.ics.disciple.labels

sealed trait PossibleLabel extends Any

case class Label(sym: Symbol) extends AnyVal with PossibleLabel

case object NoLabel extends PossibleLabel
