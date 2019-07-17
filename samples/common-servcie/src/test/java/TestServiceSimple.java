

import com.eve.common.entity.User;
import com.eve.common.service.AService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by xieyang on 19/7/4.
 */
public class TestServiceSimple {
    public final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testDefaultQuery(){
        String[]  configurLocations = {"spring-application.xml"};
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(configurLocations) ;
        AService aService = context.getBean(AService.class);
        User user = aService.queryById(2);
        System.out.println("xxxxxx");
    }

    @Test
    public void testDefaultUpdate() {
        String[] configurLocations = {"spring-application.xml"};
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(configurLocations);
        AService aService = context.getBean(AService.class);
        User user = new User();
        user.setAge(111);
        user.setName("777777");
        aService.save(user);
        logger.debug("{}", user);
    }


    @Test
    public void testMutipleDaoQuery(){
        String[]  configurLocations = {"spring-application.xml"};
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(configurLocations) ;
        AService aService = context.getBean(AService.class);
        aService.queryByIdMutipleDao(2);

    }



    @Test
    public void testMutipleDaoUpdate(){
        String[]  configurLocations = {"spring-application.xml"};
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(configurLocations) ;
        AService aService = context.getBean(AService.class);
        User user = new User();
        user.setAge(111);
        user.setName("4444444");
        aService.saveMutipleDao(user);

        System.out.printf("66666");

    }


    @Test
    public void testMutipleOperate() {
        String[] configurLocations = {"spring-application.xml"};
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(configurLocations);
        AService aService = context.getBean(AService.class);
        aService.mutipleOperate();
    }


    @Test
    public void testMutipleOperate2() {
        String[] configurLocations = {"spring-application.xml"};
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(configurLocations);
        AService aService = context.getBean(AService.class);
        aService.mutipleOperate2();
    }


}
