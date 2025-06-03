package com.DAO;

import com.models.*;
import com.util.HibernateUtil;
import org.hibernate.Session;

import java.time.LocalTime;

public class WalletDAO {
    public String updateWallet(Long id, double amount) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            org.hibernate.Transaction tx = session.beginTransaction();
            Wallet wallet = session.createQuery("from Wallet where userId = :id", Wallet.class).setParameter("id", id).uniqueResult();

            if(wallet == null) {
                return "{ \"error\": \"Resource not found\" }";
            }
            wallet.addAmount(amount);
            session.update(wallet);
            tx.commit();
            return "{ \"message\": \"Wallet topped up successfully\" }";


        }catch(Exception e){
            return "{ \"error\": \"internal error\" }";
        }
    }
    public String pay(Long orderId, double amount, PaymentMethod method, Long userId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            org.hibernate.Transaction tx = session.beginTransaction();
            Order order = session.get(Order.class, orderId);
            if(order == null || !order.getUserId().equals(userId)) {
                return "{ \"error\": \"Order not found\" }";
            }
            if(order.getStatus() == OrderStatus.submitted){              //THIS IS TOTALLY WRONG
                return "{ \"error\": \"Order is paid already\" }";
            }
            if(amount!= order.getRawPrice()){
                return "{ \"error\": \"Invalid input or insufficient balance\" }";
            }
            if(method == PaymentMethod.wallet) {
                Wallet wallet = session.createQuery("from Wallet where userId = :id", Wallet.class).setParameter("id", userId).uniqueResult();
                if (wallet == null) {
                    return "{ \"error\": \"Forbidden\" }";
                }
                if (wallet.removeAmount(amount)) {
                    session.update(wallet);
                } else {
                    return "{ \"error\": \"Invalid input or insufficient balance\" }";
                }
            }
                order.setStatus(OrderStatus.submitted);                      //WRONGGG
                session.update(order);

                Transaction not1 = sendNotification(orderId,amount,method,userId);
                Transaction not2 = sendNotification(orderId,amount,method,order.getSellerId());
                session.save(not1);
                session.save(not2);

                tx.commit();
                return "{ \"message\": \"Payment successful\" }";



        }catch(Exception e){
            e.printStackTrace();

            return "{ \"error\": \"Internal Error\" }";
        }
    }
    private Transaction sendNotification(Long orderId, double amount, PaymentMethod method, Long userId) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setMessage("Payment successful\n" + "orderId: " + orderId + "\n" + "amount: " + amount + "\n" + "method: " + method + "\n" + "payed at : " + LocalTime.now()
        );
        return transaction;
    }



}
