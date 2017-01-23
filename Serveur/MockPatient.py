import socket

sock = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
sock.connect(("172.20.10.4",3000))
sock.send("STARTSUIVI*123444\r\n".encode())
#19
sock.recv(4096) # attente du OKPROMENADE
input("envoyer premiere position")
sock.send("POSITION*7.419617*43.735383\r\n".encode())
input("envoyer deuxieme position")
sock.send("POSITION*7.08*43.7\r\n".encode())
messageOuiOuNon = input("Activer alerte sortie de zone ? y/n")
if messageOuiOuNon == "y":
    sock.send("POSITION*7.071489*43.614939\r\n".encode())
    
input("envoyer stop suivi et fermer le socket")
sock.send("STOPSUIVI\r\n".encode())
sock.recv(4096) # attente du STOPSUIVI
sock.close()
