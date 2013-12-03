package jp.ac.ritsumei.scrambledegg.server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class MainScrambledEggServlet extends HttpServlet{
	
	/**
	 * defult serial version
	 */
	private static final long serialVersionUID = 1L;
	
	

	public void doGet(HttpServletRequest request, 
            HttpServletResponse response) 
                throws ServletException, IOException {

	    response.setContentType("text/html; charset=Shift_JIS");

	    /* HTML 出力用 PrintWriter */
	    PrintWriter out = response.getWriter();
	    
	    /* HTML出力 */
	    out.println("<html>");
	    out.println("<head>");
	    out.println("<title>Hello!</title>");
	    out.println("</head>"); 
	    out.println("<body>");
	    out.println("<H1>");
	    out.println("Hello!");
	    out.println("<H1>");
	    out.println("</body>");
	    out.println("</html>");
	    out.close();
		
	}
}
