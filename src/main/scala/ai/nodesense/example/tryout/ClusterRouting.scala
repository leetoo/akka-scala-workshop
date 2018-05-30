package ai.nodesense.example.tryout

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.cluster.{Cluster, MemberStatus}
import akka.routing.FromConfig
import com.typesafe.config.ConfigFactory

object ClusterRouting extends App {

  class EchoActor extends Actor with ActorLogging {
    log.info("Started")
    def receive = {
      case m =>
        log.info(s"got $m from $sender()")
        sender() ! m
    }
  }

  class Sender(group: ActorRef) extends Actor with ActorLogging {

    for (n <- 0 to 5) {
      log.info(s"Pinging $group for the $n time")
      group ! s"hello $n"
    }

    def receive = {
      case m => log.info(s"got $m back from $sender()")
    }
  }

  val commonConfig = ConfigFactory.parseString(
    """
      |akka {
      |  actor {
      |    provider = "cluster"
      |
      |    deployment {
      |      /ClusterAwareActor {
      |        router = random-group
      |        routees.paths = ["/user/EchoGroup"]
      |        cluster {
      |          enabled = on
      |          allow-local-routees = off
      |        }
      |      }
      |      /EchoGroup {
      |        router = random-pool
      |        nr-of-instances = 10
      |        pool-dispatcher {
      |          executor = "fork-join-executor"
      |          fork-join-executor {
      |            parallelism-min = 20
      |            parallelism-max = 20
      |          }
      |        }
      |      }
      |    }
      |  }
      |  remote {
      |    netty.tcp {
      |      hostname = "127.0.0.1"
      |    }
      |  }
      |  cluster {
      |    seed-nodes = ["akka.tcp://cluster@127.0.0.1:3551"]
      |    metrics.enabled = off
      |  }
      |}
    """.stripMargin)

  val system1 = ActorSystem(
    "cluster",
    ConfigFactory.parseString("akka.remote.netty.tcp.port = 3551").withFallback(commonConfig))

  val system2 = ActorSystem(
    "cluster",
    ConfigFactory.parseString("akka.remote.netty.tcp.port = 3552").withFallback(commonConfig))

  val system3 = ActorSystem(
    "cluster",
    ConfigFactory.parseString("akka.remote.netty.tcp.port = 3553").withFallback(commonConfig))


  val systems = List(system1, system2, system3)
  systems.foreach(system =>
    system.actorOf(FromConfig.props(Props[EchoActor]), "EchoGroup") // local random pool
  )


  // wait for cluster to have formed and all nodes knowing about that
  def upNodes(sys: ActorSystem): Int = Cluster(sys).state.members.count(_.status == MemberStatus.Up)
  while(!systems.forall(s => upNodes(s) == systems.size)) {
    Thread.sleep(250)
  }

  val systemAndGroups = systems.map { system =>
    val group = system.actorOf(FromConfig.props(Props.empty), "ClusterAwareActor") // cluster aware group
    system -> group
  }

  // wait a bit because the groups won't find all actors until after a while
  Thread.sleep(250)

  systemAndGroups.foreach { case (system, group) =>
    system.actorOf(Props(new Sender(group)), "sender")
  }

}