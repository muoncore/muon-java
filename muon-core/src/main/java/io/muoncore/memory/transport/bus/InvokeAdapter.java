package io.muoncore.memory.transport.bus;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public class InvokeAdapter implements Consumer<Object> {

  private Object delegate;
  private Class<? extends Annotation> annotation;

  public InvokeAdapter(Object target, Class<? extends Annotation> annotation) {
    this.delegate = target;
    this.annotation = annotation;
  }

  @Override
  public void accept(Object event) {
    final Method[] methods = delegate.getClass().getMethods();
    for (Method method : methods) {
      if (method.getName().startsWith("lambda$") || !method.isAnnotationPresent(annotation)) {
        continue;
      }
      final Class<?>[] parameterTypes = method.getParameterTypes();
      for (Class<?> parameterType : parameterTypes) {
        if (parameterType.isAssignableFrom(event.getClass())) {
          try {
            method.invoke(delegate, event);
          } catch (Exception e) {
            throw new IllegalStateException("Unable to handle event: ".concat(event.getClass().getName()), e);
          }
        }
      }
    }
  }
}
