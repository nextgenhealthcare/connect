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
            State currentState;
            State lastState = currentState = State.OBJECT_KEY;
            if (lineIndex != 0 && lineInfo[lineIndex - 1].obj != null) {
                lastState = currentState = (State) lineInfo[lineIndex - 1].obj;
            }

            switch (lastState) {
                case OBJECT_KEY:
                    insideObject = true;
                    isValue = false;
                    break;
                case OBJECT_VALUE:
                    insideObject = true;
                    isValue = true;
                    break;
                case ARRAY_ELEMENT:
                    insideObject = false;
                    isValue = true;
                    break;
            }

            for (int i = offset; i < length; i++) {
                int i1 = (i + 1);
                char c = array[i];

                if (c == '\\') {
                    backslash = !backslash;
                } else if (Character.isDigit(c) && token != Token.KEYWORD3) {
                    token = Token.DIGIT;
                }

                switch (token) {
                    case Token.NULL:
                        switch (c) {
                            case '"': // inside a String
                                if (insideObject && !isValue) {
                                    token = Token.KEYWORD1;
                                } else {
                                    token = Token.KEYWORD3;
                                }
                                addToken(i1 - lastOffset, token);
                                lastOffset = lastKeyword = i1;
                                break;
                            case '-': // inside a numeric value
                                token = Token.DIGIT;
                                addToken(i1 - lastOffset, token);
                                lastOffset = lastKeyword = i1;
                                break;
                            default:
                                backslash = false;
                                switch (c) {
                                    case '{':
                                        insideObject = true;
                                        isValue = false;
                                        lineInfo[lineIndex].obj = currentState = State.OBJECT_KEY;
                                        break;
                                    case '[':
                                        insideObject = false;
                                        isValue = true;
                                        lineInfo[lineIndex].obj = currentState = State.ARRAY_ELEMENT;
                                        break;
                                }
                                if (Character.isLetter(c)) {
                                    addToken(i - lastOffset, token);
                                    lastOffset = lastKeyword = i;
                                    token = Token.COMMENT1;
                                } else {
                                    addToken(i1 - lastOffset, token);
                                    lastOffset = lastKeyword = i1;
                                }
                        }
                        break;
                    case Token.KEYWORD1: // inside a key
                        addToken(i1 - lastOffset, token);
                        lastOffset = lastKeyword = i1;

                        if (c == '"') { // end key
                            if (backslash) {
                                backslash = false;
                            } else {
                                isValue = true;
                                lineInfo[lineIndex].obj = currentState = State.OBJECT_VALUE;
                                token = Token.NULL;
                            }
                        }
                        break;
                    case Token.KEYWORD3: // inside a value
                        addToken(i1 - lastOffset, token);
                        lastOffset = lastKeyword = i1;

                        if (c == '"') { // end String value
                            if (backslash) {
                                backslash = false;
                            } else {
                                if (insideObject) {
                                    lineInfo[lineIndex].obj = currentState = State.OBJECT_KEY;
                                    isValue = false;
                                }
                                token = Token.NULL;
                            }
                        }
                        break;
                    case Token.DIGIT: // inside a numeric value
                        if (!Character.isDigit(c) && c != '.' && c != 'E' && c != '+') { // end of value
                            token = Token.NULL;

                            if (insideObject) {
                                isValue = false;
                                lineInfo[lineIndex].obj = currentState = State.OBJECT_KEY;
                            }
                        }
                        addToken(i1 - lastOffset, token);
                        lastOffset = lastKeyword = i1;

                        break;
                    case Token.COMMENT1:
                        if (!Character.isLetter(c)) {
                            if (!doKeyword(line, i, c)) {
                                token = Token.NULL;
                                addToken(i1 - lastOffset, token);
                                lastOffset = lastKeyword = i1;

                                if (insideObject) {
                                    isValue = false;
                                    lineInfo[lineIndex].obj = currentState = State.OBJECT_KEY;
                                }
                            }
                        }
                }
            }

            if (lineInfo[lineIndex].obj == null) {
                lineInfo[lineIndex].obj = currentState;
            }

            if (token == Token.COMMENT1) {
                addToken(length - lastOffset, Token.NULL);
            }

            return token;
        }
    }

    private boolean doKeyword(Segment line, int i, char c) {
        int i1 = i + 1;
        int len = i - lastKeyword;
        byte id = keywords.lookup(line, lastKeyword, len);
        if (id != Token.NULL) {
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
    private boolean insideObject = true;
    private boolean isValue = false;

    private enum State {
        OBJECT_KEY, OBJECT_VALUE, ARRAY_ELEMENT;
    }
}