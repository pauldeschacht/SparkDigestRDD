package io.nomad47.rdd

import org.apache.spark.rdd.RDD

import scala.reflect.ClassTag

object implicits {
  implicit def addCustomFunctions[K : Ordering : ClassTag, V: ClassTag](rdd: RDD[(K,V)]) = new Checksum(rdd)
}
