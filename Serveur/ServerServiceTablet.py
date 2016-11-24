import socketio
import eventlet
import eventlet.wsgi
from flask import Flask, render_template

from threading import Thread


def startSockerIOassistantServeur(port):
    sio = socketio.Server()
    app = Flask(__name__)

    @sio.on('connect', namespace='/')
    def connect(sid, environ):
        print("connect ", sid)

    @sio.on('chat message', namespace='/')
    def message(sid, data):
        print("message ", data)
        sio.emit('reply', room=sid)

    @sio.on('disconnect', namespace='/')
    def disconnect(sid):
        print('disconnect ', sid)

    app = socketio.Middleware(sio, app)
    eventlet.wsgi.server(eventlet.listen(('', port)), app)

    
class ServerAssistant(Thread):
    def __init__(self,port):
        Thread.__init__(self)
        self.PORT = port
        self.RECV_BUFFER = 4096
        self.maxTabletSocket = 100
        self.socketIoThread = Thread(target=startSockerIOassistantServeur,args=(port,))
        self.socketIoThread.start()

    def stopServer(self):
        self.sio.disconnect()

    def run(self):
        ## while
        pass




s = ServerAssistant(2000)
s.start()
s.join()
