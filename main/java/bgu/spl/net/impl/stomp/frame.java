package bgu.spl.net.impl.stomp;
import java.util.HashMap;

public class frame {
    public String command;
    public String body = null;
    public HashMap<String, String> headers = new HashMap<String, String>();

    public frame(String input)
    {
        headers = new HashMap<String, String>();

        //convert input String into frame object
        String[] allFrameLines = input.split("\\r?\\n|\\r");
        command = allFrameLines[0];

        for (int i =1 ; i<allFrameLines.length; i++)
        {
            if(allFrameLines[i] != "")
            {
                String header = allFrameLines[i];
                String[] headerSeperate = header.split(":");
                headers.put(headerSeperate[0], headerSeperate[1]);

            }else {
            }

        }

    }

    public void addHeader (String s, String t)
    {
        headers.put(s,t);
    }

    public void addBody (String s)
    {
        body = s;
    }


    public String frameToString()
    {
        String output = command + "\n";

        for(String header :headers.keySet() )
        {
            output = output + header + ":";
            output = output + headers.get(header) + "\n";
        }

        if (body != null)
        {
           output = output + body;
        }

        output = output + "\u0000" ;
        return output;
    }


}
