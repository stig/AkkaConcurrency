package zzz.akka.avionics

import akka.actor.{Props, Actor, ActorLogging}

object Plane {

  // Returns the control surface to the Actor that
  // asks for them
  case object GiveMeControl

}

// We want the Plane to own the Altimeter and we're going to
// do that by passing in a specific factory we can use to
// build the Altimeter
class Plane extends Actor with ActorLogging {

  import Altimeter._
  import Plane._

  val cfgstr = "zzz.akka.avionics.flightcrew"
  val altimeter = context.actorOf(Props(Altimeter()), "Altimeter")

  val controls = context.actorOf(Props(new ControlSurfaces(altimeter)), "ControlSurfaces")
  val config = context.system.settings.config
  val pilot = context.actorOf(Props[Pilot], config.getString(s"$cfgstr.pilotName"))
  val copilot = context.actorOf(Props[CoPilot], config.getString(s"$cfgstr.copilotName"))
  val autopilot = context.actorOf(Props[AutoPilot], "AutoPilot")
  val flightAttendant = context.actorOf(Props(LeadFlightAttendant()), config.getString(s"$cfgstr.leadAttendantName"))

  override def preStart() {
    // Register ourself with the Altimeter to receive updates
    // on our altitude
    altimeter ! EventSource.RegisterListener(self)
    List(pilot, copilot) foreach { _ ! Pilots.ReadyToGo }
  }

  def receive = {
    case AltitudeUpdate(altitude) =>
          log info(s"Altitude is now: $altitude")

    case GiveMeControl =>
      log info ("Plane giving control.")
      sender ! controls
  }


}
