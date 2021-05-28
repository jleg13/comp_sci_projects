
/**
 * In Challenge 2, the functions you will implement will produce a Scrabble scorer. 
 * It should take into account the letters in the word *and* the squares the word sits on.
 * 
 * Again, where you see ??? you need to write the implementation of the function.
 */
object Challenge2 {

  sealed trait Square
  case object OrdinarySquare extends Square
  case object DoubleLetterScore extends Square
  case object TripleLetterScore extends Square
  case object DoubleWordScore extends Square
  case object TripleWordScore extends Square

  /**
  Create a List with scores of corresponding letters. Then filter to find the string that contains the character being looked up return corresponding score
  */
  def letterScore(char:Char):Int = {
    val scores = List(
        1 ->"AEIOULNSTR",
        2->"DG",
        3->"BCMP",
        4->"FHVWY",
        5->"K",
        8->"JX",
        10->"QZ")

      scores.filter((_, letters) => letters.contains(char)).head._1
  }

  /**
    Create a match on the square. If matched to Double letter then find letter score and * 2, if triple letter find letter score and * 3, all other cases just find letter score
    */
  def letterAndSquareScore(char:Char, sq:Square):Int = { sq match 
        case DoubleLetterScore => letterScore(char) * 2
        case TripleLetterScore => letterScore(char) * 3
        case _ => letterScore(char)
  }

  /** 
   Create a sequence of character, square type tuples, by zipping string word and seq squares together. On each tuple call letterAndSquareScore then sum the resulting seq[int]
    */
  def totalLetterScore(word:String, squares:Seq[Square]):Int = {
    word.zip(squares).map((letter, square) => letterAndSquareScore(letter, square)).sum
  }

  /**
    Factor in Double or triple word scores. If none are present in the seq of squares just call totalLetterScore. Otherwise check the type of each square making a product sum to multiply totalLetterScore by.
    */
  def scrabbleScore(word:String, squares:Seq[Square]):Int = {
      if (!squares.contains(DoubleWordScore) && !squares.contains(TripleWordScore)) {
        totalLetterScore(word, squares)
      } else {
        squares.map({
          case DoubleWordScore => 2
          case TripleWordScore => 3
          case _ => 1
        }).product * totalLetterScore(word, squares)
      }
    }

}