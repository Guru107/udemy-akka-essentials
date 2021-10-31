package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChildActors.CreditCard.AttachToAccount
import part2actors.ChildActors.Parent.{CreateChild, TellChild}

object ChildActors extends App {

  object Parent {
    case class CreateChild(message: String)
    case class TellChild(message: String)
  }
  class Parent extends Actor {
    import Parent._
    override def receive: Receive = {
      case CreateChild(name) =>
        println(s"${self.path} Creating child")
        val childRef = context.actorOf(Props[Child],name)
        context.become(withChild(childRef))
    }

    def withChild(ref: ActorRef): Receive ={
      case TellChild(message) => ref forward message
    }
  }
  class Child extends Actor {
    override def receive: Receive = {
      case message: String => println(s"${self.path} I got: $message")
    }
  }

  val system = ActorSystem("ParentChildDemo")

  val parent = system.actorOf(Props[Parent],"parent")
  parent ! CreateChild("Jeff")
  parent ! TellChild("Hello, World!")

  val actorSelection = system.actorSelection("/user/parent/Jeff")
  actorSelection ! "I found you!"

  /**
   * Danger!
   * NEVER PASS MUTUABLE STATE OR the `this` reference to a CHILD ACTOR
   */

  object NaiveBankAccount {
    case class Deposit(amount: Int)
    case class Withdraw(amount: Int)
    case object InitializeAccount
  }
  class NaiveBankAccount extends Actor {
    import NaiveBankAccount._
    var amount = 0
    override def receive: Receive = {
      case InitializeAccount => {
        val creditCardRef = context.actorOf(Props[CreditCard],"card")
        creditCardRef ! AttachToAccount(this)
      }
      case Deposit(funds) => deposit(funds)
      case Withdraw(funds) => withdraw(funds)
    }
    def withdraw(funds: Int): Unit = {
      println(s"${self.path} withdrawing $funds from $amount")
      amount-=funds
    }
    def deposit(funds: Int): Unit = {
      println(s"${self.path} depositing $funds from $amount")
      amount+=funds
    }
  }

  object CreditCard {
    case class AttachToAccount(bankAccount: NaiveBankAccount) //!!
    case object CheckStatus
  }

  class CreditCard extends Actor {
    import CreditCard._

    def attachTo(account: NaiveBankAccount): Receive = {
      case CheckStatus => println(s"${self.path} Your message has been processed")
        //benign
        account.withdraw(1) // Because I can, but should not
    }

    override def receive: Receive = {
      case AttachToAccount(account) => context.become(attachTo(account))
    }
  }
  import CreditCard._
  import NaiveBankAccount._

  val bankAccountRef = system.actorOf(Props[NaiveBankAccount],"account")
  bankAccountRef ! InitializeAccount
  bankAccountRef ! Deposit(100)

  Thread.sleep(500)
  val cardActorSelection = system.actorSelection("/user/account/card")
  cardActorSelection ! CheckStatus
//NEVER DO THIS WAY, ALWAYS PASS MESSAGES ELSE IT DEFEATS THE PURPOSE OF ACTORS
}
