package org.syntax.jedit.tokenmarker;

import javax.swing.text.Segment;

import org.syntax.jedit.KeywordMap;

public class JSONTokenMarker extends TokenMarker {

    public JSONTokenMarker() {
        getKeywords();
    }

    public void getKeywords() {
        if (keywords == null) {
            keywords = new KeywordMap(false);
            keywords.add("null", Token.LABEL);
            keywords.add("true", Token.LABEL);
            keywords.add("false", Token.LABEL);
        }
    }

    @Override
    protected byte markTokensImpl(byte token, Segment line, int lineIndex) {
        {
            char[] array = line.array;
            int offset = line.offset;
            lastOffset = offset;
            lastKeyword = offset;
            int length = line.count + offset;
            boolean backslash = false;

            for (int i = offset; i < length; i++) {
                int i1 = (i + 1);
                char c = array[i];

                if (c == '\\') {
                    backslash = !backslash;
                } else {
                    switch (token) {
                        case Token.NULL:
                            switch (c) {
                                case '"': // inside a key
                                    addToken(i - lastOffset, token);
                                    token = Token.KEYWORD1;
                                    lastOffset = lastKeyword = i;
                                    addToken(i1 - lastOffset, token);
                                    lastOffset = lastKeyword = i1;
                                    break;
                                case '[': // inside an array
                                    arrLevel++;
                                case ':': // inside a value
                                    addToken(i1 - lastOffset, token);
                                    token = Token.KEYWORD2;
                                    lastOffset = lastKeyword = i1;
                                    break;
                                case '}': // end of object
                                    insideObject = false;
                                default:
                                    addToken(i1 - lastOffset, token);
                                    lastOffset = lastKeyword = i1;
                                    break;
                            }
                            break;
                        case Token.KEYWORD1: // inside a key
                            addToken(i1 - lastOffset, token);
                            lastOffset = lastKeyword = i1;

                            if (c == '"') {
                                if (backslash) {
                                    backslash = false;
                                } else {
                                    token = Token.NULL;
                                }
                            }
                            break;
                        case Token.KEYWORD2: // inside a value
                            if (c == '{') { // inside an object
                                token = Token.NULL;
                                addToken(i1 - lastOffset, token);
                                lastOffset = lastKeyword = i1;
                                insideObject = true;
                            } else if (c == '[') { // inside an array
                                addToken(i1 - lastOffset, Token.NULL);
                                lastOffset = lastKeyword = i1;
                                arrLevel++;
                            } else if (c == '"') { // inside a String value
                                if (backslash) {
                                    backslash = false;
                                } else {
                                    token = Token.KEYWORD3;
                                    addToken(i1 - lastOffset, token);
                                    lastOffset = lastKeyword = i1;
                                }
                            } else if (c == '-' || Character.isDigit(c)) { // inside a numeric value
                                addToken(i - lastOffset, token);
                                lastOffset = lastKeyword = i;
                                token = Token.DIGIT;
                                addToken(i1 - lastOffset, token);
                                lastOffset = lastKeyword = i1;
                            } else if (Character.isLetter(c)) { // inside an alphabetic value, possibly a keyword
                                addToken(i - lastOffset, Token.NULL);
                                lastOffset = lastKeyword = i;
//                                doKeyword(line, i, c);
                                addToken(i1 - lastOffset, Token.NULL);
                                lastOffset = lastKeyword = i1;
                                token = Token.OPERATOR;
                            } else {
                                if (c != ' ') { // ignore leading spaces of values
                                    token = Token.NULL;
                                }
                                addToken(i1 - lastOffset, Token.NULL);
                                lastOffset = lastKeyword = i1;
                            }
                            break;
                        case Token.KEYWORD3: // inside a String value
                            addToken(i1 - lastOffset, token);
                            lastOffset = lastKeyword = i1;
                            if (c == '"') {
                                if (backslash) {
                                    backslash = false;
                                } else {
                                    token = Token.NULL;
                                }
                            }
                            break;
                        case Token.DIGIT: // inside a numeric value
                            if (!Character.isDigit(c) && c != '.') { // end of value
                                token = Token.NULL;
                            }
                            addToken(i1 - lastOffset, token);
                            lastOffset = lastKeyword = i1;
                            break;
                        case Token.OPERATOR: // inside a keyword value, possibly
//                            doKeyword(line, i, c);
                            addToken(i1 - lastOffset, Token.NULL);
                            lastOffset = i1;
                            if (!Character.isLetter(c)) { // end of value
                                token = Token.NULL;
                            }
                            break;
                    }
                }
                
//                addToken(length - lastOffset, token);

                if (token == Token.NULL) {
                    // If within an array and item isn't an object, the following characters are values
                    if (arrLevel > 0 && !insideObject) {
                        token = Token.KEYWORD2;
                    }

                    if (c == ']') { // end of an array
                        arrLevel--;
                    } else if (c == '}') { // end of an object
                        insideObject = false;
                    }
                }
            }

            return token;
        }
    }

    private boolean doKeyword(Segment line, int i, char c) {
        int i1 = i + 1;
        int len = i - lastKeyword;
        byte id = keywords.lookup(line, lastKeyword, len);
        if (id != Token.NULL) {
            System.out.println("the id!: " + id);
            if (lastKeyword != lastOffset) {
                addToken(lastKeyword - lastOffset, Token.NULL);
            }
            addToken(len, id);
            lastOffset = i;
        }
        lastKeyword = i1;
        return false;
    }

    private KeywordMap keywords;

    private int lastOffset;
    private int lastKeyword;
    private int arrLevel = 0; // how many levels deep the current value is contained in an array
    private boolean insideObject = false;
}