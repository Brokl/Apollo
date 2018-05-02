package rpc;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;
import external.TicketMasterAPI;

/**
 * Servlet implementation class SearchItem
 */

// "search" mapping to URL(endpoint) , it is a annotation. you can modify it.
// it is the same as class name by default
@WebServlet("/search")
public class SearchItem extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchItem() {
        //super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//6 real case from TicketMaster API
		double lat = Double.parseDouble(request.getParameter("lat"));
		double lon = Double.parseDouble(request.getParameter("lon"));
		
		// term can be empty
		String keyword = request.getParameter("term");
		
		DBConnection conn = DBConnectionFactory.getConnection();
		List<Item> items = conn.searchItems(lat, lon, keyword);
		
		// for display of red heart on frontend
		String userId = request.getParameter("user_id");
		Set<String> historyItems = conn.getFavoriteItemIds(userId); // get item_id only
		
        conn.close(); 
        // instead of below two lines
		//TicketMasterAPI tmAPI = new TicketMasterAPI();
		//List<Item> items = tmAPI.search(lat, lon, keyword);
        
        JSONArray array = new JSONArray();
        
		try {
			//convert item to json object, json object is for frontend
			for (Item item : items) {
				JSONObject obj = item.toJSONObject();
				
				// for display of red heart on frontend
			    obj.put("favorite", historyItems.contains(item.getItemId()));
				
				array.put(obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		RpcHelper.writeJsonArray(response, array);
		
		
		// TODO Auto-generated method stub
		//1
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		
		
		//2
		/*
		PrintWriter out = response.getWriter();
		
		if (request.getParameter("username") != null) {
			String username = request.getParameter("username");
			out.print("Hello " + username);
		}
		
		out.close();
		*/
		
		//3
		/*
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
		out.println("<html><body>");
		out.println("<h1>This is a HTML page</h1>");
		out.println("</body></html>");
		
		out.close();
		*/
		
		//4
		/*
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		
		String username = "";
		if (request.getParameter("username") != null) {
			username = request.getParameter("username");
		}
		JSONObject obj = new JSONObject();
		try {
			obj.put("username", username);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		out.print(obj);
		out.close();
		*/
		
		
		
		//5 list
		/*
		response.setContentType("application/json");
		
		//PrintWriter out = response.getWriter();
		
		String username = "";
		if (request.getParameter("username") != null) {
			username = request.getParameter("username");
		}
		
		JSONArray array = new JSONArray();
		try {
			array.put(new JSONObject().put("usrename", username));
			array.put(new JSONObject().put("usrename", "abcd"));
			array.put(new JSONObject().put("usrename", "1234"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		//out.print(array);
		//out.close();
		//instead of below 
		RpcHelper.writeJsonArray(response, array);
		
		*/		
		
		
	}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
