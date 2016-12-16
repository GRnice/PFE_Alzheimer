# Socket server in python using select function

import sqlite3 as lite
from threading import Thread,RLock
import time
import socket, select
import queue
from ServerServiceTablet import *
import sys

poolRequest = queue.Queue(500) # MAX 500 requetes à traiter

lockMap = RLock()

class Tracker: ## Classe representant un tracker
    def __init__(self):
        self.id = None
        self.nom = None
        self.prenom = None
        self.position = tuple() # (longitude, latitude)
        self.etat = 0
        # 0 -> connecté mais aucune information de l'utilisateur;
        # 1 -> connecté avec information;
        # 172.20.10.2
        self.lastEmit = None # date de la dernier emission du tracker
        self.nbFollower = 0


    def reset(self):
        self.id = None
        self.nom = None
        self.prenom = None
        self.position = tuple()
        self.etat = 0
        self.lastEmit = None
        self.nbFollower = 0


class Mapper: ## HashMap permettant d'associer un socket à un utilisateur
    def __init__(self,serverAssistant):
        self.mapIdSock = dict() # HashMap<IdTel,sockPatient>
        self.dictSocketPatient = dict() # <sockPatient,Tracker>

        self.dictAssistance = dict() # HashMap<sockAssistant,sockPatient>
        
        self.serverAssistant = serverAssistant
        self.serverAssistant.setMapper(self)
        print("Mapper ready [ok]")

    def addTracker(self,socket):
        with lockMap:
            #print("add tracker")
            print(socket)
            self.dictSocketPatient[socket] = Tracker()

    def delTracker(self,socket):
        with lockMap:
            if socket in self.dictSocketPatient.keys():
                del self.dictSocketPatient[socket]

    def getTracker(self,socket):
        with lockMap:
            if socket in self.dictSocketPatient.keys():
                return self.dictSocketPatient[socket]

    def getSocketPatientById(self,idTel):
        with lockMap:
            if idTel in self.mapIdSock.keys():
                return self.mapIdSock[idTel]

    def getTrackerById(self,idTel):
        with lockMap:
            if idTel in self.mapIdSock.keys():
                sock = self.mapIdSock[idTel]
                return self.getTracker(sock)

    def getSocketPatient(self):
        with lockMap:
            return self.dictSocketPatient.keys()

    #################################################


    def getSocketsAssistant(self):
        return self.dictAssistance.keys()

    
    def attachAssistant(self,socketPatient,socketAssistant):
        #print(socketPatient,"attach with",socketAssistant)
        with lockMap:
            if socketAssistant in self.dictAssistance.keys():
                listeSocketPatient = self.dictAssistance[socketAssistant]
                listeSocketPatient.append(socketPatient)

            else:
                self.dictAssistance[socketAssistant] = [socketPatient]

    def detachAssistant(self,socketPatient,socketAssistant):
        with lockMap:
            if socketAssistant in self.dictAssistance.keys():
                listeSpatient = self.dictAssistance[socketAssistant]
                if socketPatient in listeSpatient:
                    index = listeSpatient.index(socketPatient)
                    listeSpatient.pop(index)
                    self.dictAssistance[socketAssistant] = listeSpatient

    def addAssistant(self,assistantSock):
        with lockMap:
            if assistantSock not in self.dictAssistance.keys():
                self.dictAssistance[assistantSock] = []

                
    def removeAssistant(self,sockAssistant):
        with lockMap:
            if sockAssistant in self.dictAssistance.keys():
                del self.dictAssistance[sockAssistant]

    def removePatient(self,sockPatient):
        with lockMap:
            allAssistants = list(self.dictAssistance.keys())
            for assistantSock in allAssistants:
                listeSock = self.dictAssistance[assistantSock]
                if (sockPatient in listeSock):
                    index = listeSock.index(sockPatient)
                    listeSock.pop(index)
                    self.dictAssistance[assistantSock] = listeSock

                    
    def apply(self,commande):
        ## parse la commande et applique la commande
        # commande contient un tuple (socket , commande)
        ## les commandes:
        ## entete*parametre*parametre*...
        ## separateur: *
        ## POSITION*longitude(float)*latitude(float) -> rafraichir la position du tracker sur le serveur
        ## STARTSUIVI*IDSMARTPHONE -> associer le socket à l'id smartphone
        ## STOPSUIVI -> indiquer au smartphone d'arreter le suivi
        socket = commande[0]
        requete = commande[1].rstrip()
        requeteArray = requete.split("*")
        entete = requeteArray[0]

        if (entete == "STARTSUIVI"):
            idTel = requeteArray[1]
            tracker = self.getTracker(socket)
            tracker.etat = 1
            tracker.id = idTel
            tracker.lastEmit = time.time()
            self.dict[socket] = tracker
            self.mapIdSock[idTel] = socket
            #print("Demarrage du suivi pour le tel à l'id : ",idTel)
            self.serverAssistant.event("STARTSUIVI",socket,tracker)
            #socket.send("OKPROMENADE\r\n".encode("utf-8")) # Pour l'instant on valide de suite


        elif (entete == "POSITION"):
            longitude = float(requeteArray[1])
            latitude = float(requeteArray[2])
            tracker = self.getTracker(socket)
            if (tracker == None):
                # print("Erreur, tracker inconnu")
                return

            print("Nouvelle position connue pour :",tracker.id," (",longitude,",",latitude,")")
            tracker.position = (longitude,latitude)
            tracker.lastEmit = time.time()
            self.dict[socket] = tracker
            self.serverAssistant.event("POSITION",socket,tracker)

        elif (entete == "STOPSUIVI"):
            tracker = self.getTracker(socket)
            tracker.reset()
            #print("le tracker ayant l'id :",tracker.id," a terminé la promenade")
            socket.send("STOPSUIVI\r\n".encode('utf-8'))

        elif (entete == "CONTINUE"):
            print("CONTINUE RECEIVE")
            idTel = requeteArray[1]
            nwTracker = self.dict[socket]
            
            allkeys = list(self.dict.keys())
            for key in allkeys:
                if self.dict[key].id == idTel:
                    old = self.dict[key]
                    nwTracker.id = idTel
                    nwTracker.position = old.position
                    nwTracker.etat = old.etat
                    nwTracker.lastEmit = time.time()
                    self.dict[socket] = nwTracker
                    self.mapIdSock[idTel] = socket
                    del self.mapIdSock[key]
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
            time.sleep(0.4)
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
    def __init__(self,portPatient,sizeBuffer,maxClientSocket,mapper):
        Thread.__init__(self)
        self.PORT = portPatient
        
        self.RECV_BUFFER = sizeBuffer
        self.maxClientSocket = maxClientSocket
        self.CONNECTION_LIST = [] # liste des patients connectés (socket)
        self.serverOnline = True
        self.mapper = mapper # HashMap<socket,Profil>

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
            
            liste = list(self.mapper.getSocketPatient()) # les sockets
                
            liste.extend(self.CONNECTION_LIST)
            socketsClients = list(self.mapper.getSocketPatient())
            print("====== ALL TRACKERS ========")
            for sock in socketsClients:
                tracker = self.mapper.getTracker(sock)
                print("id : ",tracker.id,
                      ", etat : ",tracker.etat,
                      ", nom : ",tracker.nom,
                      ", prenom : ",tracker.prenom,
                      ", position : ",tracker.position,
                      ", follower : ",tracker.nbFollower)
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
    serveAssistant = AssistanceServer(3100,4096,100) # port 3100
    mapper = Mapper(serveAssistant)
    servePatient = PatientServer(3000,4096,200,mapper) # sur le port 3000
    servePatient.start()
    serveAssistant.start()
    servePatient.join()
    serveAssistant.join()
