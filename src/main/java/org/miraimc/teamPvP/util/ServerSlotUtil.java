package org.miraimc.teamPvP.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class ServerSlotUtil {

    public static void setMaxPlayers(int amount) {
        try {
            File file = new File("server.properties");
            Properties props = new Properties();

            // Load Settings
            FileInputStream in = new FileInputStream(file);
            props.load(in);
            in.close();

            props.setProperty("max-players", String.valueOf(amount));

            // Save
            FileOutputStream out = new FileOutputStream(file);
            props.store(out, null);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
