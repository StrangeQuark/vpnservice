import json
import os
import re
import secrets
import subprocess
import time
from http.server import BaseHTTPRequestHandler, HTTPServer

PEERS_FILE = "/config/dynamic-peers.json"


def command(*args, input_value=None, check=True):
    return subprocess.run(args, input=input_value, text=True, check=check, capture_output=True).stdout.strip()


def get_peers():
    if not os.path.exists(PEERS_FILE):
        return {}

    with open(PEERS_FILE) as file:
        return json.load(file)


def save_peers(peers):
    temporary_file = PEERS_FILE + ".tmp"

    with open(temporary_file, "w") as file:
        json.dump(peers, file)

    os.replace(temporary_file, PEERS_FILE)


def wait_for_interface():
    while subprocess.run(["wg", "show", "wg0"], capture_output=True).returncode != 0:
        time.sleep(1)


def restore_peers():
    for public_key, vpn_address in get_peers().items():
        command("wg", "set", "wg0", "peer", public_key, "allowed-ips", vpn_address + "/32")
        command("ip", "route", "replace", vpn_address + "/32", "dev", "wg0")


class WireGuardControlHandler(BaseHTTPRequestHandler):
    def do_POST(self):
        parent = re.fullmatch(r"00-([0-9a-f]{32})-([0-9a-f]{16})-([0-9a-f]{2})",
                              self.headers.get("traceparent", ""))
        self.trace_id = (parent.group(1) if parent and int(parent.group(1), 16)
                         and int(parent.group(2), 16) else secrets.token_hex(16))
        self.span_id = secrets.token_hex(8)
        if self.headers.get("X-WIREGUARD-CONTROL-TOKEN") != os.environ["WIREGUARD_CONTROL_TOKEN"]:
            self.send_response(401)
            self.end_headers()
            return

        try:
            body = json.loads(self.rfile.read(int(self.headers["Content-Length"])))
            if self.path == "/create-peer":
                private_key = command("wg", "genkey")
                public_key = command("wg", "pubkey", input_value=private_key)
                command("wg", "set", "wg0", "peer", public_key, "allowed-ips", body["vpnAddress"] + "/32")
                command("ip", "route", "replace", body["vpnAddress"] + "/32", "dev", "wg0")
                peers = get_peers()
                peers[public_key] = body["vpnAddress"]
                save_peers(peers)
                response = {"privateKey": private_key, "publicKey": public_key,
                            "serverPublicKey": command("wg", "show", "wg0", "public-key")}
            elif self.path == "/remove-peer":
                command("wg", "set", "wg0", "peer", body["publicKey"], "remove")
                command("ip", "route", "del", body["vpnAddress"] + "/32", "dev", "wg0", check=False)
                peers = get_peers()
                peers.pop(body["publicKey"], None)
                save_peers(peers)
                response = {}
            else:
                self.send_response(404)
                self.end_headers()
                return

            encoded_response = json.dumps(response).encode()
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_header("Content-Length", str(len(encoded_response)))
            self.end_headers()
            self.wfile.write(encoded_response)
        except Exception as exception:
            self.send_response(500)
            self.end_headers()
            self.wfile.write(str(exception).encode())

    def send_response(self, code, message=None):
        if not getattr(self, "trace_id", None):
            self.trace_id = secrets.token_hex(16)
            self.span_id = secrets.token_hex(8)
        super().send_response(code, message)
        self.send_header("X-Trace-ID", self.trace_id)

    def log_message(self, format, *args):
        print(json.dumps({"message": format % args, "level": "INFO",
                          "traceId": getattr(self, "trace_id", ""),
                          "spanId": getattr(self, "span_id", "")}), flush=True)


def main():
    wait_for_interface()
    restore_peers()
    HTTPServer(("0.0.0.0", 51821), WireGuardControlHandler).serve_forever()


if __name__ == "__main__":
    main()
