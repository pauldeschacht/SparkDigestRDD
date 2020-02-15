package io.nomad47.rdd

import org.apache.spark.rdd.RDD
import org.scalatest.FunSuite
import io.nomad47.rdd.implicits._
import org.apache.spark.sql.{Dataset, SparkSession}

import scala.util.Random

class ChecksumTest extends FunSuite {

  lazy implicit val spark: SparkSession = {
    SparkSession
      .builder()
      .master("local[*]")
      .appName("Spark Digest RDD Test")
      .getOrCreate()
  }
  test("checksum RDD independant of number partitions") {
    val input: Seq[(Int,String)] = Seq("one", "two", "three", "four", "five").zipWithIndex.map{r => (r._2,r._1)}

    val s1 = {
      val rdd: RDD[(Int,String)] = spark.sparkContext.parallelize(input, 3)
      assert(rdd.getNumPartitions == 3)
      rdd.md5.toString(16)
    }

    val s2 = {
      val rdd: RDD[(Int,String)] = spark.sparkContext.parallelize(input, 1)
      assert(rdd.getNumPartitions == 1)
      rdd.md5.toString(16)
    }
    assert(s2.compareTo(s1) == 0)

    // when shuffling the input, the MD5 is the same. The absolute order is defined by the key of the input
    val s3 = {
      val r = scala.util.Random
      val shuffled: Seq[(Int,String)] = r.shuffle(input)
      val rdd: RDD[(Int,String)] = spark.sparkContext.parallelize(shuffled)
      rdd.md5.toString(16)
    }
    assert(s3.compareTo(s1) == 0)

    val different_input: Seq[(Int,String)] = Seq("one!", "two", "three", "four", "five").zipWithIndex.map{r => (r._2,r._1)}
    val s4 = {
      val r = scala.util.Random
      val shuffled: Seq[(Int,String)] = r.shuffle(different_input)
      val rdd: RDD[(Int,String)] = spark.sparkContext.parallelize(shuffled)
      rdd.md5.toString(16)
    }
    assert(s4.compareTo(s1) != 0)
  }

  test("ds") {
    case class SubRecord(id:Int)
    case class Record(id:Int, f: String, l:String, lines: Array[SubRecord])

    def generateSubRecords() : Array[SubRecord] = Range(0,(10 * Random.nextDouble() ).toInt).map(i => SubRecord(i)).toArray
    def generateRecords(num: Int) : Array[Record] = Range(0,num).map(i => Record(i, Random.alphanumeric.take(5).mkString,Random.alphanumeric.take(10).toString, generateSubRecords)).toArray

    import spark.implicits._

    val ds: Dataset[Record] = spark.sparkContext.parallelize(generateRecords(100)).toDS()
    val ds_key = ds.map(rec => (rec.id,rec))
    ds_key.rdd.md5
  }
}
