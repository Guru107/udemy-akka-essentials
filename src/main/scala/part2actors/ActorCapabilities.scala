package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App {
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "Hi!" => sender() ! "Hello, there!"
      case message:String => println(s"[$self] I have received $message from ${sender().path}")
      case number: Int => println(s"[simple actor] I have received a NUMBER: $number")
      case SpecialMessage(content) => println(s"[simple actor] I have received a SpecialMessage: $content")
      case SendSomethingToYourself(content) => self ! content
      case SayHi(ref) => ref ! "Hi!"
      case WirelessPhoneMessage(content, ref) => ref forward content+"s"
    }
  }

  val actorSystem = ActorSystem("actorCapabilitiesDemo")

  val simpleActor = actorSystem.actorOf(Props[SimpleActor],"simpleActor")

  simpleActor ! "hello, actor"
  //1 - messages can be of any type as long as following conditions hold
  //a) Messages must be IMMUTABLE
  //b) Messages must be SERIALIZABLE
  //in practice use case classes and case objects

  simpleActor ! 42

  case class SpecialMessage(content: String)
  simpleActor ! SpecialMessage("special content")

  //2- Actors have information about their context and about themselves
  //context.self === `this` in OOP
  case class SendSomethingToYourself(content: String)
  simpleActor ! SendSomethingToYourself("I am an actor")

  //3 - actors can REPLY to messages
  val alice = actorSystem.actorOf(Props[SimpleActor],"alice")
  val bob = actorSystem.actorOf(Props[SimpleActor],"bob")
  case class SayHi(ref: ActorRef)
  alice ! SayHi(bob)
  //4 - dead letters
  alice ! "Hi!"

  //5 - forwarding messages
  //D -> A -> B
  case class WirelessPhoneMessage(content: String, ref: ActorRef)

  alice ! WirelessPhoneMessage("Hi",bob)
}
