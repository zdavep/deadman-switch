package org.sofi.deadman.test.event

import akka.actor._
import com.rbmhtechnology.eventuate.EventsourcedView
import org.sofi.deadman.messages.command._
import org.sofi.deadman.messages.event._
import org.sofi.deadman.test.TestSystem
import scala.concurrent.duration._

final class TaskWarningTest extends TestSystem {
  // Helper view that forwards a `TaskWarning` event back to the test actor for assertion
  final class TaskWarningForwarder(val id: String, val eventLog: ActorRef) extends EventsourcedView {
    def onCommand = { case _ ⇒ }
    def onEvent = {
      case event: TaskWarning ⇒
        testActor ! event
    }
  }
  "A task actor" must {
    "Successfully persist a task warning event" in {
      system.actorOf(Props(new TaskWarningForwarder(aggregate, eventLog)))
      taskActor ! ScheduleTask("test", aggregate, "0", 10.days.toMillis, Seq(1.second.toMillis))
      expectMsg(CommandResponse(ResponseType.SUCCESS))
      expectMsgPF() {
        case event: TaskWarning ⇒
          event.ttw must be(1.second.toMillis)
          event.task.key must be("test")
          event.task.aggregate must be(aggregate)
          event.task.entity must be("0")
      }
    }
  }
}
