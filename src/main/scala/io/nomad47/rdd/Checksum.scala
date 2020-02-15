package io.nomad47.rdd

import java.io.Serializable
import java.security.MessageDigest

import org.apache.spark.HashPartitioner
import org.apache.spark.rdd.RDD

import scala.reflect.ClassTag

import io.nomad47.CaseClassTraversal

/**
 * A class used to calculate a consistent MD5 hash on a RDD, independent from the order and the number of partitions.
 * The RDD Hash can used be to compare 2 different RDD's.
 *
 * 1. The data is sorted in a fixed number of partitions, this approach ensures that the hash calculation is independent from the number of partitions
 * 2. Once sorted, the key is no longer used
 * 3. For each partition in the RDD, iterate over the sorted data and update the hash calculation (digest.update). Each partitions generates a MD5 string.
 * 4. The resulting strings are collected and sorted (collect does not guarantee the order)
 * 5. The sorted strings are used to generate the final MD5 hash.
 *
 * The CaseClassUtils.str is used to generate a consist string representation of the data.
 *
 * @param rdd: RDD[K,V] must respect [K: Ordering], V can be any data
 * @param ordering$K$0
 * @param classTag$K$1
 * @param classTag$V$0
 * @tparam K key used to partition & sort the RDD
 * @tparam V data on which the MD5 is calculated
 */
class Checksum[K : Ordering : ClassTag, V: ClassTag](val rdd: RDD[(K, V)], val numPartitions: Int = 20) extends Serializable {
  private[this] def acc_md5(acc: MessageDigest, message: V) : MessageDigest = {
    acc.update(CaseClassTraversal.str(message).toString.getBytes("UTF-8"))
    acc
  }
  private[this] def acc_md5(acc: MessageDigest, message: String) : MessageDigest = {
    acc.update(message.getBytes("UTF-8"))
    acc
  }
  def md5 : BigInt = {
    val messageDigest =
      rdd
        .repartitionAndSortWithinPartitions(new HashPartitioner(numPartitions))
        .map(_._2)
        .mapPartitions(it => Iterator(it.foldLeft(MessageDigest.getInstance("MD5"))(acc_md5)))
        .map(md5 => new java.math.BigInteger(1, md5.digest()).toString(16))
        .collect()
        .sorted
        .foldLeft(MessageDigest.getInstance("MD5"))(acc_md5)
    BigInt(new java.math.BigInteger(1, messageDigest.digest()))
  }
}