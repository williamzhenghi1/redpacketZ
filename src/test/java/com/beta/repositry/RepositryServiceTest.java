package com.beta.repositry;

import com.beta.RedpacketApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.*;
@SpringBootTest(classes = RedpacketApplication.class)
@RunWith(SpringJUnit4ClassRunner.class) // SpringJUnit支持，由此引入Spring-Test框架支持！
@Slf4j
public class RepositryServiceTest {

    @Autowired
    RepositryService repositryService;

    @Test
    public void test1()
    {
       log.info(repositryService.findByRedid("123").toString());
    }

}