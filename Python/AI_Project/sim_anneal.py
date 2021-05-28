#!/usr/bin/env python3
import math
import random
import copy

#  define constants
NUM_VEHICLES = 5
NUM_STATEMENTS = 15
# Define index values
MODEL = 0
TIME = 1
NATIONALITY = 2
COLOR = 3
DESTINATION = 4

# Define Attributes
model = ["Toyota Camry", "Hyundai Accent", "Holden Barina", "Nissan X-Trail", "Honda Civic"]
time = ["6:00am", "9:00am", "5:00am", "7:00am", "8:00am"]
nationality = ["British", "French", "Chinese", "Indian", "Canadian"]
color = ["black", "blue", "green", "red", "white"]
destination = ["Gold Coast", "Sydney", "Newcastle", "Tamworth", "Port Macquarie"]

attributes = [model, time, nationality, color, destination]

def print_intro():
     print("""
    #    #### ####  # ####   #### #  # #### #### #    #    ####
    #    #  # #  #  # #      #  # #  #    #    # #    #    #
    #    #  # #     # #      #### #  #   #    #  #    #    ##
    #    #  # #  ## # #      #    #  #  #    #   #    #    #
    #### #### ####  # ####   #    #### #### #### #### #### ####
    """)
     print("""
    Welcome to a Logic Grid Puzzle Solver.
          """)
     print("""
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
    a Canadian couple?""")
     print("""
    The program has two options for input parameters. There is an
    optimisation option where optimised values for initial 
    temperature, cooling rate and local walk have been established 
    through testing and depends on the initial fitness (since the 
    initial state is randomised these 'optimum' values may not 
    represent the absolute optimum for every initial state. These 
    optimum values are averages from iteratively testing the 
    program). The program also allows experimentation by allowing 
    the user to select their own choice for the following
    parameters:\n
           - initial temperature
           - cooling rate
           - local walk duration

    A parameter set might look like: temp = 1; cooling = 0.99;
    walk = 500. When experimenting with parameter sets a break 
    point can be set to prevent the program running too long. 
    The program allows for the Puzzle to be reset and attempted 
    with different configurations. 
    """)
     
def get_input_parameters(value, i, optimum):
    while True:
        param = input("\nPlease enter input paramater for: {val} (example:{opt}) ".format(val=value, opt=optimum))
        try:
            if (i == 0):
                assert param.isdigit()
                assert int(param) > 0
            elif(i == 1):
                assert isinstance(float(param), float)
                assert float(param) > 0
                assert float(param) < 1
        except AssertionError:
            if (i == 0):
                print("\nInvalid input for {val}. Please enter int value > 0.".format(val=value))
            elif(i == 1):
                print("\nInvalid input for {val}. Please enter decimal between 0 and 1.".format(val=value))
        except ValueError:
             if (i == 0):
                    print("\nInvalid input for {val}. Please enter int value > 0.".format(val=value))
             elif(i == 1):
                print("\nInvalid input for {val}. Please enter decimal between 0 and 1.".format(val=value))     
        else:
            break
    return param

def get_initial_state():
    initial_state = []
    attributes_cpy = copy.deepcopy(attributes)
    count = 5
    for i in range(NUM_VEHICLES):
        vehicle = []
        for j in range(NUM_VEHICLES):
           rand_index = random.randint(0, count - 1)
           attr = attributes_cpy[j].pop(rand_index)
           vehicle.append(attr)
        count -= 1 
        initial_state.append(vehicle)
    return initial_state

def optimize_parameters(initial_fitness):
    # cooling, walk, temp
    if initial_fitness <= 7:
        return 0.29, 107, 3
    elif initial_fitness == 8:
        return 0.13, 105, 16
    elif initial_fitness == 9:
        return 0.18, 121, 46
    elif initial_fitness == 10:
        return 0.16, 128, 31
    elif initial_fitness == 11:
        return 0.16, 133, 31
    elif initial_fitness == 12:
        return 0.18, 133, 35
    elif initial_fitness == 13:
        return 0.18, 132, 33
    elif initial_fitness == 14:
        return 0.18, 131, 32
    elif initial_fitness == 15:
        return 0.16, 131, 32

def calculate_fitness(state):
    state_cost = 15
    for i in range(NUM_VEHICLES):
        # The Toyota Camry was hired at 6:00am by a British couple.
        if(state[i][MODEL] == "Toyota Camry" and state[i][TIME] == "6:00am" and state[i][NATIONALITY] == "British"):
            state_cost -= 1
        # The car in the middle had a black colour.
        if(i == NUM_VEHICLES//2 and state[i][COLOR] == "black"):
            state_cost -= 1
        # The Hyundai Accent left the depot at 9:00am.
        if(state[i][MODEL] == "Hyundai Accent" and state[i][TIME] == "9:00am"):
            state_cost -= 1
        # The Holden Barina with a blue colour was to the left of the car that carries the British couple.
        if(i < NUM_VEHICLES - 1 and state[i][MODEL] == "Holden Barina" and state[i][COLOR] == "blue" and state[i+1][NATIONALITY] == "British"):
            state_cost -= 1
        # To the right of the car hired by a French lady was the car going to GoldCoast.
        if(i < NUM_VEHICLES - 1 and state[i][NATIONALITY] == "French" and state[i+1][DESTINATION] == "Gold Coast"):
            state_cost -= 1
        # The Nissan X-Trail was heading for Sydney.
        if(state[i][MODEL] == "Nissan X-Trail" and state[i][DESTINATION] == "Sydney"):
            state_cost -= 1
        # To the right of the car carrying a Chinese businessman was the car with a green colour.
        if(i < NUM_VEHICLES - 1 and state[i][NATIONALITY] == "Chinese" and state[i+1][COLOR] == "green"):
            state_cost -= 1
        # The car going to Newcastle left at 5:00am.
        if(state[i][DESTINATION] == "Newcastle" and state[i][TIME] == "5:00am"):
            state_cost -= 1
        # The Honda Civic left at 7:00am and was on the right of the car heading for Gold Coast.
        if(i < NUM_VEHICLES - 1 and state[i][DESTINATION] == "Gold Coast" and state[i+1][MODEL] == "Honda Civic" and state[i+1][TIME] == "7:00am" ):
            state_cost -= 1
        # The car with a red colour was going to Tamworth.
        if(state[i][COLOR] == "red" and state[i][DESTINATION] == "Tamworth"):
            state_cost -= 1
        # To the left of the car that left at 7:00am was the car with a white colour.
        if(i < NUM_VEHICLES - 1 and state[i][COLOR] == "white" and state[i+1][TIME] == "7:00am" ):
            state_cost -= 1
        # The last car was hired by an Indian man.
        if(i == NUM_VEHICLES - 1 and state[i][NATIONALITY] == "Indian"):
            state_cost -= 1
        # The car with a black colour left at 8:00am.
        if(state[i][COLOR] == "black" and state[i][TIME] == "8:00am"):
            state_cost -= 1
        # The car carrying an Indian man was to the right of the car hired by a Chinese businessman.
        if(i < NUM_VEHICLES - 1 and state[i][NATIONALITY] == "Chinese" and state[i+1][NATIONALITY] == "Indian"):
            state_cost -= 1
        # The car heading for Tamworth left at 6:00am.
        if(state[i][DESTINATION] == "Tamworth" and state[i][TIME] == "6:00am"):
            state_cost -= 1
    
    return state_cost

def proposed_change_state(current_state):
    new_state = copy.deepcopy(current_state)
     # choose a random pair to swap
    attribute = random.randint(0, 4)
    # choose a random pair to swap
    element_1 = random.randint(0, 4)
    element_2 = random.randint(0, 4)
    # change element if the same
    while(element_2 == element_1):
        element_2 = random.randint(0, 4)
    # swap the two points
    new_state[element_1][attribute], new_state[element_2][attribute] = new_state[element_2][attribute], new_state[element_1][attribute]
    return new_state

def accept_proposal(current_cost, proposed_cost, current_temp):
    cost_diff = proposed_cost - current_cost
    try:
        prob = math.exp(-(cost_diff) / current_temp)
    except OverflowError:
        prob = float('inf')
    
    test = True if random.uniform(0, 1) < prob else False
    return True if cost_diff <= 0 | test else False

def simulated_annealing(initial_state, initial_fitness, initial_temperature, cooling, walk, optimize, break_point):
    #  set up variables
    current_state = initial_state
    current_fitness = initial_fitness
    current_temp = initial_temperature
    counter = 0
    while(current_fitness > 0):
        for i in range(walk):
            test_state = proposed_change_state(current_state)
            test_fitness = calculate_fitness(test_state)
            
            if(accept_proposal(current_fitness, test_fitness, current_temp)):
                current_fitness = test_fitness
                current_state = test_state
                
            counter += 1
        if counter > break_point:
            break
        if current_temp > 0.01:
            current_temp *= cooling            
    return current_state, counter

def print_results(solution, counter, cooling, temp, walk, break_point):
    print("""\n********************************************************************************
                        Results of Puzzle Solver
********************************************************************************
    """)
    print("Parameters:")
    print("Cooling = ", cooling)
    print("Temperature = ", temp)
    print("Local walk = {walk}\n".format(walk=walk))
    
    if counter > break_point:
        print("Program reached your break-point without finding a solution.\nTry increasing search space or changing parameters.")
    else:
        for i in range(NUM_VEHICLES):
            # find solutions to questions
            if (solution[i][DESTINATION] == "Port Macquarie"):
                port_mac = i+1
            if (solution[i][NATIONALITY] == "Canadian"):
                canadian = i+1
            print("Car: {index}".format(index=i+1))
            print("| Model: {mod} | Time: {tim} | Nationality: {nat} | Color: {col} |\n| Destination: {des} |\n".format(mod=solution[i][MODEL], tim=solution[i][TIME], nat=solution[i][NATIONALITY], col=solution[i][COLOR], des=solution[i][DESTINATION]))
        
        print("In {it} iterations the program solved the puzzle to reveal that:\n\n\t.... the car going to Port Macquarie was: Car {port}\n\t.... and the car hired by the Canadians was: Car {can}".format(it=counter, port=port_mac, can=canadian))

def choose_next(instruction):
    while True:
        replay = input(instruction)
        try:
            assert replay.lower() == 'y' or replay.lower() == 'n'
        except AssertionError:
            print("\nInvalid Entry! Please enter 'y' or 'n'")
        else:
            break
    return True if replay.lower() == 'y' else False 
   
def run_solve_logic_puzzle(start):
    # display introduction
    if start:
        print_intro()
    
    # choose parameter selection mode
    optimize = choose_next("\nWould you like to run optimisation (y/n)? ")

    if optimize:
        print("\nThe program is now selecting opitimized parameters and calculating the solution,\none moment please.....")
        while optimize:
            # get random initial state
            initial = get_initial_state()
            
            # calculate fitness of initial state
            fitness = calculate_fitness(initial)
            
            # optimize parameters based on initial fitness
            cooling, walk, temp = optimize_parameters(fitness)
            break_point = 700;
            
            # from initial state find solution with simulated annealing search algorithm
            solution, counter = simulated_annealing(initial, fitness, temp, cooling, walk, optimize, break_point)
            
            optimize = True if counter > break_point else False
            
    else:
        # get input parameters from user
        temp = int(get_input_parameters("initial temperature", 0, 1))
        cooling = float(get_input_parameters("cooling rate", 1, 0.99))
        walk = int(get_input_parameters("walk distance", 0, 500))
        break_point = int(get_input_parameters("break point", 0, 1000000))
        
        print("\nThe program is now calculating the solution, one moment please.....")
        # get random initial state
        initial = get_initial_state()
            
        # calculate fitness of initial state
        fitness = calculate_fitness(initial)
            
        # from initial state find solution with simulated annealing search algorithm
        solution, counter = simulated_annealing(initial, fitness, temp, cooling, walk, optimize, break_point)
    
    #  display the results to terminal
    print_results(solution, counter, cooling, temp, walk, break_point)
    
    # run program again?
    if choose_next("\nWould you like run program again (y/n)? "):
        run_solve_logic_puzzle(False)
    else:
        print("\n-----Thank you for using this program!-----\n")

    

run_solve_logic_puzzle(True)