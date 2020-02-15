## Usage

The library calculates the MD5 hash of a RDD[K <: Ordering, V: ClassTag]. The hash is independent of the order of elements and of the number of partitions. 

It uses a helper class ```CaseClassTraversal``` to fix the inconsistent behavior of Array.toString() and Array.hashCode(), which depend on the reference of the object and not the content. 
The ```CaseClassTraversal```walks recursively through the data structure and builds up a toString/hashCode result.

The implementation of RDD MD5 was inspired by https://github.com/destrys/spark-md5

## Example

```scala
import org.apache.spark.rdd.RDD
import io.nomad47.rdd.implicits._
import org.apache.spark.sql.{Dataset, SparkSession}

// read Dataset
val ds: Dataset[Record] = spark.read.format("parquet").load(filename)
// transform to Dataset[K <: Ordering, V]
val ds_key = ds.map(rec => (rec.id,rec))
// calculate the MD5
val md5: BigInt = ds_key.rdd.md5
println(md5.toString(16))
```

## Build 

```shell
sbt clean compile test assembly
``` 