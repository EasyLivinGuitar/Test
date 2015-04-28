package de.webis.httpserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

/**
 * @author jan.grassegger@uni-weimar.de
 * @author clement.welsch@uni-weimar.de
 */
public class RequestHandler implements Runnable {

    private final Socket socket;
    private final File documentRoot;
    private OutputStream outStream;

    public RequestHandler(Socket socket, File documentRoot) {
        this.socket = socket;
        this.documentRoot = documentRoot;
    }

    /**
     * Called first in Runnable.
     */
    @Override
    public void run() {
        try {

            //Get client hostname via InetAddress (used in the introduction tutorial code example)
            String clientHost = socket.getInetAddress().getHostName();

            //initialize Reader and Writer
            //readers and writers allow simpler handling of input and output streams

            //BufferedReader is faster than simple stream readers and implements readLine
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // waits until next line reaches the stream
            // (like keyboard input in the introduction tutorial code example)
            String request = reader.readLine();
            System.out.printf("%s: %s%n", clientHost, request);

            // server sends response
            try (OutputStream out = socket.getOutputStream()) {
                outStream = out;
                if (isValid(request)) {
                    // process request.
                    // Your code starts here:

                    // handle valid requests:
                    // TODO implement isValid (check header)
                    File outputFile=new File(documentRoot, request.split(" ")[1]);
                    // TODO build response
                    // - check if file/path exists
                    if(outputFile.exists())
                    {
                        statusLine(200, "Okay");
                        endHeader();

                        if(outputFile.isDirectory())
                        {
                            listDir(outputFile);
                        }
                        else if (outputFile.isFile())
                        {
                            sendFile(outputFile);
                        }
                    }
                    else
                    {
                        statusLine(404, "Not found!");
                        endHeader();
                    }
                    // - check if path points to file or folder
                    // - call listDir for directory listings
                    // - call sendFile to serve files
                    // - set appropriate status lines and headers

                    // HINTS
                    // serve files relative do documentRoot
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        // always executed after try block. Even if try block fails.
        finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Check the validity of the given request. You should only consider
     * HTTP-1.1 GET requests. Write appropriate status line if not valid.
     *
     * @param request Given client request
     * @return <true> if request is valid, <false> otherwise.
     */
    private boolean isValid(String request) throws IOException {

        // TODO implementation of isValid

        String[] requestToken=request.split(" ");
        //GET phathsskk HTTP/1.1

        // - is it a GET request?
        if(!requestToken[0].equals("GET"))
        {
            statusLine(405,"Method not allowed");
            return false;
        }

        // - do I have a path?

        // - is it the right protocol and version?
        else if(!requestToken[2].equals("HTTP/1.1"))
        {
            statusLine(505, "HTTP version not supported");
            return false;
        }

        // - What are the appropriated response codes?

        // TODO check path
        // - don't allow paths above documentRoot

        // HINTS
        // don't allow paths above documentRoot (think twice)
        // watch out for paths starting with '/'


        // for example: catch all error
        else
        {
            //statusLine(200, "Okay");
            endHeader();
            return true;
        }
    }

    private void sendFile(File file) throws IOException {
        Files.copy(file.toPath(), outStream);
    }

    private void listDir(File dir) throws IOException {
        StringBuilder buffer = new StringBuilder();
        buffer.append(String.format("listing %s%n", dir));
        File[] files = dir.listFiles();

        // dir.listFiles might return null. So we have to check it.
        // Returning null is very bad style.
        // !!!_Always_ throw exceptions instead of returning null!!! (in Java)
        if (files != null) {
            buffer.append(String.format("total %d%n", files.length));
            for (File file : files) {
                // for one-liners blocks are not necessary
                if (file.isDirectory()) buffer.append(String.format("%s  <DIR>%n", file.getName()));
                else buffer.append(String.format("%s%n", file.getName()));
            }
        }
        outStream.write(buffer.toString().getBytes());
    }

    /**
     * Write the HTTP status line
     */
    private void statusLine(int status, String message) throws IOException {
        outStream.write(String.format("HTTP/1.1 %d %s\r\n", status, message).getBytes());
    }

    /**
     * Write a HTTP header key-value-pair
     */
    private void headerLine(String name, String value) throws IOException {
        outStream.write(String.format("%s: %s%n", name, value).getBytes());
    }

    /**
     * Finalize the header with a CRLF
     */
    private void endHeader() throws IOException {
        outStream.write("\r\n".getBytes());
    }


}
