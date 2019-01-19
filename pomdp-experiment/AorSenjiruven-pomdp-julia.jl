
#A policy is built first and then the Wumpus world is initialized and actions are executed

using POMDPs   #For general POMDP functions
#POMDPs.add("POMDPToolbox")   #For model production some of the solving-related functions
#Pkg.add("Distributions")     #For use of the SparseCat distribution
#Pkg.add("SARSOP")            #For the solver itself (the using statement follows below)

using Distributions
using POMDPToolbox
using SARSOP

struct GridState
    xA::Int64
    yA::Int64
    xGold::Int64
    yGold::Int64
    xPit1::Int64
    yPit1::Int64
    xPit2::Int64
    yPit2::Int64
    xWumpus::Int64
    yWumpus::Int64
end

struct Observation
    gold::Bool
    breeze::Bool
    stench::Bool
end

GridState(xA::Int64, yA::Int64) = GridState(xA,yA,3,3,3,2,3,1,1,3)

goldFound(s1::GridState) = s1.xA==s1.xGold && s1.yA==s1.yGold

pitFound(s1::GridState) = (s1.xA==s1.xPit1 && s1.yA==s1.yPit1) || (s1.xA==s1.xPit2 && s1.yA==s1.yPit2)

wumpusFound(s1::GridState) = s1.xA==s1.xWumpus && s1.yA==s1.yWumpus

type GridPOMDP <: POMDP{GridState, Int64, Observation} #{states, actions, observations}
    size_x::Int64       #Number of grid cells in the x-direction
    size_y::Int64       #Number of grid cells in the x-direction
    r_gold::Int64       #Reward for finding gold(+1000)
    r_pit::Int64        #Reward for falling in pit/wumpus(-1000)
    r_wumpus::Int64
    r_move::Int64       #Reward for actions(-1)
    discount::Float64   #Discount factor
    states::Array{GridState, 1}
end

function GridPOMDP()
    return GridPOMDP(4,4,1000,-1000,-1000,-1,0.95, [])
end;

pomdp = GridPOMDP()

function POMDPs.isterminal(pomdp::GridPOMDP, s::GridState) 
    if goldFound(s) 
        true
    elseif pitFound(s)
        true
    elseif wumpusFound(s)
        true
    else
        false
    end
end

function POMDPs.states(pomdp::GridPOMDP)
    s = GridState[] #initialize array of GridWorldStates
    for xA=1:pomdp.size_x, yA=1:pomdp.size_y, xGold=1:pomdp.size_x, yGold=1:pomdp.size_y, xP1=1:pomdp.size_x, yP1=1:pomdp.size_y, xP2=1:pomdp.size_x, yP2=1:pomdp.size_y, xW=1:pomdp.size_x, yW=1:pomdp.size_y
        push!(s, GridState(xA, yA, xGold, yGold, xP1, yP1, xP2, yP2, xW, yW))
    end
    
    states = GridState[]
    
#     for l=1:256
#         i = rand(1:length(s))
#         push!(states, s[i])
#         deleteat!(s, i)
#     end
    pomdp.states = states
    return states   #array of states
end;

function POMDPs.state_index(pomdp::GridPOMDP, state::GridState)    
    #return findfirst(pomdp.states, state) 
    return sub2ind((pomdp.size_x, pomdp.size_y, pomdp.size_x, pomdp.size_y, pomdp.size_x, pomdp.size_y, pomdp.size_x, pomdp.size_y, pomdp.size_x, pomdp.size_y), state.xA, state.yA, state.xGold, state.yGold, state.xPit1, state.yPit1, state.xPit2, state.yPit2, state.xWumpus, state.yWumpus)
end

POMDPs.n_states(p::GridPOMDP) = (p.size_x*p.size_y)*(p.size_x*p.size_y)*(p.size_x*p.size_y)*(p.size_x*p.size_y)*(p.size_x*p.size_y)

POMDPs.actions(p::GridPOMDP) = [1,2,3,4,5] #1-up, 2-left, 3-right, 4-down, 5-no-op, 6-shoot
POMDPs.n_actions(p::GridPOMDP) = 5
POMDPs.actions(pomdp::GridPOMDP, state::GridState) = POMDPs.actions(pomdp)

function POMDPs.action_index(::GridPOMDP, a::Int64)
    if a==1
        return 1
    elseif a==2
        return 2
    elseif a==3
        return 3
    elseif a==4
        return 4
    elseif a==5
        return 5
    #elseif a==6
    #    return 6
    end
    error("invalid action: $a")  #note the $ placeholder for var reference in print
end;

function isInbounds(pomdp::GridPOMDP, st::GridState)
    if (1 <= st.xA <= pomdp.size_x) && (1 <= st.yA <= pomdp.size_y)
        return true
    end
    return false
end

function POMDPs.transition(p::GridPOMDP, s::GridState, a::Int64)
    x = s.xA
    y = s.yA 
    
    #The neighbor array represents the possible states to which the
    #agent in its current state may transition. The states correspond to 
    #the integer representation of each action.
    neighbor = [
        GridState(x,y+1),  #up
        GridState(x-1,y),  #left
        GridState(x+1,y),  #right
        GridState(x,y-1),   #down
        GridState(x,y)#,       #original cell
        #GridState(x,y)       #original cell but no more wumpus
    ]
    
    #The target cell is the location at the index of the appointed action.
    target = neighbor[a]
    
    #If the target cell is out of bounds, the agent remains in 
    #the same cell. Otherwise the agent transitions to the target 
    #cell.
    if !isInbounds(p,target)
        return SparseCat([s], [1.0])
    else
        return SparseCat([target], [1.0])
    end
end

tObs = Observation[]

for g in [false, true], b in [false, true], st in [false, true]
    push!(tObs, Observation(g, b, st))
end

POMDPs.observations(::GridPOMDP) = tObs
POMDPs.observations(pomdp::GridPOMDP, s::GridState) = POMDPs.observations(pomdp);
POMDPs.n_observations(::GridPOMDP) = 8

#The observation distribution establishes the likelihood
#of a true observation (glitter)
type ObservationDistribution
    gold_true::Float64
    breeze_true::Float64
    stench_true::Float64
end
ObservationDistribution() = ObservationDistribution(0.11, 0.45, 0.45)
iterator(od::ObservationDistribution) = tObs

function POMDPs.obs_index(pomdp::GridPOMDP, obs::Observation)
    return sub2ind((2, 2, 2), Int(obs.stench+1), Int(obs.breeze+1), Int(obs.gold+1))
end

function POMDPs.pdf(od::ObservationDistribution, obs::Observation)
#     result = 1.0
    
#     result = obs.gold ? result * od.gold_true : result * (1 - od.gold_true)
#     result = obs.breeze ? result * od.breeze_true : result * (1 - od.breeze_true)
#     result = obs.stench ? result * od.stench_true : result * (1 - od.stench_true)
#     #result = obs.bump ? result * od.bump_true : result * (1 - od.bump_true)
#     #println(result)
#     return result == 0.0 ? 0.1 : result

    if !obs.gold && !obs.breeze && !obs.stench
        return (1-od.gold_true) * (1-od.breeze_true) * (1-od.stench_true)
    elseif !obs.gold && !obs.breeze && obs.stench
        return (1-od.gold_true) * (1-od.breeze_true) * (od.stench_true)
    elseif !obs.gold && obs.breeze && !obs.stench
        return (1-od.gold_true) * (od.breeze_true) * (1-od.stench_true)
    elseif !obs.gold && obs.breeze && obs.stench
        return (1-od.gold_true) * (od.breeze_true) * (od.stench_true)
    elseif obs.gold && !obs.breeze && !obs.stench
        return (od.gold_true) * (1-od.breeze_true) * (1-od.stench_true)
    elseif obs.gold && !obs.breeze && obs.stench
        return (od.gold_true) * (1-od.breeze_true) * (od.stench_true)
    elseif obs.gold && obs.breeze && !obs.stench
        return (od.gold_true) * (od.breeze_true) * (1-od.stench_true)
    else
        return (od.gold_true) * (od.breeze_true) * (od.stench_true)
    end

end

function POMDPs.rand(rng::AbstractRNG, od::ObservationDistribution)
#     if rand(rng) <= 0.5 #od.op_true
#         return true
#     else
#         return false
#     end
    s8 = (od.gold_true) * (od.breeze_true) * (od.stench_true) 
    s7 = (od.gold_true) * (od.breeze_true) * (1-od.stench_true) + s8
    s6 = (od.gold_true) * (1-od.breeze_true) * (od.stench_true) + s7
    s5 = (od.gold_true) * (1-od.breeze_true) * (1-od.stench_true) + s6
    s4 = (1-od.gold_true) * (od.breeze_true) * (od.stench_true) + s5
    s3 = (1-od.gold_true) * (od.breeze_true) * (1-od.stench_true) + s4 
    s2 =  (1-od.gold_true) * (1-od.breeze_true) * (od.stench_true) + s3
    s1 = (1-od.gold_true) * (1-od.breeze_true) * (1-od.stench_true) + s2

    if rand(rng) <= s8
        return temp_obs[8]
    elseif rand(rng) <= s7
        return temp_obs[7]
    elseif rand(rng) <= s6
        return temp_obs[6]
    elseif rand(rng) <= s5
        return temp_obs[5]
    elseif rand(rng) <= s4
        return temp_obs[4]
    elseif rand(rng) <= s3
        return temp_obs[3]
    elseif rand(rng) <= s2
        return temp_obs[2]
    else
        return temp_obs[1]
    end
    
#     temp = Int(ceil(rand(rng)/(od.gold_true*od.breeze_true*od.stench_true)))[1]
#     #return tObs[rand(rng, 1:length(tObs))]
#     return tObs[temp]
end

pitNearby(s1::GridState) = (s1.xA+1==s1.xPit1 && s1.yA==s1.yPit1) || (s1.xA==s1.xPit1 && s1.yA-1==s1.yPit1) || (s1.xA==s1.xPit1 && s1.yA+1==s1.yPit1) || (s1.xA-1==s1.xPit1 && s1.yA==s1.yPit1) || (s1.xA+1==s1.xPit2 && s1.yA==s1.yPit2) || (s1.xA==s1.xPit2 && s1.yA-1==s1.yPit2) || (s1.xA==s1.xPit2 && s1.yA+1==s1.yPit2) || (s1.xA-1==s1.xPit2 && s1.yA==s1.yPit2)

wumpusNearby(s1::GridState) = (s1.xA+1==s1.xWumpus && s1.yA==s1.yWumpus) || (s1.xA==s1.xWumpus && s1.yA-1==s1.yWumpus) || (s1.xA==s1.xWumpus && s1.yA+1==s1.yWumpus) || (s1.xA-1==s1.xWumpus && s1.yA==s1.yWumpus)

function POMDPs.observation(pomdp::GridPOMDP, s::GridState)
    od = ObservationDistribution()
    if goldFound(s)
        od.gold_true = 1.0
    else
        od.gold_true = 0.0
    end
    if pitNearby(s)
        od.breeze_true = 1.0
    else
        od.breeze_true = 0.0
    end
    if wumpusNearby(s)
        od.stench_true = 1.0
    else
        od.stench_true = 0.0
    end
    #println(od)
    od
end

function POMDPs.reward(p::GridPOMDP, s::GridState, a::Int64)
    r = 0.0
    if pitFound(s)
        r += p.r_pit
    elseif wumpusFound(s)
        r += p.r_wumpus
    elseif goldFound(s)
        r +=  p.r_gold
    elseif a != 5
        r += p.r_move
    end
    r
end

POMDPs.reward(pomdp::GridPOMDP, s::GridState, a::Int64, obs::Observation) = reward(pomdp,s,a)
POMDPs.discount(p::GridPOMDP) = p.discount

function POMDPs.initial_state_distribution(pomdp::GridPOMDP) 
    return SparseCat([GridState(1,1)], [1.0])
end;

pomdp = GridPOMDP()

#SARSOP solver
#println("SARSOP")
#The using keyword initializes use of the SARSOP Julia solver
#using SARSOP

#Initializing the solver loads the model file

solver = SARSOPSolver()

#=
Solver options are established through a dictionary that can be initialized in advance or at runtime as described below (note default values) and at 
https://github.com/JuliaPOMDP/SARSOP.jl/blob/master/src/solver.jl
Be aware of some minor format intricacies that are noted below:
The SARSOP solver options dictionary:

fast::Bool=false, # Use fast (but very picky) alternate parser for .pomdp files
randomization::Bool=false, # run ends when target precision is reached
precision::Float64=DEFAULT_PRECISION, 
**DEFAULT_PRECISION = 0.001
**Turn on randomization for the sampling algorithm.
timeout::Float64=NaN, # [sec] If running time exceeds the specified value, pomdpsol writes out a policy and terminates
memory::Float64=NaN, # [MB] If memory usage exceeds the specified value, pomdpsol writes out a policy and terminates
trial-improvement-factor::Float64=DEFAULT_TRIAL_IMPROVEMENT_FACTOR,
**DEFAULT_TRIAL_IMPROVEMENT_FACTOR = 0.5
**A trial terminates at a belief when the gap between its upper and lower bound is within
**improvement_constant` of the current precision at the initial belief
policy-interval::Float64=NaN 
**the time interval between two consecutive write-out of policy files; defaults to only exporting at end
=#

#=
Changing solver options (either one or several) from defaults may be accomplished by calliing and reassigning new values to the options dictionary in the initialized SARSOPSolver instance. Note that for bool options (fast and random), the dict initialization to "true" is (e.g.) "randomization"=>""
=#

solver.options = Dict(#="randomization"=>"",=#"precision"=>0.1, "timeout"=>100,"memory"=>NaN, "trial-improvement-factor"=>0.5, "policy-interval"=>NaN)



#Running the solve function creates and saves the policy file (out.policy)
#that determines an action for each state and observation

policy = solve(solver, pomdp)

#alphas(policy)

b = uniform_belief(pomdp)
a = action(policy, b) 

pomdp = GridPOMDP() # initialize problem
init_dist = initial_state_distribution(pomdp) # initialize distribution over state

up = updater(policy) # belief updater for the policy

hr = HistoryRecorder(max_steps=50, rng=MersenneTwister(1)) # history recorder that keeps track of states, observations and beliefs

hist = simulate(hr, pomdp, policy, up, init_dist)

#Print out each packet of simulated information
nxtRew = 0.0
for (s, b, a, r, sp, op) in hist
    nxtRew = reward(pomdp,sp,a)
    println("s: $s, b: $(b.b), action: $a, obs: $op")
    println(nxtRew)
end
res1 = discounted_reward(hist) + nxtRew
println("Total reward: $res1")

pomdp = POMDPFile(pomdp, "model.pomdpx")

import SARSOP.polgraph
import SARSOP.PolicyGraphGenerator
import SARSOP._get_options_list
graphgen = PolicyGraphGenerator("Grid.dot")
polgraph(graphgen, pomdp, policy)