from threading import RLock

lockLookup = RLock()

class LookupAssistantPatient:
    def __init__(self):
        self.dict = dict() ## HashMap<socketAssistant,[socketPatient]>

    def keys(self):
        return list(self.dict.keys())

    def attach(self,socketPatient,socketAssistant):
        #print(socketPatient,"attach with",socketAssistant)
        with lockLookup:
            if socketAssistant in self.dict.keys():
                listeSocketPatient = self.dict[socketAssistant]
                listeSocketPatient.append(socketPatient)

            else:
                self.dict[socketAssistant] = [socketPatient]

    def detach(self,socketPatient,socketAssistant):
        with lockLookup:
            if socketAssistant in self.dict.keys():
                listeSpatient = self.dict[socketAssistant]
                if socketPatient in listeSpatient:
                    index = listeSpatient.index(socketPatient)
                    listeSpatient.pop(index)
                    self.dict[socketAssistant] = listeSpatient

    def addAssistant(self,assistantSock):
        with lockLookup:
            if assistantSock not in self.dict.keys():
                self.dict[assistantSock] = []

                
    def removeAssistant(self,sockAssistant):
        with lockLookup:
            if sockAssistant in self.dict.keys():
                del self.dict[sockAssistant]

    def removePatient(self,sockPatient):
        with lockLookup:
            allAssistants = list(self.dict.keys())
            for assistantSock in allAssistants:
                listeSock = self.dict[assistantSock]
                if (sockPatient in listeSock):
                    index = listeSock.index(sockPatient)
                    listeSock.pop(index)
                    self.dict[assistantSock] = listeSock
        
  
