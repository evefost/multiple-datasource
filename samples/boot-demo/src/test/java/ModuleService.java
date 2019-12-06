import java.util.List;

/**
 * Created by xieyang on 19/11/30.
 */
public class ModuleService {

    private ModuleCacheService moduleCacheService;

    private ModuleItemConverter itemConverter;

    public List<Object>  listModuleItemByPage(){

        prepareParams();
        String moduleKey ="module_1";
        List<String> moduleItems = moduleCacheService.listModuleItemKeys(moduleKey);
        List<Object> itemsData = moduleCacheService.listModuleItems(moduleItems);
        List<Object> objects = itemConverter.convertItemData(itemsData);
        return objects;

    }

    private void prepareParams() {

    }





}
