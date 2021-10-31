package part1recap

object MultithreadingRecap extends App {

  val aThread = new Thread(() => println("I am a thread, i am running in parallel"))
  aThread.start()
  aThread.join()

  val threadHello = new Thread(() => (1 to 1000).foreach(_ => println("Hello!")))
  val threadGoodBye = new Thread(() => (1 to 1000).foreach(_ => println("Good Bye!")))
  threadHello.start()
  threadGoodBye.start()
}
