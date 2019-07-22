package test.com.eve.datasource;

import com.eve.common.entity.User;
import com.eve.common.service.AService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by xieyang on 19/7/4.
 */
public class TestServiceComplex {
    public final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testDefaultQuery(){
        String[]  configurLocations = {"spring-beans.xml"};
        ClassPathXmlApplicationContext context = new ConfigurationApplicationContext(configurLocations);
        AService aService = context.getBean(AService.class);
        User user = aService.queryById(2);
        System.out.println("xxxxxx");
    }



}
