import math

class Circle: # HITBOX
    def __init__(self,longOrigine,latOrigine,longExtremite,latExtremite):
        self.longOrigine = longOrigine
        self.latOrigine = latOrigine
        self.rayon = math.sqrt( ((longExtremite - longOrigine)**2) + ((latExtremite - latOrigine)**2) )
        
    def isInside(self,longP,latP):
        return math.sqrt(((longP - self.longOrigine)**2) + ((latP - self.latOrigine)**2)) < self.rayon

class WatchMan:
    def __init__(self):
        #load all circles of death, si un patient entre dans l'un de ces
        # cercles ont déclenche une alerte.
        self.listOfCircles = []
        self.loadCircles()
        pass

    def loadCircles(self): # load all HITBOXS
        with open("listCercles.txt","r",encoding="utf-8") as file:
            line = file.readline()
            line = line.rstrip().split(",")
            circle = Circle(line[0],line[1],line[2],line[3])
            self.listOfCircles.append(circle)
            
            
    def positionIsGood(self,longP,latP):
        for circle in self.listOfCircles:
            if circle.isInside(longP,latP):
                return False

        return True
