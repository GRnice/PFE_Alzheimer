# Socket server in python using select function

import sqlite3 as lite
from threading import Thread,RLock

import socket, select
import queue
from ServerServiceTablet import *

poolRequest = queue.Queue(500) # MAX 500 requetes à traiter

class Profile:
    def __init__(self, nom, prenom):
        self.firstName = prenom
        self.lastName = nom
        
class ManagerProfile:
    def __init__(self):
        self.listProfile = []

    
    def read(self, nomFichier):
        with open(nomFichier, 'r') as f:
            for line in f:
                myLine = line.strip()
                listLine = myLine.split(",")
                p = Profile(listLine[0], listLine[1])
                self.listProfile.append(p)
        f.close()

        for elt in self.listProfile:
            print(elt.firstName + " " + elt.lastName)
        

    def write(self, nomFichier, profile):
        with open(nomFichier, 'a') as out:
            out.write(profile.firstName + "," + 
                      profile.lastName + "\n")
        out.close()
        

class Tracker: ## Classe representant un tracker
    def __init__(self):
        self.id = None
        self.position = tuple() # (longitude, latitude)
        self.etat = 0
        # 0 -> connecté mais aucune information de l'utilisateur;
        # 1 -> connecté avec information;


class Mapper: ## HashMap permettant d'associer un socket à un utilisateur
    def __init__(self):
        self.dict = dict()
        print("Mapper ready [ok]")
    def addTracker(self,socket):
        print("add tracker")
        print(socket)
        self.dict[socket] = Tracker()

    def delTracker(self,socket):
        if socket in self.dict.keys():
            del self.dict[socket]

    def getTracker(self,socket):
        if socket in self.dict.keys():
            return self.dict[socket]

    def getSockets(self):
        return self.dict.keys()
            

    def apply(self,commande):
        ## parse la commande et applique la commande
        # commande contient un tuple (socket , commande)
        ## les commandes:
            ## entete*parametre*parametre*...
            ## separateur: *
            ## POSITION*longitude(float)*latitude(float) -> rafraichir la position du tracker sur le serveur
            ## STARTSUIVI*IDSMARTPHONE -> associer le socket à l'id smartphone
            ## STOPSUIVI -> indiquer au smartphone d'arreter le suivi
        with lockMap:
            socket = commande[0]
            requete = commande[1].rstrip()
            requeteArray = requete.split("*")
            entete = requeteArray[0]

            if (entete == "STARTSUIVI"):
                idTel = requeteArray[1]
                tracker = self.getTracker(socket)
                tracker.etat = 1
                tracker.id = idTel
                self.dict[socket] = tracker
                print("Demarrage du suivi pour le tel à l'id : ",idTel)
                socket.send("OKPROMENADE\r\n".encode("utf-8")) # Pour l'instant on valide de suite


            elif (entete == "POSITION"):
                longitude = float(requeteArray[1])
                latitude = float(requeteArray[2])
                tracker = self.getTracker(socket)
                if (tracker == None):
                    print("Erreur, tracker inconnu")
                    return

                print("Nouvelle position connue pour : (",longitude,",",latitude,")")
                tracker.position = (longitude,latitude)
                self.dict[socket] = tracker

            elif (entete == "STOPSUIVI"):
                tracker = self.getTracker(socket)
                tracker.etat = 0
                print("le tracker ayant l'id :",tracker.id," a terminé la promenade")
                socket.send("STOPSUIVI\r\n".encode('utf-8'))

            elif (entete == "CONTINUE"):
                idTel = requeteArray[1]
                nwTracker = self.dict[socket]
                
                allkeys = list(self.dict.keys())
                for key in allkeys:
                    if self.dict[key].id == idTel:
                        old = self.dict[key]
                        nwTracker.id = idTel
                        nwTracker.position = old.position
                        nwTracker.etat = old.etat
                        self.dict[socket] = nwTracker
                        key.close()
                        del self.dict[key]
                        socket.send("OKPROMENADE\r\n".encode('utf-8'))
                        break
            
                
            

class Pool(Thread):
    def __init__(self,mapper):
        Thread.__init__(self)
        self.mapper = mapper

    def run(self):
        print("Poll ready [ok]")
        commande = None
        while True:
            
            with lockPool:
                if (not poolRequest.empty()):
                    commande = poolRequest.get() # (socket , requete)

            if commande != None:
                self.mapper.apply(commande)

            commande = None
    



###############################################
###############################################
###############################################
##
## SERVEUR_PATIENT <---------------> Smartphone
##
###############################################
###############################################
###############################################
class PatientServer(Thread):
    def __init__(self,port,sizeBuffer,maxClientSocket):
        Thread.__init__(self)
        self.PORT = port
        self.RECV_BUFFER = sizeBuffer
        self.maxClientSocket = maxClientSocket
        self.CONNECTION_LIST = [] # liste des patients connectés (socket)
        self.serverOnline = True
        self.mapper = Mapper() # HashMap<socket,Profil>

    def getMapper(self):
        return self.mapper

    def stopServer(self):
        self.serverOnline = False
    
    def run(self):
        server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server_socket.bind(('', self.PORT))
        server_socket.listen(self.maxClientSocket)
        # Add server socket to the list of readable connections
        self.CONNECTION_LIST.append(server_socket)
        
        pool = Pool(self.mapper)
        pool.start()
        print("Chat server started on port " + str(self.PORT) + " [ok]")
        print("=============SERVEUR ONLINE=============")
        while self.serverOnline:
        # Get the list sockets which are ready to be read through select
            liste = []
            with lockMap:
                liste = list(self.mapper.getSockets()) # les sockets
            liste.extend(self.CONNECTION_LIST)
            read_sockets,write_sockets,error_sockets = select.select(liste,[],[])
     
            for sock in read_sockets:
                 
                #New connection
                if sock == server_socket:
                    sockfd, addr = server_socket.accept()
                    with lockMap:
                        self.mapper.addTracker(sockfd)
                    print("Client (%s, %s) connected" % addr)
                     
                else:
                    # Data recieved from client, process it
                    try:
                        #In Windows, sometimes when a TCP program closes abruptly,
                        # a "Connection reset by peer" exception will be thrown
                        data = sock.recv(self.RECV_BUFFER)

                        if len(data) > 0:
                            with lockPool:
                                poolRequest.put((sock,data.decode('utf-8')))

                        else:
                            print("Client (%s) is offline" % sock)
                            sock.close()
                            with lockMap:
                                self.mapper.delTracker(sock)

         
                     
                    # client disconnected, so remove from socket list
                    except:
                        #broadcast_data(sock, "Client (%s, %s) is offline" % addr)
                        print("Client (%s) is offline" % sock)
                        sock.close()
                        with lockMap:
                            self.mapper.delTracker(sock)

             
        server_socket.close()
        print("=============SERVEUR OFFLINE=============")

if __name__ == "__main__":
    serveAssistant = ServerAssistant(2000)
    servePatient = PatientServer(3000,4096,200) # sur le port 3000
    
    serveAssistant.start()
    servePatient.start()
##    message = " "
##    print("ALT-F4 pour tuer le serveur")
##    while True:
##        message = input("entrez une commande: ")
##        ## STOP -> envoyer STOP au premier socket trouvé dans la hashmap<socket,Tracker>
##        if message == "STOP" and len(list(servePatient.mapper.dict.keys())) > 0:
##            with lockPool:
##                poolRequest.put((list(servePatient.mapper.dict.keys())[0],"STOPSUIVI")) # fonctionne uniquement
##            

    serveAssistant.join()
    servePatient.join()

## TEST POOL
##mapper = Mapper()
##pool = Pool(mapper)
##pool.start()
##with mlock:
##    poolRequest.put(("0","first request"))
##    poolRequest.put(("0","second request"))
##    poolRequest.put(("1","third request"))


##if __name__ == "__main__":
##      
##    CONNECTION_LIST = []    # list of socket clients
##    RECV_BUFFER = 4096 # Advisable to keep it as an exponent of 2
##    PORT = 3000
##         
##    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
##
##    server_socket.bind(('', PORT))
##    server_socket.listen(1000)
## 
##    # Add server socket to the list of readable connections
##    CONNECTION_LIST.append(server_socket)
## 
##    print("Chat server started on port " + str(PORT))
## 
##    while 1:
##        # Get the list sockets which are ready to be read through select
##        read_sockets,write_sockets,error_sockets = select.select(CONNECTION_LIST,[],[])
## 
##        for sock in read_sockets:
##             
##            #New connection
##            if sock == server_socket:
##                # Handle the case in which there is a new connection recieved through server_socket
##                sockfd, addr = server_socket.accept()
##                CONNECTION_LIST.append(sockfd)
##                print("Client (%s, %s) connected" % addr)
##                 
##            #Some incoming message from a client
##            else:
##                # Data recieved from client, process it
##                try:
##                    #In Windows, sometimes when a TCP program closes abruptly,
##                    # a "Connection reset by peer" exception will be thrown
##                    data = sock.recv(RECV_BUFFER)
##                    
##                    # echo back the client message
##                    if data:
##                        print(data)
##                        sock.send("COUCOU CA MARCHE\r\n".encode('utf-8'))
##                        #sock.send('OK ... ' + data)
##                 
##                # client disconnected, so remove from socket list
##                except:
##                    broadcast_data(sock, "Client (%s, %s) is offline" % addr)
##                    print("Client (%s, %s) is offline" % addr)
##                    sock.close()
##                    CONNECTION_LIST.remove(sock)
##                    continue
##
##         
##    server_socket.close()
