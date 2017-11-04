package systems.whitestar.Routes;

import org.jtwig.web.servlet.JtwigRenderer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * @author Tom Paulus
 * Created on 10/28/17.
 */
public class Mediasite extends HttpServlet {
    private static final String TEMPLATE_PATH = "/WEB-INF/templates/ms-mon.twig";
    private final JtwigRenderer renderer = JtwigRenderer.defaultRenderer();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        renderer.dispatcherFor(TEMPLATE_PATH)
                .render(request, response);
    }
}
