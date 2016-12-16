class Profile:
    def __init__(self, nom, prenom,risqueFranchissementBarriere):
        self.firstName = prenom
        self.lastName = nom
        self.risqueFranchissementBarriere = risqueFranchissementBarriere
        
class ManagerProfile:
    def __init__(self):
        self.listProfile = []

    def __str__(self):
        chaine = ""
        for elt in self.listProfile:
            chaine += elt.lastName+","+elt.firstName+","+elt.risqueFranchissementBarriere+"*"

        return chaine[0:-1]

    def read(self, nomFichier):
        with open(nomFichier, 'r') as f:
            for line in f:
                myLine = line.strip()
                listLine = myLine.split(",")
                p = Profile(listLine[0], listLine[1],listLine[2])
                self.listProfile.append(p)
        f.close()

        for elt in self.listProfile:
            print(elt.firstName + " " + elt.lastName)
        

    def write(self, nomFichier, profile):
        with open(nomFichier, 'a') as out:
            out.write(profile.firstName + "," + 
                      profile.lastName + "," +
                      profile.risqueFranchissementBarriere + "\n")
        out.close()
        
