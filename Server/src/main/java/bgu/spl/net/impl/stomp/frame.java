package bgu.spl.net.impl.stomp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class frame {
    public String command = "";
    public HashMap<String, String> headers = new HashMap<String, String>();
    public String body="";

    public frame(String input)
    {
        String[] allFrameLines = input.split("\n");
        boolean stop=false;
        int j=0;
        for(int i=1; i<allFrameLines.length && !stop; i++)
        {
            String header = allFrameLines[i];
            if(header.indexOf(':')!=-1) {
                command = allFrameLines[i - 1];
                stop=true;
            }
            j=i;
        }
        while(j<allFrameLines.length && !allFrameLines[j].equals(""))
        {
            String[] h = allFrameLines[j].split(":");
            headers.put(h[0],h[1]);
            j++;
        }
        j++;
        if(command.equals("MESSAGE"))
            body = "\n"+body;
        while(j<allFrameLines.length)
        {
            body = body+allFrameLines[j];
            j++;
        }
    }

    private void initHeaders(String input) {
        headers = new HashMap<String, String>();
        String[] allFrameLines = input.split("\n");
        boolean stop = false;
        int bodyStartIndex = 0;
        for (int i=1 ; i<allFrameLines.length && !stop; i++) {
            if(allFrameLines[i] != "" && allFrameLines[i]!=null ) {
                String[] splitLine = allFrameLines[i].split(":");
                headers.put(splitLine[0],splitLine[1]);
            }
            else
                stop = true;
        }
    }

    public String getCommand() {
        return command;
    }
    public void addHeader (String key, String value) {
        headers.put(key, value);
    }
    public String getHeader(String header) {
        return headers.get(header);
    }

    public HashMap<String,String> getHeaders(){
        return headers;
    }
    public void addBody (String bodyString) {
        body = bodyString;
    }
    public String getBody() {
        return body;
    }
    public String receiptFrame(String receiptId) {
        return "RECEIPT\nreceipt-id:"+receiptId+"\n\n\u0000";
    }
    public String connectedFrame() {
        return "CONNECTED\nversion:1.2\n\n\u0000";
    }

    public String errorFrame(frame errorReason, String body) {
        String errorReasonCommand = errorReason.getCommand();
        String errorType = "";
        switch (errorReasonCommand)
        {
            case("CONNECT"):
                errorType = "connect";
                break;
            case("DISCONNECT"):
                errorType = "disconnect";
                break;
            case("SEND"):
                errorType = "message";
                break;
            case("SUBSCRIBE"):
                errorType = "subscribe";
                break;
            case("UNSUBSCRIBE"):
                errorType = "unsubscribe";
                break;
            default: //assume this case will never happen
                return null;
        }
        String receiptId = errorReason.getHeader("receipt-id");
        if(receiptId==null)
            return "ERROR\nthe frame you sent:\n"+errorReason.frameToString()+"\n"+body+"\n\n\u0000";
        return "ERROR\nreceipt-id:"+errorType+"-"+receiptId+"\nthe frame you sent:\n"+errorReason.frameToString()+"\n"+body+"\n\n\u0000";
    }
    public String frameToString() {
        String output = command + "\n";
        for(String header: headers.keySet())
            output = output + header + ":" + headers.get(header) + "\n";
        if (body != "\u0000")
            output = output + body;
        return output;
    }
}
