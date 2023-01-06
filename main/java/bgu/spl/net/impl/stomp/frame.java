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
        Boolean stop = false;
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
                    body = body + allFrameLines + "\n";
                    stop = true;
                }

            }
        }
    }

    public void addHeader (String key, String value)
    {
        headers.put(key, value);
    }

    public void addBody (String bodyString)
    {
        body = bodyString;
    }

    public String frameToString()
    {
        String output = command + "\n";

        for(String header: headers.keySet())
            output = output + headers.get(header) + "\n";

        if (body != "~@")
        {
            output = output + body;
        }

        output = output + "\u0000" ;
        return output;
    }

    public String getCommand() { return command;}

    public String getHeader(String header)    {return headers.get(header);}


}
