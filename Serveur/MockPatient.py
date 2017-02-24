import socket
import time

sock = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
sock.connect(("127.0.0.1",3000))
idtel = input("entrez IDTEL:")
sock.send(("STARTSUIVI*"+str(idtel)+"\r\n").encode())
#19
sock.recv(4096) # attente du OKPROMENADE
input("envoyer premiere position")
sock.send("POSITION*7.079131*43.612139*100\r\n".encode())  ## 43.612139, 7.079131
input("envoyer deuxieme position")
sock.send("POSITION*7.079684*43.612120*100\r\n".encode())  ## 43.612120, 7.079684
messageOuiOuNon = input("Activer alerte sortie de zone ? y/n")
if messageOuiOuNon == "y":
    sock.send("POSITION*7.079676*43.612066*100\r\n".encode())    ##43.612489, 7.079131
    time.sleep(0.5)

input("envoyer Avant derniere position")
sock.send("POSITION*7.079791*43.612153*100\r\n".encode())  ## 43.612153, 7.079791


messageOuiOuNon = input("Activer alerte sortie de zone ? y/n")
if messageOuiOuNon == "y":
    sock.send("POSITION*7.079754*43.612062*100\r\n".encode())    ##43.611987, 7.079584
    time.sleep(0.5)

messageOuiNon = input("Activer alerte sortie de zone BARRIERE ? y/n")
if messageOuiNon == "y":
    sock.send("POSITION*7.071950111*43.615400*50\r\n".encode())
    time.sleep(0.5)

input("envoyer derniere position")
sock.send("POSITION*7.079708*43.612136*100\r\n".encode())  ## 43.612136, 7.079708


input("envoyer stop suivi et fermer le socket")
sock.send("STOPSUIVI\r\n".encode())
sock.recv(4096) # attente du STOPSUIVI
sock.close()


## hors zone bariere, vert:
#43.612542, 7.079719 - 43.612530, 7.079732 -  43.612510, 7.079756 - 43.612498, 7.079793 - 43.612540, 7.079769


## hors zone gauche, magenta:
# 43.612491, 7.079075 -  43.612536, 7.079129 -

## hors zone south, jaune:
##43.612111, 7.079108 - 43.612076, 7.079142 -

## hors zone droite, red:
# 43.612066, 7.079676 - 43.612062, 7.079754





#Reste
#7.079869,43.611771,7.079730,43.612091
