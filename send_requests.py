import requests
import uuid
import time
from datetime import datetime


URL = "http://localhost:8081/api/tickets"
HEADERS = {"Content-Type": "application/json"}

def send_requests(count=500, delay=2):
    for i in range(count):
        user_id = str(uuid.uuid4())
        payload = {
            "userId": user_id,
            "flightNumber": "EK4004",
            "seatNumber": None
        }
        try:
            response = requests.post(URL, json=payload, headers=HEADERS)
            print(datetime.now().strftime('%Y-%m-%d %H:%M:%S'))
            print(f"[{i+1}/{count}] Status: {response.status_code} | userId: {user_id}")
        except Exception as e:
            print(f"[{i+1}/{count}] Error: {e}")

        time.sleep(delay)

if __name__ == "__main__":
    send_requests()
