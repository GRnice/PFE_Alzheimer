hote = "127.0.0.1"
port = 3000

from ServerPy import PatientServer

import unittest
import socket
import time

class TestProtocoleServeurPatient(unittest.TestCase):

    def testStartSuivi(self):
        cl = PatientServer(3000,4096,100)
        cl.start()
        
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.connect((hote, port))
        time.sleep(1)        
        sock.send("STARTSUIVI*123456789\r\n".encode("utf-8"))
        messageServeur = sock.recv(4096)
        messageServeur = messageServeur.rstrip()
        self.assertEqual(messageServeur.decode("utf-8"),"OKPROMENADE")
        sock.close()
        cl.stopServer()
        cl.join()

        
if __name__ == '__main__':
    unittest.main()
