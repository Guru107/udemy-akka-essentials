package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChildActorsExercise extends App {

  object WordCounterMaster {
    case class Initialize(nChildren: Int)
    case class WordCountTask(text: String)
    case class WordCountReply(count: Int)
  }
  class WordCounterMaster extends Actor {
    import WordCounterMaster._

    override def receive: Receive = {
      case Initialize(nChildren) => context.become(
        workCountTaskAssigner(nChildren,(0 to nChildren).map {
        id => context.actorOf(Props[WordCounterWorker],s"wordCountWorker${id}")
      }.toArray,0))

    }

    def workCountTaskAssigner(nChildren: Int, childActors: Array[ActorRef],taskIndex: Int): Receive = {
      case text: String =>
        println(s"Task Assigned to actor: ${taskIndex}")
        childActors(taskIndex) ! WordCountTask(text)
        context.become(workCountTaskAssigner(nChildren, childActors,(taskIndex + 1) % nChildren))
      case WordCountReply(n) => println(s"Number of words: ${n}")
    }

  }

  class WordCounterWorker extends Actor {
    import WordCounterMaster._
    override def receive: Receive = {
      case WordCountTask(text) => sender() ! WordCountReply(text.split(" ").length)
    }
  }

  /*
    Create WordCounterMaster
    send Initialize(10) to wordCounterMaster
    send "Akka is awesome" to wcm
      wcm will send WordCountTask to one of its children
        child will reply with a WordCountReply(3) to the master
      master replies 3 to the sender
   */

  import WordCounterMaster._
  val actorSystem = ActorSystem("WordCountSystem")

  val wordCounterMaster = actorSystem.actorOf(Props[WordCounterMaster],"wordCountMaster")

  wordCounterMaster ! Initialize(3)
  wordCounterMaster ! "Akka is awesome"
  wordCounterMaster ! "Akka is awesome and difficult"
  wordCounterMaster ! "Akka"
  wordCounterMaster ! "Akka is"
  wordCounterMaster ! "A b c d e"
  wordCounterMaster ! "A b c d e f g"


}
