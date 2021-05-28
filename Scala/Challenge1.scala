
/*
 * This is Challenge1.
 * Where you see ??? you need to write the code that will implement the function.
 */
object Challenge1 {

  /** 
    Return the list using reverse method
   */
  def isPalindrome[T](list:List[T]):Boolean = {
    list == list.reverse
  }

  /** 
    Zip with index creates a seq of tuples then forall returns true if the expression value > index is true for all tuples
   */
  def entriesBiggerThanIndex(list:List[Int]):Boolean = {
    list.zipWithIndex.forall((x, i) => x > i )
  }


  /**
   * Call isPalindrome passing in the List result from using zipWithIndex to again create list of tuples then filter those looking for every second entry. Then use map on all the tuples to extract just the values
  */
  def secondPalindrome[T](list:List[T]):Boolean = {
    isPalindrome(list.zipWithIndex.filter((_, i) => i % 2 == 1).map((num, _) => num))
  }

}