package com.DAO;

import com.models.Wallet;
import com.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class WalletDAO {
    public String updateWallet(Long id, double amount) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction tx = session.beginTransaction();
            Wallet wallet = session.createQuery("from Wallet where userId = :id", Wallet.class).setParameter("id", id).uniqueResult();

            if(wallet == null) {
                return "{ \"error\": \"Not Found\" }";
            }
            wallet.addAmount(amount);
            session.update(wallet);
            tx.commit();
            return "{ \"message\": \"Wallet topped up successfully\" }";


        }catch(Exception e){
            return "{ \"error\": \"Internal Error\" }";
        }
    }
}
