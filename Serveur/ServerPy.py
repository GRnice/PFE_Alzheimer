# Socket server in python using select function

import sqlite3 as lite
from threading import Thread,RLock
import time
import socket, select
import queue
from ServerServiceTablet import *
from WatchMan import WatchMan
import sys

poolRequest = queue.Queue(500) # MAX 500 requetes à traiter

lockMap = RLock()

class Tracker: ## Classe representant un tracker
    def __init__(self):
        self.reset()

    def reset(self):
        self.id = None
        self.nom = None
        self.prenom = None
        self.position = tuple() # (longitude, latitude)
        self.etat = 0
        # 0 -> connecté mais aucune information de l'utilisateur;
        # 1 -> connecté avec information;
        # 2 -> connecté et suivi
        # 172.20.10.2
        self.lastEmit = None # date de la dernier emission du tracker
        self.nbFollower = 0

    def startPromenade(self,nom,prenom):
        if self.etat == 1:
            self.etat = 2
            return True

        if self.etat == 0:
            raise PermissionError("startPromenade appelé, mais le tracker est anonyme")

        if self.etat == 2:
            raise PermissionError("startPromenade appelé, mais le tracker est deja suivi")

    def stopPromenade(self):
        if self.etat == 2:
            self.etat = 0
            self.reset()
            return True

        if self.etat == 1:
            raise PermissionError("stopPromenade appelé, mais une demande de suivi a été envoyée et attend une réponse")

        if self.etat == 0:
            raise PermissionError("stopPromenade appelé, mais le tracker est anonyme")

    def startSuiviCalled(self,idTel,lastemit):
        if self.etat == 0:
            self.etat = 1
            self.id = idTel
            self.lastEmit = lastemit
            return True

        if self.etat == 1:
            raise PermissionError("startSuiviCalled appelé , mais le tracker a deja émit une demande de suivi et attend une réponse")

        if self.etat == 2:
            raise PermissionError("startSuiviCalled appelé , mais le tracker est en cours de promenade")
    

    def startSuiviAborted(self):
        if self.etat == 1:
            self.etat = 0
            return True

        if self.etat == 0:
            raise PermissionError("startSuiviAborted appelé , mais aucune demande de suivi n'a été formulée pour ce tracker")

        if self.etat == 2:
            raise PermissionError("startSuiviAborted appelé , mais le tracker en cours de promenade")

    def updatePosition(self,position,timeupdate):
        self.position = position
        self.lastEmit = timeupdate
        

class Mapper: ## HashMap permettant d'associer un socket à un utilisateur
    def __init__(self,serverAssistant):
        self.mapIdSock = dict() # HashMap<IdTel,sockPatient>
        self.dictSocketPatient = dict() # <sockPatient,Tracker>
        self.watchman = WatchMan()
        self.dictAssistance = dict() # HashMap<sockAssistant,sockPatient>
        
        self.serverAssistant = serverAssistant
        self.serverAssistant.setMapper(self)
        print("Mapper ready [ok]")

    def addTracker(self,socket):
        with lockMap:
            #print("add tracker")
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

            tracker = self.getTracker(socketPatient)
            tracker.nbFollower += 1

    def detachAssistant(self,socketPatient,socketAssistant):
        with lockMap:
            if socketAssistant in self.dictAssistance.keys():
                listeSpatient = self.dictAssistance[socketAssistant]
                if socketPatient in listeSpatient:
                    index = listeSpatient.index(socketPatient)
                    listeSpatient.pop(index)
                    self.dictAssistance[socketAssistant] = listeSpatient
                    tracker = self.mapper.getTracker(socketPatient)
                    tracker.nbFollower -= 1

    def addAssistant(self,assistantSock):
        with lockMap:
            if assistantSock not in self.dictAssistance.keys():
                self.dictAssistance[assistantSock] = []

                
    def removeAssistant(self,sockAssistant):
        with lockMap:
            if sockAssistant in self.dictAssistance.keys():
                del self.dictAssistance[sockAssistant]

    def updateSocketPatient(self,nwSocketPatient,oldSocketPatient):
        with lockMap:
            allAssistants = list(self.getSocketsAssistant())
            for assistantSock in allAssistants:
                listeSock = self.dictAssistance[assistantSock]
                if (oldSocketPatient in listeSock):
                    index = listeSock.index(oldSocketPatient)
                    listeSock[index] = nwSocketAssistant

    def removePatient(self,sockPatient):
        with lockMap:
            tracker = self.getTracker(sockPatient)
            allAssistants = list(self.getSocketsAssistant())
            for assistantSock in allAssistants:
                listeSock = self.dictAssistance[assistantSock]
                if (sockPatient in listeSock):
                    index = listeSock.index(sockPatient)
                    listeSock.pop(index)
                    #self.dictAssistance[assistantSock] = listeSock
            
            del self.mapIdSock[tracker.id]
            self.serverAssistant.broadcast("SYNCH$STOPPROMENADE_"+str(tracker.id)+"\r\n")

                    
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
        requete = commande[1]
        requeteArray = requete.split("*")
        entete = requeteArray[0]

        if (entete == "STARTSUIVI"):
            idTel = requeteArray[1]
            tracker = self.getTracker(socket)
            tracker.startSuiviCalled(idTel,time.time())
            self.mapIdSock[idTel] = socket
            
            #print("Demarrage du suivi pour le tel à l'id : ",idTel)
            self.serverAssistant.event("STARTSUIVI",socket,tracker)
            #socket.send("OKPROMENADE\r\n".encode("utf-8")) # Pour l'instant on valide de suite


        elif (entete == "POSITION"):
            longitude = float(requeteArray[1])
            latitude = float(requeteArray[2])
            tracker = self.getTracker(socket)
            tracker.updatePosition((longitude,latitude),time.time())
            print("Nouvelle position connue pour :",tracker.id," (",longitude,",",latitude,")")
            self.serverAssistant.event("POSITION",socket,tracker)

            if (not self.watchman.positionIsGood(longitude,latitude)):
                print("WARNING HORS ZONE /!\/!\\")

        elif (entete == "STOPSUIVI"):
            tracker = self.getTracker(socket)
            self.removePatient(socket)
            tracker.stopPromenade()
            #print("le tracker ayant l'id :",tracker.id," a terminé la promenade")
            socket.send("STOPSUIVI\r\n".encode('utf-8'))

        elif (entete == "CONTINUE"):
            print("CONTINUE RECEIVE")
            idTel = requeteArray[1]
            nwTracker = self.getTracker(socket)
            oldSocket = self.mapIdSock[idTel]
        
            alltrackers = list(self.dictSocketPatient.values())
    #                self.mapIdSock = dict() # HashMap<IdTel,sockPatient>
   #     self.dictSocketPatient = dict() # <sockPatient,Tracker>
  #      self.watchman = WatchMan()
 #       self.dictAssistance = dict() # HashMap<sockAssistant,sockPatient>
 
            for tracker in alltrackers:
                if tracker != nwTracker and tracker.id == idTel:
                    print("JE TRAITE CONTINUE")
                    oldTracker = tracker
                    nwTracker.id = idTel
                    nwTracker.position = oldTracker.position
                    nwTracker.etat = oldTracker.etat
                    nwTracker.lastEmit = time.time()
                    nwTracker.nbFollower = oldTracker.nbFollower
                    
                    del self.dictSocketPatient[oldSocket]
                    self.mapIdSock[idTel] = socket
                    self.dictSocketPatient[socket] = nwTracker
                    self.updateSocketPatient(oldSocket,socket)
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
            print("NB sockets -> ",len(socketsClients))
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
                        data = sock.recv(self.RECV_BUFFER).rstrip()

                        if len(data) > 0:
                            with lockPool:
                                poolRequest.put((sock,data.decode('utf-8')))

                        else:
                            print("Client (%s) is offline" % sock)
                            sock.close()
                            with lockMap:
                                tracker = self.mapper.getTracker(sock)
                                if tracker == None:
                                    pass
                                
                                elif (tracker == 2 or tracker == 1):
                                    print("Socket d'un patient perdu !")
                                    
                                if (tracker != None and tracker.etat != 2):
                                    self.mapper.delTracker(sock)

                                

         
                     
                    # client disconnected, so remove from socket list
                    except Exception as err:
                        print(err)
                        #broadcast_data(sock, "Client (%s, %s) is offline" % addr)
                        print("Client (%s) is offline" % sock)
                        sock.close()
                        with lockMap:
                            tracker = self.mapper.getTracker(sock)
                            if tracker == None:
                                pass
                            elif (tracker == 2 or tracker == 1):
                                    print("Except Socket d'un patient perdu !")
                                    
                            if (tracker != None and tracker.etat != 2):
                                print("Except suppression d'un socket, il n'est pas à l'etat 2")
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
