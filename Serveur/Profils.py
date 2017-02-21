

class Profile:
    def __init__(self, nom, prenom,idAvatar,risqueFranchissementBarriere):
        self.firstName = prenom
        self.lastName = nom
        self.risqueFranchissementBarriere = risqueFranchissementBarriere
        self.idAvatar = idAvatar
        
class ManagerProfile:
    def __init__(self,nomFichier):
        self.nomFichier = nomFichier
        self.listProfile = []

    def __str__(self):
        chaine = ""
        for elt in self.listProfile:
            chaine += elt.lastName+","+elt.firstName+","+elt.idAvatar+","+elt.risqueFranchissementBarriere+"*"

        return chaine[0:-1]

    def read(self):
        print("load profils")
        with open(self.nomFichier, 'r') as f:
            for line in f:
                myLine = line.rstrip()
                listLine = myLine.split(",")
                print(listLine)
                p = Profile(listLine[0], listLine[1],listLine[2],listLine[3])
                self.listProfile.append(p)
        f.close()

        for elt in self.listProfile:
            print(elt.firstName + " " + elt.lastName)
        
    def addProfil(self,nom,prenom,idAvatar,barriere):
        unProfil = Profile(nom,prenom,idAvatar,barriere)
        self.listProfile.append(unProfil)
        self.write()

    def supprProfil(self,nom,prenom):
        print("dans supprprofil")
        for prof in self.listProfile:
            if (prof.firstName == prenom and prof.lastName == nom):
                print('suppression')
                index = self.listProfile.index(prof)
                self.listProfile.pop(index)
                self.write()
                return

    def modifProfil(self,oldSignature,newSignature):
        oldProf = oldSignature.split(",")
        newProf = newSignature.split(",")
        for prof in self.listProfile:
            if (prof.firstName == oldProf[1] and prof.lastName == oldProf[0]):
                prof.lastName = newProf[0]
                prof.firstName = newProf[1]
                prof.idAvatar = newProf[2]
                prof.risqueFranchissementBarriere = newProf[3]
                self.write()

    def write(self):
        chaine = ""
        for elt in self.listProfile:
            chaine += elt.lastName+","+elt.firstName+","+elt.idAvatar+","+elt.risqueFranchissementBarriere+"\n"

        
        with open(self.nomFichier, 'w') as out:
            out.write(chaine)
        
