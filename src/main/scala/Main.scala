import cats.effect._
import cats.syntax.all._
import skunk._
import skunk.codec.all._
import skunk.implicits._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import io.opentelemetry.api.GlobalOpenTelemetry
import org.typelevel.otel4s.java.OtelJava
import org.typelevel.otel4s.trace.Tracer

// a data type
case class Pet(name: String, age: Short)

// a service interface
trait PetService[F[_]] {
  def insert(pet: Pet): F[Unit]
  def insert(ps: List[Pet]): F[Unit]
  def selectAll: F[List[Pet]]
}

// a companion with a constructor
object PetService {

  // command to insert a pet
  private val insertOne: Command[Pet] =
    sql"INSERT INTO pets VALUES ($varchar, $int2)"
      .command
      .to[Pet]

  // command to insert a specific list of pets
  private def insertMany(ps: List[Pet]): Command[ps.type] = {
    val enc = (varchar *: int2).to[Pet].values.list(ps)
    sql"INSERT INTO pets VALUES $enc".command
  }

  // query to select all pets
  private val all: Query[Void, Pet] =
    sql"SELECT name, age FROM pets"
      .query(varchar *: int2)
      .to[Pet]

  // construct a PetService
  def fromSession[F[_]: MonadCancelThrow](s: Session[F]): PetService[F] =
    new PetService[F] {

      // With a transaction, the error is logged
      def insert(pet: Pet): F[Unit] = 
        s.prepare(insertOne).flatMap(p => 
          s.transaction.use(_ => 
            p.execute(pet)
              // The error is logged, unless you handle it on every execution inside the tx
              //.handleErrorWith(_ => Logger[F].info("******************************** Handled error on execute").as(skunk.data.Completion.Insert(0)))
          )
        ).void

      // Without a transaction, the error is not logged
      // def insert(pet: Pet): F[Unit] = s.prepare(insertOne).flatMap(_.execute(pet)).void

      def insert(ps: List[Pet]): F[Unit] = s.prepare(insertMany(ps)).flatMap(_.execute(ps)).void
      def selectAll: F[List[Pet]] = s.execute(all)
    }

}

object CommandExample extends IOApp {

  implicit val log: Logger[IO] =
    Slf4jLogger.getLoggerFromName("example-logger")

  def tracer: IO[Tracer[IO]] =
    IO(GlobalOpenTelemetry.get)
      .flatMap(OtelJava.forAsync[IO])
      .flatMap(_.tracerProvider.get("example-service"))

  // a source of sessions
  def session(implicit t: Tracer[IO]): Resource[IO, Session[IO]] =
    Session.single(
      host     = "localhost",
      user     = "postgres",
      database = "postgres",
      password = Some("postgres"),
      redactionStrategy = skunk.RedactionStrategy.All
    )

  // a resource that creates and drops a temporary table
  def withPetsTable(s: Session[IO]): Resource[IO, Unit] = {
    val alloc = s.execute(sql"CREATE TEMP TABLE pets (name varchar PRIMARY KEY, age int2)".command).void
    val free  = s.execute(sql"DROP TABLE pets".command).void
    Resource.make(alloc)(_ => free)
  }

  // some sample data
  val bob     = Pet("Bob", 12)
  val beagles = List(Pet("John", 2), Pet("George", 3), Pet("Paul", 6), Pet("Ringo", 3))

  // our entry point
  def run(args: List[String]): IO[ExitCode] =
    tracer.flatMap { implicit t =>
      session.flatMap(s => withPetsTable(s).map(_ => s)).map(PetService.fromSession(_)).use { s =>
        for {
          _  <- s.insert(bob)
          _  <- s.insert(beagles)

          // The following insert fails due to unique constraint violation
          // Note that this code does not log the error, but it still appears on the console
          _  <- s.insert(bob).handleErrorWith(_ => log.info("******************************* Error"))

          ps <- s.selectAll
          _  <- ps.traverse(p => IO.println(s"***************************************** $p"))
        } yield ExitCode.Success
      }
    }

}
