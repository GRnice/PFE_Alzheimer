import socket , select

from threading import Thread,RLock
import Profils
import LookupAssistantPatient

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

    def broadcast(self,messageString):
        allAssistants = self.mapper.getSocketsAssistant()
        for assistant in allAssistants:
            assistant.send(messageString.encode("utf-8"))

    def broadcastFilter(self,messageString,socketAssistant):
        # broadcast le message sauf a socketAssistant
        allAssistants = self.mapper.getSocketsAssistant()
        for assistant in allAssistants:
            if (assistant != socketAssistant):
                assistant.send(messageString.encode("utf-8"))

    def event(self, evt, socket, tracker):
        if (evt == "STARTSUIVI"):
            print("(startsuivi) DEMANDE D'UN SUIVI POUR ",tracker.id)
            self.broadcast("NEWSESSION$"+tracker.id+"\r\n")

        elif (evt == "POSITION"):
            print("(update) TRANSMISSION DE LA POSITION DE ",tracker.id)
            self.broadcast("UPDATE$"+str(tracker.id) + "*" + str(tracker.position[0]) + "*" +
                                       str(tracker.position[1])+"\r\n")


    def addAssistant(self,sockAssistant):
        self.mapper.addAssistant(sockAssistant)
        # lui transmettre tous les promenés.
        listOfSocketPatient = list(self.mapper.getSocketPatient())
        
        for sockPatient in listOfSocketPatient:
            tracker = self.mapper.getTracker(sockPatient)
            if tracker.etat == 2: # si le tracker courant a recu un OKPROMENADE
                message = "SYNCH$NWPROMENADE_"+tracker.id+"*"+tracker.nom+"*"+tracker.prenom+"\r\n"
                print("addAssistant, message --> ",message)
                sockAssistant.send(message.encode('utf-8'))

    def removeAssistant(self,sid):
        self.mapper.removeAssistant(sid)

    def unfollow(self,sockAssistant,data):
        socketPatient = self.mapper.getSocketpatientById(data)
        self.mapper.detachAssistant(sockAssistant,socketPatient)

    def follow(self,sockAssistant,data):
        print(self.mapper.getSocketPatient())
        data = data.split("*")
        if (len(data) == 1):
            # si FOLLOW$idTel
            idTel = data[0].rstrip()
            sockPatient = self.mapper.getSocketPatientById(idTel)
            tracker = self.mapper.getTrackerById(idTel)
            tracker.nbFollower += 1
            self.mapper.attachAssistant(sockPatient,sockAssistant)

        elif (len(data) == 3):
            # si FOLLOW$idTel*prenom*nom
            idTel = data[0].rstrip()
            prenom = data[1].rstrip()
            nom = data[2].rstrip()
            sockPatient = self.mapper.getSocketPatientById(idTel)
            tracker = self.mapper.getTrackerById(idTel)

            if (tracker.nbFollower == 0): # le premier qui défini le profil du device
                tracker.nom = nom.rstrip()
                tracker.prenom = prenom.rstrip()
                tracker.etat = 2
                print("OKPROMENADE POUR ",tracker.id)
                sockPatient.send("OKPROMENADE\r\n".encode("utf-8"))
                self.broadcastFilter("SYNCH$NWPROMENADE_"+idTel+"*"+nom+"*"+prenom+"\r\n",sockAssistant)
                
            self.mapper.attachAssistant(sockPatient,sockAssistant)
            tracker.nbFollower += 1

    def stopPromenade(self,sockAssistant,data):
        data = data.split("*")
        idTel = data[0]
        sockPatient = self.mapper.getSocketById(idTel)
        tracker = self.mapper.getTracker(sockPatient)
        tracker.etat = 0
        print('(stopsuivi) ARRET DU SUIVI DE ',idTel)
        sockPatient.send("STOPSUIVI\r\n".encode("utf-8"))
        self.mapper.removePatient(sockPatient)

    def stopServer(self):
        self.serverOnline = False
    
    def run(self):
        self.serverOnline = True
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
                    self.addAssistant(sockfd)
                    message = "PROFILES$"+str(self.managerProfils)+"\r\n"
                    sockfd.send(message.encode('utf-8'))
                    print("Assistant (%s, %s) connected" % addr)
                    
                else:
                    # Data recieved from client, process it
                    try:
                        #In Windows, sometimes when a TCP program closes abruptly,
                        # a "Connection reset by peer" exception will be thrown
                        data = sock.recv(self.RECV_BUFFER)
                        
                        if len(data) > 0:
                            
                            message = (data.decode('utf-8')).rstrip().split("$")
                            # Si c'est un follow
                            if message[0] == "FOLLOW":
                                print('follow received')
                                #"entete$idelTel*prenom*nom"
                                self.follow(sock,message[1])

                            elif message[0] == "UNFOLLOW":
                                    # entete$idTel
                                print('unfollow received')
                                self.unfollow(sock,message[1])

                            elif message[0] == "STOPPROMENADE":
                                # "entete$idTel"
                                self.stopPromenade(sock,message[1])

                            # ENTETE$nom,prenom,barriereAlerte | barriereNormal
                            elif message[0] == "ADDPROFIL":
                                print("ADDPROFIL",message[1])
                                profilStr = message[1].split(',')
                                nom = profilStr[0]; prenom = profilStr[1]; barriere = profilStr[2]
                                self.managerProfils.addProfil(nom,prenom,barriere)
                                self.broadcastFilter("SYNCH$NWPROFIL_"+nom+"*"+prenom+"*"+barriere+"\r\n",sock)

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
        print("=============SERVEUR OFFLINE=============")
