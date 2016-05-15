import cats.data.{NonEmptyList, Xor}
import cats.std.list._

/** Logical rules of Go, as described in https://tromp.github.io/go.html.
  * Inspired from the haskell implementation linked on that page. */
object Go {
  // 1a. Go is played on a 19x19 square grid of points
  val size: Int = 19
  val cords: List[Int] = 1.to(size).toList
  case class Point(x: Int, y: Int) {
    def neighbours: List[Point] =
      List((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1))
        .collect {
          case (a, b) if a >= 1 && a <= size && b >= 1 && b <= size =>
            Point(a, b)
        }
  }
  // val points: List[Point] = for { x <- cords; y <- cords } yield Point(x, y)

  // 1b. By two players called Black and White.
  sealed trait Player
  case object Black extends Player
  case object White extends Player

  // 2. Each point on the grid may be colored black, white or empty.
  sealed trait Color
  case object Empty extends Color
  case class Stone(p: Player) extends Color

  type Position = Point => Color

  // 3. A point P, not colored C, is said to reach C, if there is a path of (vertically or horizontally) adjacent points of P's color from P to a point of color C.

  def connectedGroup(position: Position)(point: Point): Set[Point] = {
    import collection.{mutable => m}
    val color: Color = position(point)
    val toSee: m.Queue[Point] = m.Queue(point)
    val seen: m.Set[Point] = m.Set.empty
    val group: m.Set[Point] = m.Set.empty

    while (!toSee.isEmpty) {
      val p = toSee.dequeue
      seen += p
      if (position(p) == color) {
        group += p
        toSee ++= p.neighbours.filterNot(seen.contains)
      }
    }

    group.toSet
  }

  // 4. Clearing a color is the process of emptying all points of that color that don't reach empty.
  def clear(position: Position, points: List[Point]): Position = {
    def dead(group: Set[Point]) = group
      .filter(_.neighbours.map(position).contains(Empty))
      .isEmpty

    val captured = points
      .map(connectedGroup(position))
      .filter(dead)
      .flatten

    pt => if (captured.contains(pt)) Empty else position(pt)
  }

  // 5a. Starting with an empty grid,
  val emptyPosition: Position = _ => Empty
  // 5b. the players alternate turns, starting with Black.
  def result(turns: List[Turn]): Position = null // TODO

  // 6a. A turn is either a pass; or a move
  sealed trait Turn
  case object Pass extends Turn
  case class Move(point: Point) extends Turn

  sealed trait IllegalMove
  // 6b. that doesn't repeat an earlier grid coloring.
  case class Superko(cycleLength: Int) extends IllegalMove
  // 8. The game ends after two consecutive passes.
  case object PlayAfterTwoPasses extends IllegalMove
  // 7a. A move consists of coloring an empty point one's own color;
  case object Occupied extends IllegalMove

  // 7b. then clearing the opponent color, and then clearing one's own color.
  def move(player: Player, point: Point, position: Position): Position = {
    val moved: Position = pt =>
      if (pt == point) Stone(player) else position(pt)

    val affectedOther: List[Point] = point.neighbours
      .filter(n => position(n) == Stone(if (player == White) Black else White))

    val cleanedOther: Position = clear(moved, affectedOther)
    val cleanedSelf: Position = clear(cleanedOther, List(point))
    cleanedSelf
  }

  def play(player: Player, turn: Turn)(past: NonEmptyList[Position])
      : Xor[IllegalMove, NonEmptyList[Position]] = {
    val position: Position = past.head
    val unwraped: List[Position] = past.unwrap
    turn match {
      case Pass =>
        if (Some(position) == past.tail.headOption) // TODO proper test
          Xor.Left(PlayAfterTwoPasses)
        else
          Xor.Right(NonEmptyList(position, unwraped))

      case Move(point) =>
        if (position(point) != Empty)
          Xor.Left(Occupied)
        else {
          val moved = move(player, point, position)
          unwraped.zipWithIndex.collectFirst { case (p, i) if p == moved =>
            Xor.Left(Superko(i)) // TODO proper test
          }.getOrElse(
            Xor.Right(NonEmptyList(moved, unwraped))
          )
        }
    }
  }

  // 9. A player's score is the number of points of her color, plus the number of empty points that reach only her color.

  // 10. The player with the higher score at the end of the game is the winner. Equal scores result in a tie.
}
