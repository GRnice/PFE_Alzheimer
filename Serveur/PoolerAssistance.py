import queue

from threading import RLock

lockEventPoolAssistance = RLock()

class PoolerAssistance:
    def __init__(self):
        self.dict = dict()
        # HashMap<sid,queue(event,event)>

    def addEvent(sid,event):
        with lockEventPoolAssistance:
            queue = self.dict[sid]
            self.dict[sid] = queue.put(event)

    def broadcast(self,event):
        with lockEventPoolAssistance:
            keys = self.dict.keys()
            for cle in keys:
                queue = self.dict[cle]
                queue.put(event)
                self.dict[cle] = queue

    def nextEventFor(self,sid):
        with lockEventPoolAssistance:
            queue = self.dict[sid]
            if not queue.empty():
                return queue.get()

            return None

    def addAssistant(self,sid):
        with lockEventPoolAssistance:
            self.dict[sid] = queue.Queue(50)

    def removeAssistant(self,sid):
        with lockEventPoolAssistance:
            del self.dict[sid]
