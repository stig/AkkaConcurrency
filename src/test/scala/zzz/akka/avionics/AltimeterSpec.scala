package zzz.akka.avionics

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{TestActorRef, TestKit,
TestLatch, ImplicitSender}

import scala.concurrent.duration._
import scala.concurrent.Await
import org.scalatest.{WordSpec, BeforeAndAfterAll}
import org.scalatest.matchers.MustMatchers

class AltimeterSpec extends TestKit(ActorSystem("AltimeterSpec"))
  with ImplicitSender
  with WordSpec
  with MustMatchers
  with BeforeAndAfterAll {

  import Altimeter._

  override def afterAll() {
    system.shutdown()
  }

  // We'll instantiate a Helper class for every test, making
  // things nicely reusable.
  class Helper {

    object EventSourceSpy {
      // The latch gives us fast feedback when
      // something happens
      val latch = TestLatch(1)
    }

    // Our special derivation of EventSource gives us the
    // hooks into concurrency
    trait EventSourceSpy extends EventSource {
      def sendEvent[T](event: T): Unit =
        EventSourceSpy.latch.countDown()

      // We don't care about processing the messages that
      // EventSource usually processes so we simply don't
      // worry about them.
      def eventSourceReceive = Actor.emptyBehavior
    }

    // The slicedAltimeter constructs our Altimeter with
    // the EventSourceSpy
    def slicedAltimeter = new Altimeter with EventSourceSpy

    // This is a helper method that will give us an ActorRef
    // and our plain ol' Altimeter that we can work with
    // directly.
    def actor() = {
      val a = TestActorRef[Altimeter](Props(slicedAltimeter))
      (a, a.underlyingActor)
    }
  }

  "Altimeter" should {
    "record rate of climb changes" in new Helper {
      val (_, real) = actor()
      real.receive(RateChange(1f))
      real.rateOfClimb must be(real.maxRateOfClimb)
    }
    "keep rate of climb changes within bounds" in new Helper {
      val (_, real) = actor()
      real.receive(RateChange(2f))
      real.rateOfClimb must be(real.maxRateOfClimb)
    }
    "calculate altitude changes" in new Helper {
      val ref = system.actorOf(Props(Altimeter()))
      ref ! EventSource.RegisterListener(testActor)
      ref ! RateChange(1f)
      fishForMessage() {
        case AltitudeUpdate(altitude) if altitude == 0f =>
          false
        case AltitudeUpdate(altitude) =>
          true
      }
    }
    "send events" in new Helper {
      val (ref, _) = actor()
      Await.ready(EventSourceSpy.latch, 1.second)
      EventSourceSpy.latch.isOpen must be(true)
    }
  }
}