package sample;

import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

class CommandInterpreter {
    private Charset charset = StandardCharsets.US_ASCII;

    CommandInterpreter(Socket socket) {
        this.socket = socket;
        try {
            activeINetAddress = socket.getInetAddress();
            passiveINetAddress = socket.getInetAddress();
            socket.getOutputStream().write(Messager.getMessage(220).getBytes(charset));
        } catch (IOException ioe) {
            System.err.println("C001" + ioe.toString());
        }
    }

    private Socket socket;
    private Socket dataSocket;
    private File pwdFile = new File("/");
    private String userName = "anonymous";

    private ServerSocket passiveConnection;
    private InetAddress activeINetAddress;
    private int activePort;
    private InetAddress passiveINetAddress;
    private boolean binaryMode = true;
    private int bufferSize = 1024 * 1024;

    void user(String args) {
        String userName = args.trim();
        sendMessage((userName.equalsIgnoreCase(this.userName)) ? 331 : 530);
    }

    void pass(String args) {
        String password = args.trim();
        sendMessage((password.equals("zasamen@yandex.ru")) ? 230 : 530);
    }

    void quit() {
        sendMessage(221);
        closePassiveConnection();
    }

    void noop() {
        if (!checkUser()) return;
        sendMessage(200);
    }

    void syst() {
        if (!checkUser()) return;
        sendMessage(215);
    }

    void port(String args) {
        if (!checkUser()) return;
        closePassiveConnection();
        setActiveAddressFromByteString(args);
        sendMessage(200, " PORT command successful\r\n", true);
    }

    void pasv() {
        if (!checkUser()) return;
        closePassiveConnection();
        openPassiveConnection();
        sendMessage(227);
    }

    void list(String args) {
        if (!checkUser()) return;
        Thread thread = new Thread(() ->
        {
            File someDir = getDirForList(args);
            if ((checkDir(someDir)) &&
                    (someDir != null)) {
                File[] files = someDir.listFiles();
                if (files != null) {
                    System.out.print("Open data connection.");
                    sendMessage(150);
                    if (checkConnection())
                        openActiveConnection();
                    for (File file : files) {
                        sendData(getFormattedFileInfo(file));
                    }
                    System.out.print("Data sent.\r\n");
                    closeDataSocket();
                }
            }
            sendMessage(226);
        });
        thread.start();
    }

    void abor(String args) {
        if (args.trim().equals(args))
            sendMessage(502);
    }

    void type(String args) {
        binaryMode = args.trim().toUpperCase().equals("I");
        sendMessage(200, " Switching mode.", true);
    }

    void retr(String args) {
        if (!checkUser()) return;
        Thread thread = new Thread(() -> {
            File file = new File(pwdFile.getAbsolutePath() + "/" + args.trim());
            if (file.exists() && (file.isFile())) {
                sendMessage(150);
                System.out.print("File uploading.");
                if (checkConnection())
                    openActiveConnection();
                sendData(file);
                sendMessage(226);
                System.out.print("Uploading finished.\r\n");
                closeDataSocket();
            }
        });
        thread.start();
    }

    void stor(String args) {
        if (!checkUser()) return;
        Thread thread = new Thread(() -> {
            File file = new File(pwdFile.getAbsolutePath() + "/" + args.trim());
            if (tryCreateFile(file)) {
                System.out.print("File created.");
                sendMessage(150);
                System.out.print("Downloading file.");
                if (checkConnection())
                    openActiveConnection();
                getData(file);
                closeDataSocket();
                System.out.print("File Downloaded."+file.length()+"b \n");
                sendMessage(226);
            }
        });
        thread.start();
    }

    void rein(String args) {
        binaryMode = true;
        if (args.trim().equals(args))
            pwdFile = new File("/");
    }

    void dele(String args) {
        if (!checkUser()) return;
        File file = new File(pwdFile.getAbsolutePath() +"/"+ args.trim());
        if ((!file.exists())||(file.delete()))
            sendMessage(250);
        else sendMessage(450);
    }

    void mkd(String args) {
        if (!checkUser()) return;
        File file = new File(pwdFile.getAbsolutePath() +"/"+ args.trim());
        if (file.mkdir()) {
            Messager.setPathname(file.getAbsolutePath());
            sendMessage(257);
        } else
            sendMessage(450);
    }

    void rmd(String args) {
        if (!checkUser()) return;
        dele(args);
    }

    void cdup() {
        if (!checkUser()) return;
        File file = pwdFile.getParentFile();
        pwdFile=(file==null)?pwdFile:file;
        sendMessage(230);
    }

    void pwd() {
        if (!checkUser()) return;
        Messager.setPathname(pwdFile.getAbsolutePath());
        sendMessage(257);
    }

    void cwd(String args) {
        if (!checkUser()) return;
        File file = new File(pwdFile.getAbsolutePath() + "/" + args.trim());
        if (file.isDirectory()) {
            pwdFile = file;
            sendMessage(250);
        } else {
            sendMessage(550);
        }
    }

    void size(String args) {
        if (!checkUser()) return;
        File file = new File(args.trim());
        Messager.setText("" + file.length());
        sendMessage(213);
    }

    void feat(String args) {
        if (!checkUser()) return;
        if (args.trim().equals(args))
            sendMessage(211, "-Features\r\nPASV\r\nSIZE\r\n", false);
    }

    void opts(String args) {
        if (args.trim().equals(args))
            sendMessage(200, " CHARSET CHANGED", true);
    }

    void def(){
        sendMessage(502);
    }

    private void sendMessage(int code) {
        try {
            socket.getOutputStream().write(Messager.getMessage(code).getBytes(charset));
        } catch (IOException ioe) {
            System.err.println("C009" + ioe.toString());
        }
    }

    private void sendMessage(int code, String string, boolean monoLine) {
        try {
            if (!monoLine)
                socket.getOutputStream().write((code + string + code + " End\r\n").getBytes(charset));
            else
                socket.getOutputStream().write((code + string + "\r\n").getBytes(charset));
        } catch (IOException ioe) {
            System.err.println("C010" + ioe.toString());
        }
    }

    private void sendData(String data) {
        if (binaryMode)return;
        try {
            dataSocket.getOutputStream().write((data + "\r\n").getBytes(charset));
        } catch (IOException ioe) {
            System.err.println("C011" + ioe.toString());
        }
    }

    private void sendData(File file) {
        if (!binaryMode)return;
        byte[] bytes = new byte[bufferSize];
        int count;
        try {
            FileInputStream fis = new FileInputStream(file);
            do {
                count = fis.read(bytes);
                if (count != -1)
                    dataSocket.getOutputStream().write(bytes, 0, count);
            } while (count != -1);
        } catch (IOException ioe) {
            System.err.println("C013" + ioe.toString());
            sendMessage(450);
        }
    }

    private void getData(File file) {
        if (!binaryMode)return;
        byte[] bytes = new byte[bufferSize];
        int count;
        try {
            FileOutputStream fos = new FileOutputStream(file);
            do {
                count = dataSocket.getInputStream().read(bytes);
                if (count != -1) fos.write(bytes, 0, count);
            } while (count != -1);
        } catch (IOException ioe) {
            System.err.println("C015" + ioe.toString());
            sendMessage(450);
        }
    }

    private boolean checkConnection() {
        if (passiveConnection == null) {
            try {
                if (dataSocket != null)
                    dataSocket.close();
                return true;
            } catch (IOException ioe) {
                System.err.println("C017" + ioe.toString());
            }
        }
        return false;
    }

    private void closePassiveConnection() {
        try {
            if (passiveConnection != null) {
                passiveConnection.close();
                passiveConnection = null;
                if (dataSocket != null) {
                    dataSocket.close();
                    dataSocket = null;
                }
            }
        } catch (IOException ioe) {
            System.err.println("C018" + ioe);
        }
    }

    private void setActiveINetAddressFromByteStringArray(String[] strings) {
        byte[] bytesIPAndPort = new byte[4];
        for (int i = 0; i < 4; i++) {
            bytesIPAndPort[i] = (byte) Integer.parseInt(strings[i]);
        }
        try {
            activeINetAddress = InetAddress.getByAddress(bytesIPAndPort);
        } catch (UnknownHostException uhe) {
            System.err.println("C019 " + uhe.toString());
        }
    }

    private void setActivePortFromByteStringArray(String[] strings) {
        activePort = Integer.parseInt(strings[5]) + (Integer.parseInt(strings[4]) << 8);
    }

    private void setActiveAddressFromByteString(String string) {
        String[] strings = string.split(",");
        setActiveINetAddressFromByteStringArray(strings);
        setActivePortFromByteStringArray(strings);
    }

    private void openPassiveConnection() {
        try {
            int passivePort;
            passiveConnection = new ServerSocket();
            passiveConnection.bind(new InetSocketAddress(passiveINetAddress, 0));
            byte[] bytes = passiveINetAddress.getAddress();
            passivePort = passiveConnection.getLocalPort();
            Messager.setPassiveModes(bytes[0], bytes[1], bytes[2], bytes[3], passivePort >> 8, passivePort & 0xFF);
            dataSocket = passiveConnection.accept();
        } catch (IOException ioe) {
            System.err.println("C020 " + ioe);
        }
    }

    private boolean checkUser() {
        if (userName == null) {
            sendMessage(530);
            return false;
        }
        return true;
    }

    @Nullable
    private File getDirForList(String string) {
        return (string.trim().isEmpty())
                ? (pwdFile.isDirectory()) ? pwdFile : null
                : new File(pwdFile.getAbsolutePath() + "/" + string.trim());

    }

    private boolean checkDir(File file) {
        if ((file == null) ||
                (!file.exists()) ||
                (!file.isDirectory())) {
            sendMessage(501);
            return false;
        }
        return true;
    }

    private String getFormattedFileInfo(File file) {
        String fileName = file.getName(), size = "", dir = "-";
        Calendar calendar = Calendar.getInstance();
        if (file.isFile()) {
            size += file.length();
        } else {
            dir = "d";
        }
        calendar.setTimeInMillis(file.lastModified());
        return String.format("%2$10S %3$Tm-%3$Td-%3$Ty %3$TH:%3$TM:%3$TS %4$1S %1$S", fileName, size, calendar, dir);
    }

    private void closeDataSocket() {
        try {
            dataSocket.close();
        } catch (IOException ioe) {
            System.err.println("C021 " + ioe);
        }
    }

    private void openActiveConnection() {
        try {
            dataSocket = new Socket(activeINetAddress, activePort);
        } catch (IOException ioe) {
            System.err.println("C022 " + ioe);
        }
    }

    private boolean tryCreateFile(File file) {
        try {
            return ((!file.exists()) && (file.createNewFile()));
        } catch (IOException ioe) {
            System.err.println("C023 "+ioe);
        }
        return false;
    }
}