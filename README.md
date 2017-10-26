# Iello Admin App (Read me in lavorazione) #
Questa repository contiene il codice dell'app Android per amministratori di Project Iello. L'applicazione ha lo specifico compito di gestire il database remoto, inserendo i dati relativi ai posteggi per disabili, tramite [Iello API](https://github.com/IelloDevTeam/IelloAPI "Iello API Repo")
di Iello. 

A differenza dell' [app Iello](https://github.com/IelloDevTeam/IelloAndroidApp), questa è stata pensata per essere utilizzata solo dagli sviluppatori o agli addetti alla ricerca dei posteggi per garantirel'affidabilità dei dati.


## Integrazione con Iello Api ##
L'applicazione utilizza le funzionalità offerte da [Iello API](https://bitbucket.org/piattaformeteam/iello-api "Iello API Repo") per inviare nuove posizioni,
visualizzare quelle già presenti nel database e eliminare quelle indesiderate.


## Funzionalità dell'applicazione ##

### Visualizzazione di una posizione sulla mappa ###
L'applicazione si base sulla ricerca di una posizione all'interno della mappa che viene visualizzata all'avvio. Essa può essere individuata in due modi: con la propria posizione o ricerca per indirizzo.

La **ricerca per posizione** si effettua premendo il pulsante di geolocalizzazione posizionato in alto a destra dello schermo. In questo modo viene lanciata una ricerca basata sulla posizione dell'utente, fornita dal GPS del proprio smartphone.

La **ricerca per indirizzo** avviene scrivendo un indirizzo nella barra superiore grigia dello schermo e viene lanciata una ricerca basta su quest'ultimo.


### Visualizzazione dei parcheggi presenti nel database ###
Una volta individuata la zona desiderata, sullo schermo vengono visualizzati, con dei markers **di colore verde**, le posizioni già presenti nel database dei parcheggi per i disabili, in un raggio di 500 metri dalla propria posizione. Se ci si sposta nella mappa, si possono visualizzare i parcheggi premendo il pulsante FAB, in basso e a destra dello schermo, e selezionando **Mostra parcheggi in zona**

TO DO: collegamento con l'API

### Inserimento di una nuova posizione ###
La funzionalità principale di questa applicazione è l'inserimento di nuovi dati all'interno del database. Dopo aver individuato la posizione del parcheggio, marcata con il colore **rosso**, è necessario premere nuovamente sul marker e cambiare il suo colore a **giallo**. Questa operazione può essere svolta anche su più posizioni contemporaneamente. Una volta terminata la ricerca, l'inserimento delle posizioni può essere completato premendo il tasto FAB, in basso a destra
dello schermo, e selezionare dal menù **invia i marker selezionati** o **invia posizione attuale** se si vuole inviare la propria posizione GPS.

Infine si segnala che solo i marker gialli vengono inviati al database. Se si desidera deselezionarne uno, occore premere di nuovo su di esso.

DO TO: collegamento con l'API


### Eliminazione di una posizione ###
Con questa applicazione è possibile, inoltre, eliminare dal database delle posizioni inserite precedentemente. La procedura da adottare è la seguente: selezionare un marker verde e scegliere di eliminare la posizione attraverso la finestra che compare. 
Per eliminare dalla mappa tutti marker gialli selezionati e non ancora inseriti, è necessario premere il tasto FAB e selezionare **Elimina i marker da caricare dalla mappa**.

TO DO: collegamento all'API

### Note finali ###
Nell'applicazione, per agevolarne l'utilizzo, è stata inserita una legenda con i colori dei marker e i rispettivi significati. Inoltre, in altro a destra dello schermo, è stata posta una icona che rimanda alle istruzioni di base. 

### Licenza ###
Copyright (C) 2017 IelloDevTeam
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except 
in compliance with the License. You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

### Contatti & Credits ###

App realizzata come parte di un progetto di esame (PDGT) 
da Riccardo Maldini, Andrea Petreti, Elia Trufelli e Alessia Ventani. Se vuoi contattarci, scrivi un e-mail a riccardo.maldini@gmail.com
