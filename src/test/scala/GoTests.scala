package game

import org.scalatest.FunSuite

class GoTests extends FunSuite {
  val players: Stream[Player] = Stream.continually(List(Black, White)).flatten

  def show(position: Position[Point5]): String =
    (1 to 5).map { y =>
      (1 to 5).map { x =>
        position.at(Point5(x, y)) match {
          case Empty => "."
          case Stone(Black) => "x"
          case Stone(White) => "o"
        }
      }.mkString(" ")
    }.mkString("|")

  test("5x5 capture") {
    val moves = Seq(Point5(1, 2), Point5(1, 1), Point5(2, 1))
    val p = players.zip(moves).foldLeft(Position.empty[Point5]) {
      case (position, (player, point)) =>
        position.move(player, point)
    }
    assert(show(p) ==
        """. x . . .
          |x . . . .
          |. . . . .
          |. . . . .
          |. . . . .""".split('\n').map(_.trim).mkString)
  }

  test("5x5 3-1 score") {
    val moves = Seq(Point5(1, 2), Point5(3, 3), Point5(2, 1))
    val p = players.zip(moves).foldLeft(Position.empty[Point5]) {
      case (position, (player, point)) =>
        position.move(player, point)
    }
    assert(show(p) ==
      """. x . . .
        |x . . . .
        |. . o . .
        |. . . . .
        |. . . . .""".split('\n').map(_.trim).mkString)
    assert(p.score(Black) -> p.score(White) == 3 -> 1)
  }

  test("5x5 9-11 score") {
    val moves = Seq(
      Point5(3, 4), Point5(3, 3), Point5(4, 3), Point5(4, 4), Point5(4, 5), Point5(2, 4),
      Point5(5, 4), Point5(3, 2), Point5(2, 5), Point5(1, 4), Point5(4, 2), Point5(3, 1)
    )
    val p = players.zip(moves).foldLeft(Position.empty[Point5]) {
      case (position, (player, point)) => position.move(player, point)
    }
    assert(show(p) ==
      """. . o . .
        |. . o x .
        |. . o x .
        |o o x . x
        |. x . x .""".split('\n').map(_.trim).mkString)
    assert(p.score(Black) -> p.score(White) == 9 -> 11)
  }

  // http://erikvanderwerf.tengen.nl/5x5/5x5solved.html
  val movesOptimal34: Seq[Point5] = Seq(
    (3, 4), (3, 3), (4, 3), (4, 4), (4, 5),
    (2, 4), (5, 4), (3, 2), (2, 5), (1, 4),
    (4, 2), (3, 1), (1, 2), (2, 2), (3, 5),
    (1, 3), (4, 1), (1, 1), (1, 5)
  ).map(Function.tupled(Point5.apply))

  test("5x5 positions integration") {
    // Optimal play for opening at Point5(3, 4)
    val positionsOptimal34: Seq[String] = Seq(
      ". . . . .|. . . . .|. . . . .|. . x . .|. . . . .",
      ". . . . .|. . . . .|. . o . .|. . x . .|. . . . .",
      ". . . . .|. . . . .|. . o x .|. . x . .|. . . . .",
      ". . . . .|. . . . .|. . o x .|. . x o .|. . . . .",
      ". . . . .|. . . . .|. . o x .|. . x o .|. . . x .",
      ". . . . .|. . . . .|. . o x .|. o x o .|. . . x .",
      ". . . . .|. . . . .|. . o x .|. o x . x|. . . x .",
      ". . . . .|. . o . .|. . o x .|. o x . x|. . . x .",
      ". . . . .|. . o . .|. . o x .|. o x . x|. x . x .",
      ". . . . .|. . o . .|. . o x .|o o x . x|. x . x .",
      ". . . . .|. . o x .|. . o x .|o o x . x|. x . x .",
      ". . o . .|. . o x .|. . o x .|o o x . x|. x . x .",
      ". . o . .|x . o x .|. . o x .|o o x . x|. x . x .",
      ". . o . .|x o o x .|. . o x .|o o x . x|. x . x .",
      ". . o . .|x o o x .|. . o x .|o o x . x|. x x x .",
      ". . o . .|x o o x .|o . o x .|o o x . x|. x x x .",
      ". . o x .|x o o x .|o . o x .|o o x . x|. x x x .",
      "o . o x .|. o o x .|o . o x .|o o x . x|. x x x .",
      "o . o x .|. o o x .|o . o x .|o o x . x|x x x x ."
    )
    players.zip(movesOptimal34.zip(positionsOptimal34)).foldLeft(Position.empty[Point5]) {
      case (position, (player, (move, expected))) =>
        val next = position.move(player, move)
        assert(show(next) == expected)
        next
    }
  }

  test("5x5 scores integration") {
    val scoresOptimal34: Seq[(Int, Int)] = Seq(
      (25, 0), (1, 1),  (2, 1), (2, 2),  (3, 2),
      (3, 3), (6, 2),  (6, 3),  (8, 3),  (8, 4),
      (9, 4),  (9, 11), (10, 5), (10, 6), (10, 6),
      (10, 8), (14, 8), (13, 11), (14, 11)
    )
    players.zip(movesOptimal34.zip(scoresOptimal34)).foldLeft(Position.empty[Point5]) {
      case (position, (player, (move, (backScore, whiteScore)))) =>
        val next = position.move(player, move)
        assert(next.score(Black) -> next.score(White) == backScore -> whiteScore)
        next
    }
  }
}
