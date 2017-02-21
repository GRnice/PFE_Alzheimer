from threading import Thread
import time
class Pinger(Thread):
    def __init__(self,mapper,serverAssistant):
        Thread.__init__(self)
        self.DELAYMAX = 20
        self.DELAYMAXALERT = 20 # Délai d'inaction d'alertes
        self.mapper = mapper
        self.serverAssistant = serverAssistant
        self.onRun = None

    def stop(self):
        self.onRun = False

    def run(self):
        self.onRun = True
        while self.onRun:
            time.sleep(4)
            
            allPatients = list( self.mapper.getSocketPatient())
            if (len(allPatients) == 0):
                continue
            
            for i in range(len(allPatients)):
                tracker = self.mapper.getTracker(allPatients[i])
                ## check le temps d'emission de la derniere alerte...
                if (tracker.lastEmit != None):
                    tempsCourant = time.time()
                    if ((tempsCourant - tracker.lastEmit) > self.DELAYMAX and (not tracker.updateTimeout)): # si > 20s
                        print("DETECT UPDATE TIMEOUT")
                        tracker.updateTimeout = True
                        self.serverAssistant.event("ALERT-TIMEOUT-UPDATE_START",None,tracker)

                    elif ((tempsCourant - tracker.lastEmit) < self.DELAYMAX and tracker.updateTimeout):
                        print("RELEASE UPDATE TIMEOUT")
                        tracker.updateTimeout = False
                        self.serverAssistant.event("ALERT-TIMEOUT-UPDATE_STOP",None,tracker)
                if (tracker.lastAlert != None):
                    tempsCourant = time.time()
                    if ((tempsCourant - tracker.lastAlert[0]) > self.DELAYMAXALERT and (not tracker.alertTimeout)): # si > 20s
                        print("DETECT ALERTES PAS GEREES")
                        tracker.alertTimeout = True
                        for id, val in enumerate(tracker.lastAlert):
                            if(id != 0):
                                self.serverAssistant.broadcast(val)
                        
                    elif ((tempsCourant - tracker.lastAlert[0]) < self.DELAYMAXALERT and tracker.alertTimeout):
                        print("RELEASE UPDATE TIMEOUT")
                        tracker.alertTimeout = False
                    
                
            
