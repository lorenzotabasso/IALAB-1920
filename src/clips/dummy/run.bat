(load 0_Main.clp)
(load 1_Env.clp)
(load ../maps/fleet0.clp)
(load 6_Heat.clp)
(load 4_Deliberate.clp)
(load 5_Planning.clp)
(load 3_Agent.clp)
(load 7_Neighborhood_fire.clp)
(reset)
;(watch focus)
(set-break game-over)
(run)
(run 2)
(focus ENV)
;; (facts)
