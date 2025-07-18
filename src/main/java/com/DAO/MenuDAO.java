package com.DAO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.models.FoodItem;
import com.models.Menu;
import com.models.Restaurant;
import com.models.User;
import com.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;


public class MenuDAO {
    public JsonObject addMenu(Menu menu, Long userId, Long restaurantId) {
        JsonObject response = new JsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            User seller = session.get(User.class, userId);
            if (seller == null) {
                response.addProperty("error", "Resource not found");
                System.out.println("seller not found");
                return response;
            }
            Restaurant restaurant = session.get(Restaurant.class, restaurantId);
            if (restaurant == null) {
                response.addProperty("error", "Resource not found");
                System.out.println("restaurant not found");
                return response;
            }
            if (!restaurant.getOwnerId().equals(userId)) {
                response.addProperty("error", "Forbidden request");
                return response;
            }
            menu.setRestaurantId(restaurantId);
            System.out.println(menu.getRestaurantId());
            List<Menu> existingMenus = session.createQuery(
                            "SELECT m FROM Menu m WHERE m.title = :title AND m.restaurantId = :restaurantId",
                            Menu.class)
                    .setParameter("title", menu.getTitle())
                    .setParameter("restaurantId", restaurantId)
                    .getResultList();

            if(!existingMenus.isEmpty()) {
                response.addProperty("error", "Conflict occurred");
                return response;
            }

            session.persist(menu);
            tx.commit();
            return gson.toJsonTree(menu).getAsJsonObject();

        }catch (Exception e) {
            response.addProperty("error", "Internal server error");
            e.printStackTrace();
            return response;
        }
    }
    public JsonObject deleteMenu( Long userId, Long restaurantId,String title) {
        JsonObject response = new JsonObject();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            User seller = session.get(User.class, userId);
            if (seller == null) {
                response.addProperty("error", "Resource not found");
                return response;
            }
            Restaurant restaurant = session.get(Restaurant.class, restaurantId);
            if (restaurant == null) {
                response.addProperty("error", "Resource not found");
                return response;
            }
            if (!restaurant.getOwnerId().equals(userId)) {
                response.addProperty("error", "Forbidden request");
                return response;
            }
            Menu menu = session.createQuery(
                            "SELECT m FROM Menu m WHERE m.title = :title AND m.restaurantId = :restaurantId", Menu.class)
                    .setParameter("title", title)
                    .setParameter("restaurantId", restaurantId)
                    .uniqueResultOptional()
                    .orElse(null);
            if (menu == null) {
                response.addProperty("error", "Resource not found");
                return response;
            }
            session.createNativeQuery(
                    "DELETE FROM menusList WHERE menu_id = :menuId"
            ).setParameter("menuId", menu.getMenuId()).executeUpdate();


            session.delete(menu);
            tx.commit();
            response.addProperty("message", "Food menu removed from restaurant successfully");
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            response.addProperty("error", "Internal server error");
            return response;
        }

    }
    public JsonObject addItem(Long userId, Long restaurantId, Long item_id,String title) {
        JsonObject response = new JsonObject();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            User seller = session.get(User.class, userId);
            if (seller == null) {
                response.addProperty("error", "Resource not found");
                return response;
            }
            Restaurant restaurant = session.get(Restaurant.class, restaurantId);
            if (restaurant == null) {
                response.addProperty("error", "Resource not found");
                return response;
            }
            if (!restaurant.getOwnerId().equals(userId)) {
                response.addProperty("error", "Forbidden request");
                return response;
            }
            FoodItem foodItem = session.get(FoodItem.class, item_id);
            if(foodItem == null) {
                response.addProperty("error", "Resource not found");
                return response;
            }
            if(!foodItem.getVendorId().equals(restaurantId)) {
                response.addProperty("error", "Forbidden request");
                return response;
            }
            Menu menu = session.createQuery(
                            "SELECT m FROM Menu m WHERE m.title = :title AND m.restaurantId = :restaurantId", Menu.class)
                    .setParameter("title", title)
                    .setParameter("restaurantId", restaurantId)
                    .uniqueResultOptional()
                    .orElse(null);
            if (menu == null) {
                response.addProperty("error", "Resource not found");
                return response;
            }
            if(foodItem.menuExists(menu.getMenuId())){
                response.addProperty("error", "Conflict occurred");
                return response;
            }
            foodItem.addMenuId(menu.getMenuId());
            session.update(foodItem);
            tx.commit();
            response.addProperty("message", "Food item created and added to restaurant successfully");
            return response;


        } catch (Exception e) {
             response.addProperty("error", "Internal server error");
             e.printStackTrace();
             return response;

        }
    }
    public JsonObject deleteItem(Long userId, Long restaurantId,String title, Long itemId) {
        JsonObject response = new JsonObject();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            User seller = session.get(User.class, userId);
            if (seller == null) {
                response.addProperty("error", "Resource not found");
                return response;
            }
            Restaurant restaurant = session.get(Restaurant.class, restaurantId);
            if (restaurant == null) {
                response.addProperty("error", "Resource not found");
                return response;
            }
            if (!restaurant.getOwnerId().equals(userId)) {
                response.addProperty("error", "Forbidden request");
                return response;
            }
            FoodItem foodItem = session.get(FoodItem.class, itemId);
            if(foodItem == null) {
                response.addProperty("error", "Resource not found");
                return response;
            }
            if(!foodItem.getVendorId().equals(restaurantId)) {
                response.addProperty("error", "Forbidden request");
                return response;
            }
            Menu menu = session.createQuery(
                            "SELECT m FROM Menu m WHERE m.title = :title AND m.restaurantId = :restaurantId", Menu.class)
                    .setParameter("title", title)
                    .setParameter("restaurantId", restaurantId)
                    .uniqueResultOptional()
                    .orElse(null);
            if (menu == null) {
                response.addProperty("error", "Resource not found");
                return response;
            }
            if(!foodItem.menuExists(menu.getMenuId())){
                response.addProperty("error", "Resource not found");
                return response;
            }
            foodItem.removeMenuId(menu.getMenuId());
            session.update(foodItem);
            tx.commit();
            response.addProperty("message", "Item removed from restaurant menu successfully");
            return response;

        }catch(Exception e) {
            response.addProperty("error", "Internal server error");
            e.printStackTrace();
            return response;
        }
    }

    public JsonArray getAllMenusByRestaurantId(Long restaurantId) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Menu> menus = session.createQuery("from Menu where restaurantId = :restaurantId", Menu.class)
                    .setParameter("restaurantId", restaurantId)
                    .list();

            return gson.toJsonTree(menus).getAsJsonArray();
        } catch (Exception e) {
            e.printStackTrace();
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Internal server error while fetching menus.");
            JsonArray errorArray = new JsonArray();
            errorArray.add(errorResponse);
            return errorArray;
        }
    }



    public JsonArray getFoodItemsByMenuId(Long menuId) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Menu menu = session.get(Menu.class, menuId);
            if (menu == null) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("error", "Menu not found");
                JsonArray errorArray = new JsonArray();
                errorArray.add(errorResponse);
                return errorArray;
            }

            String sql = "SELECT fi.* FROM fooditems fi " +
                    "JOIN menusList ml ON fi.foodid = ml.foodItem_Id " +
                    "WHERE ml.menu_id = :menuId";

            Query<FoodItem> query = session.createNativeQuery(sql, FoodItem.class);
            query.setParameter("menuId", menuId);

            List<FoodItem> items = query.getResultList();

            return gson.toJsonTree(items).getAsJsonArray();

        } catch (Exception e) {
            e.printStackTrace();
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Internal server error while fetching food items for menu.");
            JsonArray errorArray = new JsonArray();
            errorArray.add(errorResponse);
            return errorArray;
        }
    }


}
