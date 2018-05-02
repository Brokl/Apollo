package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import db.DBConnection;
import entity.Item;
import entity.Item.ItemBuilder;
import external.TicketMasterAPI;

public class MySQLConnection implements DBConnection {
	
	private Connection conn;
	// step 1. constructor
	public MySQLConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver").getConstructor().newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	@Override
	public void close() {
		// TODO Auto-generated method stub
		// step 2.
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		
	}

	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
		// if we use List<String>, it means we support multiple items-saving at the same time.
		// 但事實上目前我們的 project 是一次只收藏一個(滑鼠一次只能點一個項目收為我的最愛, 這裡用 list是因為前端是宣告成 array形式的關係才這麼寫, 但萬一已後支援多選收藏,預做保留也可
		
		// TODO Auto-generated method stub
		if (conn == null) {
			return;
		}
		String sql = "INSERT IGNORE INTO history (user_id, item_id) VALUES (?,?)"; // tell SQL, we only insert two
																						// columns, another one use
																						// default value.
		try {
			//PreparedStatement statement = conn.prepareStatement(sql); // how about putting here ???
			
			for (String itemId : itemIds) {
				PreparedStatement statement = conn.prepareStatement(sql);
				statement.setString(1, userId);
				statement.setString(2, itemId);
				statement.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		// TODO Auto-generated method stub
		if (conn == null) {
			return;
		}
		String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
		try {
			for (String itemId : itemIds) {
				PreparedStatement statement = conn.prepareStatement(sql);
				statement.setString(1, userId);
				statement.setString(2, itemId);
				statement.executeUpdate();
			}		
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		// TODO Auto-generated method stub
		
		Set<String> itemIds = new HashSet<>();
		if (conn == null) {
			return itemIds;
		}
		String sql = "SELECT item_id FROM history WHERE user_id = ?";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				itemIds.add(rs.getString("item_id"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return itemIds;
	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {
		// TODO Auto-generated method stub
		
		Set<Item> items = new HashSet<>();
		if (conn == null) {
			return items;
		}
		// step1
		Set<String> itemIds = getFavoriteItemIds(userId);
		String sql = "SELECT * FROM items WHERE item_id = ?";

		try {
			for (String itemId : itemIds) {
				PreparedStatement statement = conn.prepareStatement(sql);
				statement.setString(1, itemId);
				ResultSet rs = statement.executeQuery();
				
				//rs is [{"name":"abcd", "rating":0, ...}], only one obj
				
				while (rs.next()) { 
				// only execute while one time because we just have one object [...] in our case.
				// therefore, we can use if(rs.next()) instead in this case
					
					ItemBuilder builder = new ItemBuilder();
					builder.setItemId(rs.getString("item_id"));
					builder.setName(rs.getString("name"));
					builder.setRating(rs.getDouble("rating"));
					builder.setAddress(rs.getString("address"));
					builder.setImageUrl(rs.getString("image_url"));
					builder.setUrl(rs.getString("url"));
					builder.setDistance(rs.getDouble("distance"));
					
					builder.setCategories(getCategories(itemId));
					
					items.add(builder.build());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return items;
	}

	@Override
	public Set<String> getCategories(String itemId) {
		// TODO Auto-generated method stub
		
		Set<String> categories = new HashSet<>();
		if (conn == null) {
			return categories;
		}
		String sql = "SELECT category FROM categories WHERE item_id = ?";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, itemId);

			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				categories.add(rs.getString("category"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return categories;
	}

	@Override
	public List<Item> searchItems(double lat, double lon, String term) {
		// TODO Auto-generated method stub
		
		TicketMasterAPI tmAPI = new TicketMasterAPI();
		List<Item> items = tmAPI.search(lat, lon, term);
		for (Item item : items) {
			saveItem(item);
		}
		return items;
		
	}

	@Override
	public void saveItem(Item item) {
		// TODO Auto-generated method stub
			
		if (conn == null) {
			return;
		}
		try {
			// First, insert into items table
			String sql = "INSERT IGNORE INTO items VALUES (?,?,?,?,?,?,?)";
			// keyword IGNORE : avoid exception of PIMARY KEY duplication
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, item.getItemId());
			statement.setString(2, item.getName());
			statement.setDouble(3, item.getRating());
			statement.setString(4, item.getAddress());
			statement.setString(5, item.getImageUrl());
			statement.setString(6, item.getUrl());
			statement.setDouble(7, item.getDistance());
			statement.executeUpdate();

			// Second, update categories table for each category.
			sql = "INSERT IGNORE INTO categories VALUES (?,?)";
			for (String category : item.getCategories()) {
				statement = conn.prepareStatement(sql);
				statement.setString(1, item.getItemId());
				statement.setString(2, category);
				statement.executeUpdate();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		
	}

	@Override
	public String getFullname(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		// TODO Auto-generated method stub
		return false;
	}

}
