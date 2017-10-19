#Iello Admin App (Read me in lavorazione) #

Questa repository contiene il codice dell'app Android per amministratori di Project Iello. L'applicazione ha lo specifico compito 
di popolare il database remoto, inserendo i dati relativi ai posteggi per disabili, tramite [Iello API](https://bitbucket.org/piattaformeteam/iello-api "Iello API Repo")
di Iello. 
A differenza dell' [app Iello](https://bitbucket.org/piattaformeteam/iello-app), questa è stata pensata per essere utilizzata solo dagli sviluppatori o agli addetti alla ricerca dei posteggi per garantire
l'affidabilità dei dati.

## Integrazione con Iello Api##

L'applicazione utilizza le funzionalità offerte da [Iello API](https://bitbucket.org/piattaformeteam/iello-api "Iello API Repo") per inviare nuove posizioni,
visualizzare quelle già presenti nel database e eliminare quelle indesiderate.

## Funzionalità dell'applicazione ##

### Visualizzazione di una posizione sulla mappa ###

L'applicazione si base sulla ricerca di una posizione all'interno della mappa che viene visualizzata all'avvio. La posizione desiderata può essere individuata in 
due modi: 
* ricerca con la propria posizione; 
* ricarca per indirizzo.
La **ricerca per posizione** si effettua premendo il pulsante di geolocalizzazione posizionato in alto a destra dello schermo. In questo modo viene 
lanciata una ricerca basata sulla posizione dell'utente, fornita dal GPS del proprio smartphone.
La **ricerca per indirizzo** avviene scrivendo un indirizzo nella barra superiore grigia dello schermo e viene lanciata una ricerca basta su quest'ultimo.

Una volta individuata la zona desiderata, sullo schermo vengono visualizzati, con dei markers **di colore verde**, le posizioni già presenti nel database
dei parcheggi per i disabili in un raggio di 500 m dalla propria posizione.

### Inserimento di una nuova posizione ###

La funzionalità principale di questa applicazione è l'inserimento di nuovi dati all'interno del database. 




### Contribution guidelines ###

* Writing tests
* Code review
* Other guidelines

### Who do I talk to? ###

* Repo owner or admin
* Other community or team contact