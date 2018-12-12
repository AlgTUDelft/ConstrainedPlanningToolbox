#################################################################################
# ConstrainedPlanningToolbox
# Copyright (C) 2019 Algorithmics group, Delft University of Technology
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#################################################################################

import socket


def send_string(sock, text):
    to_send = text
    to_send += "\n"
    sock.sendall(to_send.encode())


def receive_string(sock):
    data = sock.recv(1024)
    data = data.decode()
    data = data.strip('\n')
    return data


def send_command(sock, command):
    send_string(sock, command)
    data = receive_string(sock)
    return data


class ToolboxServer:

    sock = None

    @staticmethod
    def connect():
        assert ToolboxServer.sock == None
        try:
            ToolboxServer.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            ToolboxServer.sock.connect(("localhost", 8080))
        except ConnectionRefusedError:
            print("Could not connect to server on port 8080")
            quit()

    @staticmethod
    def disconnect():
        assert ToolboxServer.sock != None
        send_command(ToolboxServer.sock, "disconnect")
        sock = None

    @staticmethod
    def shutdown():
        assert ToolboxServer.sock != None
        send_command(ToolboxServer.sock, "shutdown")

    @staticmethod
    def send_request(request):
        assert ToolboxServer.sock != None
        return send_command(ToolboxServer.sock, request)

