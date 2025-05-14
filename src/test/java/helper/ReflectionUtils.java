package helper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import taskSet.TaskSet;

public class ReflectionUtils {
    
    /**
     * Sets the value of a specified field on the given target object using reflection.
     *
     * @param target    the object whose field should be modified
     * @param fieldName the name of the field to set
     * @param value     the new value to assign to the field
     * @throws RuntimeException if the field does not exist or cannot be accessed
     */
    public static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Errore nell'accesso al campo " + fieldName, e);
        }
    }

    /**
     * Retrieves the value of a specified field from the given target object using reflection.
     *
     * @param target    the object from which to retrieve the field value
     * @param fieldName the name of the field to retrieve
     * @return the value of the specified field in the target object
     * @throws RuntimeException if the field does not exist or cannot be accessed
     */
    public static Object getField(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Errore nel leggere il campo " + fieldName, e);
        }
    }

    /**
     * Invokes a no-argument method with the specified name on the given target object using reflection.
     *
     * @param target the object on which to invoke the method
     * @param methodName the name of the method to invoke
     * @throws RuntimeException if the method cannot be found or invoked
     */
    public static void invokeMethod(Object target, String methodName) {
        try {
            var method = target.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(target);
        } catch (Exception e) {
            throw new RuntimeException("Errore nell'invocazione del metodo " + methodName, e);
        }
    }

    /**
     * Invokes a argument method with the specified name on the given target object using reflection.
     *
     * @param target the object on which to invoke the method
     * @param methodName the name of the method to invoke
     * @param params the parameters to pass to the method
     * @throws RuntimeException if the method cannot be found or invoked
     */
    public static Object invokeMethod(Object target, String methodName, Object... params) {
        try {
            Class<?>[] paramTypes = Arrays.stream(params)
                .map(Object::getClass)
                .toArray(Class<?>[]::new);
            
            Method method = target.getClass().getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            return method.invoke(target, params);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method " + methodName, e);
        }
    }

    public static Object createContextInstance(TaskSet taskSet) {
        try {
            Class<?> contextClass = Class.forName("scheduler.RMScheduler$Context");
            Constructor<?> constructor = contextClass.getDeclaredConstructor(TaskSet.class);
            constructor.setAccessible(true);
            return constructor.newInstance(taskSet);
        } catch (Exception e) {
            throw new RuntimeException("Impossibile creare l'istanza di Context via reflection", e);
        }
    }

}