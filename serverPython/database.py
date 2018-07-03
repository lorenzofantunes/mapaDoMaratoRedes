import datetime
from pymongo import MongoClient

#connecting with the database

def connect(base):
    client = MongoClient()
    db = client[base]
    return db, client

def insert(db, session):
    session['quando'] = datetime.datetime.utcnow()
    session['onde'] = {
        'type': "Point",
        'coordinates': [
            float(session['onde'][0]),
            float(session['onde'][1])
        ]
    }
    sessions = db.sessions
    session_id = sessions.insert_one(session).inserted_id
    return session_id    
