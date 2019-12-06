import java.util.List;

/**
 * Created by xieyang on 19/11/30.
 */
public interface ModuleCacheService {


    List<String> listModuleItemKeys(String moduleKey);

    List<Object> listModuleItems(List<String>  itemKeys);

}
