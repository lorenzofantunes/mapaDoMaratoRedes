import socket
import json
import database
import threading
from bson import Binary, Code
from bson.json_util import dumps, CANONICAL_JSON_OPTIONS

class UDP():

    def __init__(self, origin):
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.sock.bind((origin[0], origin[1]))
        self.clients_list = []

    def deal(self, msg, client):
        print(msg)
        msg = str(msg.decode())
        msg = json.loads(msg)
        pair = msg['onde'].replace('Pair{', '').replace('}', '').split(', ')
        msg['onde'] = ((pair[0]), (pair[1]))

        if isinstance(msg['palavras'], str):
            msg['palavras'] = list(set(msg['palavras'].split()))

        session_id = database.insert(db, msg)

        retorno = database.pesquisarPorTempoEspacoPalavra(db, 5, msg['onde']['coordinates'][0], msg['onde']['coordinates'][1], msg['palavras'], msg['nome'])
        pessoas = []
        for x in retorno:
            palavras = []
            for palavra in x['palavras']:
                if (palavra in msg['palavras']):
                    palavras.append(palavra)
            x['palavras'] = palavras
            x['lat'] = x['onde']['coordinates'][0]
            x['long'] = x['onde']['coordinates'][1]
            x.pop('onde', None)
            pessoas.append(x)

        print('achou:', len(pessoas))
        sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM) # UDP
        pessoas = dumps(pessoas, json_options=CANONICAL_JSON_OPTIONS)

        sock.sendto(bytes(pessoas, "utf-8"), (client[0], client[1]))

    def start(self):
        while True:
            msg, client = self.sock.recvfrom(1024)
            t = threading.Thread(target=self.deal, args=(msg, client))
            t.start()

db, client = database.connect('maroto')

HOST = ''     # Endereco IP do Servidor

udp = UDP((HOST, 5000))
udp.start()
