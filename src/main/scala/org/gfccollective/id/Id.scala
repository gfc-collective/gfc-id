package org.gfccollective.id

import scala.beans.BeanProperty

/**
 * A typed Identifier
 */
object Id {
  def apply[T, V](v: V) = new Id[T, V](v)
  def unapply[T, V](id: Id[T, V]): Option[V] = Some(id.value)
}

@SerialVersionUID(0L)
sealed class Id[+T, V](@BeanProperty val value: V) extends Serializable {
  require(value != null, "value must not be null")

  final override def toString: String = value.toString

  final override val hashCode: Int = value.hashCode

  final override def equals(obj: Any): Boolean = (obj == value)

  /**
   * Safe way to cast an Id of marker type T to Id of marker type U,
   * with the same value and value type V.
   *
   * This should only be used to convert between Ids with equivalent
   * marker types (i.e. Id[com.foo.User, _] -> Id[org.bar.User, _]).
   *
   * It should not be used to convert between Ids with unrelated marker
   * types, like User -> Order.
   *
   * @tparam U The target marker type
   * @return a new Id
   */
  final def typeCast[U]: Id[U, V] = new Id(value)
}
