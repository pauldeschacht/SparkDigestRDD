package io.nomad47

/**
 * CaseClassTraversal walks over the hierarchical data structure and can be used to correct https://github.com/scala/bug/issues/1607
 * The main issue is that Array(1,2).toString != Array(1,2). The same is true for hashCode
 *
 * The CaseClassTraversal allows to walk over the nested data structure and apply a custom function per data element
 *
 * For example
 *   List(Array(1,2), Array(3,4,5)) walks over each element of the list. Each element is an array and thus walk will iterate over each element of the array.
 *
 *   Array(Person("Ada", "Lovelace", null), Person("Charles", "Babbage", Birth(18, "October", 1871)) )
 *      - will iterate over each Person in the array
 *      - for each Person, iterator over the different fields (String, String, Birth)
 *      - walk over the fields of Birth
 *
 */
object CaseClassTraversal {
  private[this] def product[V](x: Product, z: V, f: Any => V, g: (String, Iterable[V]) => V) : Iterable[V] = {
    x.productIterator
    x.productIterator.map(walk(_,z,f,g)).toIterable
  }
  private[this] def iterable[U,V](xs: Iterable[U],z: V, f: Any => V, g: (String, Iterable[V]) => V) : Iterable[V] = {
    xs.map(walk(_,z,f,g))
  }
  /**
   * The function walks over the data structure and applies a map and reduce function
   *
   * @param x
   * @param z: zero value of type V
   * @param f: transform an instance of any data type to the type V (map function)
   * @param g: transform a list of V values into the final V value (reduce function)
   * @tparam U: type of the input parameter
   * @tparam V: type of the output parameter
   * @return
   */
  def walk[U,V](x:U, z: V, f: Any => V, g: (String, Iterable[V]) => V) : V = {
    x match {
      case null             => z
      case e: List[_]       => g("List", iterable(e,z,f,g))
      case e: Seq[_]        => g("Seq", iterable(e,z,f,g))
      case e: Set[_]        => g("Set", iterable(e,z,f,g))
      case e: Map[_,_]      => g("Map",iterable(e,z,f,g))
      case e: scala.Product => g("Product", product(e,z,f,g))
      case e: Array[_]      => g("Array", iterable(e,z,f,g))
      case e: Iterable[_]   => g("Iterable", iterable(e,z,f,g))
      case _ => f(x)
    }
  }
  /**
   * Traverse the nested structure of the case class and transform each node into a string
   * @param x instance of the case clas to stringify
   * @tparam T type of the case class to stringify
   * @return String representing the case class
   */
  def str[T](x:T) : String = walk(x, "", _.toString, (prefix: String, strings: Iterable[String]) => strings.mkString(prefix+"(",",",")"))

  private[this] def h(v: Int): Int = {
    var h = v & 1
    for (i <- 1 to 31) {
      h <<= 1;
      h |= ((v >>> i) & 1)
    }
    h
  }
  /**
   * Traverse the nested structure of the case class and transform each node into a hash
   * @param x instance of the case clas to hashify
   * @tparam T type of the case class to hashify
   * @return hashcode of the case class
   */
  def hash[T](x:T): Int = walk(x, 0, _.hashCode, (_:String, ints: Iterable[Int]) => {
    ints.reduceOption(_ + _) match {
      case Some(i) => i
      case None => 0
    }
  })
}