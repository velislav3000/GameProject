package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import agents.items.BaseItem.ItemQuality;
import agents.items.equipment.Equipment.EquipmentType;
import agents.items.potions.BasePotion.PotionType;
import collectiables.materials.BaseMaterial.MaterialType;

public class DBConnector {
	
	private final static String DB_NAME = "jdbc:h2:~/data/game";
	private final static String DB_USERNAME = "v";
	private final static String DB_PASSWORD = "123";
	
	public static void initDatabase() {
		try {
			Connection conn = DriverManager.getConnection(DB_NAME, DB_USERNAME, DB_PASSWORD);
			
			String sqlCreateBasePriceTable = "CREATE TABLE IF NOT EXISTS ITEM_BASE_PRICES (" +
												"Id INT NOT NULL PRIMARY KEY AUTO_INCREMENT," +
												"ItemTypeName VARCHAR(20) NOT NULL," +
												"Price INT NOT NULL);";
			
			String sqlCreateMaterialPriceMultiplierTable = "CREATE TABLE IF NOT EXISTS MATERIAL_PRICE_MULTIPLIER (" +
					"Id INT NOT NULL PRIMARY KEY AUTO_INCREMENT," +
					"MaterialName VARCHAR(20) NOT NULL," +
					"PriceMultiplier DECIMAL(4,2) NOT NULL);";
			
			String sqlCreateQualityPriceMultiplierTable = "CREATE TABLE IF NOT EXISTS QUALITY_PRICE_MULTIPLIER (" +
					"Id INT NOT NULL PRIMARY KEY AUTO_INCREMENT," +
					"QualityName VARCHAR(20) NOT NULL," +
					"PriceMultiplier DECIMAL(4,2) NOT NULL);";
			
			String sqlCreateMaterialBasePriceTable = "CREATE TABLE IF NOT EXISTS MATERIAL_BASE_PRICES (" +
					"Id INT NOT NULL PRIMARY KEY AUTO_INCREMENT," +
					"MaterialName VARCHAR(20) NOT NULL," +
					"Price INT NOT NULL);";
			
			String sqlCheckIfTableIsEmpty = "Select * from ITEM_BASE_PRICES;";
			
			//String drop = "DROP TABLE MATERIAL_PRICE_MULTIPLIER; DROP TABLE QUALITY_PRICE_MULTIPLIER;";
			
			Statement statement = conn.createStatement();
			//statement.execute(drop);
			statement.execute(sqlCreateBasePriceTable);
			statement.execute(sqlCreateMaterialPriceMultiplierTable);
			statement.execute(sqlCreateQualityPriceMultiplierTable);
			statement.execute(sqlCreateMaterialBasePriceTable);
			
			ResultSet resultSet = statement.executeQuery(sqlCheckIfTableIsEmpty);
			if(resultSet.next() == false) {
				String sqlInserts = "INSERT INTO ITEM_BASE_PRICES (ItemTypeName, Price) VALUES ('" + EquipmentType.BOOTS.toString() + "', 30);" +
						"INSERT INTO ITEM_BASE_PRICES (ItemTypeName, Price) VALUES ('" + EquipmentType.CHESTPLATE.toString() + "', 90);" +
						"INSERT INTO ITEM_BASE_PRICES (ItemTypeName, Price) VALUES ('" + EquipmentType.LEGGINGS.toString() + "', 75);" +
						"INSERT INTO ITEM_BASE_PRICES (ItemTypeName, Price) VALUES ('" + EquipmentType.HELMET.toString() + "', 45);" +
						"INSERT INTO ITEM_BASE_PRICES (ItemTypeName, Price) VALUES ('" + EquipmentType.SHIELD.toString() + "', 90);" +
						"INSERT INTO ITEM_BASE_PRICES (ItemTypeName, Price) VALUES ('" + EquipmentType.SWORD.toString() + "', 60);" +
						"INSERT INTO ITEM_BASE_PRICES (ItemTypeName, Price) VALUES ('" + EquipmentType.DAGGER.toString() + "', 35);" +
						"INSERT INTO ITEM_BASE_PRICES (ItemTypeName, Price) VALUES ('" + EquipmentType.BOW.toString() + "', 50);" +
						"INSERT INTO ITEM_BASE_PRICES (ItemTypeName, Price) VALUES ('" + EquipmentType.HAMMER.toString() + "', 80);" +
						"INSERT INTO ITEM_BASE_PRICES (ItemTypeName, Price) VALUES ('" + PotionType.HEALTH.toString() + "', 50);" +
						"INSERT INTO ITEM_BASE_PRICES (ItemTypeName, Price) VALUES ('" + PotionType.FLEEING.toString() + "', 100);" +
						"INSERT INTO ITEM_BASE_PRICES (ItemTypeName, Price) VALUES ('" + PotionType.REGENARATION.toString() + "', 50);" +
						"INSERT INTO ITEM_BASE_PRICES (ItemTypeName, Price) VALUES ('" + PotionType.SUPERHEALTH.toString() + "', 150);";
				statement.execute(sqlInserts);
			}
			
			sqlCheckIfTableIsEmpty = "Select * from MATERIAL_PRICE_MULTIPLIER;";
			resultSet = statement.executeQuery(sqlCheckIfTableIsEmpty);
			if(resultSet.next() == false) {
				String sqlInserts = "INSERT INTO MATERIAL_PRICE_MULTIPLIER (MaterialName, PriceMultiplier) VALUES ('" + MaterialType.STONE.toString() + "', 1.0);" +
						"INSERT INTO MATERIAL_PRICE_MULTIPLIER (MaterialName, PriceMultiplier) VALUES ('" + MaterialType.IRON.toString() + "', 3.0);" +
						"INSERT INTO MATERIAL_PRICE_MULTIPLIER (MaterialName, PriceMultiplier) VALUES ('" + MaterialType.COBALT.toString() + "', 5.0);" +
						"INSERT INTO MATERIAL_PRICE_MULTIPLIER (MaterialName, PriceMultiplier) VALUES ('" + MaterialType.MYTHRIL.toString() + "', 8.0);" +
						"INSERT INTO MATERIAL_PRICE_MULTIPLIER (MaterialName, PriceMultiplier) VALUES ('" + MaterialType.ADAMANTITE.toString() + "', 12.0);";
				statement.execute(sqlInserts);
			}
			
			sqlCheckIfTableIsEmpty = "Select * from QUALITY_PRICE_MULTIPLIER;";
			resultSet = statement.executeQuery(sqlCheckIfTableIsEmpty);
			if(resultSet.next() == false) {
				String sqlInserts = "INSERT INTO QUALITY_PRICE_MULTIPLIER (QualityName, PriceMultiplier) VALUES ('" + ItemQuality.POOR.toString() + "', 0.5);" +
						"INSERT INTO QUALITY_PRICE_MULTIPLIER (QualityName, PriceMultiplier) VALUES ('" + ItemQuality.AVERAGE.toString() + "', 1.0);" +
						"INSERT INTO QUALITY_PRICE_MULTIPLIER (QualityName, PriceMultiplier) VALUES ('" + ItemQuality.GREAT.toString() + "', 2.0);" +
						"INSERT INTO QUALITY_PRICE_MULTIPLIER (QualityName, PriceMultiplier) VALUES ('" + ItemQuality.SUPREME.toString() + "', 4.0);";
				statement.execute(sqlInserts);
			}
			
			sqlCheckIfTableIsEmpty = "Select * from MATERIAL_BASE_PRICES;";
			resultSet = statement.executeQuery(sqlCheckIfTableIsEmpty);
			if(resultSet.next() == false) {
				String sqlInserts = "INSERT INTO MATERIAL_BASE_PRICES (MaterialName, Price) VALUES ('" + MaterialType.STONE.toString() + "', 2);" +
								"INSERT INTO MATERIAL_BASE_PRICES (MaterialName, Price) VALUES ('" + MaterialType.IRON.toString() + "', 6);" + 
								"INSERT INTO MATERIAL_BASE_PRICES (MaterialName, Price) VALUES ('" + MaterialType.COBALT.toString() + "', 10);" + 
								"INSERT INTO MATERIAL_BASE_PRICES (MaterialName, Price) VALUES ('" + MaterialType.MYTHRIL.toString() + "', 16);" + 
								"INSERT INTO MATERIAL_BASE_PRICES (MaterialName, Price) VALUES ('" + MaterialType.ADAMANTITE.toString() + "', 24);" + 
								"INSERT INTO MATERIAL_BASE_PRICES (MaterialName, Price) VALUES ('" + MaterialType.STRING.toString() + "', 6);" + 
								"INSERT INTO MATERIAL_BASE_PRICES (MaterialName, Price) VALUES ('" + MaterialType.DAYBLOOM.toString() + "', 6);" + 
								"INSERT INTO MATERIAL_BASE_PRICES (MaterialName, Price) VALUES ('" + MaterialType.MOONGLOW.toString() + "', 10);" + 
								"INSERT INTO MATERIAL_BASE_PRICES (MaterialName, Price) VALUES ('" + MaterialType.SHADOWSTALK.toString() + "', 12);" + 
								"INSERT INTO MATERIAL_BASE_PRICES (MaterialName, Price) VALUES ('" + MaterialType.BLOODROSE.toString() + "', 12);";
				statement.execute(sqlInserts);
			}
			
			resultSet.close();
			statement.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static int getItemBasePrice(String type) {
		try {
			Connection conn = DriverManager.getConnection(DB_NAME, DB_USERNAME, DB_PASSWORD);
			
			String sql = "SELECT PRICE FROM ITEM_BASE_PRICES WHERE ITEMTYPENAME = '"+ type + "'";
			
			Statement statement = conn.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			
			if(resultSet.next()) {
				return resultSet.getInt("PRICE");
			}

			resultSet.close();
			statement.close();
			conn.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public static float getMaterialPriceMultiplier(String material) {
		try {
			Connection conn = DriverManager.getConnection(DB_NAME, DB_USERNAME, DB_PASSWORD);
			
			String sql = "SELECT PRICEMULTIPLIER FROM MATERIAL_PRICE_MULTIPLIER WHERE MATERIALNAME = '"+ material + "'";
			
			Statement statement = conn.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			
			if(resultSet.next()) {
				return resultSet.getFloat("PRICEMULTIPLIER");
			}

			resultSet.close();
			statement.close();
			conn.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public static float getQualityPriceMultiplier(String quality) {
		try {
			Connection conn = DriverManager.getConnection(DB_NAME, DB_USERNAME, DB_PASSWORD);
			
			String sql = "SELECT PRICEMULTIPLIER FROM QUALITY_PRICE_MULTIPLIER WHERE QUALITYNAME = '"+ quality + "'";
			
			Statement statement = conn.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			
			if(resultSet.next()) {
				return resultSet.getFloat("PRICEMULTIPLIER");
			}

			resultSet.close();
			statement.close();
			conn.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public static int getMaterialBasePrice(String material) {
		try {
			Connection conn = DriverManager.getConnection(DB_NAME, DB_USERNAME, DB_PASSWORD);
			
			String sql = "SELECT PRICE FROM MATERIAL_BASE_PRICES WHERE MATERIALNAME = '"+ material + "'";
			
			Statement statement = conn.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			
			if(resultSet.next()) {
				return resultSet.getInt("PRICE");
			}

			resultSet.close();
			statement.close();
			conn.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
