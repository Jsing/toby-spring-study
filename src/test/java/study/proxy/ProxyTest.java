package study.proxy;

import java.lang.reflect.Proxy;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import config.AppContext;
import config.TestAppContect;
import study.proxy.advice.UppercaseAdvice;
import study.proxy.invocation.UpperCaseHandler;
import study.proxy.proxy.HelloUppercase;
import study.proxy.target.Hello;
import study.proxy.target.HelloImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { AppContext.class, TestAppContect.class })
@ActiveProfiles("test")
public class ProxyTest {
    @Autowired
    ApplicationContext context;

    @Test
    public void manualProxy() {
        Hello hello = new HelloUppercase(new HelloImpl());
        Assertions.assertEquals("HELLO TOBY", hello.sayHello("Toby"));
        Assertions.assertEquals("HI TOBY", hello.sayHi("Toby"));
        Assertions.assertEquals("THANK YOU TOBY", hello.sayThankYou("Toby"));
    }

    @Test
    public void dynamicProxy() {
        Hello hello = (Hello) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[] { Hello.class },
                new UpperCaseHandler(new HelloImpl(), "say"));

        Assertions.assertEquals("HELLO TOBY", hello.sayHello("Toby"));
        Assertions.assertEquals("HI TOBY", hello.sayHi("Toby"));
        Assertions.assertEquals("THANK YOU TOBY", hello.sayThankYou("Toby"));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void proxyFactoryBean() {
        ProxyFactoryBean factoryBean = new ProxyFactoryBean();
        factoryBean.setTarget(new HelloImpl());
        factoryBean.addAdvice(new UppercaseAdvice());
        Hello hello = (Hello) factoryBean.getObject();

        Assertions.assertEquals("HELLO TOBY", hello.sayHello("Toby"));
        Assertions.assertEquals("HI TOBY", hello.sayHi("Toby"));
        Assertions.assertEquals("THANK YOU TOBY", hello.sayThankYou("Toby"));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void pointCutAdvisor() {
        // ???????????? ProxyFactoryBean ??????
        ProxyFactoryBean factoryBean = new ProxyFactoryBean();

        // ??????????????? ?????? ??????
        factoryBean.setTarget(new HelloImpl());

        // ??????????????? ??????????????? ????????? ??????
        NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
        pointcut.setMappedName("sayH*");
        UppercaseAdvice advice = new UppercaseAdvice();

        // ??????????????? ???????????? ??????
        factoryBean.addAdvisor(new DefaultPointcutAdvisor(pointcut, advice));

        // ????????? ??????
        Hello hello = (Hello) factoryBean.getObject();

        Assertions.assertEquals("HELLO TOBY", hello.sayHello("Toby"));
        Assertions.assertEquals("HI TOBY", hello.sayHi("Toby"));
        Assertions.assertEquals("Thank You Toby", hello.sayThankYou("Toby"));
    }

    @SuppressWarnings("serial")
    @Test
    public void classNamePointCutAdvisor() {
        // ???????????? ??????
        NameMatchMethodPointcut classMethodPointCut = new NameMatchMethodPointcut() {
            // ?????? ?????? ???????????? ???????????? ???????????? ?????? ???????????? ????????? ??? ?????????.
            @Override
            public ClassFilter getClassFilter() {
                return clazz -> clazz.getSimpleName().startsWith("HelloI");
            }
        };
        classMethodPointCut.setMappedName("sayH*");

        // ?????????
        checkAdviced(new HelloImpl(), classMethodPointCut, true);
        // ?????? ????????? ?????? ???????????? ????????? ?????? ??? ?????????.
        class HelloWorld extends HelloImpl {}
        checkAdviced(new HelloWorld(), classMethodPointCut, false);
        class HelloToby extends HelloImpl {}
        checkAdviced(new HelloToby(), classMethodPointCut, false);
    }

    private static void checkAdviced(Object target, Pointcut pointcut, boolean adviced) {
        // 1. ????????? ????????? ??? ??????
        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
        // 2. ?????? ??????
        proxyFactoryBean.setTarget(target);
        // 3. ????????? ????????? ???????????? ?????? ????????? ????????? ??? ????????? ?????? PointCut ????????? ????????? Advisor ??????
        proxyFactoryBean.addAdvisor(new DefaultPointcutAdvisor(pointcut, new UppercaseAdvice()));
        // 4. ????????? ??????
        Hello proxiedHello = (Hello) proxyFactoryBean.getObject();

        assert proxiedHello != null;
        if (adviced) {
            Assertions.assertEquals("HELLO TOBY", proxiedHello.sayHello("Toby"));
            Assertions.assertEquals("HI TOBY", proxiedHello.sayHi("Toby"));
        } else {
            Assertions.assertEquals("Hello Toby", proxiedHello.sayHello("Toby"));
            Assertions.assertEquals("Hi Toby", proxiedHello.sayHi("Toby"));
        }
        Assertions.assertEquals("Thank You Toby", proxiedHello.sayThankYou("Toby"));
    }
}
