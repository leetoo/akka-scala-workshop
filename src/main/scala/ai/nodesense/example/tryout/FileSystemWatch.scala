package ai.nodesense.example.tryout


import java.nio.file._
import java.nio.file.StandardWatchEventKinds._
import java.nio.file.attribute.BasicFileAttributes
import collection.JavaConversions._
import concurrent.duration._
import akka.actor.{Cancellable, ActorRef, Actor}
import ai.nodesense.example.tryout.FileSystemWatchActor._


object FileSystemWatchActor {
  // messages for interacting with the actor
  // accepted
  case class WatchDir(path: Path, listener: ActorRef)
  case class StopWatchingDir(path: Path, listener: ActorRef)
  // sent
  case class Created(path: Path)
  case class Deleted(path: Path)
  case class Changed(path: Path)

  // internal
  case object CheckForNewEvents

}

class FileSystemWatchActor extends Actor {

  import context.dispatcher

  var listeners = Seq[(Path, ActorRef)]()

  var watchService: Option[WatchService] = None

  var newEventsCancellable: Option[Cancellable] = None


  override def preStart() {
    watchService = Some(FileSystems.getDefault.newWatchService())
    newEventsCancellable = Some(context.system.scheduler.schedule(100 millisecond, 100 millisecond)(self ! CheckForNewEvents))
  }
  override def postStop() {
    watchService.foreach(_.close())
    watchService = None
    newEventsCancellable.foreach(_.cancel())
    newEventsCancellable = None
  }

  def receive = {

    case WatchDir(path, listener) =>
      listeners = listeners :+ (path, listener)
      watchRecursively(path)

    case StopWatchingDir(path, listener) =>
      listeners = listeners.filterNot(_ == (path, listener))

    case CheckForNewEvents => pollEvents()
  }

  def informListeners(message: AnyRef, path: Path) {
    listeners.filter { case (path, _ ) =>
      path.startsWith(path)
    }.foreach { case (_, listener) =>
      listener ! message
    }
  }

  def pollEvents() {
    val maybeKey = for {
      service <- watchService
      key <- Option(service.poll())
    } yield key

    maybeKey.map { key =>
      key.pollEvents().foreach { event: WatchEvent[_] =>
        val relativePath = event.context().asInstanceOf[Path]
        val path = key.watchable().asInstanceOf[Path].resolve(relativePath)
        val message = event.kind() match {

          case ENTRY_CREATE =>
            if (path.toFile.isDirectory) watchRecursively(path)
            Created(path)

          case ENTRY_DELETE => Deleted(path)
          case ENTRY_MODIFY => Changed(path)
        }
        informListeners(message, path)
      }
      key.reset()
    }
  }


  def watchRecursively(root: Path) {
    watch(root)
    Files.walkFileTree(root, new SimpleFileVisitor[Path] {
      override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes) = {
        watch(dir)
        FileVisitResult.CONTINUE
      }
    })
  }

  private def watch(path: Path) {
    watchService.foreach(service => path.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY))
  }
}