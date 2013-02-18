package zzz.akka.avionics

// Imports to help us create Actors, plus logging

import akka.actor.{Props, Actor, ActorSystem, ActorLogging}

// The duration package object extends Ints with some
// timing functionality

import scala.concurrent.duration._

object Altimeter {

  // Sent to the Altimeter to inform it about
  // rate-of-climb changes
  case class RateChange(amount: Float)

  // Sent by the Altimeter at regular intervals
  case class AltitudeUpdate(altitude: Double)

  def apply() = new Altimeter with ProductionEventSource

}

class Altimeter extends Actor with ActorLogging {
    this: EventSource =>

  import Altimeter._

  // We need an "ExecutionContext" for the scheduler.  This
  // Actor's dispatcher can serve that purpose.  The
  // scheduler's work will be dispatched on this Actor's own
  // dispatcher
  implicit val ec = context.dispatcher
  // The maximum ceiling of our plane in 'feet'
  val ceiling = 43000
  // The maximum rate of climb for our plane in
  // 'feet per minute'
  val maxRateOfClimb = 5000
  // The varying rate of climb depending on the movement of
  // the stick
  var rateOfClimb = 0f
  // Our current altitude
  var altitude = 0d
  // As time passes, we need to change the altitude based on
  // the time passed.  The lastTick allows us to figure out
  // how much time has passed
  var lastTick = System.currentTimeMillis

  // We need to periodically update our altitude.  This
  // scheduled message send will tell us when to do that
  val ticker = context.system.scheduler.schedule(100.millis, 100.millis, self, Tick)

  // An internal message we send to ourselves to tell us
  // to update our altitude
  case object Tick

  def altimeterReceive: Receive = {
    // Our rate of climb has changed
    case RateChange(amount) =>
      // Truncate the range of 'amount' to [-1, 1]
      // before multiplying
      rateOfClimb = amount.min(1.0f).max(-1.0f) * maxRateOfClimb
      log info(s"Altimeter changed rate of climb to $rateOfClimb.")

    // Calculate a new altitude
    case Tick =>
      val tick = System.currentTimeMillis
      altitude = altitude + ((tick - lastTick) / 60000.0) * rateOfClimb
      lastTick = tick
      sendEvent(AltitudeUpdate(altitude))
  }

  def receive = eventSourceReceive orElse altimeterReceive

  // Kill our ticker when we stop
  override def postStop(): Unit = ticker.cancel
}
