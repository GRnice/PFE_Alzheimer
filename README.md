# PFE_Alzheimer
Repository du PFE Suivi GPS pour patient Alzheimer

Serveur:
- Installer la librairie matplotlib via la commande : pip3 install matplotlib
- Lancer la serveur avec la commande : python3 ServerPy.py


Android :
- Télécharger les applications et changer l'adresse IP dans la classe CommunicationServer.java
- Installer les deux applications Android
- Lancer les deux applications 


Les fichiers:
- coordoneneCentre.txt : 
Contient les coordonnées du polynome du centre

- profils.txt : 
Contient la liste de tous les profils des patients

- cercleBarriere.txt : 
Contient les coordonnées des cercles sous le format suivant : 
longitude du centre du cercle,latitude du centre du cercle,longitude du rayon,latitude du rayon

Ces cercles représentent des zones d'alertes de hors zone, la barrière du centre par example.