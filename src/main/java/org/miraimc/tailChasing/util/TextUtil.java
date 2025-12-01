package org.miraimc.tailChasing.util;

import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public final class TextUtil {

    private static final Pattern RGB_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient:#[A-Fa-f0-9]{6}:#[A-Fa-f0-9]{6}>(.*?)</gradient>");

    public static final String LONG_LINE = translate("&7&m----------------------------------------");
    public static final String SHORT_LINE = translate("&7&m-------------------");

    /**
     * Translate color codes (&), RGB codes (&#xxxxxx), and gradients.
     */
    public static String translate(String string) {
        if (string == null || string.isEmpty()) {
            return "";
        }
        String translated = ChatColor.translateAlternateColorCodes('&', string);
        translated = applyRGB(translated);
        return applyGradient(translated);
    }

    /**
     * Translate with PlaceholderAPI support.
     */
    public static String translate(String string, Player player) {
        if (string == null || string.isEmpty()) {
            return "";
        }
        String withPlaceholders = PlaceholderAPI.setPlaceholders(player, string);
        return translate(withPlaceholders);
    }

    /**
     * Apply RGB hex colors in format &#RRGGBB
     */
    public static String applyRGB(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        Matcher matcher = RGB_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            String replacement = net.md_5.bungee.api.ChatColor.of("#" + hex).toString();
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * Apply gradient in format <gradient:#RRGGBB:#RRGGBB>text</gradient>
     */
    public static String applyGradient(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        Matcher matcher = GRADIENT_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String content = matcher.group(1);

            // Extract start and end colors
            String[] parts = matcher.group(0).split(":");
            if (parts.length < 3) {
                continue;
            }
            String startHex = parts[1].replace("#", "");
            String endHex = parts[2].substring(0, 6); // ensure only 6 hex digits

            List<String> gradient = generateGradient(startHex, endHex, content.length());
            StringBuilder colored = new StringBuilder();
            for (int i = 0; i < content.length(); i++) {
                colored.append(net.md_5.bungee.api.ChatColor.of("#" + gradient.get(i)))
                        .append(content.charAt(i));
            }
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(colored.toString()));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * Generate gradient colors between two hex values.
     */
    private static List<String> generateGradient(String startHex, String endHex, int steps) {
        int r1 = Integer.parseInt(startHex.substring(0, 2), 16);
        int g1 = Integer.parseInt(startHex.substring(2, 4), 16);
        int b1 = Integer.parseInt(startHex.substring(4, 6), 16);

        int r2 = Integer.parseInt(endHex.substring(0, 2), 16);
        int g2 = Integer.parseInt(endHex.substring(2, 4), 16);
        int b2 = Integer.parseInt(endHex.substring(4, 6), 16);

        List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.max(steps, 1); i++) {
            int r = r1 + (r2 - r1) * i / Math.max(steps - 1, 1);
            int g = g1 + (g2 - g1) * i / Math.max(steps - 1, 1);
            int b = b1 + (b2 - b1) * i / Math.max(steps - 1, 1);
            result.add(String.format("%02x%02x%02x", r, g, b));
        }
        return result;
    }
}
