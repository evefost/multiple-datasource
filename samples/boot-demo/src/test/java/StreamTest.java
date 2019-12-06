import java.util.ArrayList;
import java.util.List;

/**
 * Created by xieyang on 19/12/4.
 */
public class StreamTest {

    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);
        list.add(6);
        list.add(7);
        list.add(8);
        list.add(9);
        list.add(10);
        long l = System.currentTimeMillis();
        list.stream().parallel().forEach(i->{
            System.out.println(i+"===thread:"+Thread.currentThread().getId());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        long l1 = System.currentTimeMillis() - l;
        System.out.printf("耗时====="+l1);

    }
}
