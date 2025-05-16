package com.game.repository;

import com.game.entity.Player;
import org.hibernate.*;
import org.hibernate.cfg.*;
import org.hibernate.query.*;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {
    private final SessionFactory sessionFactory;

    public PlayerRepositoryDB() {
        Properties properties = new Properties();
        properties.put(Environment.DIALECT,"org.hibernate.dialect.MySQLDialect");
        //properties.put(Environment.DRIVER,"com.mysql.cj.jdbc.Driver");
        //properties.put(Environment.URL,"jdbc:mysql://localhost:3306/rpg");
        properties.put(Environment.DRIVER,"com.p6spy.engine.spy.P6SpyDriver");
        properties.put(Environment.URL, "jdbc:p6spy:mysql://localhost:3306/rpg");
        properties.put(Environment.USER,"root");
        properties.put(Environment.PASS,"1251grif");
//        properties.put(Environment.SHOW_SQL, "true");
//        properties.put(Environment.FORMAT_SQL, "true");
        properties.put(Environment.HBM2DDL_AUTO, "update");
        sessionFactory = new Configuration()
                .setProperties(properties)
                .addAnnotatedClass(Player.class)
                .buildSessionFactory();
    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        List<Player> players = new ArrayList<>();
        try(Session session = sessionFactory.openSession()){
            String sql = "SELECT * FROM player";
            NativeQuery<Player> nativeQuery = session.createNativeQuery(sql, Player.class);
            nativeQuery.setFirstResult(pageNumber * pageSize);
            nativeQuery.setMaxResults(pageSize);
            players = nativeQuery.list();
        } catch (HibernateException he){
            he.printStackTrace();
        }
        return players;
    }

    @Override
    public int getAllCount() {
        int count = 0;
        try(Session session = sessionFactory.openSession()){
            Long totalCount = session.createNamedQuery("Player_FindTotalCount", Long.class).uniqueResult();
            try{
                count = Math.toIntExact(totalCount);
            } catch (ArithmeticException e){
                e.printStackTrace();
            }
        } catch (HibernateException he){
            he.printStackTrace();
        }
        return count;
    }

    @Override
    public Player save(Player player) {
        Transaction transaction = null;
        Long id = null;
        Player playerDB = null;
        try(Session session = sessionFactory.openSession()){
            transaction = session.beginTransaction();
           id =(Long) session.save(player);
           transaction.commit();
            playerDB = session.get(Player.class, id);
        } catch (HibernateException he){
            transaction.rollback();
        }
        return playerDB;
    }

    @Override
    public Player update(Player player) {
        Transaction transaction = null;
        Player playerDB = null;
        try(Session session = sessionFactory.openSession()){
            transaction = session.beginTransaction();
            session.update(player);
            transaction.commit();
            playerDB = session.get(Player.class, player.getId());
        } catch (HibernateException he){
            transaction.rollback();
        }
        return playerDB;
    }

    @Override
    public Optional<Player> findById(long id) {
        try(Session session = sessionFactory.openSession()){
            Player player = session.find(Player.class, id);
            return Optional.ofNullable(player);
        }
    }

    @Override
    public void delete(Player player) {
        Transaction transaction = null;
    try(Session session = sessionFactory.openSession()){
        transaction = session.beginTransaction();
        session.remove(player);
        transaction.commit();
    } catch (HibernateException he){
        transaction.rollback();
    }
    }

    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();
    }
}