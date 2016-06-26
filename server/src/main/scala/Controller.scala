package nineteen.server

import monix.reactive.Observable

object Controller {
  def foo(i: Int, j: Int): Observable[Int]       = Observable.fromIterable(i to j)
  def bar(s: String): Observable[Option[String]] = Observable.pure(Some(":" + s))
}
