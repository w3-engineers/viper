package com.w3engineers.mesh.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/*
 *  ****************************************************************************
 *  * Created by : Md Tariqul Islam on 10/4/2019 at 6:13 PM.
 *  * Email : tariqul@w3engineers.com
 *  *
 *  * Purpose:
 *  *
 *  * Last edited by : Md Tariqul Islam on 10/4/2019.
 *  *
 *  * Last Reviewed by : <Reviewer Name> on <mm/dd/yy>
 *  ****************************************************************************
 */

public class TimeUtil {
    public static String parseMillisToTime(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss",Locale.getDefault());

        return dateFormat.format(new Date(time));
    }
}
