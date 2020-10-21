## 执行顺序
代码->编译->字节码(也可打包成Jar文件)->jvm加载需要的字节码，变成持久代/元数据区上的class对象->执行逻辑。

## 归档与执行
 将 class 文件和java源文件归档到一个名为hello.jar 的档案中
```java
  jar cvf hello.jar Hello.class Hello.java
```
归档的同时，通过 e 选项指定jar的启动类 Hello 
```java
   jar cvfe hello.jar Hello Hello.class Hello.java
```
‐jar 选项来执行jar包
```java
   java ‐jar hello.jar
```
## java程序的第一步就是加载class文件
### 1.类的生命周期和加载过程
类的生命周期七个步骤
<font color='red' size='3px'>加载 loading</font >
<font color='red' size='3px'>校验 verification</font >
<font color='red' size='3px'>准备 preparation</font >
<font color='red' size='3px'>解析 resolution</font >
<font color='red' size='3px'>初始化 initalization</font >
<font color='black' size='3px'>使用 using</font >
<font color='black' size='3px'>卸载 Unloading</font >

红色部分统称为类加载 
其中校验，准备 ，解析 三个过程叫做链接(Linking)

1）加载
 根据明确的的class完全限定名，获取二进制classfile格式的字节流(找到文件系统的/jar中的任何地方的class文件) 如果找不到抛出NoClassDefFound
 
本阶段不会检查获取到的classfile的语法与格式。

类加载的整个过程主要由jvm和java的类加载系统共同完成，具体到加载 loading就是JVM与具体的某一个类加载（java.lang.classLoader）写作完成

2）校验
链接的第一个阶段校验。
确保class文件里的字节流信息符合当前虚拟机的要求，不会危害虚拟机。
校验过程检查classfile的语义，判断常量池中的符号，并执行类型检查
主要是判断字节码的合法性，比如magic number，对版本号进行验证。这些检查可能会抛出VerifyError，ClassFormatError或者UnsuppertedClassVersionError。

在进行classfile的验证的时候，JVM必须加载其所有的超类和接口。

如果类层次结构有问题，如该类是自己的超类或者接口死循环，则jvm将抛出ClassCircularityError。
而如果实现的接口不是一个interface，或者声明的超类是一个interface则会抛出incompatibleClassChangeError。

3）准备
准备阶段将会创建静态字段，并将其初始化为标准默认值（如null 0），并分配方法表，即在方法区中分配这个变量所使用的内存空间。
该阶段并未执行任何java代码。
```java
public static int i = 1;
```
准备阶段i会被初始化为0，类初始化阶段才会赋值1；

```java
public static final int i = 1;
```
 常量i，在准备阶段就会被赋值1

4）解析
解析符号引用/解析常量池

主要有以下四种
类或接口的解析
字段解析
类方法解析
接口方法解析

例如一个变量引用一个对象，这个引用在class文件中以符号引用来存储的。
解析阶段就是将其解析并链接为直接引用（相当于指向实际对象），而如果有了直接引用，那引用的对象必定在堆中存在。
加载一个class时，需要加载所有的super类和super接口。

5）初始化
JVM规范明确规定, 必须在类的首次“主动使用”时才能执行类初始化。

初始化的过程包括执行：
类构造器方法 
static静态变量赋值语句
static静态代码块

如果要对一个子类进行初始化，会先对父类进行初始化，所以在java中初始化一个类，那么必然先初始化过java.lang.Object类，因为所有的java类都继承自java.lang.Object。

为了提高性能，HotSpot JVM 通常要等到类初始化时才去装载和链接类。 因此， 如果A类引用了B类，那么加载A类并不一定会去加载B类（除非需要进行验证）。 主动对B类执行第一条指令时才会导致B类的初始化，这就需要先完成对B类的装 载和链接

### 2. 类加载时机
类的初始化何时会被触发

 - 当虚拟机启动时，初始化用户指定的主类，就是启动执行的 main方法所在的 类；
 - 当遇到用以新建目标类实例的 new 指令时，初始化 new 指令的目标类，就是 new一个类的时候要初始化；
 - 当遇到调用静态方法的指令时，初始化该静态方法所在的类；
 - 当遇到访问静态字段的指令时，初始化该静态字段所在的类；
 - 子类的初始化会触发父类的初始化；
 - 如果一个接口定义了 default 方法，那么直接实现或者间接实现该接口的类的初 始化，会触发该接口的初始化；
 - 使用反射 API 对某个类进行反射调用时，初始化这个类，其实跟前面一样，反射 调用要么是已经有实例了，要么是静态方法，都需要初始化；
 - 当初次调用 MethodHandle 实例时，初始化该 MethodHandle 指向的方法所在的 类。

同时以下几种情况不会执行类初始化：
- 通过子类引用父类的静态字段，只会触发父类的初始化，而不会触发子类的初始化。
- 常量在编译期间会存入调用类的常量池中，本质上并没有直接引用定义常量的 类，不会触发定义常量所在的类。
- 通过类名获取Class对象，不会触发类的初始化，Hello.class不会让Hello类初始化。
- 通过Class.forName加载指定类时，如果指定参数initialize为false时，也不会触发 类初始化，其实这个参数是告诉虚拟机，是否要对类进行初始化。Class.forName(“jvm.Hello”)默认会加载Hello类。
- 通过ClassLoader默认的loadClass方法，也不会触发初始化动作（加载了，但是不初始化）。

诸如 Class.forName(), classLoader.loadClass() 等Java API, 反射API, 以及 JNI_FindClass 都可以启动类加载。 JVM本身也会进行类加载。 比如在JVM启动时加载核心类，java.lang.Object, java.lang.Thread 等等。

### 3. 类加载器机制
类加载过程可以描述为 通过一个类的全限定名a.b.c.XXClass来获取描述此类的Class 对象，这个过程由“类加载器（ClassLoader）来完成。这样的好处在于，子类加载器可以复用父加载器加载的类

系统自带的类加载器分为三种
启动类加载器（BootstrapClassLoader）  C++实现的 代码中找不到
扩展类加载器（ExtClassLoader） 
应用类加载器（AppClassLoader）

一般启动类加载器（BootstrapClassLoader） 由JVM内部实现，javaAPI无法拿到

扩展类加载器（ExtClassLoader） 
应用类加载器（AppClassLoader）
以上两种加载器在Oracle Hotspot JVM中，是在sun.misc.Launcher定义的，扩展类加载器和应用类加载器一般都是继承自URLClassLoader类。
这个类默认实现了从各种不同来源加载class字节码转换成Class的方法

ClassLoader
URLClassLoader
ExtClassLoader ， AppClassLoader

1）bootstrap class loader 
启动类加载器用来加载java的核心类的并不继承 java.lang.ClassLoader（负责加载JDK中 jre/lib/rt.jar里所有的class）

bootstrap class loader 可看作是jvm自带的
代码层面无法直 接获取到启动类加载器的引用，所以不允许直接操作它， 如果打印出来就是个 null

举例来说，java.lang.String是由启动类加载器加载的，所以 String.class.getClassLoader()就会返回null

2）ExtClassLoader 
扩展类加载器，负责加载jre的扩展目录
lib/ext 或者由java.ext.dirs系统属性指定的目录中的JAR包的类，代码里直接获取它的父 类加载器为null（因为无法拿到启动类加载器）。

3）AppClassLoader
应用类加载器负载在JVM启动时加载来自Java命令的-classpath 或者-cp选项，java.class.path系统属性指定的jar包和类路径。如果没有自定义类加载器的情况下，用户自定义的类都是这个加载器加载的。

4）自定义类加载器
如果用户自定义类加载器，则自定义类加载器都以AppClassLoader作为父类加载器。
AppClassLoader加载器的父类加载器为ExtClassLoader。

bootstrap class loader 又叫根加载器，是ExtClassLoader的父加载器，但是直接从ExtClassLoader是拿不到他的引用的，同样会返回null

类加载机制有三个特点：
1. 双亲委托：当一个自定义类加载器需要加载一个类，比如java.lang.String，它很懒，不会一上来就直接试图加载它，而是先委托自己的父加载器去加载，父加载器如果发现自己还有父加载器，会一直往前找，这样只要上级加载器，比如启动 类加载器已经加载了某个类比如java.lang.String，所有的子加载器都不需要自己 加载了。如果几个类加载器都没有加载到指定名称的类，那么会抛出 ClassNotFountException异常。
2. 负责依赖：如果一个加载器在加载某个类的时候，发现这个类依赖于另外几个类 或接口，也会去尝试加载这些依赖项。
3. 缓存加载：为了提升加载效率，消除重复加载，一旦某个类被一个类加载器加 载，那么它会缓存这个加载结果，不会重复加载。


```java
package bean;

import sun.misc.Launcher;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

public class JvmClassLoaderPrintPath {
    public static void main(String[] args) {
        //启动类加载器
        URL[] urLs = Launcher.getBootstrapClassPath().getURLs();
        System.out.println("启动类加载器");
        for (URL urL : urLs) {
            System.out.println(" ==> "+ urL.toExternalForm());
        }

        //扩展类加载器
        printClassLoader("扩展类加载器", JvmClassLoaderPrintPath.class.getClassLoader().getParent());
        //应用类加载器
        printClassLoader("应用类加载器", JvmClassLoaderPrintPath.class.getClassLoader());

    }

    public static void printClassLoader(String name, ClassLoader CL) {
        if (CL != null) {
            System.out.println(name + "ClassLoader ->" + CL.toString());
            printURLForClassLoader(CL);
        } else {
            System.out.println(name + "ClassLoader -> null");
        }
    }

    private static void printURLForClassLoader(ClassLoader cl) {
        Object ucp = insightField(cl, "ucp");
        Object path = insightField(ucp, "path");
        ArrayList ps = (ArrayList) path;
        for (Object p : ps) {
            System.out.println(" ==> " + p.toString());
        }
    }

    private static Object insightField(Object obj, String ucp) {
        try {
            Field f = null;
            if (obj instanceof URLClassLoader) {
                f = URLClassLoader.class.getDeclaredField(ucp);
            } else {
                f = obj.getClass().getDeclaredField(ucp);
            }
            f.setAccessible(true);
            return f.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

```

输出结果
主要内容在 这个包里 file:/E:/JDK8_64/jre/lib/rt.jar
```java
启动类加载器
 ==> file:/E:/JDK8_64/jre/lib/resources.jar
 ==> file:/E:/JDK8_64/jre/lib/rt.jar
 ==> file:/E:/JDK8_64/jre/lib/sunrsasign.jar
 ==> file:/E:/JDK8_64/jre/lib/jsse.jar
 ==> file:/E:/JDK8_64/jre/lib/jce.jar
 ==> file:/E:/JDK8_64/jre/lib/charsets.jar
 ==> file:/E:/JDK8_64/jre/lib/jfr.jar
 ==> file:/E:/JDK8_64/jre/classes
扩展类加载器ClassLoader ->sun.misc.Launcher$ExtClassLoader@28d93b30
 ==> file:/E:/JDK8_64/jre/lib/ext/access-bridge-64.jar
 ==> file:/E:/JDK8_64/jre/lib/ext/cldrdata.jar
 ==> file:/E:/JDK8_64/jre/lib/ext/dnsns.jar
 ==> file:/E:/JDK8_64/jre/lib/ext/jaccess.jar
 ==> file:/E:/JDK8_64/jre/lib/ext/jfxrt.jar
 ==> file:/E:/JDK8_64/jre/lib/ext/localedata.jar
 ==> file:/E:/JDK8_64/jre/lib/ext/nashorn.jar
 ==> file:/E:/JDK8_64/jre/lib/ext/sunec.jar
 ==> file:/E:/JDK8_64/jre/lib/ext/sunjce_provider.jar
 ==> file:/E:/JDK8_64/jre/lib/ext/sunmscapi.jar
 ==> file:/E:/JDK8_64/jre/lib/ext/sunpkcs11.jar
 ==> file:/E:/JDK8_64/jre/lib/ext/zipfs.jar
应用类加载器ClassLoader ->sun.misc.Launcher$AppClassLoader@14dad5dc
 ==> file:/E:/JDK8_64/jre/lib/charsets.jar
 ==> file:/E:/JDK8_64/jre/lib/deploy.jar
 ==> file:/E:/JDK8_64/jre/lib/ext/access-bridge-64.jar
 ==> file:/E:/JDK8_64/jre/lib/ext/cldrdata.jar
 ==> file:/E:/JDK8_64/jre/lib/ext/dnsns.jar
 ==> file:/E:/JDK8_64/jre/lib/ext/jaccess.jar
 ==> file:/E:/JDK8_64/jre/lib/ext/jfxrt.jar
 ==> file:/E:/JDK8_64/jre/lib/ext/localedata.jar
 ==> file:/E:/JDK8_64/jre/lib/ext/nashorn.jar
 ==> file:/E:/JDK8_64/jre/lib/ext/sunec.jar
 ==> file:/E:/JDK8_64/jre/lib/ext/sunjce_provider.jar
 ==> file:/E:/JDK8_64/jre/lib/ext/sunmscapi.jar
 ==> file:/E:/JDK8_64/jre/lib/ext/sunpkcs11.jar
 ==> file:/E:/JDK8_64/jre/lib/ext/zipfs.jar
 ==> file:/E:/JDK8_64/jre/lib/javaws.jar
 ==> file:/E:/JDK8_64/jre/lib/jce.jar
 ==> file:/E:/JDK8_64/jre/lib/jfr.jar
 ==> file:/E:/JDK8_64/jre/lib/jfxswt.jar
 ==> file:/E:/JDK8_64/jre/lib/jsse.jar
 ==> file:/E:/JDK8_64/jre/lib/management-agent.jar
 ==> file:/E:/JDK8_64/jre/lib/plugin.jar
 ==> file:/E:/JDK8_64/jre/lib/resources.jar
 ==> file:/E:/JDK8_64/jre/lib/rt.jar
 ==> file:/C:/Users/CodeTest/IdeaProjects/helloJava/out/production/helloJava/
 ==> file:/C:/Program%20Files/JetBrains/IntelliJ%20IDEA%202019.1.2/lib/idea_rt.jar
```
