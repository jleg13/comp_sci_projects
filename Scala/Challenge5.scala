
/**
 
  Challenge 5 is John Conway most famous creation: Conway's Game of Life.
  https://en.wikipedia.org/wiki/Conway's_Game_of_Life
  
  Suppose we have a grid of squares, say 20 by 20
  And a square can be filled in (alive) or not filled in (dead).
  
  And at each "time step", we generate a new grid using the following rules:
  Any live cell with fewer than two live neighbours dies, as if caused by underpopulation.
  Any live cell with two or three live neighbours lives on to the next generation.
  Any live cell with more than three live neighbours dies, as if by overpopulation.
  Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.
  (Each cell has eight neighbours)

  */
object Challenge5 {

  type Position = (Int, Int)
  type ConwayState = Map[Position, Boolean]

  /**
    Return value for associated position in map, or return false
    */
  def isAlive(p:Position, s:ConwayState):Boolean = {
    s.getOrElse(p, false)
  }

  val blinker1:ConwayState = Map(
    (2, 1) -> true, (2, 2) -> true, (2, 3) -> true
  )
  val blinker2:ConwayState = Map(
    (1, 2) -> true, (2, 2) -> true, (3, 2) -> true
  )

  /**
    Filter the map on keys to include only those directly surronding a given position, then get the size of map gives number of live neighbours in
    */
  def liveNeighbours(pos:Position, state:ConwayState):Int = {
    state.view.filterKeys(p => (p._1 <= pos._1 + 1) && (p._1 >= pos._1 - 1) && (p._2 <= pos._2 + 1) && (p._2 >= pos._2 - 1) && p != pos).filter(p => p._2).size
  }

  /**
    Get number of live neighbours then match against the predetermined rules of wheather or not next turn will be alive or dead
    */
  def aliveNextTurn(pos:(Int, Int), state:ConwayState):Boolean = {
    val alive = liveNeighbours(pos, state)
    if (alive == 5 || alive == 3 || alive == 2) then true else false
  }

  /**
    * Nested for loop structure to iterate through given size of row/columns of grid. Check for blinker states and perform relevant action else remove from map or add to map depending on required action
    */
  def nextConwayState(state:ConwayState, maxSize:(Int, Int) = (20, 20)):ConwayState = {
    for {col <- (0 to maxSize._1); row <- (0 to maxSize._2)} yield
      val p:Position = (col, row) 
      if state == blinker1 then return blinker2
      else if state == blinker2 then return blinker1
      else 
        if !isAlive(p, state) && aliveNextTurn(p, state)
        then state + (p -> true) 
        else if isAlive(p, state) && !aliveNextTurn(p, state)
        then state - p
    state
  }
}