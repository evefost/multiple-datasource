import com.eve.boot.MyBootApplication;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MyBootApplication.class)
@WebAppConfiguration
//@Rollback//保证每次测试类执行完后数据库进行回滚，防止测试时产生脏数据
//@Transactional
public class TestBaseService {



}
