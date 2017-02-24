import socket
import time

sock = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
sock.connect(("10.212.102.221",3000))
idtel = input("entrez IDTEL:")
sock.send(("STARTSUIVI*"+str(idtel)+"\r\n").encode())
#19
sock.recv(4096) # attente du OKPROMENADE
input("envoyer premiere position")
sock.send("POSITION*7.079131*43.612139*100\r\n".encode())  ## 43.612139, 7.079131
input("envoyer deuxieme position")
sock.send("POSITION*7.079275*43.612615*100\r\n".encode())  ## 43.612615, 7.079275
messageOuiOuNon = input("Activer alerte sortie de zone ? y/n")
if messageOuiOuNon == "y":
    sock.send("POSITION*7.079240*43.612685*100\r\n".encode())    ## 43.612685, 7.079240
    time.sleep(0.5)

input("envoyer Avant derniere position")
sock.send("POSITION*7.079324*43.612672*100\r\n".encode())  ## 43.612672, 7.079324


messageOuiOuNon = input("Activer alerte sortie de zone ? y/n")
if messageOuiOuNon == "y":
    sock.send("POSITION*7.079157*43.612685*100\r\n".encode())    ## 43.612256, 7.080074
    time.sleep(0.5)

input("envoyer derniere position")
sock.send("POSITION*7.079235*43.612590*100\r\n".encode())  ## 43.612590, 7.079235


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
# 43.612103, 7.079829 - 43.612062, 7.079754


## hors zone south droite gray:
#43.611931, 7.079408 - 43.611931, 7.079366

## hors zone gauche bas, black:
## 43.612256, 7.078941 - 43.612281, 7.078919 - 43.612244, 7.078957

## hors zone haut gauche Cyan:
#43.612761, 7.079305 - 43.612758, 7.079347 - 43.612741, 7.079342

## hors zone haut droite Bleu:
##43.612264, 7.080148 - 43.612251, 7.080125

## hors zone north, un peu a gauche: - WHITE
## 43.612690, 7.079596 - 43.612714, 7.079563

## hors zone gauche en haut, apres le magenta, RED;
#43.612685, 7.079240 -  43.612646, 7.079157
