import socketio
import eventlet
import eventlet.wsgi
import time
import queue
from flask import Flask, render_template
import json

from threading import Thread,RLock
import Profils
import LookupAssistantPatient
import PoolerAssistance

lockPool = RLock()
lockMap = RLock()



def startSockerIOassistantServeur(port,serverAssistant):
    sio = socketio.Server(logger=False)
    app = Flask(__name__)
    
    @sio.on('connect', namespace='/')
    def connect(sid, environ):
        print("connect ", sid)
        print(sid)
        serverAssistant.addAssistant(sid)
        print("SEND",sio.emit("PROFILES",serverAssistant.getProfilsToString(),room=sid))
        #sio.emit("NEWSESSION","12fa90c",room=sid)

    @sio.on('FOLLOW',namespace="/")
    def follow(sid,data):
        # message attendu "idTel*prenom*nom"
        print(sid)
        data = data.split("*")
        idTel = data[0]
        prenom = data[1]
        nom = data[2]
        sockPatient = serverAssistant.mapper.getSocketById(idTel)
        tracker = serverAssistant.mapper.getTracker(sockPatient)
        tracker.nom = nom
        tracker.prenom = prenom

        serverAssistant.lookup.attach(sockPatient,sid)
        sockPatient.send("OKPROMENADE\r\n".encode("utf-8"))


    @sio.on('STOPPROMENADE',namespace="/")
    def stopPromenade(sid,data):
        print("stop promenade",sid)
        idTel = data[0]
        sockPatient = serverAssistant.mapper.getSocketById(idTel)
        sockPatient.send("STOPSUIVI\r\n".encode("utf-8"))
        serverAssistant.lookup.removePatient(sockPatient)
        

    @sio.on("UP",namespace="/")
    def up(sid,data):
        print("up",sid)
        event = serverAssistant.poolerEvent.nextEventFor(sid)
        if event != None:
            header = event[0]
            body = event[1]
            print("send it ->",body)
            sio.emit(header,body,room=sid)
        
    @sio.on('chat', namespace='/')
    def message(sid, data):
        print("message ", data)
        sio.emit('message','COUCOU', room=sid)

    @sio.on('disconnect', namespace='/')
    def disconnect(sid):
        print('disconnect ', sid)
        print("remove du lookup")
        serverAssistant.removeAssistant(sid)
        

    app = socketio.Middleware(sio, app)
    eventlet.wsgi.server(eventlet.listen(('', port)), app) 
            

class ServerAssistant(Thread):
    def __init__(self,port):
        Thread.__init__(self)
        self.PORT = port
        self.RECV_BUFFER = 4096
        self.maxTabletSocket = 100
        self.lookup = LookupAssistantPatient.LookupAssistantPatient()
        self.poolerEvent = PoolerAssistance.PoolerAssistance()
        self.managerProfils = Profils.ManagerProfile()
        self.managerProfils.read("./profils.txt")
        self.socketIoThread = Thread(target=startSockerIOassistantServeur,args=(port,self))
        self.socketIoThread.start()

    def setMapper(self,mapper):
        self.mapper = mapper

    def addAssistant(self,sid):
        self.lookup.addAssistant(sid)
        self.poolerEvent.addAssistant(sid)

    def removeAssistant(self,sid):
        self.lookup.removeAssistant(sid)
        self.poolerEvent.removeAssistant(sid)

    def getProfilsToString(self):
        return str(self.managerProfils)

    def event(self,evt,socket,tracker):
        if (evt == "STARTSUIVI"):
            self.poolerEvent.broadcast(("NEWSESSION",tracker.id))

        elif (evt == "POSITION"):
            self.poolerEvent.broadcast(("UPDATE",
                                        str(tracker.id)+"*"+str(tracker.position[0])+"*"+str(tracker.position[1])))
                
    def stopServer(self):
        self.sio.disconnect()

    def run(self):
        ## while
        pass


