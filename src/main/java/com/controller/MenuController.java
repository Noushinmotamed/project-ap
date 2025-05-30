package com.controller;

import com.DAO.MenuDAO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.models.Menu;

public class MenuController {
    private MenuDAO menuDAO = new MenuDAO();
    public JsonObject addMenu(Menu menu, Long userId, Long restaurantId) {
        return  menuDAO.addMenu(menu,userId,restaurantId);
    }
    public JsonObject deleteMenu( Long userId, Long restaurantId, String title) {
        return  menuDAO.deleteMenu(userId,restaurantId,title);
    }
    public JsonObject addItem( Long userId, Long restaurantId, Long item_id, String title) {
        return menuDAO.addItem(userId,restaurantId,item_id,title);
    }
    public JsonObject deleteItem( Long userId, Long restaurantId,  String title, Long itemId) {
        return menuDAO.deleteItem(userId,restaurantId,title,itemId);
    }
}
