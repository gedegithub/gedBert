*****  To run application HDLC protocole  ******

1. run MainRecepteur with the following arguments
	1.<Numero_Port>

2.While MainRecepteur is still running run MainEmetteur with the following arguments
	1.<Nom_Machine> 
	2.<Numero_Port> 
	3.<Nom_fichier> 
	4.<0> 


*****  To run tests  ******

- When running MainReceptor, choose among the options presented.
	1 : Introduce bitwise errors to frames
        2 : Introduce burst errors to frames
        3 : Introduce delay in tram transmission
        4 : Introduce tram loss in communication

- Note that each type of error has a 0.2 chance of happening, so executions amy vary from each other and that if the file to test
  is small enough, it is possible that no error happen.