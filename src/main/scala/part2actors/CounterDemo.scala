package part2actors

import akka.actor.Status.{Failure, Success}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.CounterDemo.CounterActor.{Decrement, Increment, Print}

object CounterDemo extends App {

  object CounterActor {
    case object Increment
    case object Decrement
    case object Print
  }
  class CounterActor extends Actor {
    var counter = 0
    override def receive: Receive = {
      case Increment => counter = counter + 1
      case Decrement => counter = counter - 1
      case Print => println(s"[counter actor] counter value: ${counter}")
    }
  }

  val actorSystem = ActorSystem("ActorSystem")

  val counterActor = actorSystem.actorOf(Props[CounterActor],"counterActor")

  counterActor ! Increment
  counterActor ! Print
  counterActor ! Decrement
  counterActor ! Print


  class BankAccount extends Actor {
    var balance = 0
    override def receive: Receive = {
      case Deposit(amount, holder) =>
        if(amount > 0) {
          balance = balance + amount
          holder ! Success
        } else holder ! Failure
      case Withdraw(amount,holder) => {
        if(balance > 0 && amount < balance && amount > 0){
          balance = balance - amount
          holder ! Success
        } else holder ! Failure
      }
      case "Statement" => println(s"[bank account] Balance in account: ${balance}")
    }
  }
  case class Deposit(amount: Int, holder: ActorRef)
  case class Withdraw(amount: Int, holder: ActorRef)


  val bankAccountActor = actorSystem.actorOf(Props[BankAccount],"bankAccountActor")
  class AccountHolder extends Actor {
    override def receive: Receive = {
      case Success => println("Transaction Success")
      case Failure => println("Transaction failed")
    }
  }
  val accountHolder = actorSystem.actorOf(Props[AccountHolder],"accountHolder")

  bankAccountActor ! Deposit(100,accountHolder)
  bankAccountActor ! "Statement"

  bankAccountActor ! Withdraw(101,accountHolder)




}
