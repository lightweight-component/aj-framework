package org.example;

import com.ajaxjs.service.tools.IIdCard;
import org.apache.dubbo.config.bootstrap.builders.ReferenceBuilder;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class JpaUtil {
    private static final EntityManagerFactory entityManagerFactory =
            Persistence.createEntityManagerFactory("my-persistence-unit");

    public static EntityManager getEntityManager() {
        return entityManagerFactory.createEntityManager();
    }
}
