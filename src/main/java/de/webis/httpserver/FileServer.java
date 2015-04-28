package de.webis.httpserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A simple filer server implementation using Sockets. For the assignment,
 * implement the functions in {@link RequestHandler}.
 * Test your implementation with telnet or netcat, and with your web browser.
 * 
 * @author tim.gollub@uni-weimar.de
 * @author michael.voelske@uni-weimar.de
 * @author jan.grassegger@uni-weimar.de
 */
public class FileServer {
	private final File documentRoot;
    private final int port, maxConnections;


    public FileServer(String documentRoot) throws IOException {
        this(documentRoot, 4242, 100);
    }

    public FileServer(String rootPath, int port, int maxConnections)
			throws IOException {
		// create document root.
		this.documentRoot = new File(rootPath);
        this.port = port;
        this.maxConnections = maxConnections;

		if (!(documentRoot.exists() && documentRoot.isDirectory())) {
			throw new FileNotFoundException("Document root does not exist or is not a directory.");
		}
	}

    public void start() throws IOException {
        // start passive socket.
        ServerSocket passiveSocket = new ServerSocket(port, maxConnections);
        System.out.printf("Server started on port %d.%n", port);

        while (true) {
            // wait for a connection.
            Socket activeSocket = passiveSocket.accept();

            // start thread that handles incoming requests.
            RequestHandler requestHandler = new RequestHandler(activeSocket, documentRoot);
            Thread newThread = new Thread(requestHandler);
            newThread.start();
        }
    }

    public static void main(String[] args) throws IOException {
        //QUIZ: Wie geht's einfacher/kürzer?
        String documentRoot;
        if (args.length > 0) documentRoot = args[0];
        else documentRoot = ".";
        System.out.print(documentRoot+"\n");
        //END QUIZ

        FileServer server =  new FileServer(documentRoot);

        server.start();

    }
}
