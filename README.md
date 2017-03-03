# PFE_Alzheimer
## Repository du PFE Suivi GPS pour patient Alzheimer


**Serveur:**
- Fonctionne sous Python 3.\*
- Installer pip
- Installer la librairie matplotlib via la commande : pip3 install matplotlib
- Lancer la serveur avec la commande : python3 ServerPy.py


**Android :**
- Télécharger les applications et changer l'adresse IP du serveur dans la classe CommunicationServer.java des deux projets Android
- Puis Run pour les deux applications Android


**Les fichiers txt du dossier Serveur:**
- *coordonneeCentre.txt* : 
Contient la position des sommets du polygone du centre, ce polygone couvre la zone de promenade autorisée (à améliorer ce n'est pas totalement fiable !)

- *profils.txt* : 
Contient la liste de tous les profils des patients

- *cercleBarriere.txt* : 
Contient les coordonnées des cercles sous le format suivant : 
longitude du centre du cercle,latitude du centre du cercle,longitude du rayon,latitude du rayon

Ces cercles représentent des zones d'alertes de hors zone, la barrière du centre par example.
