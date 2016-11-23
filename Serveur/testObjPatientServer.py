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
        self.assertTrue(tracker != None)
        self.assertTrue(tracker.id == "123456789")
        self.assertTrue(tracker.etat == 1)
        sockClient.send("POSITION*45.22*78.64".encode("utf-8"))

        time.sleep(1)
        mapper = cl.getMapper()
        sockserv = list(mapper.dict.keys())[0]
        tracker = mapper.dict[sockserv]
        self.assertTrue(tracker != None)
        self.assertTrue(tracker.id == "123456789")
        self.assertTrue(tracker.etat == 1)
        self.assertTrue(tracker.position[0] == float("45.22"))
        self.assertTrue(tracker.position[1] == float("78.64"))
        
        cl.stopServer()
        sockClient.close()
        cl.join()

if __name__ == '__main__':
    unittest.main()
