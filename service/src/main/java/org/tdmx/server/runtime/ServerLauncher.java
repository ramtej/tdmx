package org.tdmx.server.runtime;

import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;


public class ServerLauncher {
	
    public static void main(String[] args) throws Exception {
        String javaVersion = System.getProperty("java.version");

        StringTokenizer tokens = new StringTokenizer(javaVersion, ".-_");

        int majorVersion = Integer.parseInt(tokens.nextToken());
        int minorVersion = Integer.parseInt(tokens.nextToken());

        if (majorVersion < 2) {
            if (minorVersion < 7) {
                System.err.println("TDMX-Server requires Java 7 or later.");
                System.err.println("Your java version is " + javaVersion);
                System.err.println("Java Home:  " + System.getProperty("java.home"));
                System.exit(0);
            }
        }

        /*
        SSlServerCheck check = new SSlServerCheck();
        check.checkSSLCapabilities();
        
    	ApplicationContext context =
    		    new ClassPathXmlApplicationContext(new String[] {"server-context.xml"});
    	System.out.println(context);
   		*/
        
        BeanFactoryLocator beanFactoryLocator = ContextSingletonBeanFactoryLocator.getInstance();
        BeanFactoryReference beanFactoryReference = beanFactoryLocator.useBeanFactory("applicationContext");
        ApplicationContext context = (ApplicationContext)beanFactoryReference.getFactory();
        System.out.println(context);

        ServerContainer sc = (ServerContainer)context.getBean("serverContainer");
    	sc.startJetty();
    	
    	Thread.sleep(10000);
    	
    }

    private static class SSlServerCheck {
      public void checkSSLCapabilities() {
        int port = 8888;

        try {
          System.out.println("Locating server socket factory for SSL...");
          SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

          System.out.println("Creating a server socket on port " + port);
          SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(port);

          String[] suites = serverSocket.getSupportedCipherSuites();
          System.out.println("Support cipher suites are:");
          for (int i = 0; i < suites.length; i++) {
            System.out.println(suites[i]);
          }
          serverSocket.setEnabledCipherSuites(suites);

          System.out.println("Support protocols are:");
          String[] protocols = serverSocket.getSupportedProtocols();
          for (int i = 0; i < protocols.length; i++) {
            System.out.println(protocols[i]);
          }
          /*
          System.out.println("Waiting for client...");
          SSLSocket socket = (SSLSocket) serverSocket.accept();

          System.out.println("Starting handshake...");
          socket.startHandshake();

          System.out.println("Just connected to " + socket.getRemoteSocketAddress());
          */
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    
}
