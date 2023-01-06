package bgu.spl.net.impl.stomp;
import java.util.HashMap;

public class frame {
    public String command;
    public HashMap<String, String> headers = new HashMap<String, String>();
    public String body = null;

    public frame(String input)
    {
        headers = new HashMap<String, String>();
        //convert input String into frame object
        String[] allFrameLines = input.split("\\r?\\n|\\r");
        command = allFrameLines[0];
        boolean stop = false;
        for (int i=1 ; i<allFrameLines.length && !stop; i++)
        {
            if(allFrameLines[i] != "")
            {
                String nextLine = allFrameLines[i];
                int index = 0;
                String key = "";
                while(nextLine.length()>index && nextLine.charAt(index)!=':')
                {
                    key = key+nextLine.charAt(index);
                    index++;
                }
                String value = nextLine.substring(index-1, nextLine.length());
                headers.put(key,value);
            }
            else
            {
                if(allFrameLines[i]!="~@")
                {
                    body = allFrameLines + "\n";
                    stop = true;
                }
            }
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
    public void addBody (String bodyString) {
        body = bodyString;
    }
    public String getBody() {
        return body;
    }
    public frame receiptFrame(String receiptId)
    {
        return new frame("RECEIPT \n receipt-id:"+receiptId);
    }
    public frame connectedFrame()
    {
        return new frame("CONNECTED \n version:1.2");
    }
    public frame messageFrame(frame sent)
    {
        String subId = sent.getHeader("subscription");
        String messId = sent.getHeader("message-id");
        String dest = sent.getHeader("destination");
        String bodyMes = sent.getBody();
        frame message = new frame("MESSAGE \n " +
                "subscription:"+subId+
                "\n message-id:"+messId+
                "\n destination:/topic/"+dest+
                "\n \n"+bodyMes);
        return message;
    }
    public frame errorFrame(frame errorReason)
    {
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
        String receiptId = errorReason.getHeader("receipt_id");
        frame error = new frame("ERROR \n receipt_id:"+errorType+"-"+receiptId+"\n the frame you sent: \n"+errorReason.frameToString()+"\n \n ~@");
        return error;
    }
    public String frameToString()
    {
        String output = command + "\n";
        for(String header: headers.keySet())
            output = output + headers.get(header) + "\n";
        if (body != "~@")
            output = output + body;
        output = output + "\u0000" ;
        return output;
    }
}
