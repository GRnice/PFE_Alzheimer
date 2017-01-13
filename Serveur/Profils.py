class Profile:
    def __init__(self, nom, prenom,risqueFranchissementBarriere):
        self.firstName = prenom
        self.lastName = nom
        self.risqueFranchissementBarriere = risqueFranchissementBarriere
        
class ManagerProfile:
    def __init__(self,nomFichier):
        self.nomFichier = nomFichier
        self.listProfile = []

    def __str__(self):
        chaine = ""
        for elt in self.listProfile:
            chaine += elt.lastName+","+elt.firstName+","+elt.risqueFranchissementBarriere+"*"

        return chaine[0:-1]

    def read(self):
        with open(self.nomFichier, 'r') as f:
            for line in f:
                myLine = line.rstrip()
                listLine = myLine.split(",")
                p = Profile(listLine[0], listLine[1],listLine[2])
                self.listProfile.append(p)
        f.close()

        for elt in self.listProfile:
            print(elt.firstName + " " + elt.lastName)
        
    def addProfil(self,nom,prenom,barriere):
        unProfil = Profile(nom,prenom,barriere)
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
                prof.firstName = newProf[1]; prof.lastName = newProf[0]; prof.risqueFranchissementBarriere = newProf[2]
                self.write()

    def write(self):
        chaine = ""
        for elt in self.listProfile:
            chaine += elt.lastName+","+elt.firstName+","+elt.risqueFranchissementBarriere+"\n"

        
        with open(self.nomFichier, 'w') as out:
            out.write(chaine)
        out.close()
        
