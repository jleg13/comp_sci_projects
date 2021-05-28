package muck.server.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import muck.core.Id;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

/**
 * A General DOA that can be specialised to each data object that maps to a database table
 * @param <T> Takes the object type that extends Parcel and that maps to a database table
 */
public class GeneralDAO<T> {

    /** A logger for logging output */
    private static final Logger logger = LogManager.getLogger(GeneralDAO.class);
    private String table;
    private Class clazz;

    public GeneralDAO() { }

    /**
     * Constructor
     * @param table
     */
    public GeneralDAO(String table, Class clazz){
        this.table = table;
        this.clazz = clazz;
        }

    private Class getClazz(){
        return this.clazz;
    }

    /**
     * Generic method to save add a row to any table
     * @param Parcel
     * @return
     */
    public Boolean saveRow(T Parcel){
        JSONObject row = getJsonObject(Parcel);
        try{
            MuckDatabase.addRow(row, this.table);
        } catch (SQLException e){
            logger.warn("Problem adding row to database");
            return false;
        }
        return true;
    }

    /**
     *
     * @param updates
     * @param conditions
     * @return
     */
    public Boolean update(Map<String, Object> updates, Map<String, Object> conditions, ArrayList<String> separators,
                          int transactionId){
        JSONObject jo1 = getJsonObject(updates);
        JSONObject jo2 = getJsonObject(conditions);
        try{
            MuckDatabase.updateRow(this.table, jo1, jo2, separators, transactionId);
        } catch (SQLException e){
            logger.warn("Problem updating row/s in database" + e);
            return false;
        }
        return true;
    }

    /**
     * Generic method to get a row from any table
     * @param pkValues
     * @param userId
     * @param <T>
     * @return
     */
    public <T> T getRow(String[] pkValues, Id userId)throws SQLException{
        JSONArray row = null;
        try{
            row = MuckDatabase.getRow(pkValues, this.table, userId);
        } catch (SQLException e){
            logger.warn("Problem getting row from database");
            throw e;
        }
        return getParcel(row);
    }

    /**
     * Generic method to get a row/rows from any table based on the provided conditions
     * @param userId
     * @param rst
     * @param conditions
     * @return
     */
    public ArrayList<T> getRows(Id userId, ArrayList<T> rst, String[] conditions)throws SQLException{
        JSONArray row;
        try{
            row = MuckDatabase.getRows(this.table, userId, conditions);
        } catch (SQLException e){
            logger.warn("Problem getting row from database");
            throw e;
        }
        return getParcels(row, rst);
    }

    /**
     * Utility method to convert generic object to JSON
     * @param Parcel
     * @return
     */
    private JSONObject getJsonObject(T Parcel){
        String json = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd hh:mm:ss.S")
                .create()
                .toJson(Parcel);
        JSONObject jo = new JSONObject(json);
        return jo;
    }

    /**
     * Utility method to convert Map<> to JSON
     * @param Parcel
     * @return
     */
    private JSONObject getJsonObject(Map<String, Object> Parcel){
        String json = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd hh:mm:ss.S")
                .create()
                .toJson(Parcel);
        JSONObject jo = new JSONObject(json);
        return jo;
    }

    /**
     * Utility method to convert JSON to generic object
     * @param js
     * @return
     */
    @SuppressWarnings("unchecked")
    private <T> T getParcel(JSONArray js){
        T Parcel  = (T) new GsonBuilder()
                .setDateFormat("yyyy-MM-dd hh:mm:ss.S")
                .create()
                .fromJson(js.optString(0), getClazz());
        return Parcel;
    }

    /**
     *  Utility method to convert JSONArray to generic ArrayList of generic objects
     * @param js
     * @return
     */
    @SuppressWarnings("unchecked")
    private ArrayList<T>  getParcels(JSONArray js, ArrayList<T> rst){
        for(int i = 0; i < js.length(); i ++){
            T Parcel  = (T) new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd hh:mm:ss.S")
                    .create()
                    .fromJson(js.optString(i), getClazz());
            rst.add(Parcel);
        }
        return rst;
    }
}
