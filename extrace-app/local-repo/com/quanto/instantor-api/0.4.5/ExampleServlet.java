package com.mycompany.servlets;

import com.instantor.api.InstantorException;
import com.instantor.api.InstantorParams;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Example of Instantor API in {@link HttpServlet}.
 */
public class ExampleServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(ExampleServlet.class.getName());

    private static final String PARAMETER_SOURCE = "source";
    private static final String PARAMETER_MESSAGE_ID = "msg_id";
    private static final String PARAMETER_ACTION = "action";
    private static final String PARAMETER_ENCRYPTION = "encryption";
    private static final String PARAMETER_PAYLOAD = "payload";
    private static final String PARAMETER_TIMESTAMP = "timestamp";
    private static final String PARAMETER_HASH = "hash";

    /** The init parameter {@code apiKey}. */
    public static final String INIT_PARAM_API_KEY = "apiKey";
    
    private String apiKey;

    /**
     * Default constructor.
     */
    public ExampleServlet() {
        // containers typically use the Class.newInstance() method to load servlets
    }

    /**
     * Constructs a new instance with specified {@code apiKey}.
     *
     * @param apiKey
     *        The API key parameter
     */
    public ExampleServlet(String apiKey) {
        this.apiKey = apiKey;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void init() throws ServletException {
        super.init();
        if (apiKey == null)
            apiKey = getInitParameter(INIT_PARAM_API_KEY);
        if (apiKey == null || apiKey.isEmpty())
            throw new ServletException("Unspecified init parameter '" + INIT_PARAM_API_KEY + "'");
    }

    /**
     * Processes Instantor HTTP requests.
     *
     * @param request
     *        The servlet request
     * @param response
     *        The servlet response
     * @throws ServletException
     *         If a servlet-specific error occurs
     * @throws IOException
     *         If an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pS = request.getParameter(PARAMETER_SOURCE);
        String pM = request.getParameter(PARAMETER_MESSAGE_ID);
        String pA = request.getParameter(PARAMETER_ACTION);
        String pE = request.getParameter(PARAMETER_ENCRYPTION);
        String pP = request.getParameter(PARAMETER_PAYLOAD);
        String pT = request.getParameter(PARAMETER_TIMESTAMP);
        String pH = request.getParameter(PARAMETER_HASH);

        try(PrintWriter pw = response.getWriter()) {
            String payload = InstantorParams.loadResponse(pS,
                                                          apiKey,
                                                          pM,
                                                          pA,
                                                          pE,
                                                          pP,
                                                          pT,
                                                          pH);
            handlePayload(payload);

            response.setContentType("text/html;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);

            pw.println("OK: " + pM);
            pw.flush();

        } catch (InstantorException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    /**
     * Handles the decrypted request payload.
     *
     * @param payload
     *        The decrypted request payload
     * @throws ServletException
     *         If a servlet-specific error occurs
     * @throws IOException
     *         If an I/O error occurs
     */
    protected void handlePayload(String payload) throws ServletException, IOException {
        // TODO: implement
    }


    /**
     * Handles the HTTP {@code POST} method.
     *
     * @param request
     *        The servlet request
     * @param response
     *        The servlet response
     * @throws ServletException
     *         If a servlet-specific error occurs
     * @throws IOException
     *         If an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

}

