from util.ToolboxServer import ToolboxServer

class SolutionManager:

    @staticmethod
    def writeCMDPSolution(filename):
        command = "writeCMDPSolution_"
        command += filename
        ToolboxServer.send_request(command)

    @staticmethod
    def writeCPOMDPSolution(filename):
        command = "writeCPOMDPSolution_"
        command += filename
        ToolboxServer.send_request(command)

    @staticmethod
    def readCMDPSolution(filename):
        command = "readCMDPSolution_"
        command += filename
        ToolboxServer.send_request(command)

    @staticmethod
    def readCPOMDPSolution(filename):
        command = "readCPOMDPSolution_"
        command += filename
        ToolboxServer.send_request(command)