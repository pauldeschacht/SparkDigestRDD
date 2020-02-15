## Usage

The library calculates the MD5 hash of a RDD[K <: Ordering, V: ClassTag]. The hash is independent of the order of elements and of the number of partitions. 

It uses a helper class ```CaseClassTraversal``` to fix the inconsistent toString and hashCode for Array types. The ```CaseClassTraversal```walks recursively through the data structure and builds up a toString/hashCode result.

## Example

```scala
import org.apache.spark.rdd.RDD
import io.nomad47.rdd.implicits._
import org.apache.spark.sql.{Dataset, SparkSession}

val ds: Dataset[Record] = spark.read.format("parquet").load(filename)
val ds_key = ds.map(rec => (rec.id,rec))
val md5: BigInt = ds_key.rdd.md5
println(md5.toString(16))
```

## Build 

```shell
sbt clean compile test assembly
``` 