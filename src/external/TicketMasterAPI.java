package external;

import java.io.BufferedReader;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONException;

import org.json.JSONArray;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;

public class TicketMasterAPI {
	private static final String URL = "https://app.ticketmaster.com/discovery/v2/events.json"; //endpoint of URL
	private static final String DEFAULT_KEYWORD = ""; // no restriction
	private static final String API_KEY = "wyKEczakHFeOVa6HOAbWLzlA4j7FVxgL"; //my API key from TicketMaster
	
	
    //public JSONArray search(double lat, double lon, String keyword) {
	public List<Item> search(double lat, double lon, String keyword) {
        if (keyword == null) {
        	keyword = DEFAULT_KEYWORD;
        }
        
        try {
        	keyword = java.net.URLEncoder.encode(keyword, "UTF-8"); //Rick Sun -> Rick20%Sun
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        String geoHash = GeoHash.encodeGeohash(lat, lon, 9); // coordinate X Y -> string
        
        //"apikey=12345&geoPoint=abcd&keyword=music&radius=50"
        String query = String.format("apikey=%s&geoPoint=%s&keyword=%s&radius=%s", API_KEY, geoHash, keyword, "50");

        try {
        	//class URL -> Class HttpURLConnection
        	HttpURLConnection connection = (HttpURLConnection) new URL(URL + "?" + query).openConnection(); //openConnection return an URL connection
        	connection.setRequestMethod("GET");
        	
        	//for debug - you can debug this code to check if connection is successful 
        	int responseCode = connection.getResponseCode(); //sent request then got result (the result is response code)
        	System.out.println(responseCode);
        	
        	
        	BufferedReader in = new BufferedReader (new InputStreamReader(connection.getInputStream()));
        	String inputLine;
        	StringBuilder response = new StringBuilder();
        	
        	while ((inputLine = in.readLine()) != null) {
        		response.append(inputLine);
        	}
        	in.close();
        	
        	
        	JSONObject obj = new JSONObject(response.toString());
        	if (obj.isNull("_embedded")) {
        		//return new JSONArray();
        		return new ArrayList<>();
        	}
        	
        	JSONObject embedded = obj.getJSONObject("_embedded");
        	JSONArray events = embedded.getJSONArray("events");
        	//return events;
        	return getItemList(events);
        	
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        //return new JSONArray();
        return new ArrayList<>();
    }
    

    /**
	 * Helper methods
	 */

	//  {
	//    "name": "laioffer",
              //    "id": "12345",
              //    "url": "www.laioffer.com",
	//    ...
	//    "_embedded": {
    //      comment ... [ ] : represents array
    //      comment { } : represents obj
	//	    "venues": [
	//	        {
	//		        "address": {
	//		           "line1": "101 First St,",
	//		           "line2": "Suite 101",
	//		           "line3": "...",
	//		        },
	//		        "city": {
	//		        	"name": "San Francisco"
	//		        }
	//		        ...
	//	        },
	//	        {
	//		        "address": {
	//		           "line1": "102 First St,",
	//		           "line2": "Suite 102",
	//		           "line3": "...",
	//		        },
	//		        "city": {
	//		        	"name": "San Francisco"
	//		        }
	//		        ...
	//	        },
	//	        ...
	//	    ]
	//    }
	//    ...
	//  }
    // Helper - get address
	private String getAddress(JSONObject event) throws JSONException {
		if (!event.isNull("_embedded")) {
			JSONObject embedded = event.getJSONObject("_embedded");
			
			if (!embedded.isNull("venues")) {
				JSONArray venues = embedded.getJSONArray("venues");
				
				// find the first valid address 
				for (int i = 0; i < venues.length(); ++i) {
					JSONObject venue = venues.getJSONObject(i);
					
					StringBuilder sb = new StringBuilder();
					if (!venue.isNull("address")) {
						JSONObject address = venue.getJSONObject("address");
						if (!address.isNull("line1")) {
							sb.append(address.getString("line1"));
						}
						if (!address.isNull("line2")) {
							sb.append(address.getString("line2"));
						}
						if (!address.isNull("line3")) {
							sb.append(address.getString("line3"));
						}
						sb.append(",");
					}
					
					if (!venue.isNull("city")) {
						JSONObject city = venue.getJSONObject("city");
						if (!city.isNull("name")) {
							sb.append(city.getString("name"));
						}
					}
					
					if (!sb.toString().equals("")) {
						return sb.toString();
					}
				}
			}
		}
			
		return "";
	}

    // Helper - get image url
	// {"images": [{"url": "www.example.com/my_image.jpg"}, ...]}
	private String getImageUrl(JSONObject event) throws JSONException {
		
		if (!event.isNull("images")) {
			JSONArray array = event.getJSONArray("images");
			
			for (int i = 0; i < array.length(); i++) {
				JSONObject image = array.getJSONObject(i);
				
				if (!image.isNull("url")) {
					return image.getString("url");
				}
			}
		}
		
		return "";
	}

	// Helper - get category
	// {"classifications" : [{"segment": {"name": "music"}}, ...]}
	private Set<String> getCategories(JSONObject event) throws JSONException {
		Set<String> categories = new HashSet<>();
		if (!event.isNull("classifications")) {
			JSONArray classifications = event.getJSONArray("classifications");
			for (int i = 0; i < classifications.length(); i++) {
				JSONObject classification = classifications.getJSONObject(i);
				if (!classification.isNull("segment")) {
					JSONObject segment = classification.getJSONObject("segment");
					if (!segment.isNull("name")) {
						String name = segment.getString("name");
						categories.add(name);
					}
				}
			}
		}
		return categories;
	}

	// Convert JSONArray to a list of item objects.
	private List<Item> getItemList(JSONArray events) throws JSONException {
		List<Item> itemList = new ArrayList<>();
		
		for (int i = 0; i < events.length(); ++i) {
			JSONObject event = events.getJSONObject(i);
			
			ItemBuilder builder = new ItemBuilder();
			
			if (!event.isNull("name")) {
				builder.setName(event.getString("name"));
			}
			if (!event.isNull("id")) {
				builder.setItemId(event.getString("id"));
			}
			if (!event.isNull("url")) {
				builder.setUrl(event.getString("url"));
			}
			if (!event.isNull("rating")) {
				builder.setRating(event.getDouble("rating"));
			}
			if (!event.isNull("distance")) {
				builder.setDistance(event.getDouble("distance"));
			}
			
			builder.setCategories(getCategories(event));
			builder.setAddress(getAddress(event));
			builder.setImageUrl(getImageUrl(event));
			
			//Item item = builder.build();
			//itemList.add(builder.build());
			// or
			itemList.add(builder.build());
		}
		
		return itemList;
	}

    //---------------------------
    
    
    
    
    
    //queryAPI --- debug purpose
	private void queryAPI(double lat, double lon) {
	/*
		JSONArray events = search(lat, lon, null);
		try {
		    for (int i = 0; i < events.length(); i++) {
		        JSONObject event = events.getJSONObject(i);
		        System.out.println(event); //output to console
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}
	*/
		
		List<Item> itemList = search(lat, lon, null);
		try {
			
		    //for (int i = 0; i < itemList.size(); i++) {
		    //    JSONObject jsonObject = itemList.get(i).toJSONObject();
		    //    System.out.println(jsonObject); //output to console
		    //}
		    // or
			for (Item item : itemList) {
				JSONObject jsonObject = item.toJSONObject();
				System.out.println(jsonObject);   
		    }
			
		} catch (Exception e) {
			e.printStackTrace();
		}	
		
	}


	

	/**
	 * Main entry for sample TicketMaster API requests.
	 */
	// test TicketMasterAPI
	public static void main(String[] args) {
		TicketMasterAPI tmApi = new TicketMasterAPI();
		// Mountain View, CA
		// tmApi.queryAPI(37.38, -122.08);
		// London, UK
		// tmApi.queryAPI(51.503364, -0.12);
		// Houston, TX
		tmApi.queryAPI(29.682684, -95.295410);
	}

	
	
}
