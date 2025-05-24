package com.DAO;

import com.models.FoodItem;
import com.models.*;
import com.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * WHAT IS VENDOR_ID ???
 * DO I RETURN THE FOODITEM OBJECT ???
 */
public class FoodItemDAO {


    public String addItem(FoodItem foodItem,Long userId,Long restaurantId) {
        try( Session session = HibernateUtil.getSessionFactory().openSession() ){
            Transaction tx = session.beginTransaction();
            User seller = session.get(User.class,userId);
            if(seller == null){
                return "{\n  \"error\":\"Resource not found\"\n}";
            }
            Restaurant restaurant = session.get(Restaurant.class,restaurantId);
            if(restaurant == null){
                return "{\n  \"error\":\"Resource not found\"\n}";
            }
            session.save(foodItem);
            tx.commit();

            return "{\n  \"message\": \"Food item created and added to restaurant successfully\"\n}";

        } catch (Exception e) {
           return "{\n  \"error\":\"Internal server error\"\n}";
        }
    }
}
