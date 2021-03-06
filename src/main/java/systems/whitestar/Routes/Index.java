package systems.whitestar.Routes;

import com.google.common.hash.Hashing;
import org.jtwig.resource.exceptions.ResourceNotFoundException;
import org.jtwig.web.servlet.JtwigRenderer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;


/**
 * @author Tom Paulus
 * Created on 10/28/17.
 */
public class Index extends HttpServlet {
    private static final String TEMPLATE_PATH = "/WEB-INF/templates%s.twig";
    private static final String INDEX = "/index";
    private static final int RANDOM_MINIMUM = 1;
    private static final int RANDOM_MAXIMUM = 15;
    private final JtwigRenderer renderer = JtwigRenderer.defaultRenderer();

    /**
     * Calculate the sum of an int array.
     *
     * @param a Array
     * @return Sum
     */
    private static int sum(int[] a) {
        int sum = 0;
        for (int i : a) {
            sum += i;
        }
        return sum;
    }

    private static int randomInt() {
        return Math.abs(ThreadLocalRandom.current().nextInt(RANDOM_MINIMUM, RANDOM_MAXIMUM + 1));
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int randomInt[] = {randomInt(), randomInt()};

        request.setAttribute("contact_verification_number1", randomInt[0]);
        request.setAttribute("contact_verification_number2", randomInt[1]);
        request.setAttribute("contact_verification_nonce", Hashing.sha256()
                .hashString(Integer.toString(sum(randomInt)), StandardCharsets.UTF_8)
                .toString());

        String requestedPage = request.getRequestURI();
        requestedPage = !requestedPage.equals("/") ? requestedPage: INDEX; // Index page if is Blank

        try {
            renderer.dispatcherFor(String.format(TEMPLATE_PATH, requestedPage))
                    .render(request, response);
        } catch (ResourceNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
