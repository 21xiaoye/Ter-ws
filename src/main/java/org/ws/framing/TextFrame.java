package org.ws.framing;

import org.ws.enums.OpCode;

public class TextFrame extends DataFrame{
    public TextFrame(){
        super(OpCode.TEXT);
    }
}
