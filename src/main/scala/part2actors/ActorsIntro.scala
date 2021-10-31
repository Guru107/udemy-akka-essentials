package part2actors

import akka.actor.{Actor, ActorSystem, Props}

object ActorsIntro extends App {
//part1 - actor system
  val actorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name)
  // part2 - create actors
  //word count
  class WordCountActor extends Actor {
    var totalWords = 0
    def receive: Receive = {
      case message: String =>
        println(s"I have received a message: $message")
        totalWords += message.split(" ").length
      case msg => println(s"[word count actor] I cannot understand ${msg.toString}")
    }
  }

  //part3 - instantiate our actor

  val wordCounter = actorSystem.actorOf(Props[WordCountActor],"wordCounter")
  val anotherWordCounter = actorSystem.actorOf(Props[WordCountActor],"anotherWordCounter")

  //part4 - communicate with our actor
  wordCounter ! "I am learning akka and it is good"
  anotherWordCounter ! "A different message"
  //message sending is asynchronous

  //Actors with constructor parameters

  object Person {
    def props(name: String) = Props(new Person(name))
  }
  case class Person(name: String) extends Actor{
    override def receive: Receive = {
      case "hi" => println(s"Hi, my name is $name")
      case _ =>
    }
  }

  val person = actorSystem.actorOf(Props(new Person("Bob")),"personActor") // Not best practice to instantiate here, create companion object
  person ! "hi"

  val anotherPerson = actorSystem.actorOf(Person.props("Alice"),"anotherPersonActor") // Using best practice
  anotherPerson ! "hi"


}
