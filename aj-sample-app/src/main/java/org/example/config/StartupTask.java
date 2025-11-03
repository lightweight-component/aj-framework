package org.example.config;

import com.ajaxjs.framework.database.DataBaseConnection;
import com.ajaxjs.sqlman.model.UpdateResult;
import com.ajaxjs.sqlman_v2.Action;
import com.ajaxjs.sqlman_v2.crud.Update;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupTask implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) {
        DataBaseConnection.initDb();

        System.out.println("✅ 所有 Bean 已加载完成，应用启动后执行初始化任务...");
        String sql = "CREATE TABLE shop_address (\n" +
                "    id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                "    name VARCHAR(255) NOT NULL,\n" +
                "    address VARCHAR(255) NOT NULL,\n" +
                "    phone VARCHAR(20),\n" +
                "    receiver VARCHAR(255),\n" +
                "    stat INT,\n" +
                "    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
                "    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP\n" +
                ");";
        Action action = new Action(sql);
        Update update = new Update(action);
        UpdateResult execute = update.execute();

//        log.info("Executed: {}", execute);

        action.setSql("INSERT INTO shop_address (name, address, phone, receiver, stat)\n" +
                "VALUES\n" +
                "('Shop A', '123 Main St', '123-456-7890', 'John Doe', 0),\n" +
                "('Shop B', '456 Elm St', '234-567-8901', 'Jane Smith',0),\n" +
                "('Shop C', '789 Oak St', '345-678-9012', 'Alice Johnson', 0),\n" +
                "('Shop D', '101 Maple St', '456-789-0123', 'Bob Brown', 1),\n" +
                "('Shop E', '202 Birch St', '567-890-1234', 'Charlie Davis', 1);");
        new Update(action).update();
    }
}