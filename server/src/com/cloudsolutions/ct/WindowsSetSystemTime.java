package com.cloudsolutions.ct;

import java.io.IOException;
import java.lang.annotation.Native;
import java.text.SimpleDateFormat;
import java.util.Date;


public class WindowsSetSystemTime {


/*    public boolean SetLocalTime(SYSTEMTIME st) {
        return Kernel32.instance.SetLocalTime(st);
    }*/

    public boolean SetLocalTime(double offsetInMilliSeconds) throws IOException {

        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy HH:mm:ss");

        long currentTime = System.currentTimeMillis();
        System.out.println("Current Time = " + sdf.format(new Date(currentTime)).toString());
        System.out.println("Current Time in milliseconds = " + currentTime);



        long correctedTime=currentTime+(long)offsetInMilliSeconds;
        System.out.println("Corrected Time in milliseconds = " + correctedTime);


        String resultdate = sdf.format(new Date(correctedTime)).toString();

        System.out.println("Corrected Time = " + resultdate);

        String [] dateAndTime = resultdate.split(" ");

        String datestr = dateAndTime[0];
        String timestr = dateAndTime[1];

        Runtime rt = Runtime.getRuntime();
        Process proc;
        proc = rt.exec("cmd /C date " + datestr);
        proc = rt.exec("cmd /C time " + timestr);

        System.out.println("System Time Successfully updated");
        return true;
    }
}
