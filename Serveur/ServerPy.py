# Socket server in python using select function

import sqlite3 as lite
from threading import Thread,RLock

import socket, select
import queue

poolRequest = queue.Queue(500) # MAX 100 requetes à traiter
mlock = RLock()


class Tracker: ## Classe representant un tracker
    def __init__(self):
        self.name = None
        self.position = tuple() # (latitude longitude)
        self.etat = 0 # 0 -> connecté mais aucune information de l'utilisateur; 1 -> connecté avec information;
        


class Mapper: ## HashMap permettant d'associer un socket à un utilisateur
    def __init__(self):
        self.dict = dict()
        print("Mapper ready [ok]")
    def addTracker(self,socket):
        print("add tracker")
        print(socket)
        self.dict[socket] = Tracker()

    def delTracker(self,socket):
        del self.dict[socket]

    def getTracker(self,socket):
        if socket in self.dict.keys():
            return self.dict[socket]

    def getSockets(self):
        return self.dict.keys()
            

    def apply(self,commande): ## parse la commande et applique la commande
        # commande contient un tuple (socket , commande)
        ## les commandes:
            ## entete*nbParametres*parametres... separateur: *
            ## POSITION*3*nom*latitude(float)*longitude(float) -> rafraichir la position du tracker sur le serveur
            ## STARTSUIVI*1*nom -> associer le socket à 'nom'
            ## STOPSUIVI*1*nom -> supprimer le socket associé à 'nom'
        socket = commande[0]
        requete = commande[1]
        requeteArray = requete.split("*")
        entete = requeteArray[0]
        nbParams = requeteArray[1]
        if (entete == "STARTSUIVI"):
            name = requeteArray[2]
            print("Demarrage du suivi de ",name)
            tracker = self.getTracker(socket)
            tracker.etat = 1
            tracker.name = name

        elif (entete == "STOPSUIVI"):
            name = requeteArray[2]
            tracker = self.getTracker(socket)
            if (tracker != None):
                print("Arret du suivi de ",name)
                tracker.etat = 0
                tracker.name = None

        elif (entete == "POSITION"):
            name = requeteArray[2]
            latitude = float(requeteArray[3])
            longitude = float(requeteArray[4])
            tracker = self.getTracker(socket)
            if (tracker == None):
                print("Erreur, tracker inconnu")
                return

            print("nouvelle position connue pour",name," : (",latitude,",",longitude,")")
            tracker.position = (latitude,longitude)
            self.dict[socket] = tracker
            

class Pool(Thread):
    def __init__(self,mapper):
        Thread.__init__(self)
        self.mapper = mapper

    def run(self):
        print("Poll ready [ok]")
        commande = None
        while True:
            
            with mlock:
                if (not poolRequest.empty()):
                    commande = poolRequest.get() # (socket , requete)

            if commande != None:
                self.mapper.apply(commande)

            commande = None
        
        
    
        

class SocketServer:
    def __init__(self,port,sizeBuffer,maxClientSocket):
        self.PORT = port
        self.RECV_BUFFER = sizeBuffer
        self.maxClientSocket = maxClientSocket
        self.CONNECTION_LIST = [] # list of socket clients

    def start(self):
        server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server_socket.bind(('', self.PORT))
        server_socket.listen(self.maxClientSocket)
        # Add server socket to the list of readable connections
        self.CONNECTION_LIST.append(server_socket)
        mapper = Mapper()
        pool = Pool(mapper)
        pool.start()
        print("Chat server started on port " + str(self.PORT) + " [ok]")
        
        while 1:
        # Get the list sockets which are ready to be read through select
            liste = list(mapper.getSockets()) # les sockets
            liste.extend(self.CONNECTION_LIST)
            read_sockets,write_sockets,error_sockets = select.select(liste,[],[])
     
            for sock in read_sockets:
                 
                #New connection
                if sock == server_socket:
                    # Handle the case in which there is a new connection recieved through server_socket
                    sockfd, addr = server_socket.accept()
                    mapper.addTracker(sockfd)
                    sockfd.send("CONNECTED\r\n".encode('utf-8'))
                    print("Client (%s, %s) connected" % addr)
                     
                #Some incoming message from a client
                else:
                    # Data recieved from client, process it
                    try:
                        #In Windows, sometimes when a TCP program closes abruptly,
                        # a "Connection reset by peer" exception will be thrown
                        data = sock.recv(self.RECV_BUFFER)

                        if len(data) > 0:
                            with mlock:
                                poolRequest.put((sock,data.decode('utf-8')))
                                
                            sock.send("CHECK\r\n".encode('utf-8'))

                        else:
                            print("Client (%s, %s) is offline" % addr)
                            sock.close()
                            mapper.delTracker(sock)

         
                     
                    # client disconnected, so remove from socket list
                    except:
                        #broadcast_data(sock, "Client (%s, %s) is offline" % addr)
                        print("Client (%s, %s) is offline" % addr)
                        sock.close()
                        mapper.delTracker(sock)
                        continue

             
        server_socket.close()

if __name__ == "__main__":
    sock = SocketServer(3000,4096,200)
    sock.start()

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
