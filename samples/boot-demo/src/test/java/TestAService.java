import com.eve.common.entity.User;
import com.eve.common.service.AService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static com.eve.multiple.DatasourceManager.TRANSACTION_MANAGER_PREFIX;

/**
 * Created by xieyang on 19/7/17.
 */
public class TestAService extends TestBaseService {

    @Autowired
    AService aService;

    @Transactional(transactionManager = TRANSACTION_MANAGER_PREFIX+"ds0")
    @Rollback
    @Test
    public void insert(){
        User user = new User();
        user.setAge(11118);
        user.setName("xiexiexie");
        Integer id = aService.save(user);
        System.out.println(id);


    }


}
