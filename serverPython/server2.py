import socket
import threading
import json
import database
from bson import Binary, Code
from bson.json_util import dumps, CANONICAL_JSON_OPTIONS

def treatMSG(msg):
    msg = str(msg.decode('utf-8'))
    msg = json.loads(msg)
    pair = msg['onde'].replace('Pair{', '').replace('}', '').split(', ')
    msg['onde'] = ((pair[0]), (pair[1]))

    if isinstance(msg['palavras'], str):
        msg['palavras'] = list(set(msg['palavras'].split()))

    return msg

def treatSearch(search, msg):
    pessoas = []
    for x in search:
        palavras = []
        for palavra in x['palavras']:
            if (palavra in msg['palavras']):
                palavras.append(palavra)
        x['palavras'] = palavras
        x['lat'] = x['onde']['coordinates'][0]
        x['long'] = x['onde']['coordinates'][1]
        x.pop('onde', None)
        pessoas.append(x)

    pessoas = dumps(pessoas, json_options=CANONICAL_JSON_OPTIONS)
    print('SENT:', pessoas, '\n')
    return bytes(pessoas, "utf-8")

class TCP ():
    def __init__(self, host, port):
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.socket.bind((host, port))
        self.socket.listen(1)
        #self.socket.settimeout(30)

    def deal(self, conn, addr, data):
        print(data)
        msg = treatMSG(data)

        session_id = database.insert(db, msg)

        retorno = database.pesquisarPorTempoEspacoPalavra(db, 5, msg['onde']['coordinates'][0], msg['onde']['coordinates'][1], msg['palavras'], msg['nome'])

        conn.sendall(treatSearch(retorno, msg))

        conn.close()

    def start(self):
        while True:
            try:
                conn, addr = self.socket.accept()
            except socket.timeout:
                continue

            while True:
                data = conn.recv(1024)
                if not data:
                    conn.close()
                    break
                else:
                    t = threading.Thread(target=self.deal, args=(conn, addr, data))
                    t.start()
                    break

db, client = database.connect('maroto')

HOST = '165.227.86.76'
PORT = 5000

tcpServer = TCP(HOST, PORT)
tcpServer.start()
