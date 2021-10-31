package part1recap

object AdvancedRecap extends App {
  val partialFunction: PartialFunction[Int, Int] = {
    case 1 => 42
    case 2 => 65
    case 5 => 999
  }
  println(partialFunction(5))
  val pf = (x: Int) => x match {
    case 1 => 42
    case 2 => 65
    case 5 => 999
  }
  println(pf(2))
  val function: Int => Int = partialFunction
  println(function(1))
  val modifiedList = List(1,2,3).map {
    case 1 => 42
    case _ => 0
  }
println(modifiedList)

  val lifted = partialFunction.lift
  println(lifted(1))

  type ReceiveFunction = PartialFunction[Any, Unit]
  def receive: ReceiveFunction = {
    case 1 => println("Hello")
    case _ => println("Unknown")
  }

  receive(1)

  implicit val timeout = 3000
  def setTimeout(f: () => Unit)(implicit timeout: Int) = f()

  setTimeout(() => println("hello"))

  case class Person(name: String) {
    def greet = s"Hello, I am $name"
  }
//implicit defs
  implicit def fromStringToPerson(string: String) = Person(string)

  println("Gurudatt".greet)

  //implicit class

  implicit class Dog(name: String) {
    def bark = println(s"$name,bark!!")
  }
  "tom".bark

  implicit val inverseOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _)
  println(List(1,2,3).sorted)

  object Person {
    implicit val personOrdering:Ordering[Person] = Ordering.fromLessThan((a,b) => a.name.compareTo(b.name) < 0)
  }

  println(List(Person("Bob"), Person("Alice")).sorted)
}
