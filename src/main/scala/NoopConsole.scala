import cats.Applicative
import cats.effect.std.Console

object NoopConsole {
  def apply[F[_]: Applicative]: Console[F] = 
    new Console[F] {
      override def error[A](a: A)(implicit S: cats.Show[A]): F[Unit] = Applicative[F].unit
      override def errorln[A](a: A)(implicit S: cats.Show[A]): F[Unit] = Applicative[F].unit
      override def print[A](a: A)(implicit S: cats.Show[A]): F[Unit] = Applicative[F].unit
      override def println[A](a: A)(implicit S: cats.Show[A]): F[Unit] = Applicative[F].unit
      override def readLineWithCharset(charset: java.nio.charset.Charset): F[String] = Applicative[F].pure("")
    }
}
