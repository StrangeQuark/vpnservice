import contextlib
import http.client
import io
import json
import os
import threading
import unittest
from http.server import HTTPServer
from unittest.mock import patch

from control import WireGuardControlHandler


class TraceTest(unittest.TestCase):
    def test_peer_response_and_log_continue_trace_without_logging_keys(self):
        trace_id = "4bf92f3577b34da6a3ce929d0e0e4736"
        output = io.StringIO()
        with patch.dict(os.environ, {"WIREGUARD_CONTROL_TOKEN": "test-token"}), \
                patch("control.command", return_value="private-key-never-log"), \
                patch("control.get_peers", return_value={}), patch("control.save_peers"), \
                contextlib.redirect_stdout(output):
            server = HTTPServer(("127.0.0.1", 0), WireGuardControlHandler)
            worker = threading.Thread(target=server.serve_forever, daemon=True)
            worker.start()
            connection = http.client.HTTPConnection(*server.server_address)
            try:
                for parent, expected_status, token in [
                    ("00-" + trace_id + "-00f067aa0ba902b7-01", 200, "test-token"),
                    ("invalid", 401, "wrong-token")
                ]:
                    connection.request("POST", "/create-peer", json.dumps({"vpnAddress": "10.8.0.2"}), {
                        "traceparent": parent, "X-WIREGUARD-CONTROL-TOKEN": token
                    })
                    response = connection.getresponse()
                    self.assertEqual(expected_status, response.status)
                    received_id = response.getheader("X-Trace-ID")
                    self.assertRegex(received_id, r"^[0-9a-f]{32}$")
                    if expected_status == 200:
                        self.assertEqual(trace_id, received_id)
                        self.assertIn("privateKey", json.loads(response.read()))
                    else:
                        self.assertNotEqual(trace_id, received_id)
                        response.read()
            finally:
                connection.close()
                server.shutdown()
                server.server_close()
                worker.join()
        self.assertIn(trace_id, output.getvalue())
        self.assertNotIn("private-key-never-log", output.getvalue())
        self.assertNotIn("test-token", output.getvalue())


if __name__ == "__main__":
    unittest.main()
