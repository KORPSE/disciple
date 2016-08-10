package io.ics.souce.util

sealed trait HList {
  def ::[H](h: H) = new ::(h, this)
  def ++(r: HList): HList
}

sealed trait HCons[H, T <: HList] extends HList

case object HNil extends HList {
  def ++(r: HList): HList = r
}

final case class ::[H, T <: HList](v: H, tail: T) extends HCons[H, T] {
  override def toString: String = s"$v :: $tail"
  def ++(r: HList): HList = v :: (tail ++ r)
}
