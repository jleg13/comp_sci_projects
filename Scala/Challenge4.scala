
/**

  Challenge 4 introduces Maps.

  In this, we're introducing a new immutable data structure: Map[K, T]
  A "map" associates a key with a value.

  One of the ways you can make a map is from a List or Seq of tuples.
  val list = List(1 -> 'A', 2 -> 'B', 3 -> 'C')
  val map = list.toMap

  or you can create one directly
  val map = Map(1 -> 'A', 2 -> 'B', 3 -> 'C')

  you can also get a Seq or List of tuples from a map by calling toSeq
  val seq:Seq(Int, Char) = map.toSeq

  You can get a new map with altered values by calling updated
  val withD = map.updated(4, 'D')
  val replaced = withD.updated(4, 'd')

  You can get a value from a map (given its key) by calling apply
  val d = replaced.apply(4)
  or, as by convention you don't have to say the word "apply"
  val d = replaced(4)

  */
object Challenge4 {

  val alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

  /**
   Perform action on on all entries to switch keys and values
    */
  def inverse[K, V](map:Map[K,V]):Map[V, K] = {
    map.map((k, v) => (v -> k))
  }

  /**
  Use a range to map to index values of alapha string. Slice the alpha string from each index i until the end and concat with start of string to index i. Use toMap to turn sequence of strings to map of index, alaphabet permutation pair.
    */
  def vignereTable:Map[Int, String] = { 
    (0 to 25).map((i) => i -> (alpha.slice(i, 26) ++ alpha.slice(0, i))).toMap
  }

  /**
   Perform on each character in the alaphbet string indexOf method to get a list of index values. Then toSeq to convert to sequence
    */
  def letterToNum(key:String):Seq[Int] = {
    key.map((ch) => alpha.indexOf(ch)).toSeq
  }

  /**
    Use an algorithm to make sure the Key is the same length as the plaintext
      - take the string key and repeat until is longer then the plaintext then slice the key string to correspond to the len of plaintext
   Then pass this into letterToNum function to get the sequence of index values, use zip with index to get a corresponding index in a tuple. On each of the tuples, get the corresponding entry in the vignere table then on each entry from the vignere table get the corresponding character from the index. Finally make the sequence of characters into a string with mkString
    */
  def encode(plaintext:String, key:String):String = {
    letterToNum((key * (plaintext.length/key.length + 1)).slice(0, plaintext.length)).zipWithIndex.map((i, j) => j -> vignereTable(i)).map((i, str) => str(letterToNum(plaintext)(i))).mkString
  }

  /**
   Reverse the process ensuring the key is the same length as the cipher text. Use the indexs from the cipher text to get the correct characters in the alpha string, and again use mkString to convert from seq
    */
  def decode(cyphertext:String, key:String):String = {
    letterToNum((key * (cyphertext.length/key.length + 1)).slice(0, cyphertext.length)).zipWithIndex.map((i, j) => j -> vignereTable(i)).map((i, str) => str.indexOf(cyphertext(i))).map((i) => alpha(i)).mkString
  }

}