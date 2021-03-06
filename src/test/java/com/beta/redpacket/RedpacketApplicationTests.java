package com.beta.redpacket;

import com.beta.RedpacketApplication;
import com.beta.repositry.RepositryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@SpringBootTest(classes = RedpacketApplication.class)
@RunWith(SpringRunner.class)
@WebAppConfiguration
@Slf4j
public class RedpacketApplicationTests {

    @Test
    public void contextLoads() {
    }
    @Autowired
    RepositryService repositryService;

    @Test
    public void test1()
    {
        log.info(repositryService.findByRedid("123").toString());
    }
}

