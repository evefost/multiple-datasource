import com.eve.common.entity.User;
import com.eve.common.service.AService;
import com.eve.multiple.DatabaseMeta;
import com.eve.multiple.DatasourceManager;
import com.eve.multiple.RouteContextManager;
import com.eve.multiple.interceptor.ServiceInterceptor;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.eve.multiple.DatasourceManager.TRANSACTION_MANAGER_PREFIX;

/**
 * Created by xieyang on 19/7/17.
 */
public class TestAService extends TestBaseService {


    @Autowired
    AService aService;

    @Autowired
    private DatasourceManager datasourceManager;

    @Transactional(transactionManager = TRANSACTION_MANAGER_PREFIX + "ds0")
    @Rollback
    @Test
    public void insert() {
        User user = new User();
        user.setAge(11118);
        user.setName("xiexiexie");
        Integer id = aService.save(user);
        System.out.println(id);


    }


    /**
     * 多程程测试
     */

    @Test
    public void testMultipleThread() throws InterruptedException {
        int count = 1;
        CountDownLatch countDownLatch = new CountDownLatch(count);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Random random = new Random();
        List<Throwable> throwables = new ArrayList<Throwable>();
        for (int i = 0; i < count; i++) {
            int tenantId = random.nextInt(2) + 1;
            InsertTask insertTask = new InsertTask(aService, String.valueOf(tenantId), i, countDownLatch, throwables, datasourceManager);
            executorService.submit(insertTask);
        }
        countDownLatch.await();
        assert throwables.size() == 0;

    }


    static class InsertTask implements Runnable {

        private AService aService;

        private String tenantId;
        private int index;
        CountDownLatch countDownLatch;
        List<Throwable> throwables;
        DatasourceManager datasourceManager;


        public InsertTask(AService aService, String tenantId, int index, CountDownLatch countDownLatch, List<Throwable> throwables, DatasourceManager datasourceManager) {
            this.aService = aService;
            this.tenantId = tenantId;
            this.index = index;
            this.countDownLatch = countDownLatch;
            this.throwables = throwables;
            this.datasourceManager = datasourceManager;
        }

        @Override
        public void run() {

            RouteContextManager.setCurrentTenant(tenantId);
            boolean andSetDatabase = findAndSetDatabase();
            if(!andSetDatabase){
              throw new RuntimeException("找不到database meta");
            }
            DataSourceTransactionManager transactionManager = datasourceManager.getTransactionManager();
            TransactionStatus transactionStatus = beginTransaction(transactionManager);
            String name = System.nanoTime() + "_" + index;
            User user = new User();
            user.setAge(11118);
            user.setName(name);
            try {
                Integer affectRows = aService.save(user);
                assert affectRows > 0;
                User result = aService.queryByName(name);
                assert result != null;
                transactionManager.commit(transactionStatus);
            } catch (Throwable throwable) {
                throwables.add(throwable);
            } finally {
                countDownLatch.countDown();
                RouteContextManager.setCurrentTenant(null);
                transactionManager.rollback(transactionStatus);

            }
        }

        private TransactionStatus beginTransaction(DataSourceTransactionManager transactionManager) {

            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW); // 事物隔离级别，开启新事务，这样会比较安全些。
            TransactionStatus transaction = transactionManager.getTransaction(def);
            return transaction;
        }

        private boolean findAndSetDatabase(){
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(aService);
            if(invocationHandler instanceof ServiceInterceptor){
                ServiceInterceptor serviceInterceptor = (ServiceInterceptor) invocationHandler;
                Class<?> targetClass = serviceInterceptor.getTargetClass();
                try {
                    Method method = targetClass.getMethod("save", User.class);
                    DatabaseMeta database = RouteContextManager.getDatabase(targetClass, method);
                    RouteContextManager.setCurrentDatabase(database, false);
                    return true;
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
    }


}
