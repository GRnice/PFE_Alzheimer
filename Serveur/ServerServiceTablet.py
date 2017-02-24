import socket , select

from threading import Thread,RLock
import Profils
import LookupAssistantPatient
from Pinger import Pinger
import time

lockPool = RLock()

################################################
################################################
################################################
##
## ASSISTANT_SERVEUR <---------------> Assistant
##
################################################
################################################
################################################
class AssistanceServer(Thread):
    def __init__(self,portAssistant,sizeBuffer,maxClientSocket):
        Thread.__init__(self)
        self.PORT = portAssistant

        self.RECV_BUFFER = sizeBuffer
        self.maxClientSocket = maxClientSocket
        self.CONNECTION_LIST = [] # liste des patients connectés (socket)
        self.serverOnline = False

        self.managerProfils = Profils.ManagerProfile("./profils.txt")
        self.managerProfils.read()


    def setMapper(self,mapper):
        self.mapper = mapper

    def stopServer(self):
        self.serverOnline = False

    # emet un message à tous les assistants
    def broadcast(self,messageString):
        allAssistants = self.mapper.getSocketsAssistant()
        for assistant in allAssistants:
            try:
                assistant.send(messageString.encode("utf-8"))
            except ConnectionResetError as err:
                self.removeAssistant(assistant)

    # emet un message à tous les assistants sauf à l'assistant socketAssistant

    def broadcastFilter(self,messageString,socketAssistant):
        # broadcast le message sauf a socketAssistant
        allAssistants = self.mapper.getSocketsAssistant()
        for assistant in allAssistants:
            if (assistant != socketAssistant):
                try:
                    assistant.send(messageString.encode("utf-8"))
                except ConnectionResetError as err:
                    self.removeAssistant(assistant)
                



    # traite les evenements venants de server.py
    def event(self, evt, socket, tracker):
        if (evt == "STARTSUIVI"):
            print("(startsuivi) DEMANDE D'UN SUIVI POUR ",tracker.id)
            self.broadcast("NEWSESSION$"+tracker.id+"\r\n")

        elif (evt == "POSITION"):
            print("(update) TRANSMISSION DE LA POSITION DE ",tracker.id)
            self.broadcast("UPDATE$"+str(tracker.id) + "*" + str(tracker.position[0]) + "*" +
                                       str(tracker.position[1])+ "*" +  str(tracker.battery) + "*" + str(tracker.tempsRestant) + "\r\n")

        elif (evt == "ALERT-POSITION_START"):
            messageAlerte = "ALERT$STARTHORSZONE_"+tracker.id+"\r\n"
            if(tracker.lastAlert == None):
                tracker.lastAlert = [time.time(), messageAlerte]
            else:
                tracker.lastAlert.append(messageAlerte)
            print("(alerte) HORS ZONE",tracker.id)
            self.broadcast(messageAlerte)

        elif (evt == "ALERT-POSITION_STOP"):
            messageAlerte = "ALERT$STOPHORSZONE_"+tracker.id+"\r\n"
            if(tracker.lastAlert == None):
                pass
            else:
                tracker.lastAlert.append(messageAlerte)
            print("STOP HORS ZONE",tracker.id)
            self.broadcast(messageAlerte)

        elif(evt == "ALERT-BATTERY_START"):
            messageAlerte = "ALERT$STARTBATTERY_"+tracker.id+"\r\n"
            if(tracker.lastAlert == None):
                tracker.lastAlert = [time.time(), messageAlerte]
            else:
                tracker.lastAlert.append(messageAlerte)
            print("(alerte) BATTERY FAIBLE", tracker.id)
            self.broadcast(messageAlerte)

        elif(evt == "ALERT-BATTERY_STOP"):
            messageAlerte = "ALERT$STOPBATTERY_"+tracker.id+"\r\n"
            if(tracker.lastAlert == None):
                pass
            else:
                tracker.lastAlert.append(messageAlerte)
            print("(alerte) STOP BATTERY FAIBLE", tracker.id)
            self.broadcast(messageAlerte)
			
        elif(evt == "ALERT-IMMOBILE"):
            messageAlerte = "ALERT$IMMOBILE_"+tracker.id+"\r\n"
            if(tracker.lastAlert == None):
                tracker.lastAlert = [time.time(), messageAlerte]
            else:
                tracker.lastAlert.append(messageAlerte)
            print("(alerte) IMMOBILE", tracker.id)
            self.broadcast(messageAlerte)

        elif (evt == "ALERT-IMMOBILE_STOP"):
            messageAlerte = "ALERT_STOPIMMOBILITE_"+tracker.id+"\r\n"
            if (tracker.lastAlert == None):
                pass
            else:
                tracker.lastAlert.append(messageAlerte)
            print("(alerte) STOP IMMOBILITE",tracker.id)
            self.broadcast(messageAlerte)

        elif (evt == "ALERT-TIMEOUT-UPDATE_START"):
            messageAlerte = "ALERT$STARTTIMEOUTUPDATE_"+tracker.id+"\r\n"
            if(tracker.lastAlert == None):
                tracker.lastAlert = [time.time(), messageAlerte]
            else:
                tracker.lastAlert.append(messageAlerte)
            print("(alerte) TIMEOUT UPDATE", tracker.id)
            self.broadcast(messageAlerte)

        elif (evt == "ALERT-TIMEOUT-UPDATE_STOP"):
            messageAlerte = "ALERT$STOPTIMEOUTUPDATE_"+tracker.id+"\r\n"
            if(tracker.lastAlert == None):
                pass
            else:
                tracker.lastAlert.append(messageAlerte)
            print("(alerte) STOP TIMEOUT UPDATE", tracker.id)
            self.broadcast(messageAlerte)

        elif (evt == "ALERT-DURATION_START"):
            messageAlerte = "ALERT$DURATION_"+tracker.id+"\r\n"
            if(tracker.lastAlert == None):
                tracker.lastAlert = [time.time(), messageAlerte]
            else:
                tracker.lastAlert.append(messageAlerte)
            print("(alerte) fin de promenade",tracker.id)
            self.broadcast(messageAlerte)

    # ajout d'un assistant
    def addAssistant(self,sockAssistant):
        self.mapper.addAssistant(sockAssistant)
        # lui transmettre tous les promenés.
        listOfSocketPatient = list(self.mapper.getSocketPatient())

        for sockPatient in listOfSocketPatient:
            tracker = self.mapper.getTracker(sockPatient)
            if tracker.etat == 2: # si le tracker courant a recu un OKPROMENADE
                message = "SYNCH$NWPROMENADE_"+tracker.id+"*"+tracker.nom+"*"+tracker.prenom+"\r\n"
                sockAssistant.send(message.encode('utf-8'))

            elif tracker.etat == 1:
                message = "NEWSESSION$"+tracker.id+"\r\n"
                print("REPORTED NEW SESSION")
                sockAssistant.send(message.encode('utf-8'))

    # retrait d'un assistant
    def removeAssistant(self,sid):
        self.mapper.removeAssistant(sid)

    # arret du suivi d'un assistant(sockAssistant) à un patient ayant comme id (idTel)
    def unfollow(self,sockAssistant,idTelPatient):
        socketPatient = self.mapper.getSocketPatientById(idTelPatient)
        self.mapper.detachAssistant(socketPatient,sockAssistant)

    # abonnement d'un assistant à un la promenade d'un patient
    # data contient soit idTel -> ce qui implique que l'assistant s'abonne à une promenade deja configurée
    # soit idTel*prenom*nom -> configuration d'une promenade, cet assistant s'abonne egalement au suivi de ce patient
    def follow(self,sockAssistant,data):
        data = data.split("*")
        print(data)
        if (len(data) == 1):
            # si FOLLOW$idTel
            idTel = data[0]
            socketPatient = self.mapper.getSocketPatientById(idTel)
            self.mapper.attachAssistant(socketPatient,sockAssistant)


        elif (len(data) == 5):
            # si FOLLOW$idTel*prenom*nom*duree*tempsImmobile
            idTel = data[0] ; prenom = data[1] ; nom = data[2] ; duree = data[3] ; tempsImmobile = data[4]

            sockPatient = self.mapper.getSocketPatientById(idTel)
            tracker = self.mapper.getTrackerById(idTel)

            if (tracker.etat == 1): # le premier qui défini le profil du device
                tracker.startPromenade(nom,prenom,duree)
                print("OKPROMENADE POUR ",tracker.id)
                sockPatient.send(("OKPROMENADE*" + tempsImmobile + "\r\n").encode("utf-8"))
                self.broadcastFilter("SYNCH$NWPROMENADE_"+idTel+"*"+nom+"*"+prenom+"\r\n",sockAssistant)

            self.mapper.attachAssistant(sockPatient,sockAssistant)

    def canUnfollow(self,sock,idTel):
        trackerPatient = self.mapper.getTrackerById(idTel)
        return trackerPatient.nbFollower > 1


    def stopPromenade(self,sockAssistant,data):
        data = data.split("*")
        idTel = data[0]
        sockPatient = self.mapper.getSocketById(idTel)
        print('(stopsuivi) ARRET DU SUIVI DE ',idTel)
        sockPatient.send("STOPSUIVI\r\n".encode("utf-8"))
        self.mapper.removePatient(sockPatient)

    def stopServer(self):
        self.serverOnline = False

    def run(self):
        self.serverOnline = True
        self.pinger = Pinger(self.mapper,self)
        self.pinger.start()
        server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server_socket.bind(('', self.PORT))
        server_socket.listen(self.maxClientSocket)
        # Add server socket to the list of readable connections
        self.CONNECTION_LIST.append(server_socket)

        print("Serveur Assistant on port " + str(self.PORT) + " [ok]")
        print("=============SERVEUR ONLINE=============")
        while self.serverOnline:
        # Get the list sockets which are ready to be read through select

            liste = list(self.mapper.getSocketsAssistant()) # les sockets des assistants
            liste.extend(self.CONNECTION_LIST)
            socketsClients = list(self.mapper.getSocketsAssistant())
            read_sockets,write_sockets,error_sockets = select.select(liste,[],[])

            for sock in read_sockets:
                #New connection
                if sock == server_socket:
                    sockfd, addr = server_socket.accept()
                    message = "PROFILES$"+str(self.managerProfils)+"\r\n"
                    sockfd.send(message.encode('utf-8'))
                    self.addAssistant(sockfd)
                    print("Assistant (%s, %s) connected" % addr)
                    print("NB ASSISTANT",len(self.mapper.getSocketsAssistant()))

                else:
                    # Data received from client, process it
                    try:
                        #In Windows, sometimes when a TCP program closes abruptly,
                        # a "Connection reset by peer" exception will be thrown
                        data = sock.recv(self.RECV_BUFFER)
                        tabdata = data.decode('utf-8').rstrip().split("\r\n")
                        print(tabdata)
                        for data in tabdata:

                            if len(data) > 0:

                                message = data.rstrip().split("$")
                                # Si c'est un follow
                                if message[0] == "FOLLOW":
                                    print('follow received')
                                    #"entete$idelTel*prenom*nom"
                                    self.follow(sock,message[1])

                                elif message[0] == "UNFOLLOW":
                                    # entete$idTel
                                    print('unfollow received')
                                    idTel = message[1]
                                    if (self.canUnfollow(sock,idTel)): # message[1] = idTel
                                        self.unfollow(sock,idTel)
                                        sock.send(("UNFOLLOW$ALLOW_"+idTel+"\r\n").encode('utf-8'))
                                        print("OK UNFOLLOW")
                                    else:
                                        sock.send(("UNFOLLOW$INTERDICT_"+idTel+"\r\n").encode('utf-8'))
                                        print("NO UNFOLLOW")


                                elif message[0] == "STOPPROMENADE":
                                    # "entete$idTel"
                                    self.stopPromenade(sock,message[1])

                                # ENTETE$nom,prenom,barriereAlerte | barriereNormal
                                elif message[0] == "ADDPROFIL":
                                    print("ADDPROFIL",message[1])
                                    profilStr = message[1].split(',')
                                    nom = profilStr[0]; prenom = profilStr[1];idAvatar = profilStr[2]; barriere = profilStr[3]
                                    self.managerProfils.addProfil(nom,prenom,idAvatar,barriere)
                                    self.broadcastFilter("SYNCH$NWPROFIL_"+nom+"*"+prenom+"*"+idAvatar+"*"+barriere+"\r\n",sock)

                                elif message[0] == "SUPPRPROFIL":
                                    print('SUPPRPROFIL')
                                    print(message[1])
                                    profilStr = message[1].split(',')
                                    nom = profilStr[0]; prenom = profilStr[1]; barriere = profilStr[2]
                                    self.managerProfils.supprProfil(nom,prenom)
                                    print('send du synch')
                                    print("SYNCH$RMPROFIL_"+nom+"*"+prenom)
                                    self.broadcastFilter("SYNCH$RMPROFIL_"+nom+"*"+prenom+"\r\n",sock)

                                elif message[0] == "MODIFPROFIL":
                                    print("MODIFPROFIL")
                                    oldAndNewProfil = message[1].split('*')
                                    self.managerProfils.modifProfil(oldAndNewProfil[0],oldAndNewProfil[1])
                                    print("send du synch modif")
                                    print("SYNCH$MODIFPROFIL_"+message[1])
                                    self.broadcastFilter("SYNCH$MODIFPROFIL_"+message[1]+"\r\n",sock)
                                elif message[0] == "CONTINUE":
                                    print("CONTINUE")
                                    #L'ancien socket meurt automatiquement dans le except Exception quand on essaye d'envoyer un update dans l'ancien socket
                                    #L'ancien socket est aussi supprimé dans le except
                                    #On ajoute le nouveau socket à la liste d'assistants
                                    self.addAssistant(sock)

                                    message = ""
                                    allPatients = list(self.mapper.getSocketPatient())
                                    for socketPatient in allPatients:
                                        tracker = self.mapper.getTracker(socketPatient)
                                        if tracker != None and tracker.etat == 2:
                                            tracker = self.mapper.getTracker(socketPatient)
                                            message += tracker.toString()
                                            message += "*"

                                    if message != "":
                                        sock.send(("SYNCH$SYNCH-CONTINUE_"+message[0:-1]+"\r\n").encode('utf-8'))

                                elif message[0] == "CHECKALERT":
                                    print("CHECKALERT")
                                    tracker = self.mapper.getTrackerById(message[1])
                                    if tracker != None and tracker.etat == 2:
                                        self.mapper.getTrackerById(message[1]).lastAlert = None
                            else:
                                print("Assistant (%s) is offline" % sock)
                                sock.close()
                                self.removeAssistant(sock)



                    # client disconnected, so remove from socket list
                    except Exception as err:
                        #broadcast_data(sock, "Client (%s, %s) is offline" % addr)
                        print("(fail) Assistant (%s) is offline" % sock)
                        print("raison du crash",err)
                        sock.close()
                        self.removeAssistant(sock)


        server_socket.close()
        self.pinger.stop()
        self.pinger.join()
        print("=============SERVEUR OFFLINE=============")
