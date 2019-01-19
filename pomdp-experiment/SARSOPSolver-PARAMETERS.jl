pomdp = GridPOMDP()

#SARSOP solver
println("SARSOP")
#The using keyword initializes use of the SARSOP Julia solver
using SARSOP

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

solver.options = Dict("randomization=>"","precision"=>0.03, "timeout"=>100,"memory"=>NaN, "trial-improvement-factor"=>0.5, "policy-interval"=>NaN)

#Once established, the option-laden solver can be used to build a policy
policy = solve(solver, pomdp)