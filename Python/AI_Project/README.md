
Name: Joshua Le Gresley

##  Logic Puzzle Solve
#### Description:
This Logic Puzzle Solver uses a Simulated Annealing Search Algorithm. A randomised initial state is determined then iteratively the algorithm checks if successive test-states provide a 'better' fitness. Each clue is represented and must be matched to reach the solution. Once fitness is 0 all statements are satisfied therefore the solution has been found. 

The program has **two options** for input parameters. 

1. There is an
    **optimisation** option where optimised values for initial 
    temperature, cooling rate and local walk have been established through testing and depends on the initial fitness (since the initial state is randomised these 'optimum' values may not represent the absolute optimum for every initial state. These optimum values are averages from iteratively testing the program).
    
2. The program also allows **experimentation** by allowing 
    the user to select their own choice for the following
    parameters:

           - initial temperature
           - cooling rate
           - local walk duration

    A parameter set might look like: 

    - temp = 1 
    - cooling = 0.99 
    - walk = 500
  
    When experimenting with parameter sets a break point can be set to prevent the program running too long. The program allows for the Puzzle to be reset and 
    attempted with different configurations. 

## Installation:
[Python 3](https://www.python.org/downloads/) is a system requirement for this program to run.

## Usage:

With the .py file in the working directory, the project can be run using:
```bash
python3 sim_anneal.py
```
Or as an executable file:
```bash
./sim_anneal.py
```
The following program output demonstrates the program running.

```
    #    #### ####  # ####   #### #  # #### #### #    #    ####
    #    #  # #  #  # #      #  # #  #    #    # #    #    #
    #    #  # #     # #      #### #  #   #    #  #    #    ##
    #    #  # #  ## # #      #    #  #  #    #   #    #    #
    #### #### ####  # ####   #    #### #### #### #### #### ####
    

    Welcome to a Logic Grid Puzzle Solver.
          

    This Logic Puzzle Solver uses a Simulated Annealing Search 
    Algorithm. A randomised initial state is determined then 
    iteratively the algorithm checks if successive test-states 
    provide a 'better' fitness. The logic of each clue is 
    represented and must be matched to reach the solution. 
    Once fitness is 0 all statements are satisfied therefore the 
    solution has been found. This program solves the following 
    puzzle:

    5 cars were parked in a row from left to right, that is 
    first, second, ..., and last at the Hertz depot outside 
    the Armidale Airport.

    - The Toyota Camry was hired at 6:00am by a British couple.
    - The car in the middle had a black colour.
    - The Hyundai Accent left the depot at 9:00am.
    - The Holden Barina with a blue colour was to the left of the 
      car that carries the British couple.
    - To the right of the car hired by a French lady was the car 
      going to GoldCoast.
    - The Nissan X-Trail was heading for Sydney.
    - To the right of the car carrying a Chinese businessman was 
      the car with a green colour.
    - The car going to Newcastle left at 5:00am.
    - The Honda Civic left at 7:00am and was on the right of the 
      car heading forGold Coast.
    - The car with a red colour was going to Tamworth.
    - To the left of the car that left at 7:00am was the car with 
      a white colour.
    - The last car was hired by an Indian man.
    - The car with a black colour left at 8:00am.
    - The car carrying an Indian man was to the right of the car 
      hired by a Chinese businessman.
    - The car heading for Tamworth left at 6:00am.
  
    Which car was going to Port Macquarie? Which car was hired by 
    a Canadian couple?

    The program has two options for input parameters. There is an
    optimisation option where optimised values for initial 
    temperature, cooling rate and local walk have been established 
    through testing and depends on the initial fitness (since the 
    initial state is randomised these 'optimum' values may not 
    represent the absolute optimum for every initial state. These 
    optimum values are averages from iteratively testing the 
    program). The program also allows experimentation by allowing 
    the user to select their own choice for the following
    parameters:

           - initial temperature
           - cooling rate
           - local walk duration

    A parameter set might look like: temp = 1; cooling = 0.99;
    walk = 500. When experimenting with parameter sets a break 
    point can be set to prevent the program running too long. 
    The program allows for the Puzzle to be reset and attempted 
    with different configurations. 
    

Would you like to run optimisation (y/n)? y

The program is now selecting opitimized parameters and calculating the solution,
one moment please.....

********************************************************************************
                        Results of Puzzle Solver
********************************************************************************
    
Parameters:
Cooling =  0.18
Temperature =  33
Local walk = 132

Car: 1
| Model: Holden Barina | Time: 5:00am | Nationality: Canadian | Color: blue |
| Destination: Newcastle |

Car: 2
| Model: Toyota Camry | Time: 6:00am | Nationality: British | Color: red |
| Destination: Tamworth |

Car: 3
| Model: Nissan X-Trail | Time: 8:00am | Nationality: French | Color: black |
| Destination: Sydney |

Car: 4
| Model: Hyundai Accent | Time: 9:00am | Nationality: Chinese | Color: white |
| Destination: Gold Coast |

Car: 5
| Model: Honda Civic | Time: 7:00am | Nationality: Indian | Color: green |
| Destination: Port Macquarie |

In 660 iterations the program solved the puzzle to reveal that:

        .... the car going to Port Macquarie was: Car 5
        .... and the car hired by the Canadians was: Car 1

Would you like run program again (y/n)? y

Would you like to run optimisation (y/n)? n

Please enter input paramater for: initial temperature (example:1) 1

Please enter input paramater for: cooling rate (example:0.99) 0.99

Please enter input paramater for: walk distance (example:500) 500

Please enter input paramater for: break point (example:1000000) 100000

The program is now calculating the solution, one moment please.....

********************************************************************************
                        Results of Puzzle Solver
********************************************************************************
    
Parameters:
Cooling =  0.99
Temperature =  1
Local walk = 500

Car: 1
| Model: Holden Barina | Time: 5:00am | Nationality: Canadian | Color: blue |
| Destination: Newcastle |

Car: 2
| Model: Toyota Camry | Time: 6:00am | Nationality: British | Color: red |
| Destination: Tamworth |

Car: 3
| Model: Nissan X-Trail | Time: 8:00am | Nationality: French | Color: black |
| Destination: Sydney |

Car: 4
| Model: Hyundai Accent | Time: 9:00am | Nationality: Chinese | Color: white |
| Destination: Gold Coast |

Car: 5
| Model: Honda Civic | Time: 7:00am | Nationality: Indian | Color: green |
| Destination: Port Macquarie |

In 36000 iterations the program solved the puzzle to reveal that:

        .... the car going to Port Macquarie was: Car 5
        .... and the car hired by the Canadians was: Car 1

Would you like run program again (y/n)? y

Would you like to run optimisation (y/n)? y

The program is now selecting opitimized parameters and calculating the solution,
one moment please.....

********************************************************************************
                        Results of Puzzle Solver
********************************************************************************
    
Parameters:
Cooling =  0.18
Temperature =  33
Local walk = 132

Car: 1
| Model: Holden Barina | Time: 5:00am | Nationality: Canadian | Color: blue |
| Destination: Newcastle |

Car: 2
| Model: Toyota Camry | Time: 6:00am | Nationality: British | Color: red |
| Destination: Tamworth |

Car: 3
| Model: Nissan X-Trail | Time: 8:00am | Nationality: French | Color: black |
| Destination: Sydney |

Car: 4
| Model: Hyundai Accent | Time: 9:00am | Nationality: Chinese | Color: white |
| Destination: Gold Coast |

Car: 5
| Model: Honda Civic | Time: 7:00am | Nationality: Indian | Color: green |
| Destination: Port Macquarie |

In 660 iterations the program solved the puzzle to reveal that:

        .... the car going to Port Macquarie was: Car 5
        .... and the car hired by the Canadians was: Car 1

Would you like run program again (y/n)? y

Would you like to run optimisation (y/n)? n

Please enter input paramater for: initial temperature (example:1) 1

Please enter input paramater for: cooling rate (example:0.99) .2

Please enter input paramater for: walk distance (example:500) 1

Please enter input paramater for: break point (example:1000000) 100000

The program is now calculating the solution, one moment please.....

********************************************************************************
                        Results of Puzzle Solver
********************************************************************************
    
Parameters:
Cooling =  0.2
Temperature =  1
Local walk = 1

Program reached your break-point without finding a solution.
Try increasing search space or changing parameters.

Would you like run program again (y/n)? y

Would you like to run optimisation (y/n)? y

The program is now selecting opitimized parameters and calculating the solution,
one moment please.....

********************************************************************************
                        Results of Puzzle Solver
********************************************************************************
    
Parameters:
Cooling =  0.18
Temperature =  35
Local walk = 133

Car: 1
| Model: Holden Barina | Time: 5:00am | Nationality: Canadian | Color: blue |
| Destination: Newcastle |

Car: 2
| Model: Toyota Camry | Time: 6:00am | Nationality: British | Color: red |
| Destination: Tamworth |

Car: 3
| Model: Nissan X-Trail | Time: 8:00am | Nationality: French | Color: black |
| Destination: Sydney |

Car: 4
| Model: Hyundai Accent | Time: 9:00am | Nationality: Chinese | Color: white |
| Destination: Gold Coast |

Car: 5
| Model: Honda Civic | Time: 7:00am | Nationality: Indian | Color: green |
| Destination: Port Macquarie |

In 532 iterations the program solved the puzzle to reveal that:

        .... the car going to Port Macquarie was: Car 5
        .... and the car hired by the Canadians was: Car 1

Would you like run program again (y/n)? n

-----Thank you for using this program!-----
```

## Support:
Please email joshualegresley@gmail.com if further details are required.

## Contributing:
For major changes, please open an issue first to discuss what you would like to change.