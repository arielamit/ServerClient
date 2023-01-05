package bgu.spl.net.impl.stomp;

public class mainForCheck
{
    public static void main(String[] args)
    {
        String s = "This is \n some text \n with newlines";
        String[] t = s.split("\\r?\\n|\\r");

//        System.out.println(t);

        for (String r : t)
        {
            System.out.println(r);
            System.out.println(0);

        }
    }

}
