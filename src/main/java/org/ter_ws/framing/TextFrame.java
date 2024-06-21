package org.ter_ws.framing;

import org.ter_ws.enums.OpCode;
import org.ter_ws.exceptions.InvalidDataException;

public class TextFrame extends DataFrame{
    public TextFrame() {
        super(OpCode.TEXT);
    }

    @Override
    public void isValid() throws InvalidDataException {

    }
}
