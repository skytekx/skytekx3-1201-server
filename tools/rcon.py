#!/usr/bin/env python3
"""Minimal Source-RCON client: rcon.py <host> <port> <password> <command...>"""
import socket, struct, sys
host, port, pw = sys.argv[1], int(sys.argv[2]), sys.argv[3]
cmd = " ".join(sys.argv[4:])
def pkt(reqid, typ, body):
    data = struct.pack("<ii", reqid, typ) + body.encode() + b"\x00\x00"
    return struct.pack("<i", len(data)) + data
def recv(s):
    ln = struct.unpack("<i", s.recv(4))[0]
    data = b""
    while len(data) < ln: data += s.recv(ln - len(data))
    reqid, typ = struct.unpack("<ii", data[:8])
    return reqid, typ, data[8:-2].decode("utf-8", "ignore")
s = socket.create_connection((host, port), timeout=15)
s.sendall(pkt(1, 3, pw))           # auth
rid, _, _ = recv(s)
if rid == -1:
    print("AUTH FAILED"); sys.exit(1)
s.sendall(pkt(2, 2, cmd))          # command
_, _, resp = recv(s)
print(resp.strip() if resp.strip() else "(empty response)")
s.close()
