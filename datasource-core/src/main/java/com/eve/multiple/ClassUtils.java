package com.eve.multiple;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 类说明
 * <p>
 *
 * @author xieyang
 * @date 2019/8/14
 */
public class ClassUtils {

    public static List<Class<?>> findAllInterface(Class<?> clazz) {

        List<Class<?>> classes = new ArrayList<>();

        Class<?>[] interfaces = clazz.getInterfaces();

        if (interfaces.length == 0) {
            return classes;
        }
        for (Class<?> ifc : interfaces) {
            classes.add(ifc);
            Class<?>[] innerInterfaces = ifc.getInterfaces();
            if (innerInterfaces.length > 0) {
                classes.addAll(findAllInterface(ifc));
            }

        }
        return classes;
    }


    public static String methodParamsToString(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0) {
            return "(Void)";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (Class cl : parameterTypes) {
            sb.append(cl.getSimpleName()).append(",");
        }
        String substring = sb.substring(0, sb.length() - 1);
        return substring + ")";
    }
}
