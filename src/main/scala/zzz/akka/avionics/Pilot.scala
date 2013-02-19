package zzz.akka.avionics

import akka.actor.{Actor, ActorRef}
import akka.actor.Actor.emptyBehavior

object Pilots {

  case object ReadyToGo

  case object RelinquishControl

}

class Pilot extends Actor {

  import Pilots._
  import Plane._

  var controls: ActorRef = context.system.deadLetters
  var copilot: ActorRef = context.system.deadLetters
  var autopilot: ActorRef = context.system.deadLetters
  val copilotName = context.system.settings.config.getString("zzz.akka.avionics.flightcrew.copilotName")

  def receive = {
    case ReadyToGo =>
      context.parent ! Plane.GiveMeControl
      copilot = context.actorFor("../" + copilotName)
      autopilot = context.actorFor("../AutoPilot")

    // Changed slightly from the book in order to compile
    case RelinquishControl =>
      controls = context.system.deadLetters
  }
}

class CoPilot extends Actor {

  import Pilots._

  var controls: ActorRef = context.system.deadLetters
  var pilot: ActorRef = context.system.deadLetters
  var autopilot: ActorRef = context.system.deadLetters
  val pilotName = context.system.settings.config.getString("zzz.akka.avionics.flightcrew.pilotName")

  def receive = {
    case ReadyToGo =>
      pilot = context.actorFor("../" + pilotName)
      autopilot = context.actorFor("../AutoPilot")
  }
}

// Not shown in the book, but required to make the examples compile...
class AutoPilot extends Actor {

  def receive = emptyBehavior

}

trait PilotProvider {
  def pilot: Actor = new Pilot
  def copilot: Actor = new CoPilot
  def autopilot: Actor = new AutoPilot
}