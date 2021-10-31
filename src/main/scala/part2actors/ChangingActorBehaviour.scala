package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.event.Logging
import part2actors.ChangingActorBehaviour.FussyKid.{KidAccept, KidReject}
import part2actors.ChangingActorBehaviour.Mom.MomStart

object ChangingActorBehaviour extends App {

  object FussyKid {
    case object KidAccept
    case object KidReject
    val HAPPY = "happy"
    val SAD = "sad"
  }
  class FussyKid extends Actor {
    import FussyKid._
    import Mom._
    var state = HAPPY
    override def receive: Receive = {
      case Food(VEGETABLE) => state = SAD
      case Food(CHOCLATE) => state = HAPPY
      case Ask(_) => if(state == HAPPY) sender() ! KidAccept else sender() ! KidReject
    }
  }

  class StatelessFussyKid extends Actor {
    import FussyKid._
    import Mom._
    override def receive: Receive = happyReceive
    def happyReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive,false)
      case Food(CHOCLATE) =>
      case Ask(_) => sender() ! KidAccept
    }
    def sadReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive,false)
      case Food(CHOCLATE) => context.unbecome()
      case Ask(_) => sender() ! KidReject
    }
  }

  object Mom {
    case class MomStart(kid: ActorRef)
    case class Food(food: String)
    case class Ask(message: String)
    val VEGETABLE="veggies"
    val CHOCLATE="choclate"
  }
  class Mom extends Actor {
    import Mom._
    override def receive: Receive = {
      case MomStart(kid) =>
        kid ! Food(VEGETABLE)
        kid ! Food(VEGETABLE)
        kid ! Food(CHOCLATE)
        kid ! Food(CHOCLATE)
        kid ! Ask("do you want to play?")
      case KidAccept => println("Yay! my kid is happy!")
      case KidReject => println("My kid is sad, but at least he is healthy!")
    }
  }
  val actorSystem = ActorSystem("changingBehaviourDemo")
  val kidActor = actorSystem.actorOf(Props[FussyKid],"FussyKidActor")
  val momActor = actorSystem.actorOf(Props[Mom],"MomActor")
  val statelessFussyKid = actorSystem.actorOf(Props[StatelessFussyKid],"StatelessFussyKid")
  momActor ! MomStart(statelessFussyKid)

  /**
   * Recreate counter actor with context.become and NO MUTABLE STATE
   */
  object CounterActor {
    case object Increment
    case object Decrement
    case object Print
  }
  import CounterActor._
  class CounterActor extends Actor {

    override def receive: Receive = countReceive(0)

    def countReceive(i: Int): Receive = {
      case Increment =>
        println(s"[countReceive(${i})] Increment")
        context.become(countReceive(i+1))
      case Decrement =>
        println(s"[countReceive(${i})] Decrement")
        context.become(countReceive(i-1))
      case Print => println(s"[counter] my current count is $i")
    }
  }

  val counterActor = actorSystem.actorOf(Props[CounterActor],"counterActor")

  (1 to 5).foreach(_ => counterActor ! Increment)
  (1 to 3).foreach(_ => counterActor ! Decrement)
  counterActor ! Print


  case class Vote(candidate: String)
  case object VoteStatueRequest
  case class VoteStatueReply(candidate: Option[String])
  class Citizen extends Actor {
    override def receive: Receive = vote(None)
    def vote(candidate: Option[String]): Receive = {
      case Vote(c) => context.become(vote(Some(c)))
      case VoteStatueRequest => sender() ! VoteStatueReply(candidate)
    }
  }
  case class AggregateVotes(citizens: Set[ActorRef])
  class VoteAggregator extends Actor {
    override def receive: Receive = awaitingCommand

    def awaitingCommand: Receive = {
      case AggregateVotes(actorSet) =>
        actorSet.foreach(actor => actor ! VoteStatueRequest)
        context.become(awaitingStatuses(actorSet,Map[String,Int]()))
    }

    def awaitingStatuses(refs: Set[ActorRef], statuses: Map[String, Int]): Receive = {
      case VoteStatueReply(candidate) =>
        val newStillWaiting = refs - sender()
        val currentVotesOfCandidate = statuses.getOrElse(candidate.get,0)
        val newStatuses = statuses + (candidate.get -> (currentVotesOfCandidate + 1))
        if(newStillWaiting.isEmpty)
          println(s"[aggregator] poll stats: ${newStatuses}")
        else
          context.become(awaitingStatuses(newStillWaiting,newStatuses))
    }
  }

  val alice = actorSystem.actorOf(Props[Citizen],"Alice")
  val bob = actorSystem.actorOf(Props[Citizen],"Bob")
  val charlie = actorSystem.actorOf(Props[Citizen],"Charlie")
  val daniel = actorSystem.actorOf(Props[Citizen],"Daniel")

  alice ! Vote("Martin")
  bob ! Vote("Jonas")
  charlie ! Vote("Roland")
  daniel ! Vote("Roland")

  val voteAggregator = actorSystem.actorOf(Props[VoteAggregator],"VoteAggregator")
  voteAggregator ! AggregateVotes(Set(alice,bob,charlie,daniel))
}
