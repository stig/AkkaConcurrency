package zzz.akka.avionics

import akka.actor.{OneForOneStrategy, AllForOneStrategy, SupervisorStrategy}
import akka.actor.SupervisorStrategy.Decider
import concurrent.duration.Duration

trait SupervisionStrategyFactory {
  def makeStrategy(maxNrRetries: Int,
                   withinTimeRange: Duration)(decider: Decider): SupervisorStrategy
}

trait OneForOneStrategyFactory extends SupervisionStrategyFactory {
  def makeStrategy(maxNrRetries: Int,
                   withinTimeRange: Duration)(decider: Decider): SupervisorStrategy =
    OneForOneStrategy(maxNrRetries, withinTimeRange)(decider)
}

trait AllForOneStrategyFactory extends SupervisionStrategyFactory {
  def makeStrategy(maxNrRetries: Int,
                   withinTimeRange: Duration)(decider: Decider): SupervisorStrategy =
    AllForOneStrategy(maxNrRetries, withinTimeRange)(decider)
}