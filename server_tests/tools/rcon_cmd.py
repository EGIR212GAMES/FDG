#!/usr/bin/env python3
"""Minimal Minecraft RCON client. Usage: rcon_cmd.py <host> <port> <password> <command>"""
import socket
import struct
import sys


def main():
    host, port, password, command = sys.argv[1], int(sys.argv[2]), sys.argv[3], " ".join(sys.argv[4:])
    s = socket.create_connection((host, port), timeout=5)

    def send(rid, typ, payload):
        body = struct.pack("<ii", rid, typ) + payload.encode() + b"\x00"
        s.sendall(struct.pack("<i", len(body)) + body)

    def recv():
        ln = struct.unpack("<i", s.recv(4))[0]
        buf = b""
        while len(buf) < ln:
            buf += s.recv(ln - len(buf))
        rid, typ = struct.unpack("<ii", buf[:8])
        return rid, typ, buf[8:-1].decode("utf-8", "replace")

    send(1, 3, password)
    rid, typ, resp = recv()
    print("AUTH:", typ, resp)
    send(2, 2, command)
    rid, typ, resp = recv()
    print("RESP:", typ, resp)


if __name__ == "__main__":
    main()
