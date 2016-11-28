hote = "127.0.0.1"
port = 3000

import unittest
import socket
import time
from ServerPy import PatientServer



class TestProtocoleServeurPatient(unittest.TestCase):
    
    def setUp(self):
        self.serveur = PatientServer(3000,4096,100)
        self.serveur.start()

    def tearDown(self):
        self.serveur.stopServer()
        self.serveur.join()
##        
##    def testStartSuivi(self):
##        print("TEST_START_SUIVI")
##        time.sleep(1)
##        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
##        sock.connect((hote, port))
##        time.sleep(1)        
##        sock.send("STARTSUIVI*123456789\r\n".encode("utf-8"))
##        messageServeur = sock.recv(4096)
##        messageServeur = messageServeur.rstrip()
##        self.assertEqual(messageServeur.decode("utf-8"),"OKPROMENADE")
##        sock.close()

    def testDeuxDispositifs(self):
        print("TEST_DEUX_DISPOSITIFS")
        time.sleep(1)
        sock1 = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock1.connect((hote, port))

        sock2 = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock2.connect((hote, port))
        time.sleep(1)        
        sock1.send("STARTSUIVI*123456789\r\n".encode("utf-8"))       
        sock2.send("STARTSUIVI*789555622\r\n".encode("utf-8"))
        
        messageServeur1 = (sock1.recv(4096)).rstrip()
        messageServeur2 = (sock2.recv(4096)).rstrip()
        self.assertEqual(messageServeur1.decode("utf-8"),"OKPROMENADE")
        self.assertEqual(messageServeur2.decode("utf-8"),"OKPROMENADE")
        sock1.close()
        sock2.close()
        

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
