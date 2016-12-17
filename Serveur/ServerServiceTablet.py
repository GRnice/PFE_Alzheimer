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

        self.managerProfils = Profils.ManagerProfile()
        self.managerProfils.read("./profils.txt")


    def setMapper(self,mapper):
        self.mapper = mapper

    def stopServer(self):
        self.serverOnline = False

    def broadcast(self,messageString):
        allAssistants = self.mapper.getSocketsAssistant()
        for assistant in allAssistants:
            assistant.send(messageString.encode("utf-8"))

    def event(self, evt, socket, tracker):
        if (evt == "STARTSUIVI"):
            print("(startsuivi) DEMANDE D'UN SUIVI POUR ",tracker.id)
            self.broadcast("NEWSESSION$"+tracker.id+"\r\n")

        elif (evt == "POSITION"):
            print("(update) TRANSMISSION DE LA POSITION DE ",tracker.id)
            self.broadcast("UPDATE$"+str(tracker.id) + "*" + str(tracker.position[0]) + "*" +
                                       str(tracker.position[1])+"\r\n")


    def addAssistant(self,sid):
        self.mapper.addAssistant(sid)


    def removeAssistant(self,sid):
        self.mapper.removeAssistant(sid)

    def follow(self,sockAssistant,data):
        print(self.mapper.getSocketPatient())
        data = data.split("*")
        if (len(data) == 1):
            # si FOLLOW$idTel
            idTel = data[0]
            sockPatient = self.mapper.getSocketPatientById(idTel)
            tracker = self.mapper.getTrackerById(idTel)
            tracker.nbFollower += 1
            self.mapper.attachAssistant(sockPatient,sockAssistant)

        elif (len(data) == 3):
            # si FOLLOW$idTel*prenom*nom
            idTel = data[0]
            prenom = data[1]
            nom = data[2]
            sockPatient = self.mapper.getSocketPatientById(idTel)
            tracker = self.mapper.getTrackerById(idTel)

            if (tracker.nbFollower == 0): # le premier qui défini le profil du device
                tracker.nom = nom
                tracker.prenom = prenom
                print("OKPROMENADE POUR ",tracker.id)
                sockPatient.send("OKPROMENADE\r\n".encode("utf-8"))

            self.mapper.attachAssistant(sockPatient,sockAssistant)
            tracker.nbFollower += 1

    def stopPromenade(self,sockAssistant,data):
        data = data.split("*")
        idTel = data[0]
        sockPatient = self.mapper.getSocketById(idTel)
        print('(stopsuivi) ARRET DU SUIVI DE ',idTel)
        sockPatient.send("STOPSUIVI\r\n".encode("utf-8"))
        self.mapper.removePatient(sockPatient)

    
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
                            
                            message = (data.decode('utf-8')).split("$")
                            # Si c'est un follow
                            if message[0] == "FOLLOW":
                                #"entete$idelTel*prenom*nom"
                                self.follow(sock,message[1])

                            elif message[0] == "STOPPROMENADE":
                                # "entete$idTel"
                                self.stopPromenade(sock,message[1])

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
