package muck.server.database;

import muck.core.Id;
import org.json.JSONObject;

import java.sql.*;
import java.util.Iterator;

public class JsonObjectConverter {
    public static PreparedStatement convert(JSONObject jo, PreparedStatement ps) throws SQLException{
        Iterator<String> keys = jo.keys();
        int counter = 1;

        /* set values */
        try{
            while(keys.hasNext()) {
                String key = keys.next();
                if (jo.get(key) instanceof Id || key.startsWith("_")) {
                    continue;
                }
                if (jo.get(key) instanceof JSONObject) {
                    // do something with jsonObject here
                } else if (jo.get(key) instanceof String) {
                    ps.setString(counter, jo.getString(key));
                    counter++;
                } else if (jo.get(key) instanceof Integer) {
                    ps.setInt(counter, jo.getInt(key));
                    counter++;
                } else if (jo.get(key) instanceof Double) {
                    ps.setDouble(counter, jo.getDouble(key));
                    counter++;
                } else if (jo.get(key) instanceof Boolean) {
                    ps.setBoolean(counter, jo.getBoolean(key));
                    counter++;
                } else if (jo.get(key) instanceof Date){
//                    ps.setTime(counter, jo.g);
                } else if (jo.get(key) instanceof Timestamp){
//                    ps.setTime(counter, jo.getLong(key));
                }
            }
        } catch(SQLException e){

            throw e;
        }
        return ps;
    }

    public static PreparedStatement convert(JSONObject jo1, JSONObject jo2, PreparedStatement ps) throws SQLException{
        Iterator<String> keys1 = jo1.keys();
        Iterator<String> keys2 = jo2.keys();
        int counter = 1;

        /* set values */
        try{
            while(keys1.hasNext()) {
               ps = checkInstances(jo1, keys1, ps, counter);
               counter ++;
            }
            while(keys2.hasNext()) {
                ps = checkInstances(jo2, keys2, ps, counter);
                counter ++;
            }
        } catch(SQLException e){

            throw e;
        }
        return ps;
    }

    private static PreparedStatement checkInstances(JSONObject jo, Iterator<String> keys, PreparedStatement ps, int counter) throws SQLException{

        try{
            String key = keys.next();
            if (jo.get(key) instanceof Id) {
                return ps;
            }
            if (jo.get(key) instanceof JSONObject) {
                // do something with jsonObject here
            } else if (jo.get(key) instanceof String) {
                ps.setString(counter, jo.getString(key));
            } else if (jo.get(key) instanceof Integer) {
                ps.setInt(counter, jo.getInt(key));
            } else if (jo.get(key) instanceof Double) {
                ps.setDouble(counter, jo.getDouble(key));
            } else if (jo.get(key) instanceof Boolean) {
                ps.setBoolean(counter, jo.getBoolean(key));
            }
        } catch(SQLException e){
            throw e;
        }
        return ps;

    }
}
