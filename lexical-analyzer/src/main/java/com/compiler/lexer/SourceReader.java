package com.compiler.lexer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class SourceReader {
    private final Reader reader;
    private int currentChar;
    private int line = 1;
    private int column = 0;
    private boolean wasNewLine = false;

    public SourceReader(String input) {
        this.reader = new StringReader(input);
        advance();
    }

    public void advance() {
        try {
            currentChar = reader.read();
            if (wasNewLine) {
                line++;
                column = 0;
                wasNewLine = false;
            }
            if (currentChar == '\n') {
                wasNewLine = true;
            } else {
                column++;
            }
        } catch (IOException e) {
            currentChar = -1;
        }
    }

    public char current() {
        return (char) currentChar;
    }

    public boolean hasNext() {
        return currentChar != -1;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            // Ignore
        }
    }
}