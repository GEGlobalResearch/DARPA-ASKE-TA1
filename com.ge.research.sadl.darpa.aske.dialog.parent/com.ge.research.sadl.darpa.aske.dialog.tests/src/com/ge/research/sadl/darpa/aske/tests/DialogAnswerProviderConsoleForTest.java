package com.ge.research.sadl.darpa.aske.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.function.Consumer;

import com.ge.research.sadl.darpa.aske.processing.MixedInitiativeElement;
import com.ge.research.sadl.darpa.aske.processing.MixedInitiativeTextualResponse;

public class DialogAnswerProviderConsoleForTest {

	public String addCurationManagerInitiatedContent(String content) {
        Consumer<MixedInitiativeElement> respond = a -> this.provideResponse(a);
        MixedInitiativeTextualResponse question = new MixedInitiativeTextualResponse(content);
        MixedInitiativeElement questionElement = new MixedInitiativeElement(question, respond);
        initiateMixedInitiativeInteraction(questionElement);
		return "success";
	}
	
	public String initiateMixedInitiativeInteraction(MixedInitiativeElement element) {
		System.out.println("CM: " + element.getClass().toString());
		
//		long end = System.currentTimeMillis()+60*10;
//		InputStreamReader fileInputStream = new InputStreamReader(System.in);
		read(System.in, 10000, element);
//		BufferedReader bufferedReader = new BufferedReader(fileInputStream);
//		try {
//		    while ((System.currentTimeMillis() < end)) {
//		        if (bufferedReader.ready()) {
//		            String read = bufferedReader.readLine();
//		            System.out.println(read);
//		            
//		            // construct response
//		            MixedInitiativeElement response = new MixedInitiativeElement(read, null);
//		            response.setContent(new MixedInitiativeTextualResponse(read));
//		            // make call identified in element
//		            element.getRespondTo().accept(response);
//		        }
//		    }
//		} catch (IOException e) {
//		    e.printStackTrace();
//		} finally {
//		    try {
//		        if (bufferedReader != null) {
//		            bufferedReader.close();
//		        }
//		    } catch (IOException e) {
//		        e.printStackTrace();
//		    }
//		}		
		return "AtTime-" + System.currentTimeMillis();
	}
	
	public void provideResponse(MixedInitiativeElement response) {
		System.out.println("Response: " + response.toString());
	}
	
    /**
     * Non blocking read from input stream using controlled thread
     * 
     * @param is
     *            — InputStream to read
     * @param timeout
     *            — timeout, should not be less that 10
     * @return
     */
	
    // Holder for temporary store of read(InputStream is) value
    private static String threadValue = "";

    String read(final InputStream is, int timeout, MixedInitiativeElement element) {

        // Start reading bytes from stream in separate thread
        Thread thread = new Thread() {

            public void run() {
                byte[] buffer = new byte[1024]; // read buffer
                byte[] readBytes = new byte[0]; // holder of actually read bytes
                try {
                    Thread.sleep(5);
                    // Read available bytes from stream
                    int size = is.read(buffer);
                    if (size > 0)
                        readBytes = Arrays.copyOf(buffer, size);
                    // and save read value in static variable 
                    setValue(new String(readBytes, "UTF-8"));
		            System.out.println(getValue());
		            
		            // construct response
		            MixedInitiativeElement response = new MixedInitiativeElement(getValue(), null);
		            response.setContent(new MixedInitiativeTextualResponse(getValue()));
		            // make call identified in element
		            element.getRespondTo().accept(response);
                } catch (Exception e) {
                    System.err.println("Error reading input stream\nStack trace:\n" + e.getStackTrace());
                }
            }
        };
        thread.start(); // Start thread
        try {
            thread.join(timeout); // and join it with specified timeout
        } catch (InterruptedException e) {
            System.err.println("Data were note read in " + timeout + " ms");
        }
        return getValue();

    }

    private synchronized void setValue(String value) {
        threadValue = value;
    }

    private synchronized String getValue() {
        String tmp = new String(threadValue);
        setValue("");
        return tmp;
    }
}

