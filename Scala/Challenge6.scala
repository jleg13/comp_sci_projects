
/**
  Challenge 6 

  We have a little card game that uses an unusual deck.
  There are "Add" cards from 1 to 9, a Double card, a Negative card, and a Zero card.

  The way the game is played is you are dealt eight random cards, and you have to put them
  in the order that will give you the highest score.

  The way the score is calculated is this:
  You start with a score of zero and look at the first card.

  If the card is ZeroScore, you reset your running total to zero
  If the card is Add(number), you add the number to your running total
  If the card is NegativeScore, you mutliply the running total by -1.
    (NB: We only multiple the running total by -1. It does not affect how later cards are processed.
      So, Seq(Add(2), NegativeScore) would produce -2, but
          Seq(Add(2), NegativeScore, Add(3)) would produce 1 (the result of adding 3 to -2)
  If the card is DoubleScore, you double the running total  
    (Again, we only double the running total. It does not affect how later cards are processed.
      This isn't Scrabble.)

  You then move on to the next card and do the same, and so on until you have processed
  all the cards in your hand.

  There are NEVER duplicate cards in a hand.
  
  */
object Challenge6 {

  sealed trait Card
  case object DoubleScore extends Card
  case object NegativeScore extends Card
  case class Add(n:Int) extends Card
  case object ZeroScore extends Card

  val allCards = List(DoubleScore, NegativeScore, ZeroScore) ++ (1 to 9).toList.map(Add.apply)
  
  val rng = new scala.util.Random(99)
  def deal:Seq[Card] = rng.shuffle(allCards).take(8)
  
  /**
   Use foldLeft to work out the score for a hand by performing actions based on card type
    */
  def processHand(cards:Seq[Card]):Int = {
    cards.foldLeft(0){(a, b) => b match {
      case DoubleScore => a * 2
      case NegativeScore => a * -1
      case ZeroScore => 0
      case add:Add => a + add.n
    }

    }
  }

  /**
   Perform filter on the hand to select all but the given card
    */
  def removeCard(hand:Seq[Card], card:Card):Seq[Card] = hand.filter(_ !=card)

  /**
   Process the hand with the a given card removed then subtract the value attained by processing the full hand
    */
  def diffFromRemoving(hand:Seq[Card], card:Card):Int = {
    processHand(removeCard(hand, card)) - processHand(hand)
  }

  /**
  Perform diffFromRemoving func on all cards and return card that gives minimal result
    */
  def bestCardToRemove(hand:Seq[Card]):Card = {
    hand.minBy((h) => diffFromRemoving(hand, h))
  }

  /**
   Get all permutations of hands, process each hand and choose maximal result
    */
  def bestOrder(hand:Seq[Card]):Seq[Card] = {
    hand.permutations.maxBy((h) => processHand(h))
  }

}