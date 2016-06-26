package nineteen.game

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
    val positions = Go.playAll(Seq(Point5(1, 2), Point5(1, 1), Point5(2, 1)).map(Move.apply))
    assert(show(positions.right.get.head) ==
        """. x . . .
          |x . . . .
          |. . . . .
          |. . . . .
          |. . . . .""".split('\n').map(_.trim).mkString)
  }

  test("5x5 3-1 score") {
    val positions = Go.playAll(Seq(Point5(1, 2), Point5(3, 3), Point5(2, 1)).map(Move.apply))
    val p = positions.right.get.head
    assert(show(p) ==
      """. x . . .
        |x . . . .
        |. . o . .
        |. . . . .
        |. . . . .""".split('\n').map(_.trim).mkString)
    assert(p.score(Black) -> p.score(White) == 3 -> 1)
  }

  test("5x5 9-11 score") {
    val p = Go.playAll(Seq(
      Point5(3, 4), Point5(3, 3), Point5(4, 3), Point5(4, 4), Point5(4, 5), Point5(2, 4),
      Point5(5, 4), Point5(3, 2), Point5(2, 5), Point5(1, 4), Point5(4, 2), Point5(3, 1)
    ).map(Move.apply)).right.get.head
    assert(show(p) ==
      """. . o . .
        |. . o x .
        |. . o x .
        |o o x . x
        |. x . x .""".split('\n').map(_.trim).mkString)
    assert(p.score(Black) -> p.score(White) == 9 -> 11)
  }

  // Optimal play for opening at (3, 4)
  // http://erikvanderwerf.tengen.nl/5x5/5x5solved.html
  val movesOptimal34: Seq[Point5] = Seq(
    (3, 4), (3, 3), (4, 3), (4, 4), (4, 5),
    (2, 4), (5, 4), (3, 2), (2, 5), (1, 4),
    (4, 2), (3, 1), (1, 2), (2, 2), (3, 5),
    (1, 3), (4, 1), (1, 1), (1, 5)
  ).map(Function.tupled(Point5.apply))

  test("5x5 full game positions") {
    val expectedPositions: Seq[String] = Seq(
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
    Go.playAll(movesOptimal34.map(Move.apply)).right.get.reverse
      .zip(expectedPositions).foreach { case (position, expected) =>
        assert(show(position) == expected)
      }
  }

  test("5x5 full game scores integration") {
    val expectedScores: Seq[(Int, Int)] = Seq(
      (25, 0), (1, 1), (2, 1), (2, 2), (3, 2), (3, 3), (6, 2), (6, 3), (8, 3), (8, 4),
      (9, 4), (9, 11), (10, 5), (10, 6), (10, 6), (10, 8), (14, 8), (13, 11), (14, 11)
    )
    Go.playAll(movesOptimal34.map(Move.apply)).right.get.reverse
      .zip(expectedScores).foreach { case (position, (backScore,  whiteScore)) =>
        assert(position.score(Black) -> position.score(White) == backScore -> whiteScore)
      }
  }

  test("5x5 occupied") {
    val moves = Seq(Move(Point5(1, 1)), Pass, Move(Point5(1, 1)))
    assert(Go.playAll(moves) == Left(Occupied))
  }

  test("5x5 superko") {
    val moves = Seq(Point5(1, 2), Point5(1, 1), Point5(2, 1), Point5(1, 1))
    assert(Go.playAll(moves.map(Move.apply)) == Left(Superko(0)))
  }
}
