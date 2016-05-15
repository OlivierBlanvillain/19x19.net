package game

import cats.data.NonEmptyList
import cats.std.list._
// import cats.std.either._
import game.Shape.ops._

// 1a. Go is played on a 19x19 square grid of points
case class Point19(x: Int, y: Int) {
  require(x >= 1 && x <= 19)
  require(y >= 1 && y <= 19)
}

// 1b. By two players called Black and White.
sealed trait Player
case object Black extends Player
case object White extends Player

// 2. Each point on the grid may be colored black, white or empty.
sealed trait Color
case object Empty extends Color
case class Stone(p: Player) extends Color

class Position[P: Shape] private (repr: Map[P, Color]) {
  def at(point: P): Color =
    repr(point)

  def set(point: P, color: Color): Position[P] =
    new Position(repr + (point -> color))

  def capture(points: TraversableOnce[P]): Position[P] =
    new Position(repr ++ points.map(_ -> Empty))

  val empty =
    new Position(Map()).capture(Shape[P].all)
}

// 6a. A turn is either a pass; or a move
sealed trait Turn[+P]
case object Pass extends Turn[Nothing]
case class Move[P](point: P) extends Turn[P]

sealed trait IllegalMove
// 6b. that doesn't repeat an earlier grid coloring.
case class Superko(cycleLength: Int) extends IllegalMove
// 8. The game ends after two consecutive passes.
case object PlayAfterTwoPasses extends IllegalMove
case object Occupied extends IllegalMove

/** Logical rules of Go, as described in https://tromp.github.io/go.html.
  * Inspired from the haskell implementation linked on that page. */
object Go {
  // 3. A point P, not colored C, is said to reach C, if there is a path of adjacent points of P's color from P to a point of color C.
  def connectedGroup[P: Shape](position: Position[P])(point: P): Set[P] = {
    import collection.{mutable => m}
    val color: Color = position.at(point)
    val toSee: m.Queue[P] = m.Queue(point)
    val seen: m.Set[P] = m.Set.empty
    val group: m.Set[P] = m.Set.empty

    while (!toSee.isEmpty) {
      val p = toSee.dequeue
      seen += p
      if (position.at(p) == color) {
        group += p
        toSee ++= p.neighbours.filterNot(seen.contains)
      }
    }

    group.toSet
  }

  // 4. Clearing a color is the process of emptying all points of that color that don't reach empty.
  def clear[P: Shape](position: Position[P], points: Seq[P]): Position[P] = {
    def dead(group: Set[P]) = group
      .filter(_.neighbours.map(position.at).contains(Empty))
      .isEmpty

    val captured = points
      .map(connectedGroup(position))
      .filter(dead)
      .flatten

    position.capture(captured)
  }

  // 5a. Starting with an empty grid,
  // 5b. the players alternate turns, starting with Black.

  // 7. A move consists of coloring an empty point one's own color; then
  // clearing the opponent color, and then clearing one's own color.
  def move[P: Shape](player: Player, point: P, position: Position[P]): Position[P] = {
    val other = Stone(if (player == White) Black else White)
    val affectedOther: Seq[P] = point.neighbours.filter(n => position.at(n) == other)
    val cleanedOther: Position[P] = clear(position.set(point, Stone(player)), affectedOther)
    val cleanedSelf: Position[P] = clear(cleanedOther, List(point))
    cleanedSelf
  }

  def play[P: Shape](player: Player, turn: Turn[P])(past: NonEmptyList[Position[P]])
      : Either[IllegalMove, NonEmptyList[Position[P]]] = {
    val position: Position[P] = past.head
    val unwraped: List[Position[P]] = past.unwrap
    turn match {
      case Pass =>
        if (Some(position) == past.tail.headOption)
          Left(PlayAfterTwoPasses)
        else
          Right(NonEmptyList(position, unwraped))

      case Move(point) =>
        if (position.at(point) != Empty)
          Left(Occupied)
        else {
          val moved = move(player, point, position)
          unwraped.zipWithIndex.collectFirst { case (p, i) if p == moved =>
            Left(Superko(i))
          }.getOrElse(
            Right(NonEmptyList(moved, unwraped))
          )
        }
    }
  }

  // 9. A player's score is the number of points of her color, plus the number of empty points that reach only her color.
  def score[P: Shape](player: Player, position: Position[P]): Int =
    Shape[P].all.map { point =>
      position.at(point) match {
        case Stone(p) =>
          if (p == player) 1 else 0
        case Empty =>
          // This could made way faster by memoizing on the result of connectedGroup
          val owners = connectedGroup(position)(point).flatMap(_.neighbours).toSet
          if (owners == Set(player)) 1 else 0
      }
    }.sum

  // 10. The player with the higher score at the end of the game is the winner. Equal scores result in a tie.
}
