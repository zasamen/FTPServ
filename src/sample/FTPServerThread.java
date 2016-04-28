package sample;


import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

class FTPServerThread extends Thread {

    private ServerSocket serverSocket = null;
    private CommandInterpreter commandInterpreter;
    private Charset charset=StandardCharsets.UTF_8;//StandardCharsets.US_ASCII;

    FTPServerThread() {
        super();
        this.setDaemon(true);

        this.setName("FTP Thread");
        try {
            serverSocket = new ServerSocket(21, 1);
        } catch (IOException ioe) {
            System.err.println("001"+ioe.toString());
            serverSocket = null;
        }
    }

    @Override
    public void run() {
        super.run();
        System.out.print("STARTED\n");
        if (serverSocket == null) {
            System.err.println("NULL SOcket");
            return;
        }
        Scanner scanner;
        try {
            Socket socket=serverSocket.accept();
            System.out.print("ACCEPTED\n");
            commandInterpreter = new CommandInterpreter(socket);
            scanner = new Scanner(socket.getInputStream(),charset.name());
            System.out.print("SCANNING\n");
        } catch (IOException ioe) {
            System.err.println("002"+ioe.toString());
            System.out.print(ioe.toString()+"\n");
            return;
        }
        while (scanner.hasNextLine()) {
            parseInput(scanner.nextLine());
            System.out.print("Parsed\n");
        }
    }

    private String getCommand(String line) {
        return line.split("\\s")[0];
    }

    @NotNull
    private String getArgs(String line, int start) {
        if (start!=line.length())
            return line.substring(++start);
        else return "";
    }

    private void parseInput(String line) {
        String command = getCommand(line);
        System.out.print("Read "+line+" ");
        String args = getArgs(line, command.length());
        switch (command.toUpperCase()) {
            case "USER":
                commandInterpreter.user(args);
                break;
            case "PASS":
                commandInterpreter.pass(args);
                break;
            case "ABOR":
                commandInterpreter.abor(args);
                break;
            case "CDUP":
                commandInterpreter.cdup();
                break;
            case "CWD":
                commandInterpreter.cwd(args);
                break;
            case "DELE":
                commandInterpreter.dele(args);
                break;
            case "LIST":
                commandInterpreter.list(args);
                break;
            case "MKD":
                commandInterpreter.mkd(args);
                break;
            case "NOOP":
                commandInterpreter.noop();
                break;
            case "PWD":
                commandInterpreter.pwd();
                break;
            case "QUIT":
                commandInterpreter.quit();
                break;
            case "REIN":
                commandInterpreter.rein(args);
                break;
            case "RETR":
                commandInterpreter.retr(args);
                break;
            case "RMD":
                commandInterpreter.rmd(args);
                break;
            case "SIZE":
                commandInterpreter.size(args);
                break;
            case "STOR":
                commandInterpreter.stor(args);
                break;
            case "TYPE":
                commandInterpreter.type(args);
                break;
            case "PORT":
                commandInterpreter.port(args);
                break;
            case "PASV":
                commandInterpreter.pasv();
                break;
            case "SYST":
                commandInterpreter.syst();
                break;
            case "FEAT":
                commandInterpreter.feat(args);
                break;
            case "OPTS":
                commandInterpreter.opts(args);
                break;
            case "HELP":
            case "NLST":
            case "STRU":
            case "MODE":
            case "STOU":
            case "APPE":
            case "ALLO":
            case "REST":
            case "RNFR":
            case "RNTO":
            case "SITE":
            case "STAT":
            case "ACCT":
            case "SMNT":
            default:
                commandInterpreter.def();

        }
    }

}