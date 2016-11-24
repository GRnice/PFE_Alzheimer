hote = "127.0.0.1"
port = 3000

from ServerPy import PatientServer
import unittest
import socket

from threading import RLock
import time

class TestProtocoleServeurPatient(unittest.TestCase):

    # A tester en local, il ne doit pas y avoir de latence.
    def testStartSuiviAndPositionTracker(self):
        cl = PatientServer(3000,4096,100)
        cl.start()
        
        sockClient = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sockClient.connect((hote, port))
        
        time.sleep(1)
        mapper = cl.getMapper()
        sockserv = list(mapper.dict.keys())[0]
        tracker = mapper.dict[sockserv]
        self.assertTrue(tracker != None)
        self.assertTrue(tracker.etat == 0)
        sockClient.send("STARTSUIVI*123456789".encode("utf-8"))
        
        time.sleep(1)
        mapper = cl.getMapper()
        sockserv = list(mapper.dict.keys())[0]
        tracker = mapper.dict[sockserv]
        self.assertTrue(len(list(mapper.dict.keys())) == 1)
        self.assertTrue(tracker != None)
        self.assertEqual(tracker.id , "123456789")
        self.assertTrue(tracker.etat == 1)
        sockClient.send("POSITION*45.22*78.64".encode("utf-8"))

        time.sleep(1)
        mapper = cl.getMapper()
        sockserv1 = list(mapper.dict.keys())[0]
        tracker = mapper.dict[sockserv1]
        self.assertTrue(tracker != None)
        self.assertTrue(tracker.id == "123456789")
        self.assertTrue(tracker.etat == 1)
        self.assertTrue(tracker.position[0] == float("45.22"))
        self.assertTrue(tracker.position[1] == float("78.64"))
        sockNewClient = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sockNewClient.connect((hote, port))
        sockNewClient.send("CONTINUE*123456789".encode("utf-8"))

        time.sleep(1)
        mapper = cl.getMapper()
        sockserv2 = list(mapper.dict.keys())[0]
        tracker = mapper.dict[sockserv2]
        ## s'assurer qu'il y a un seul socket ! et pas deux...
        self.assertTrue(len(list(mapper.dict.keys())) == 1)
        ## le nouveau socket doit etre dans le dictionnaire
        self.assertTrue(sockserv2 != sockserv1)
        ## le tracker attaché au nouveau socket ne doit pas etre nul
        self.assertTrue(tracker != None)
        ## l'id doit etre le meme
        self.assertTrue(tracker.id == "123456789")
        ## et l'etat à 1
        self.assertTrue(tracker.etat == 1)
        
        sockClient.close()
        sockNewClient.close()
        cl.stopServer()
        cl.join()

        

if __name__ == '__main__':
    unittest.main()
