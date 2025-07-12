package com.DAO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.models.*;
import com.util.HibernateUtil;
import org.hibernate.Session;

import java.time.LocalDateTime;
import java.util.List;
// WHICH STATUS MEAN ORDER HAS BEEN PAID ????
                                                                       // WHAT IS THE RESPONSE IF WALLET DOESN'T HAVE ENOUGH BALANCE ??
                                                                      // CAN BUYER STILL PAY CANCELLED ORDER ??
                                                                      // I ASSUME YOU SHOULD PAY AFTER THE ORDER STATUS WAS SET TO SUBMITTED ORDER (AND CHANGES TO WAITING VENDOR)
                                                                      // IF INSUFFICIENT BALANCE : DO YOU SEND ERROR OR JUST SEND TRANSACTION WITH FAILED STATUS ?!

public class WalletDAO {
    public JsonObject updateWallet(Long id, double amount) {
        JsonObject response = new JsonObject();
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            org.hibernate.Transaction tx = session.beginTransaction();
            Wallet wallet = session.createQuery("from Wallet where userId = :id", Wallet.class).setParameter("id", id).uniqueResult();

            if(wallet == null) {
                response.addProperty("error", "Resource not found");
                return response;
            }
            wallet.addAmount(amount);
            session.update(wallet);
            tx.commit();
            response.addProperty("message", "Wallet topped up successfully");
            response.addProperty("balance", wallet.getBalance());                            // for client
            return response;


        }catch(Exception e){
           response.addProperty("error", "Internal server error");
           return response;

        }
    }
    public JsonObject pay(Long orderId,  PaymentMethod method, Long userId) {
        JsonObject response = new JsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        TransactionStatus status = TransactionStatus.failed;
        Transaction transaction = new Transaction();
        transaction.setStatus(status);
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            org.hibernate.Transaction tx = session.beginTransaction();
            Order order = session.get(Order.class, orderId);
            if(order == null || !order.getCustomerId().equals(userId)) {
                response.addProperty("error", "Resource not found");
                return response;
            }
            User user = session.get(User.class, userId);
            if(user == null) {
                response.addProperty("error", "Resource not found");
                return response;
            }

            if(order.getStatus() != null && order.getStatus()!= OrderStatus.unpaidAndCancelled && order.getStatus() != OrderStatus.submitted ){              //ORDER ALREADY PAID
               response.addProperty("error", "Conflict occurred");
               return response;
            }

            if(method == PaymentMethod.wallet) {
                Wallet wallet = session.createQuery("from Wallet where userId = :id", Wallet.class).setParameter("id", userId).uniqueResult();
                if (wallet == null) {
                  response.addProperty("error", "Resource not found");
                  return response;
                }

                if (wallet.removeAmount(order.getTotalPrice())) {
                    session.update(wallet);
                    status= TransactionStatus.success;
                } else {
                    status = TransactionStatus.failed;
                }
            }else{
                status = TransactionStatus.success;
            }
                if(status == TransactionStatus.success) {
                    order.setStatus(OrderStatus.waitingVendor);                      //WRONG ?
                    order.setUpdatedAt(LocalDateTime.now().toString());
                }
                session.update(order);

                transaction.setUserId(userId);
                transaction.setStatus(status);
                transaction.setOrderId(orderId);
                transaction.setMethod(method);
                order.setStatus(OrderStatus.waitingVendor);
                session.update(order);
                session.save(transaction);
                tx.commit();
                  return gson.toJsonTree(transaction).getAsJsonObject();


        }catch(Exception e){
            e.printStackTrace();

            response.addProperty("error", "Internal server error");
            return response;
        }
    }

    public JsonElement getTranHistory(Long id){
        JsonElement response;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            User buyer = session.get(User.class, id);
            if(buyer == null) {
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Resource not found");
                return response;
            }
            if(buyer.getRole() != Role.buyer){
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Forbidden request");
                return response;
            }
            String hql = "FROM Transaction WHERE userId = :userId";
            List<Transaction> transactions = session.createQuery(hql, Transaction.class)
                    .setParameter("userId", id)
                    .getResultList();
            if(transactions.isEmpty()){
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Resource not found");
                return response;
            }
            return gson.toJsonTree(transactions);


        } catch (Exception e) {
            response = new JsonObject();
            ((JsonObject) response).addProperty("error", "Internal server error");
            return response;

        }

        }
    }





