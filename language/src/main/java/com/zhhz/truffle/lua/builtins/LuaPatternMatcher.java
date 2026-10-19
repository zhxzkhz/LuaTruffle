package com.zhhz.truffle.lua.builtins;

import com.zhhz.truffle.lua.LuaException;

import java.util.Arrays;

/** Byte-oriented implementation of Lua 5.x string patterns. */
final class LuaPatternMatcher {

    private static final int MAX_CAPTURES = 32;
    private static final int MAX_DEPTH = 200;
    private static final int CAP_UNFINISHED = -1;
    private static final int CAP_POSITION = -2;
    private static final int ESCAPE = '%';

    private final byte[] source;
    private final byte[] pattern;
    private final Capture[] captures = new Capture[MAX_CAPTURES];
    private int level;
    private int depth;

    LuaPatternMatcher(byte[] source, byte[] pattern) {
        this.source = source;
        this.pattern = pattern;
        for (int i = 0; i < captures.length; i++) {
            captures[i] = new Capture();
        }
    }

    MatchResult find(int initialPosition) {
        boolean anchored = pattern.length > 0 && unsigned(pattern[0]) == '^';
        int patternStart = anchored ? 1 : 0;
        int start = Math.max(0, Math.min(initialPosition, source.length));
        for (int position = start; position <= source.length; position++) {
            reset();
            int end = match(position, patternStart);
            if (end >= 0) {
                return snapshot(position, end);
            }
            if (anchored) {
                break;
            }
        }
        return null;
    }

    MatchResult matchAt(int position) {
        if (position < 0 || position > source.length) {
            return null;
        }
        reset();
        int end = match(position, 0);
        return end < 0 ? null : snapshot(position, end);
    }

    private void reset() {
        level = 0;
        depth = MAX_DEPTH;
    }

    private MatchResult snapshot(int start, int end) {
        Capture[] result = new Capture[level];
        for (int i = 0; i < level; i++) {
            result[i] = new Capture(captures[i].start, captures[i].length);
        }
        return new MatchResult(source, start, end, result);
    }

    private int match(int sourcePosition, int patternPosition) {
        if (depth-- == 0) {
            throw patternError("pattern too complex");
        }
        try {
            int s = sourcePosition;
            int p = patternPosition;
            while (p < pattern.length) {
                int token = unsigned(pattern[p]);
                if (token == '(') {
                    boolean positionCapture = p + 1 < pattern.length && unsigned(pattern[p + 1]) == ')';
                    return startCapture(s, positionCapture ? p + 2 : p + 1,
                            positionCapture ? CAP_POSITION : CAP_UNFINISHED);
                }
                if (token == ')') {
                    return endCapture(s, p + 1);
                }
                if (token == '$' && p + 1 == pattern.length) {
                    return s == source.length ? s : -1;
                }
                if (token == ESCAPE && p + 1 < pattern.length) {
                    int escaped = unsigned(pattern[p + 1]);
                    if (escaped == 'b') {
                        int balancedEnd = matchBalance(s, p + 2);
                        if (balancedEnd < 0) {
                            return -1;
                        }
                        s = balancedEnd;
                        p += 4;
                        continue;
                    }
                    if (escaped == 'f') {
                        int setStart = p + 2;
                        if (setStart >= pattern.length || unsigned(pattern[setStart]) != '[') {
                            throw patternError("missing '[' after '%f' in pattern");
                        }
                        int setEnd = classEnd(setStart);
                        int previous = s == 0 ? 0 : unsigned(source[s - 1]);
                        int current = s == source.length ? 0 : unsigned(source[s]);
                        if (!matchBracketClass(previous, setStart, setEnd - 1)
                                && matchBracketClass(current, setStart, setEnd - 1)) {
                            p = setEnd;
                            continue;
                        }
                        return -1;
                    }
                    if (escaped >= '0' && escaped <= '9') {
                        int captureEnd = matchCapture(s, escaped);
                        if (captureEnd < 0) {
                            return -1;
                        }
                        s = captureEnd;
                        p += 2;
                        continue;
                    }
                }

                int itemEnd = classEnd(p);
                boolean matched = singleMatch(s, p, itemEnd);
                int suffix = itemEnd < pattern.length ? unsigned(pattern[itemEnd]) : -1;
                if (!matched) {
                    if (suffix == '*' || suffix == '?' || suffix == '-') {
                        p = itemEnd + 1;
                        continue;
                    }
                    return -1;
                }

                if (suffix == '?') {
                    int result = match(s + 1, itemEnd + 1);
                    if (result >= 0) {
                        return result;
                    }
                    p = itemEnd + 1;
                    continue;
                }
                if (suffix == '+') {
                    s++;
                    return maxExpand(s, p, itemEnd);
                }
                if (suffix == '*') {
                    return maxExpand(s, p, itemEnd);
                }
                if (suffix == '-') {
                    return minExpand(s, p, itemEnd);
                }

                s++;
                p = itemEnd;
            }
            return s;
        } finally {
            depth++;
        }
    }

    private int classEnd(int position) {
        if (position >= pattern.length) {
            return position;
        }
        int token = unsigned(pattern[position++]);
        if (token == ESCAPE) {
            if (position == pattern.length) {
                throw patternError("malformed pattern (ends with '%')");
            }
            return position + 1;
        }
        if (token != '[') {
            return position;
        }
        if (position < pattern.length && unsigned(pattern[position]) == '^') {
            position++;
        }
        // 与 lstrlib.c classEnd 一致：']' 若出现在开头的第一个位置则是普通字符。
        do {
            if (position >= pattern.length) {
                throw patternError("malformed pattern (missing ']')");
            }
            int current = unsigned(pattern[position++]);
            if (current == ESCAPE && position < pattern.length) {
                position++;
            }
        } while (position < pattern.length && unsigned(pattern[position]) != ']');
        if (position >= pattern.length) {
            throw patternError("malformed pattern (missing ']')");
        }
        return position + 1;
    }

    private boolean singleMatch(int sourcePosition, int patternPosition, int itemEnd) {
        if (sourcePosition >= source.length) {
            return false;
        }
        int value = unsigned(source[sourcePosition]);
        return switch (unsigned(pattern[patternPosition])) {
            case '.' -> true;
            case ESCAPE -> matchClass(value, unsigned(pattern[patternPosition + 1]));
            case '[' -> matchBracketClass(value, patternPosition, itemEnd - 1);
            default -> value == unsigned(pattern[patternPosition]);
        };
    }

    private boolean matchBracketClass(int value, int start, int end) {
        boolean positive = true;
        int position = start + 1;
        if (position < end && unsigned(pattern[position]) == '^') {
            positive = false;
            position++;
        }
        while (position < end) {
            int current = unsigned(pattern[position]);
            if (current == ESCAPE && position + 1 < end) {
                position++;
                if (matchClass(value, unsigned(pattern[position]))) {
                    return positive;
                }
            } else if (position + 2 < end && unsigned(pattern[position + 1]) == '-') {
                int rangeEnd = unsigned(pattern[position + 2]);
                if (current <= value && value <= rangeEnd) {
                    return positive;
                }
                position += 2;
            } else if (current == value) {
                return positive;
            }
            position++;
        }
        return !positive;
    }

    private static boolean matchClass(int value, int className) {
        int lower = asciiLower(className);
        boolean recognized = lower == 'a' || lower == 'c' || lower == 'd' || lower == 'g'
                || lower == 'l' || lower == 'p' || lower == 's' || lower == 'u'
                || lower == 'w' || lower == 'x' || lower == 'z';
        if (!recognized) {
            return className == value;
        }
        boolean result = switch (lower) {
            case 'a' -> isAlpha(value);
            case 'c' -> value < 32 || value == 127;
            case 'd' -> value >= '0' && value <= '9';
            case 'g' -> value >= 33 && value <= 126;
            case 'l' -> value >= 'a' && value <= 'z';
            case 'p' -> value >= 33 && value <= 126 && !isAlphaNumeric(value);
            case 's' -> value == ' ' || value == '\t' || value == '\n' || value == '\r' || value == '\f' || value == 11;
            case 'u' -> value >= 'A' && value <= 'Z';
            case 'w' -> isAlphaNumeric(value);
            case 'x' -> value >= '0' && value <= '9' || value >= 'a' && value <= 'f' || value >= 'A' && value <= 'F';
            case 'z' -> value == 0;
            default -> false;
        };
        return (className >= 'a' && className <= 'z') == result;
    }

    private int matchBalance(int sourcePosition, int patternPosition) {
        if (patternPosition + 1 >= pattern.length) {
            throw patternError("malformed pattern (missing arguments to '%b')");
        }
        if (sourcePosition >= source.length || source[sourcePosition] != pattern[patternPosition]) {
            return -1;
        }
        int open = unsigned(pattern[patternPosition]);
        int close = unsigned(pattern[patternPosition + 1]);
        int depth = 1;
        for (int position = sourcePosition + 1; position < source.length; position++) {
            int value = unsigned(source[position]);
            if (value == close && --depth == 0) {
                return position + 1;
            }
            if (value == open) {
                depth++;
            }
        }
        return -1;
    }

    private int maxExpand(int sourcePosition, int patternPosition, int itemEnd) {
        int count = 0;
        while (singleMatch(sourcePosition + count, patternPosition, itemEnd)) {
            count++;
        }
        for (int amount = count; amount >= 0; amount--) {
            int result = match(sourcePosition + amount, itemEnd + 1);
            if (result >= 0) {
                return result;
            }
        }
        return -1;
    }

    private int minExpand(int sourcePosition, int patternPosition, int itemEnd) {
        int current = sourcePosition;
        while (true) {
            int result = match(current, itemEnd + 1);
            if (result >= 0) {
                return result;
            }
            if (!singleMatch(current, patternPosition, itemEnd)) {
                return -1;
            }
            current++;
        }
    }

    private int startCapture(int sourcePosition, int patternPosition, int captureType) {
        if (level >= MAX_CAPTURES) {
            throw patternError("too many captures");
        }
        int capture = level++;
        captures[capture].start = sourcePosition;
        captures[capture].length = captureType;
        int result = match(sourcePosition, patternPosition);
        if (result < 0) {
            level--;
        }
        return result;
    }

    private int endCapture(int sourcePosition, int patternPosition) {
        int capture = captureToClose();
        captures[capture].length = sourcePosition - captures[capture].start;
        int result = match(sourcePosition, patternPosition);
        if (result < 0) {
            captures[capture].length = CAP_UNFINISHED;
        }
        return result;
    }

    private int matchCapture(int sourcePosition, int captureName) {
        int capture = checkCapture(captureName);
        int length = captures[capture].length;
        if (sourcePosition + length > source.length) {
            return -1;
        }
        for (int index = 0; index < length; index++) {
            if (source[captures[capture].start + index] != source[sourcePosition + index]) {
                return -1;
            }
        }
        return sourcePosition + length;
    }

    private int checkCapture(int captureName) {
        int capture = captureName - '1';
        if (capture < 0 || capture >= level || captures[capture].length == CAP_UNFINISHED) {
            throw patternError("invalid capture index %" + (char) captureName);
        }
        return capture;
    }

    private int captureToClose() {
        for (int capture = level - 1; capture >= 0; capture--) {
            if (captures[capture].length == CAP_UNFINISHED) {
                return capture;
            }
        }
        throw patternError("invalid pattern capture");
    }

    private static int unsigned(byte value) {
        return value & 0xff;
    }

    private static int asciiLower(int value) {
        return value >= 'A' && value <= 'Z' ? value + ('a' - 'A') : value;
    }

    private static boolean isAlpha(int value) {
        return value >= 'a' && value <= 'z' || value >= 'A' && value <= 'Z';
    }

    private static boolean isAlphaNumeric(int value) {
        return isAlpha(value) || value >= '0' && value <= '9';
    }

    private static RuntimeException patternError(String message) {
        return (RuntimeException) LuaException.create(message, null);
    }

    private static final class Capture {
        private int start;
        private int length;

        private Capture() {
        }

        private Capture(int start, int length) {
            this.start = start;
            this.length = length;
        }
    }

    static final class MatchResult {
        private final byte[] source;
        private final int start;
        private final int end;
        private final Capture[] captures;

        private MatchResult(byte[] source, int start, int end, Capture[] captures) {
            this.source = source;
            this.start = start;
            this.end = end;
            this.captures = captures;
        }

        int start() {
            return start;
        }

        int end() {
            return end;
        }

        int captureCount() {
            return captures.length;
        }

        Object[] capturesOrWholeMatch() {
            if (captures.length == 0) {
                return new Object[]{LuaStringSupport.fromBytes(source, start, end - start)};
            }
            Object[] values = new Object[captures.length];
            for (int i = 0; i < captures.length; i++) {
                Capture capture = captures[i];
                if (capture.length == CAP_UNFINISHED) {
                    throw patternError("unfinished capture");
                }
                values[i] = capture.length == CAP_POSITION
                        ? (long) capture.start + 1
                        : LuaStringSupport.fromBytes(source, capture.start, capture.length);
            }
            return values;
        }

        byte[] wholeMatch() {
            return Arrays.copyOfRange(source, start, end);
        }
    }
}
