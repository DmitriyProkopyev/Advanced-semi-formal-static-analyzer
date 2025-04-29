package org.SNA;


/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        PipelineLLM pipe = new PipelineLLM();
        pipe.sendPrompt("3");
    }
}
