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
        self.battery = 0
        self.etat = 0
        self.isHorsZone = False
        self.batteryIsLow = False
        self.timeout = False
        self.updateTimeout = False
        self.dureePromenade = None
        self.tempsRestant = None
        self.risqueFranchissementBarriere = False
        # 0 -> connecté mais aucune information de l'utilisateur;
        # 1 -> connecté avec information -> (idtel);
        # 2 -> connecté et suivi (nom prenom et idtel connues)
        # 172.20.10.2
        self.lastEmit = None # date de la dernier emission du tracker
        self.nbFollower = 0
        self.lastAlert = None
        self.alertTimeout = False


    def toString(self):
        message = ""
        message += "IDTEL:"+self.id +":NOM:"+self.nom +":PRENOM:"+self.prenom+":POSITION:"+str(self.position)+":"
        message += "BATTERY:"+str(self.battery) + ":ISHORSZONE:" + str(self.isHorsZone) + ":BATTERYISLOW:" + str(self.batteryIsLow)+":"
        message += "TIMEOUTPROMENADE:"+str(self.timeout) + ":"
        message += "UPDATETIMEOUT:"+str(self.updateTimeout) + ":DUREEPROMENADE:" + str(self.tempsRestant)
        return message



    def startPromenade(self,nom,prenom,duree,risqueBarriere): # OKPROMENADE RECU
        if self.etat == 1:
            self.etat = 2
            self.nom = nom
            self.prenom = prenom
            self.risqueFranchissementBarriere = (risqueBarriere == "true")
            print("risqueFranchissementBarriere -> "+risqueBarriere)
            timeEpoch70 = str(time.time()).split(".") # xxx.yyy # secondes depuis les années 70
            timeEpoch70 = int(timeEpoch70[0]) # recupere que les secondes # xxx
            # xxx + temps promenade en secondes
            self.dureePromenade = timeEpoch70 + (int(duree) * 60) # passe de minutes en secondes, on obtient l'heure de fin en seconde de la promenade
            return True

        elif self.etat == 0:
            raise PermissionError("startPromenade appelé, mais le tracker est anonyme")

        elif self.etat == 2:
            raise PermissionError("startPromenade appelé, mais le tracker est deja suivi")


    def stopPromenade(self):
        if self.etat == 2:
            self.reset()
            return True
        elif self.etat == 1:
            print("annulation promenade")
            self.reset()
            return True

        elif self.etat == 0:
            return False
            print("Annulation alors que le tracker est anonyme")

    def startSuiviCalled(self,idTel,lastemit):
        if self.etat == 0:
            self.etat = 1
            self.id = idTel
            self.lastEmit = lastemit
            return True

        elif self.etat == 1:
            print("startSuiviCalled appelé , mais le tracker a deja émit une demande de suivi et attend une réponse")
            return False

        elif self.etat == 2:
            raise PermissionError("startSuiviCalled appelé , mais le tracker est en cours de promenade")


    def updatePosition(self,position,timeupdate, battery,tempsCourant):
        self.position = position
        self.lastEmit = timeupdate
        self.battery = battery
        if (self.dureePromenade != None and tempsCourant != None):
            self.tempsRestant = self.dureePromenade - tempsCourant


class Mapper: ## HashMap permettant d'associer un socket à un utilisateur
    def __init__(self,serverAssistant):
        self.mapIdSock = dict() # HashMap<IdTel,sockPatient>
        self.dictSocketPatient = dict() # <sockPatient,Tracker>
        self.watchman = WatchMan()
        self.dictAssistance = dict() # HashMap<sockAssistant,none>
        self.mapIdAssistant = dict() # HashMap<tokenId,sockAssistant>

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

                
    def removePatient(self,sockPatient):
        with lockMap:
            tracker = self.getTracker(sockPatient)
            if tracker.id in list(self.mapIdSock.keys()):
                del self.mapIdSock[tracker.id]
                self.serverAssistant.broadcast("SYNCH$STOPPROMENADE_"+str(tracker.id)+"\r\n")


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

## ASSISTANT #####

    def getSocketsAssistant(self):
        with lockMap:
            return self.dictAssistance.keys()

    def getTokensAssistant(self):
        with lockMap:
            return self.mapIdAssistant.keys()

    def getSocketAssistantByToken(self,token):
        print("list of mapIdAssistant")
        if (token in list(self.mapIdAssistant.keys())):
            return self.mapIdAssistant[token]

    def attachAssistant(self,socketPatient,socketAssistant):
        with lockMap:
            tracker = self.getTracker(socketPatient)
            tracker.nbFollower += 1

    def detachAssistant(self,socketPatient,socketAssistant):
        with lockMap:
            tracker = self.getTracker(socketPatient)
            tracker.nbFollower -= 1

    def addAssistant(self,token,assistantSock):
        with lockMap:
            if assistantSock not in self.dictAssistance.keys():
                print("add assistant ok")
                self.dictAssistance[assistantSock] = token
                self.mapIdAssistant[token] = assistantSock

    def removeAssistant(self,sockAssistant):
        with lockMap:
            if sockAssistant in self.dictAssistance.keys():
                token = self.dictAssistance[sockAssistant]
                del self.dictAssistance[sockAssistant]
                del self.mapIdAssistant[token]

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
            if(not tracker.startSuiviCalled(idTel,None)):
                print("erreur StartSuivi")
                return # probleme , demande deja faite ou deja en promenade
            
            self.mapIdSock[idTel] = socket

            #print("Demarrage du suivi pour le tel à l'id : ",idTel)
            self.serverAssistant.event("STARTSUIVI",socket,tracker)
            #socket.send("OKPROMENADE\r\n".encode("utf-8")) # Pour l'instant on valide de suite


        elif (entete == "POSITION"):
            longitude = float(requeteArray[1])
            latitude = float(requeteArray[2])
            battery = int(requeteArray[3])
            tracker = self.getTracker(socket)
            if (tracker == None or tracker.etat != 2):
                print("position sur un tracker null, peut arriver juste apres un stop suivi")
                return
            
            secondesCourantes = str(time.time()).split(".")[0] ; secondesCourantesDepuisannee70 = int(secondesCourantes)

            tracker.updatePosition((longitude,latitude),time.time(),battery,secondesCourantesDepuisannee70)
            
            
            print("Nouvelle position connue pour :",tracker.id," (",longitude,",",latitude,"), battery = ", battery)
            self.serverAssistant.event("POSITION",socket,tracker)

            
            if (tracker.tempsRestant < 0):
                # si le temps de promenade est depassé !
                if (not tracker.timeout): # et si c'est la premiere fois
                    print("WARNING TIMEOUT PROMENADE")
                    tracker.timeout = True
                    self.serverAssistant.event("ALERT-DURATION_START",socket,tracker)

            if (not self.watchman.positionIsGood(tracker,longitude,latitude)): # si mauvaise position
                if (not tracker.isHorsZone): # si le tracker n'a pas taggé isHorsZone à True
                    print("WARNING HORS ZONE /!\/!\\")
                    tracker.isHorsZone = True
                    self.serverAssistant.event("ALERT-POSITION_START",socket,tracker)
                
            elif (tracker.isHorsZone): # si la position est bonne et si le tracker a isHorsZone taggé à True
                tracker.isHorsZone = False
                self.serverAssistant.event("ALERT-POSITION_STOP",socket,tracker) # un deuxieme envoi de l'alerte la desactive


            if (battery < 21 and (not tracker.batteryIsLow)):
                print("WARNING BATTERY LOW /!\/!\\")
                tracker.batteryIsLow = True
                self.serverAssistant.event("ALERT-BATTERY_START", socket, tracker)

            elif (battery >= 21 and tracker.batteryIsLow):
                tracker.batteryIsLow = False
                self.serverAssistant.event("ALERT-BATTERY_STOP",socket,tracker) # un deuxieme envoi de l'alerte la desactive
                

        elif (entete == "STOPSUIVI"):
            tracker = self.getTracker(socket)
            print("STOPSUIVI",tracker.etat,tracker.id)
            if tracker.etat != 0:
                if tracker.etat == 1:
                    print("broadcast stop new session")
                    self.serverAssistant.broadcast("STOPNEWSESSION$" + tracker.id + "\r\n")
                    
                self.removePatient(socket)
                tracker.stopPromenade()
                print("YAP")

            socket.send("STOPSUIVI\r\n".encode('utf-8'))

        elif (entete == "IMMOBILE"):
            print("Alerte immobilite")
            tracker = self.getTracker(socket)
            tracker.lastEmit = time.time()
            self.serverAssistant.event("ALERT-IMMOBILE",socket,tracker)

        elif (entete == "IMMOBILE-STOP"):
            print("Alerte immobilite stop")
            tracker = self.getTracker(socket)
            tracker.lastEmit = time.time()
            self.serverAssistant.event("ALERT-IMMOBILE_STOP",socket,tracker)

        elif (entete == "CONTINUE"):
            print("CONTINUE RECEIVE")
            idTel = requeteArray[1]
            nwTracker = self.getTracker(socket)
            oldSocket = self.mapIdSock[idTel]
            
            alltrackers = list(self.dictSocketPatient.values())
    #                self.mapIdSock = dict() # HashMap<IdTel,sockPatient>
   #     self.dictSocketPatient = dict() # <sockPatient,Tracker>
  #      self.watchman = WatchMan()
 #       self.dictAssistance = dict() # HashMap<sockAssistant,none!>

            for tracker in alltrackers:
                if tracker != nwTracker and tracker.id == idTel:
                    print("JE TRAITE CONTINUE")
                    oldTracker = tracker
                    nwTracker.id = idTel
                    nwTracker.position = oldTracker.position
                    nwTracker.etat = oldTracker.etat
                    nwTracker.lastEmit = time.time()
                    nwTracker.nbFollower = oldTracker.nbFollower
                    nwTracker.battery = oldTracker.battery
                    nwTracker.isHorsZone = oldTracker.isHorsZone
                    nwTracker.batteryIsLow = oldTracker.batteryIsLow
                    nwTracker.timeout = oldTracker.timeout
                    nwTracker.updateTimeout = oldTracker.updateTimeout
                    nwTracker.dureePromenade = oldTracker.dureePromenade
                    nwTracker.tempsRestant = oldTracker.tempsRestant
                    nwTracker.lastEmit = time.time()

                    del self.dictSocketPatient[oldSocket]
                    self.mapIdSock[idTel] = socket
                    self.dictSocketPatient[socket] = nwTracker
                    #self.updateSocketPatient(oldSocket,socket)
                    if (nwTracker.etat == 2):
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
        self.mapper = mapper

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
            for checksocket in liste:
                if (checksocket.fileno() == -1):
                    print("fd à 1, socket remove :")
                    print("\n\n")
                    liste.pop(liste.index(checksocket))
            read_sockets,write_sockets,error_sockets = select.select(liste,[],[])
            for sock in read_sockets:
                #New connection
                if sock == server_socket:
                    sockfd, addr = server_socket.accept()
                    self.mapper.addTracker(sockfd)
                    sockfd.send("CONNECTED\r\n".encode('utf-8'))
                    print("Client (%s, %s) connected" % addr)

                else:
                    # Data recieved from client, process it
                    try:
                        #In Windows, sometimes when a TCP program closes abruptly,
                        # a "Connection reset by peer" exception will be thrown
                        data = sock.recv(self.RECV_BUFFER).rstrip()

                        if len(data) > 0:
                            print(data)
                            data = data.decode('utf-8').splitlines()
                            for instr in data:
                                if instr != "":
                                    with lockPool:
                                        poolRequest.put((sock,instr))

                        else:
                            print("Client (%s) is offline" % sock)
                            if sock != None:
                                sock.close()
                                
                            tracker = self.mapper.getTracker(sock)
                            if tracker == None:
                                pass

                            if (tracker != None and tracker.etat != 2):
                                self.mapper.delTracker(sock)





                    # client disconnected, so remove from socket list
                    except Exception as err:
                        print(err)
                        #broadcast_data(sock, "Client (%s, %s) is offline" % addr)
                        print("(fail) Client (%s) is offline" % sock)
                        if sock != None:
                            sock.close()
                            
                        tracker = self.mapper.getTracker(sock)
                        if tracker == None: # si none le socket existe plus dans mapper
                            pass

                        if (tracker != None and tracker.etat != 2): #si == 2 on attendra un continue
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
