from threading import Thread
import time
class Pinger(Thread):
    def __init__(self,mapper,serverAssistant):
        Thread.__init__(self)
        self.num = 0
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
            
            socketPatient = allPatients[self.num % len(allPatients)]
            tracker = self.mapper.getTracker(socketPatient)
            self.num = self.num + 1
            if (tracker.lastEmit != None):
                tempsCourant = time.time()
                if ((tempsCourant - tracker.lastEmit) > 10 and (not tracker.updateTimeout)): # si > 10s
                    tracker.updateTimeout = True
                    self.serverAssistant.event("ALERT-TIMEOUT-UPDATE_START",None,tracker)

                elif ((tempsCourant - tracker.lastEmit) < 10 and tracker.updateTimeout):
                    tracker.updateTime = False
                    self.serverAssistant.event("ALERT-TIMEOUT-UPDATE_STOP",None,tracker)
                    
                    
                
            
