package part1recap

import scala.concurrent.Future

object ThreadModelLimitations extends App {

  //OOP's encapsulation is broken in multi threaded environment
  //synchronization and locks to the resque
  //deadlocks, livelocks

  class BankAccount(private var amount: Int) {
    override def toString: String = " " + this.amount

    def withdraw(money: Int) = this.synchronized {
      this.amount -= money
    }

    def deposit(money: Int) = this.synchronized {
      this.amount += money
    }
  }

  //  val account = new BankAccount(2000)
  //  for(_ <- 1 to 1000){
  //    new Thread(() => account.withdraw(1)).start()
  //  }
  //
  //  for(_ <- 1 to 1000){
  //    new Thread(() => account.deposit(1)).start()
  //  }


  //Delegating something to the thread is a PAIN
  // you have a running thread and you want to pass a runnable to that thread
  var task: Runnable = null
  val runningThread: Thread = new Thread(() => {
    while (true) {
      while (task == null) {
        runningThread.synchronized {
          println("[background] waiting for a task....")
          runningThread.wait()
        }
      }

      task.synchronized {
        println("[background] I have a task!")
        task.run()
        task = null
      }
    }
  })

  def delegateTheBackgroundThread(r: Runnable) = {
    if (task == null) task = r
    runningThread.synchronized {
      runningThread.notify()
    }
  }

  runningThread.start()
  Thread.sleep(1000)
  delegateTheBackgroundThread(() => println(42))
  Thread.sleep(1000)
  delegateTheBackgroundThread(() => println("this should run in background"))


  //Tracing and dealing with errors in multithreaded environment is a PAIN

  import scala.concurrent.ExecutionContext.Implicits.global
  val futures = (0 to 9)
    .map(i => 100000 * i until 100000 * (i + 1))
    .map(range => Future{
      if(range.contains(453432)) throw new RuntimeException("invalid number")
      range.sum
    })

  val sumFuture = Future.reduceLeft(futures)(_ + _)
  sumFuture.onComplete(println)
}
