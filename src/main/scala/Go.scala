package game

import game.Shape.ops._
import scala.collection.mutable

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

// 6a. A turn is either a pass; or a move
sealed trait Turn[+P]
case object Pass extends Turn[Nothing]
case class Move[P](point: P) extends Turn[P]

sealed trait IllegalMove
// 6b. that doesn't repeat an earlier grid coloring.
case class Superko(cycleLength: Int) extends IllegalMove
case object Occupied extends IllegalMove

case class Position[P: Shape](repr: Map[P, Color]) {
  def at(point: P): Color =
    repr(point)

  def set(point: P, color: Color): Position[P] =
    Position(repr + (point -> color))

  def capture(points: TraversableOnce[P]): Position[P] =
    Position(repr ++ points.map(_ -> Empty))

  // 3. A point P, not colored C, is said to reach C, if there is a path of adjacent points of P's color from P to a point of color C.
  def connectedGroup(point: P): Set[P] = {
    val color: Color = this.at(point)
    val toSee: mutable.Queue[P] = mutable.Queue(point)
    val seen:  mutable.Set[P] = mutable.Set.empty
    val group: mutable.Set[P] = mutable.Set.empty

    while (!toSee.isEmpty) {
      val p: P = toSee.dequeue
      seen += p
      if (this.at(p) == color) {
        group += p
        toSee ++= p.neighbours.filterNot(seen.contains)
      }
    }

    group.toSet
  }

  // 4. Clearing a color is the process of emptying all points of that color that don't reach empty.
  def clear(points: Seq[P]): Position[P] = {
    def dead(group: Set[P]): Boolean = group
      .filter(_.neighbours.map(this.at).contains(Empty))
      .isEmpty

    val captured: Seq[P] = points
      .map(this.connectedGroup)
      .filter(dead)
      .flatten

    this.capture(captured)
  }

  // 7. A move consists of coloring an empty point one's own color; then
  // clearing the opponent color, and then clearing one's own color.
  def move(player: Player, point: P): Position[P] = {
    val other: Color = Stone(if (player == White) Black else White)
    val affectedOther: Seq[P] = point.neighbours.filter(n => this.at(n) == other)

    this.set(point, Stone(player))
      .clear(affectedOther)
      .clear(List(point))
  }

  // 9. A player's score is the number of points of her color, plus the number of empty points that reach only her color.
  def score(player: Player): Int =
    Shape[P].all.map { point =>
      this.at(point) match {
        case Stone(p) =>
          if (p == player) 1 else 0
        case Empty =>
          val other: Color = Stone(if (player == White) Black else White)
          val owners: Set[Color] = this.connectedGroup(point).flatMap(_.neighbours).map(this.at)
          // This could made way faster by memoizing on the result of connectedGroup
          if (owners.contains(other)) 0 else 1
      }
    }.sum
}

object Position {
  def empty[P: Shape]: Position[P] =
    Position(Map()).capture(Shape[P].all)
}

/** Logical rules of Go, as described in https://tromp.github.io/go.html.
  * Inspired from the haskell implementation linked on that page. */
object Go {
  // 5a. Starting with an empty grid,
  def playOnce[P: Shape](player: Player, turn: Turn[P], past: List[Position[P]])
      : Either[IllegalMove, List[Position[P]]] = {
    val position: Position[P] = past.headOption.getOrElse(Position.empty)
    turn match {
      case Pass =>
        Right(position :: past)

      case Move(point) =>
        if (position.at(point) != Empty)
          Left(Occupied)
        else {
          val moved = position.move(player, point)
          past.zipWithIndex.collectFirst { case (p, i) if p == moved =>
            Left(Superko(i))
          }.getOrElse(
            Right(moved :: past)
          )
        }
    }
  }

  // 5b. the players alternate turns, starting with Black.
  // 8. The game ends after two consecutive passes.
  def playAll[P: Shape](turns: Seq[Turn[P]]): Either[IllegalMove, List[Position[P]]] = {
    val players: Stream[Player] = Stream.continually(List(Black, White)).flatten
    val seed: Either[IllegalMove, List[Position[P]]] = Right(Nil)

    players.zip(turns).foldLeft(seed) {
      case (l @ Left(_), _) => l
      case (Right(past), (player, turn)) => playOnce(player, turn, past)
    }
  }
}
