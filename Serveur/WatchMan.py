import math
from matplotlib import path

class Circle: # HITBOX
    def __init__(self,longOrigine,latOrigine,longExtremite,latExtremite):
        self.longOrigine = longOrigine
        self.latOrigine = latOrigine
        self.rayon = math.sqrt( ((longExtremite - longOrigine)**2) + ((latExtremite - latOrigine)**2) )
        print("latLong center :", str(self.latOrigine) + " " + str(self.longOrigine) + " and raduis = " + str( (self.rayon / 0.01) * 900.6124 ))  
        
    def isInside(self,longP,latP):
        return math.sqrt(((longP - self.longOrigine)**2) + ((latP - self.latOrigine)**2)) < self.rayon

class WatchMan:
    def __init__(self):
        #load all circles of death, si un patient entre dans l'un de ces
        # cercles ont déclenche une alerte.
        self.limitCentre = []
        self.listCircles = [] ## liste de cercle ou le premier c'est la barriere -- le portail
        self.loadCircles()
        self.loadCentreLimits()

    def loadCentreLimits(self):
        with open("coordonneeCentre.txt","r",encoding="utf-8") as file:
            lines = file.readlines()
            for line in lines:
                if (line[0] == "#"):
                    continue
                
                line = line.rstrip().split(",")
                self.limitCentre.append((float(line[0]), float(line[1])))

    def loadCircles(self): # load all HITBOXS
        with open("cercleBarriere.txt","r",encoding="utf-8") as fileBarriere:
            lines = fileBarriere.readlines()
            for line in lines:
                if (line[0] == "#"):
                    continue
                line = line.rstrip().split(",")
                print("---- Bariere")
                circle = Circle(float(line[0]),float(line[1]),float(line[2]),float(line[3]))
                self.listCircles.append(circle)
            
            
    def positionIsGood(self,tracker,longP,latP):
        self.limitCentre = []
        self.circles = []
        self.loadCircles()
        self.loadCentreLimits()
        
        p = path.Path(self.limitCentre)
        if(not p.contains_points([(latP, longP)])):
            return False
        if tracker.risqueFranchissementBarriere and self.listCircles[0].isInside(longP,latP):
            return False
        if self.listCircles[1].isInside(longP, latP):
            return False

        return True
