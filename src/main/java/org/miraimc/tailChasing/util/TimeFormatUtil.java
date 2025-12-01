package org.miraimc.tailChasing.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TimeFormatUtil {

    public static String formatSeconds(int seconds) {
        int minutes = seconds / 60;
        return String.format("%02d:%02d", minutes, seconds % 60);
    }
}
