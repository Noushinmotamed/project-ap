package com.DAO;

import com.models.*;
import com.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalTime;

public class WalletDAO {
    public String updateWallet(Long id, double amount) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction tx = session.beginTransaction();
            Wallet wallet = session.createQuery("from Wallet where userId = :id", Wallet.class).setParameter("id", id).uniqueResult();

            if(wallet == null) {
                return "{ \"error\": \"Request not found\" }";
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
            Transaction tx = session.beginTransaction();
            Order order = session.get(Order.class, orderId);
            if(order == null || !order.getCustomerId().equals(userId)) {
                return "{ \"error\": \"Order not found\" }";
            }
            if(order.getStatus() == OrderStatus.PAID){
                return "{ \"error\": \"Order is paid already\" }";
            }
            if(amount!= order.getTotalPrice()){
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
                order.setStatus(OrderStatus.PAID);
                session.update(order);

                Notification not1 = sendNotification(orderId,amount,method,userId);
                Notification not2 = sendNotification(orderId,amount,method,order.getSellerId());
                session.save(not1);
                session.save(not2);

                tx.commit();
                return "{ \"message\": \"Payment successful\" }";



        }catch(Exception e){
            e.printStackTrace();

            return "{ \"error\": \"Internal Error\" }";
        }
    }
    private Notification sendNotification(Long orderId, double amount, PaymentMethod method, Long userId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setMessage("Payment successful\n" + "orderId: " + orderId + "\n" + "amount: " + amount + "\n" + "method: " + method + "\n" + "payed at : " + LocalTime.now()
        );
        return notification;
    }



}
