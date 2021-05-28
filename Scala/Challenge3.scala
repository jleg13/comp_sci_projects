
/**
  Challenge 3 is the "8 queens problem" -- how to put eight queens on a chessboard without any of them attacking
  each other. In chess, queens can move and attack horizontally, vertically, and diagonally
  and can move (and attack) an unlimited number of squares away.
  */
object Challenge3 {

  type Position = (Int, Int)
  type Candidate = Seq[Position]

  /** Compare y coordinate */
  def sameRow(p1:Position, p2:Position):Boolean = p1._2 == p2._2

  /** Compare x coordinate */
  def sameCol(p1:Position, p2:Position):Boolean = p1._1 == p2._1

  /** 
   Check the difference between p1 y coordinate and p2 y coordinate is equal to difference between p1 x coordinate and p2 x coordinate. Absolute value value takes into account p1 and p2 relative position along diagonal
  */
  def sameDiagonal(p1:Position, p2:Position):Boolean = {
    (p1._1 - p2._1).abs == (p1._2 - p2._2).abs
  }

  /**
   Check if positions are in same col/row/diagonal
  */
  def attackingEachOther(p1:Position, p2:Position):Boolean = {
    if(sameRow(p1,p2) && sameCol(p1,p2)) false
      else sameRow(p1,p2) == sameCol(p1,p2) == sameDiagonal(p1,p2)
  }

  /**
   Use nested for loop to test all possible positions, then use foldLeft to see if seq of booleans contains a true value
    */
  def seqContainsAttack(queens:Seq[Position]):Boolean = {
    val eval = for { i <- queens; j <- queens} yield {
        attackingEachOther(i, j)
      }
      eval.foldLeft(false)(_ || _)
  }

  /**
   Valid solution must have 8 queens in seq and contain no attacking queens
    */
  def isValid(queens:Seq[Position]):Boolean = {
    queens.length == 8 && !seqContainsAttack(queens)
  }

  /**
    Use zipWithIndex to create a sequence of tuples of positions, switch coordinates to match the correct format
    */
  def expandShortHand(queens:Seq[Int]):Seq[Position] = {
    queens.zipWithIndex.map((col, row) => (row, col))
  }

  /**
   Filter the candidate solutions call isValid return seq of only the valid solutions
    */
  def filterCandidates(candidates:Seq[Candidate]):Seq[Candidate] = {
    candidates.filter(candidate => isValid(candidate))
  }

  /**
    * Now we're going to use another trick to make the whole computation very small. As well as the queens all being
    * in different columns, they're also all in different rows. So every solution is going to be a permutation of
    * Seq(1, 2, 3, 4, 5, 6, 7, 8). But we're going to need to filter the permutations to only the ones that work.
    *
    * If your previous functions work, this should work.
    */
  def eightQueens:Seq[Candidate] = {
    val perms = Seq(0, 1, 2, 3, 4, 5, 6, 7).permutations.toSeq
    val candidates = perms.map(expandShortHand)
    filterCandidates(candidates)
  }


}