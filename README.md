# n_bodies
Multi-thread calculation and visualisation of n_bodies exercing forces on each other in 2D

## Explainations on Classes

A body object contains all the information of this body like its coordinates, mass, the forces that apply to him ... 

A buffer object is a fixed-size array that contains relevant informations at each time.

A system object is a set of initial data for a simulation, it contains mainly informations about initial bodies.

An engine object handles a Runnable object that updates the buffer.
