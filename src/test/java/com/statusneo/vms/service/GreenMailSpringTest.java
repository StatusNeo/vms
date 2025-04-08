package com.statusneo.vms.service;

import com.icegreen.greenmail.spring.GreenMailBean;
import com.icegreen.greenmail.util.GreenMail;
import org.springframework.beans.factory.annotation.Autowired;

//@SpringBootTest
public class GreenMailSpringTest {

    @Autowired
    private GreenMailBean greenMailBean;

//    @Test
    public void testCreate() {
        GreenMail greenMail = greenMailBean.getGreenMail();
    }

}
