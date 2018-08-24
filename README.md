# Simple Java RPC framework
本代码来自《分布式框架原理与实践》 1.2.2节：最简单的RPC框架实现。

### 原理简介

#### 框架的四个角色：

- 服务提供者。提供服务的接口和具体实现。
- 服务发布者。负责将服务发布成远程服务。
- 本地服务代理。负责调用远程服务，然后将结果返回给本地消费者。
- 本地消费者。本地的应用程序。

#### 服务发布者

1. 监听客户端的TCP连接，接到连接后，将其交给线程池执行。
2. 线程池执行的任务为，从客户端发送的码流中反序列化出客户端想要的服务对象，通过反射调用服务实现者，获取执行结果。
3. 再将结果序列化，通过Socket发送给客户端。

#### 本地服务代理

1. 连接到服务端的TCP端口。
2. 发送所要调用的接口类、方法名、参数列表等给服务端。
3. 等待服务端返回执行结果，再返回给本地消费者。

### 理解下`RpcImporter`的动态代理实现：

动态代理的常见写法为：

```java
public class ProxyHandler implements InvocationHandler
{
    private Object tar;

    //绑定委托对象，并返回代理类
    public Object bind(Object tar)
    {
        this.tar = tar;
        //绑定该类实现的所有接口，取得代理类 
        return Proxy.newProxyInstance(tar.getClass().getClassLoader(),
                                      tar.getClass().getInterfaces(),
                                      this);
    }

    // invoke中的逻辑可以对接口中的所有方法生效。
    public Object invoke(Object proxy , Method method , Object[] args)throws Throwable
    {
        Object result = null;
        //在调用具体函数方法前，执行功能处理
        System.out.println("before invoking...");
        result = method.invoke(tar,args);
        //在调用具体函数方法后，执行功能处理
        System.out.println("after invoking...");
        return result;
    }
}

public class Test {
    public static void main(String[] args) {
        Map map = (Map) new ProxyHandler().bind(new HashMap<>());
        map.put("k","vvvvv");
        System.out.println(map.toString());
    }
}
```

也就是，`Proxy.newProxyInstance()`返回的proxy对象的调用实现在`ProxyHandler`类中实现，方式为它自己实现`InvocationHandler`，实现`invoke()`方法。而`Proxy.newProxyHandler()`的第三个参数指定的 “Invocation Handler” 对象为`this`。所以proxy对象被调用时，`ProxyHandler`对象的`invoke`将被调用。

使用`bind()`方法并传入一个被代理对象，保存在`ProxyHandler`对象实例中，主要还是为了做被代理对象的功能**增强**，而非完全**替换**其功能。毕竟还是需要执行被代理对象的方法的。

而本项目中的写法是直接调用`Proxy.newProxyInstance()`，并给第三个参数传入一个实现了`InvocationHandler`接口的匿名类，`invoke()`方法在匿名类中实现。所以效果是一样的。这里完全没有用到被代理对象的功能，所以就无需`bind()`方法。反而，在创建代理类的`importer()`方法中，加入了`serviceClass`和`address`两个参数。可见动态代理的使用可以是非常灵活的。