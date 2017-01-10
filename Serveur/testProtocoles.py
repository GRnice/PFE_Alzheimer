hote = "127.0.0.1"
port = 3000
portAssistance = 3100

import unittest
import socket
import time
from ServerPy import *



class TestProtocoleServeurPatient(unittest.TestCase):
    
    def setUp(self):
        self.arrayOfSocket = []
        self.serveAssistant = AssistanceServer(3100,4096,100) # port 3100
        self.mapper = Mapper(self.serveAssistant)
        self.servePatient = PatientServer(3000,4096,200,self.mapper) # sur le port 3000
        self.servePatient.start()
        self.serveAssistant.start()

    def tearDown(self):
        for sock in self.arrayOfSocket:
            sock.close()
            
        self.servePatient.stopServer()
        self.serveAssistant.stopServer()
        self.servePatient.join()
        self.serveAssistant.join()

##    
##    def testStartSuivi(self):
##        # STARTSUIVI,NEWSESSION,FOLLOW,OKPROMENADE,STOPSUIVI
##        # scenario 1:   - un assistant se connecte
##        #               - un client se connecte
##        #               - le client demande a etre suivi
##        #               - le client est suivi par l'assistant
##        #               - le client stop la promenade
##        #               - Tout le monde se deconnecte
##        
##        print("TEST_START_SUIVI")
##        IDTEL = "c98D20A"
##        time.sleep(1)
##        sockAssistant = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
##        sockAssistant.connect((hote,portAssistance))
##        time.sleep(1)
##        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
##        sock.connect((hote, port))
##        time.sleep(1)
##
##        self.arrayOfSocket.append(sock)
##        self.arrayOfSocket.append(sockAssistant)
##        
##        allprofiles = sockAssistant.recv(4096).decode('utf-8').rstrip()
##        sock.send(("STARTSUIVI*"+IDTEL+"\r\n").encode("utf-8"))
##        messageNewSession = sockAssistant.recv(4096).decode('utf-8').rstrip()
##        self.assertEqual(messageNewSession,"NEWSESSION$"+IDTEL)
##        sockAssistant.send(("FOLLOW$"+IDTEL+"*prenom*nom\r\n").encode('utf-8'))
##        messageOkPromenade = sock.recv(4096).decode('utf-8').rstrip()
##        self.assertEqual(messageOkPromenade,"OKPROMENADE")
##
##        sock.send("STOPSUIVI\r\n".encode('utf-8'))
##
##        messageSynchStopPromenade = sockAssistant.recv(4096).decode('utf-8').rstrip()
##        self.assertEqual(messageSynchStopPromenade,"SYNCH$STOPPROMENADE_"+IDTEL)
##        
##        messageStopSuivi = sock.recv(4096).decode('utf-8').rstrip()
##        self.assertEqual(messageStopSuivi,"STOPSUIVI")
##        sock.close()
##        sockAssistant.close()


    def testScenario2(self):
        # STARTSUIVI,NEWSESSION,FOLLOW,OKPROMENADE,STOPSUIVI
        # scenario 2:   - un assistant se connecte
        #               - un client se connecte
                       #- un deuxieme assistant se connecte
        #               - le client demande a etre suivi
        #               - le client est suivi par le premier assistant
        #               - le client stop la promenade
        #               - Tout le monde se deconnecte
        print("SCENARIO 2")

        IDTEL = "c98D20A"
        time.sleep(1)
        sockAssistant = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
        sockAssistant.connect((hote,portAssistance))
        time.sleep(1)
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.connect((hote, port))
        time.sleep(1)
        sockAssistant2 = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
        sockAssistant2.connect((hote,portAssistance))
        time.sleep(1)

        self.arrayOfSocket.append(sock)
        self.arrayOfSocket.append(sockAssistant)
        self.arrayOfSocket.append(sockAssistant2)
        
        
        allprofiles = sockAssistant.recv(4096).decode('utf-8').rstrip()
        allprofiles = sockAssistant2.recv(4096).decode('utf-8').rstrip()
        sock.send(("STARTSUIVI*"+IDTEL+"\r\n").encode("utf-8"))

        # reception du NEWSESSION sur les deux assistants
        messageNewSession = sockAssistant.recv(4096).decode('utf-8').rstrip()
        messageNewSession2 = sockAssistant2.recv(4096).decode('utf-8').rstrip()

        self.assertEqual(messageNewSession,"NEWSESSION$"+IDTEL)
        self.assertEqual(messageNewSession2,"NEWSESSION$"+IDTEL)

        sockAssistant.send(("FOLLOW$"+IDTEL+"*prenom*nom\r\n").encode('utf-8'))
        messageOkPromenade = sock.recv(4096).decode('utf-8').rstrip()
        self.assertEqual(messageOkPromenade,"OKPROMENADE")
        
        messageSynchPromenade = sockAssistant2.recv(4096).decode('utf-8').rstrip()
        self.assertEqual(messageSynchPromenade,"SYNCH$NWPROMENADE_"+IDTEL+"*nom*prenom") # assistant2 est notifié que sock se promene

        # transmission de la position de sock
        sock.send("POSITION*45.66*78.55\r\n".encode('utf-8'))

        messageUpdate = sockAssistant.recv(4096).decode('utf-8').rstrip()
        messageUpdate2 = sockAssistant2.recv(4096).decode('utf-8').rstrip()

        self.assertEqual(messageUpdate,"UPDATE$"+IDTEL+"*45.66*78.55")
        self.assertEqual(messageUpdate2,"UPDATE$"+IDTEL+"*45.66*78.55")

        # transmission du stop suivi
        sock.send("STOPSUIVI\r\n".encode('utf-8'))

        messageSynchStopPromenade = sockAssistant.recv(4096).decode('utf-8').rstrip()
        messageSynchStopPromenade2 = sockAssistant2.recv(4096).decode('utf-8').rstrip()

        self.assertEqual(messageSynchStopPromenade,"SYNCH$STOPPROMENADE_"+IDTEL)
        self.assertEqual(messageSynchStopPromenade2,"SYNCH$STOPPROMENADE_"+IDTEL)
        
        messageStopSuivi = sock.recv(4096).decode('utf-8').rstrip()
        self.assertEqual(messageStopSuivi,"STOPSUIVI")
        sock.close()
        sockAssistant.close()
        sockAssistant2.close()
        print("END SCENARIO 2")
        

##    def testDeuxDispositifs(self):
##        print("TEST_DEUX_DISPOSITIFS")
##        time.sleep(1)
##        sock1 = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
##        sock1.connect((hote, port))
##
##        sock2 = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
##        sock2.connect((hote, port))
##        time.sleep(1)        
##        sock1.send("STARTSUIVI*123456789\r\n".encode("utf-8"))       
##        sock2.send("STARTSUIVI*789555622\r\n".encode("utf-8"))
##        
##        messageServeur1 = (sock1.recv(4096)).rstrip()
##        messageServeur2 = (sock2.recv(4096)).rstrip()
##        self.assertEqual(messageServeur1.decode("utf-8"),"OKPROMENADE")
##        self.assertEqual(messageServeur2.decode("utf-8"),"OKPROMENADE")
##        sock1.close()
##        sock2.close()
##        

##    def testContinue(self):
##        print("TEST_START_CONTINUE")
##        time.sleep(1)
##        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
##        sock.connect((hote, port))
##        time.sleep(1)
##        sock.send("STARTSUIVI*123456789\r\n".encode("utf-8"))
##        messageServeur = sock.recv(4096)
##        messageServeur = messageServeur.rstrip()
##        self.assertEqual(messageServeur.decode("utf-8"),"OKPROMENADE")
##        
##        newSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
##        newSocket.connect((hote, port))
##        newSocket.send("CONTINUE*123456789\r\n".encode("utf-8"))
##        time.sleep(1)
##        messageServeur = newSocket.recv(4096)
##        messageServeur = messageServeur.rstrip()
##        self.assertEqual(messageServeur.decode("utf-8"),"OKPROMENADE")
##        sock.close()
##        newSocket.close()
        
    
        
if __name__ == '__main__':
    unittest.main()
