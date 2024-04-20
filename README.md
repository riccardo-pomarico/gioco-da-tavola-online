# Gioco da tavolo online

Corso: Ingegneria del Software.

Autori: Riccardo Pomarico, Pietro Marco Gallo, Federico Garzia.

Il progetto riguarda l'implementazione del gioco da tavolo "Masters of Renaissance" come un sistema distribuito. Questo sistema è composto da un server in grado di gestire una partita alla volta e da un numero variabile di client, da 1 a 4, che possono connettersi per partecipare a una sola partita. Per gestire la comunicazione tra server e client e viceversa, sono stati utilizzati socket. L'architettura del progetto segue il design pattern MVC (Model-View-Controller).

Le funzionalità implementate includono un'interfaccia utente testuale da linea di comando (CLI) e un'interfaccia grafica (GUI), insieme alle regole complete del gioco. Inoltre, sono state realizzate due funzionalità avanzate: la capacità di gestire le disconnessioni dei giocatori in modo resiliente e la possibilità di giocare in modalità locale senza connessione a internet.

Per la gestione delle dipendenze e la creazione dei file eseguibili, sono stati utilizzati i jar generati con Maven Shade Plugin. Il progetto è stato sviluppato utilizzando Java versione 15.
