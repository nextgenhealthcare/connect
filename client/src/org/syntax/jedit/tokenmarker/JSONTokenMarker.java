package org.syntax.jedit.tokenmarker;

import java.util.ArrayDeque;
import java.util.Deque;

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
            Deque<JSONTokenState> lastInfo = new ArrayDeque<JSONTokenState>();

            // Get information from the previous line
            if (lineIndex != 0 && lineInfo[lineIndex - 1].obj != null) {
                lastInfo = (ArrayDeque<JSONTokenState>) lineInfo[lineIndex - 1].obj;
            }

            // The states determine whether the current token is within an object or an array (at the lowest level).
            Deque<JSONTokenState> states = new ArrayDeque(lastInfo);

            for (int i = offset; i < length; i++) {
                int i1 = (i + 1);
                char c = array[i];

                if (c == '\\') {
                    backslash = !backslash;
                } else if (token != Token.KEYWORD1 && token != Token.KEYWORD3 && Character.isDigit(c)) {
                    token = Token.DIGIT;
                }

                switch (token) {
                    case Token.NULL:
                        switch (c) {
                            case '"': // inside a String
                                //If the state we peek is null we will start marking as there are no current states
                                JSONTokenState peek = states.peek();
                                if (peek != null) {
                                    if (states.peek().equals(JSONTokenState.OBJECT_KEY)) {
                                        token = Token.KEYWORD1; // an object key
                                    } else {
                                        token = Token.KEYWORD3; // an object or array value
                                    }
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
                                        resetState(states);
                                        states.push(JSONTokenState.OBJECT_KEY);
                                        break;
                                    case '[':
                                        resetState(states);
                                        states.push(JSONTokenState.ARRAY);
                                        break;
                                    case '}':
                                        if (states.peek() != null) {
                                            states.pop();
                                        }
                                        break;
                                    case ']':
                                        if (states.peek() != null) {
                                            states.pop();
                                        }
                                        resetState(states);
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
                                if (states.peek() != null) {
                                    states.pop();
                                }
                                states.push(JSONTokenState.OBJECT_VALUE);
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
                                resetState(states);
                                token = Token.NULL;
                            }
                        }
                        break;
                    case Token.DIGIT: // inside a numeric value
                        if (!Character.isDigit(c) && c != '.' && c != 'E' && c != '+') { // end of value
                            token = Token.NULL;

                            resetState(states);
                        }
                        addToken(i1 - lastOffset, token);
                        lastOffset = lastKeyword = i1;

                        break;
                    case Token.COMMENT1:
                        if (!Character.isLetter(c)) {
                            resetState(states);
                            if (c == '}' || c == ']') {
                                if (states.peek() != null) {
                                    states.pop();
                                }
                            }
                            if (!doKeyword(line, i, c)) {
                                token = Token.NULL;
                                addToken(i1 - lastOffset, token);
                                lastOffset = lastKeyword = i1;
                            }
                        }
                }
            }

            // Set the information for this line
            lineInfo[lineIndex].obj = states;

            if (token == Token.COMMENT1) {
                doKeyword(line, length, '\0');
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

    // Reset token state from object value to object key
    private void resetState(Deque<JSONTokenState> states) {
        JSONTokenState state = states.peek();
        if (state != null && state.equals(JSONTokenState.OBJECT_VALUE)) {
            states.pop();
            states.push(JSONTokenState.OBJECT_KEY);
        }
    }

    private KeywordMap keywords;

    private int lastOffset;
    private int lastKeyword;

    private enum JSONTokenState {
        OBJECT_KEY, OBJECT_VALUE, ARRAY;
    }
}