package com.liferay.util;

import org.apache.xerces.util.XMLChar;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 * @author Jelmer Kuperus
 */
public class InvalidXmlFilterReader extends FilterReader {

    public InvalidXmlFilterReader(Reader in) {
        this(in, DEFAULT_REPLACEMENT_CHARACTER);
    }

    public InvalidXmlFilterReader(Reader in, char replacementCharacter) {
        super(in);
        this.replacementCharacter = replacementCharacter;
    }

    @Override
    public int read() throws IOException {
        int result = super.read();
        
        if (XMLChar.isInvalid(result)) {
            result = replacementCharacter;
        }
        
        return result;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int result =  super.read(cbuf, off, len);

        for (int i = 0; i < len; i++) {
            if (XMLChar.isInvalid(cbuf[off + i])) {
                cbuf[off + i] = replacementCharacter;
            }
        }
        return result;
    }
    
    private static final char DEFAULT_REPLACEMENT_CHARACTER = '?';
    private char replacementCharacter;

}