/* PassHashMap.java */
import datadog.opentracing.DDTracer;
import datadog.trace.api.DDTags;
import io.opentracing.*;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import java.util.Map;
import java.util.HashMap;

public class PassHashMap {

  /* This is the native method we want to call */
  public static native int displayHashMap(String[] keys, String[] values);

  /* Inside static block we will load shared library */
  static {
    System.loadLibrary("PassHashMap");
  }

  public static void main(String[] args) throws InterruptedException {
    /* This message will help you determine whether
       LD_LIBRARY_PATH is correctly set
    */
    System.out.println("library: " + System.getProperty("java.library.path"));

    /* Create object to pass */

        Tracer tracer = DDTracer.builder().build();

        ScopeManager sm = tracer.scopeManager();
        Tracer.SpanBuilder tb = tracer.buildSpan("servlet.request");

        Map<String,String> map=new HashMap<>();

        Span span = tb.start();
        try(Scope scope = sm.activate(span)){
            tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapAdapter(map));
            span.setTag(DDTags.SERVICE_NAME, "java");
            span.setTag(DDTags.RESOURCE_NAME, "GET /test");
            span.setTag(DDTags.SPAN_TYPE, "web");
            try {
                System.out.println("Tracing in the Java layer and calling C++");

                /* We have to convert HashMap to Array */
                String[] keys = map.keySet().toArray(new String[0]);
                String[] values = new String[keys.length];
                int idx = 0;
                for (String key : keys) {
                    values[idx++] = map.get(key);
                }

                /* Call to shared library */
                PassHashMap.displayHashMap(keys, values);

                Thread.sleep(20);
            } catch(InterruptedException e){
                e.printStackTrace();
            }
            span.finish();
        }
        Thread.sleep(2000L);
  }
}
