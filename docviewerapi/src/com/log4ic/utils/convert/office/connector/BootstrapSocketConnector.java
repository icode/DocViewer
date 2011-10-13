package com.log4ic.utils.convert.office.connector;

import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.uno.XComponentContext;
import com.log4ic.utils.convert.office.connector.server.OOoServer;

/**
 * A Bootstrap Connector which uses a socket to connect to an OOo server.
 */
public class BootstrapSocketConnector extends BootstrapConnector {

    /**
     * Constructs a bootstrap socket connector which uses the folder of the OOo installation containing the soffice executable.
     * 
     * @param   oooExecFolder   The folder of the OOo installation containing the soffice executable
     */
    public BootstrapSocketConnector(String oooExecFolder) {

        super(oooExecFolder);
    }

    /**
     * Constructs a bootstrap socket connector which connects to the specified
     * OOo server.
     * 
     * @param   oooServer   The OOo server
     */
    public BootstrapSocketConnector(OOoServer oooServer) {

        super(oooServer);
    }

    /**
     * Connects to an OOo server using a default socket and returns a
     * component context for using the connection to the OOo server.
     * 
     * @return             The component context
     */
    public XComponentContext connect() throws BootstrapException {

        // create random pipe name
        String host = "localhost";
        int    port = 8100;

        return connect(host,port);
    }

    /**
     * Connects to an OOo server using the specified host and port for the
     * socket and returns a component context for using the connection to the
     * OOo server.
     * 
     * @param   host   The host
     * @param   port   The port
     * @return         The component context
     */
    public XComponentContext connect(String host, int port) throws BootstrapException {

        // host and port
        String hostAndPort = "host="+host+",port="+port;

        // accept option
        String oooAcceptOption = "-accept=socket,"+hostAndPort+";urp;";

        // connection string
        String unoConnectString = "uno:socket,"+hostAndPort+";urp;StarOffice.ComponentContext";

        return connect(oooAcceptOption, unoConnectString);
    }

    /**
     * Bootstraps a connection to an OOo server in the specified soffice
     * executable folder of the OOo installation using a default socket and
     * returns a component context for using the connection to the OOo server.
     * 
     * @param   oooExecFolder      The folder of the OOo installation containing the soffice executable
     * @return                     The component context
     */
    public static final XComponentContext bootstrap(String oooExecFolder) throws BootstrapException {

        BootstrapSocketConnector bootstrapSocketConnector = new BootstrapSocketConnector(oooExecFolder);
        return bootstrapSocketConnector.connect();
    }

    /**
     * Bootstraps a connection to an OOo server in the specified soffice
     * executable folder of the OOo installation using the specified host and
     * port for the socket and returns a component context for using the
     * connection to the OOo server.
     * 
     * @param   oooExecFolder      The folder of the OOo installation containing the soffice executable
     * @param   host               The host
     * @param   port               The port
     * @return                     The component context
     */
    public static final XComponentContext bootstrap(String oooExecFolder, String host, int port) throws BootstrapException {

        BootstrapSocketConnector bootstrapSocketConnector = new BootstrapSocketConnector(oooExecFolder);
        return bootstrapSocketConnector.connect(host,port);
    }
}