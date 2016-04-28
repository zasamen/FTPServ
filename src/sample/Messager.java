package sample;

import org.jetbrains.annotations.Contract;

class Messager {
    private static int[] passiveModes={0,0,0,0,0,0};
    private static String pathname="/";
    private static String text="";

    static String getMessage(int code) {
        switch (code) {
            case 110: return concatString(code,"Restart marker reply.");
            case 120: return concatString(code,"Service ready in nnn minutes.");
            case 125: return concatString(code,"Data connection already open; transfer starting.");
            case 150: return concatString(code,"File status okay; about to open data connection.");

            case 200: return concatString(code,"Command okay.");
            case 202: return concatString(code,"Command not implemented, superfluous at this site.");
            case 211: return concatString(code,"System status, or system help reply.");
            case 212: return concatString(code,"Directory status.");
            case 213: return concatString(code,"File status."+text);
            case 214: return concatString(code,"Help message.");
            case 215: return concatString(code, getSystemName()+" system type.");
            case 220: return concatString(code,"Service ready for new user.");
            case 221: return concatString(code,"Service closing control connection.");
            case 225: return concatString(code,"Data connection open; no transfer in progress.");
            case 226: return concatString(code,"Closing data connection.");
            case 227: return concatString(code,"Entering Passive Mode ("+stringPassiveModes()+").");
            case 230: return concatString(code,"User logged in, proceed.");
            case 250: return concatString(code,"Requested file action okay, completed.");
            case 257: return concatString(code,"\""+getPathname()+"\" created.");

            case 331: return concatString(code,"User name okay, need password.");
            case 332: return concatString(code,"Need account for login.");
            case 350: return concatString(code,"Requested file action pending further information.");

            case 421: return concatString(code,"Service not available, closing control connection.");
            case 425: return concatString(code,"Can't open data connection.");
            case 426: return concatString(code,"Connection closed; transfer aborted.");
            case 450: return concatString(code,"Requested file action not taken.");
            case 451: return concatString(code,"Requested action aborted: local error in processing.");
            case 452: return concatString(code,"Requested action not taken.");

            case 500: return concatString(code,"Syntax error, command unrecognized.");
            case 501: return concatString(code,"Syntax error in parameters or arguments.");
            case 502: return concatString(code,"Command not implemented.");
            case 503: return concatString(code,"Bad sequence of commands.");
            case 504: return concatString(code,"Command not implemented for that parameter.");
            case 530: return concatString(code,"Not logged in.");
            case 532: return concatString(code,"Need account for storing files.");
            case 550: return concatString(code,"Requested action not taken.");
            case 551: return concatString(code,"Requested action aborted: page type unknown.");
            case 552: return concatString(code,"Requested file action aborted.");
            case 553: return concatString(code,"Requested action not taken.");
            default: return concatString(code,"");
        }
    }

    @Contract(pure = true)
    private static String concatString(int code, String mess){
        return (code+" "+mess+"\r\n");
    }

    private static String getSystemName(){
        return System.getProperties().getProperty("os.name");
    }
    static void setPassiveModes(int h1,int h2,int h3,int h4,int p1,int p2){
        passiveModes=new int[]{
                h1,h2,h3,h4,p1,p2
        };
    }

    @Contract(pure = true)
    private static String stringPassiveModes(){
        String result="";
        for (int passiveMode:passiveModes){
            result+=passiveMode+",";
        }
        return result;
    }

    static void setPathname(String pathname) {
        Messager.pathname = pathname;
    }

    private static String getPathname() {
        return pathname;
    }

    static void setText(String text) {
        Messager.text = text;
    }
}