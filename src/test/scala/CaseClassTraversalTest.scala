package io.nomad47

import org.scalatest.FunSuite

class CaseClassTraversalTest extends FunSuite {

  test("Recursive toString") {
    // Raison d'etre for the recursive to string: Array.toString and Array.hashCode are not consistent
    val a1 = Array(1,2)
    val a2 = Array(1,2)
    assert(a1.toString.compareTo(a2.toString) != 0)
    assert(a1.hashCode != a2.hashCode)

    val l1 = List(1,2)
    val l2 = List(1,2)
    assert(l1.toString.compareTo(l2.toString) == 0)
    assert(l1.hashCode == l2.hashCode)

    // RecursiveToString creates a consistent toString
    assert(CaseClassTraversal.str(a1).compareTo(CaseClassTraversal.str(a2)) == 0)
    assert(CaseClassTraversal.hash(a1) == CaseClassTraversal.hash(a2))

    assert(CaseClassTraversal.str(List(Array(1,2), Array(3,4,5))).compareTo(
      CaseClassTraversal.str(List(Array(1,2), Array(3,4,5)))) == 0)
    assert(CaseClassTraversal.hash(List(Array(1,2), Array(3,4,5))) == CaseClassTraversal.hash(List(Array(1,2), Array(3,4,5))))

    assert(CaseClassTraversal.str(List(Array(10,2), Array(3,4,5))).compareTo(
      CaseClassTraversal.str(List(Array(1,2), Array(3,4,5)))) != 0)
    assert(CaseClassTraversal.hash(List(Array(10,2), Array(3,4,5))) != CaseClassTraversal.hash(List(Array(1,2), Array(3,4,5))))


    case class Id(_id:Int, _oid: String)
    case class OrderLine(rnid: Int, title:String)
    case class Order(id:Id, name: String, ptrs: Array[OrderLine])

    val rec1 = Order(Id(1, "first id"), "first order", Array(OrderLine(123, "some line"), OrderLine(456, "another line")))
    val twin = Order(Id(1, "first id"), "first order", Array(OrderLine(123, "some line"), OrderLine(456, "another line")))

    assert(CaseClassTraversal.str(rec1).compareTo(CaseClassTraversal.str(twin)) == 0)
    assert(CaseClassTraversal.hash(rec1) == CaseClassTraversal.hash(twin))
  }

  test("Test empty fields") {
    case class Person(f: String, l:String)
    case class Class(n: String, students: Array[Person])

    val class1 = Class("CS", Array[Person]())
    assert(CaseClassTraversal.str(class1).size > 0)

    val class2 = Class(null, Array[Person]())
    assert(CaseClassTraversal.str(class2).size > 0)

    val class3 = Class(null, Array[Person](Person("paul",null)))
    assert(CaseClassTraversal.str(class3).size > 0)
  }
}
