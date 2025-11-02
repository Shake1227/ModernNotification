package shake1227.modernnotification.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFormattingUtils {

    private static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(?i)&([0-9A-FK-ORU])");

    public static List<Component> parseLegacyText(String text) {
        List<Component> lines = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            lines.add(Component.empty());
            return lines;
        }

        String[] splitByNewline = text.split("&u");

        for (int i = 0; i < splitByNewline.length; i++) {
            String lineText = splitByNewline[i];

            if (lineText.isEmpty() && i == splitByNewline.length - 1 && lines.size() > 0) {
                continue;
            }

            MutableComponent currentLine = Component.empty();
            Matcher matcher = FORMATTING_CODE_PATTERN.matcher(lineText);
            int lastEnd = 0;
            Style currentStyle = Style.EMPTY;

            while (matcher.find()) {
                int start = matcher.start();

                if (start > lastEnd) {
                    String plainText = lineText.substring(lastEnd, start);
                    currentLine.append(Component.literal(plainText).setStyle(currentStyle));
                }

                char formatChar = matcher.group(1).toLowerCase().charAt(0);
                currentStyle = applyFormat(currentStyle, formatChar);

                lastEnd = matcher.end();
            }

            if (lastEnd < lineText.length()) {
                currentLine.append(Component.literal(lineText.substring(lastEnd)).setStyle(currentStyle));
            }

            lines.add(currentLine);
        }

        return lines;
    }

    private static Style applyFormat(Style style, char formatChar) {
        switch (formatChar) {
            case '0': return style.withColor(ChatFormatting.BLACK);
            case '1': return style.withColor(ChatFormatting.DARK_BLUE);
            case '2': return style.withColor(ChatFormatting.DARK_GREEN);
            case '3': return style.withColor(ChatFormatting.DARK_AQUA);
            case '4': return style.withColor(ChatFormatting.DARK_RED);
            case '5': return style.withColor(ChatFormatting.DARK_PURPLE);
            case '6': return style.withColor(ChatFormatting.GOLD);
            case '7': return style.withColor(ChatFormatting.GRAY);
            case '8': return style.withColor(ChatFormatting.DARK_GRAY);
            case '9': return style.withColor(ChatFormatting.BLUE);
            case 'a': return style.withColor(ChatFormatting.GREEN);
            case 'b': return style.withColor(ChatFormatting.AQUA);
            case 'c': return style.withColor(ChatFormatting.RED);
            case 'd': return style.withColor(ChatFormatting.LIGHT_PURPLE);
            case 'e': return style.withColor(ChatFormatting.YELLOW);
            case 'f': return style.withColor(ChatFormatting.WHITE);

            case 'k': return style.withObfuscated(true);
            case 'l': return style.withBold(true);
            case 'm': return style.withStrikethrough(true);
            case 'n': return style.withUnderlined(true);
            case 'o': return style.withItalic(true);

            case 'r': return Style.EMPTY;

            default: return style;
        }
    }
}

