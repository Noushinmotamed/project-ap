package com.DAO;

import com.google.gson.*;
import com.models.*;
import com.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

import java.util.*;                                             // I RETURN
                                                                    // I RETURN VENDORS EVEN IF THEY INCLUDE ONLY ONE OF THE KEYWORDS !!

public class MySearchDAO {
    public JsonElement getItems(Long id, String search, int price, ArrayList<String> keywords){
        JsonElement response = null;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            User user = session.get(User.class, id);
            if(user == null){
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Resource not found");
                return response;
            }
            if(user.getRole() != Role.buyer){
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Forbidden request");
                return response;
            }
            String hql = "SELECT m FROM FoodItem m WHERE 1=1";
            if( search != null && !search.isEmpty() ) hql += " AND m.name= :name ";
            if( price >= 0 ) hql += " AND m.price= :price ";
            Query<FoodItem> query = session.createQuery(hql , FoodItem.class);

            if(search != null && !search.isEmpty()) query.setParameter("name", search);
            if(price >=0) query.setParameter("price", price);
            List<FoodItem> items = query.getResultList();
            if(items.isEmpty()){
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Resource not found");
                return response;
            }if(!keywords.isEmpty()) {
                for (String keyword : keywords) {
                    items.removeIf(item -> !item.getKeywords().contains(keyword));
                }
                if(items.isEmpty()){
                    response = new JsonObject();
                    ((JsonObject) response).addProperty("error", "Resource not found");
                    return response;
                }
            }
            tx.commit();
            return gson.toJsonTree(items);
        }catch (Exception e){
            response = new JsonObject();
            ((JsonObject) response).addProperty("error", "Internal server error");
            return response;
        }
    }
    public JsonObject getItem(Long id, Long itemId){
        JsonObject response = new JsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            User user = session.get(User.class, id);
            if(user == null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            if(user.getRole() != Role.buyer){
                response.addProperty("error", "Forbidden request");
                return response;
            }
            FoodItem item = session.get(FoodItem.class, itemId);
            if(item == null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            tx.commit();
            return gson.toJsonTree(item).getAsJsonObject();
        }
    }
    public JsonElement getVendors(Long id, String search, ArrayList<String> keywords){
        JsonElement response = new JsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, id);
            if(user == null){
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Resource not found");
                return response;
            }
            if(user.getRole() != Role.buyer){
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Forbidden request");
                return response;
            }
            String hql = "SELECT DISTINCT r FROM Restaurant r LEFT JOIN Menu m ON r.id = m.restaurantId WHERE 1=1";

            if (search != null && !search.isEmpty()) {
                hql += " AND r.name LIKE :name";
            }
            if (!keywords.isEmpty()) {
                hql += " AND (";
                for (int i = 0; i < keywords.size(); i++) {
                    if (i > 0) hql += " OR ";
                    hql += "m.title LIKE :keyword" + i;
                }
                hql += ")";
            }

            Query<Restaurant> query = session.createQuery(hql, Restaurant.class);

            // Set parameters
            if (search != null && !search.isEmpty()) {
                query.setParameter("name", "%" + search + "%");
            }

            for (int i = 0; i < keywords.size(); i++) {
                query.setParameter("keyword" + i, "%" + keywords.get(i) + "%");
            }

            List<Restaurant> vendors = query.getResultList();
            if(vendors.isEmpty()){
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Resource not found");
                return response;
            }
            return gson.toJsonTree(vendors);

        } catch (Exception e) {
            response = new JsonObject();
            ((JsonObject) response).addProperty("error", "Internal server error");
            return response;
        }
    }
    public JsonObject getVendor(Long id, Long vendorId){
        JsonObject response = new JsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, id);
            if(user == null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            if(user.getRole() != Role.buyer){
                response.addProperty("error", "Forbidden request");
                return response;
            }
            Restaurant vendor = session.get(Restaurant.class, vendorId);
            if(vendor == null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            JsonObject vendorJson = new JsonObject();
            vendorJson.addProperty("id", vendor.getId());
            vendorJson.addProperty("name", vendor.getName());
            vendorJson.addProperty("address", vendor.getAddress());
            vendorJson.addProperty("phone", vendor.getPhone());
            vendorJson.addProperty("logoBase64", vendor.getLogoBase64());
            vendorJson.addProperty("tax_fee", vendor.getTax_fee());
            vendorJson.addProperty("additional_fee", vendor.getAdditionalFee());
            response.add("vendor", vendorJson);
            //now get titles
            Query<Menu> menusQuery = session.createQuery(
                    "FROM Menu m WHERE m.restaurantId = :vendorId", Menu.class);
            menusQuery.setParameter("vendorId", vendorId);
            List<Menu> menus = menusQuery.getResultList();
            JsonArray menuTitlesArray = new JsonArray();
            Map<String, JsonArray> menuItemsMap = new LinkedHashMap<>();
            for (Menu menu : menus) {
                menuTitlesArray.add(menu.getTitle());
                String sql = "SELECT fi.* FROM fooditems fi " +
                        "JOIN menuslist ml ON fi.foodid = ml.fooditem_id " +
                        "WHERE ml.menu_id = :menuId";
                NativeQuery<FoodItem> foodItemQuery = session.createNativeQuery(sql, FoodItem.class);
                foodItemQuery.setParameter("menuId", menu.getMenuId());
                List<FoodItem> foodItems = foodItemQuery.getResultList();
                JsonArray itemsArray = gson.toJsonTree(foodItems).getAsJsonArray();
                menuItemsMap.put(menu.getTitle(), itemsArray);
                response.add("menu_titles", menuTitlesArray);
                menuItemsMap.forEach(response::add);


            }
            return response;

            }catch (Exception e){
            response.addProperty("error", "Internal server error");
            return response;
        }


        }


}
