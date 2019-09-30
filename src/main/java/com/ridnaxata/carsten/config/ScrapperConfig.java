package com.ridnaxata.carsten.config;

import com.ridnaxata.carsten.service.TrxService;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class ScrapperConfig {

    @PostConstruct
    public void setupWebDriverOnce() {
        WebDriverManager.chromedriver().setup();
    }

    // todo TrxService impls to prototype mode
    // and change this registry way to AbstractFactory way
    // to provide true multithreading
    @Bean
    public List<TrxService> trxsRegistry(@Autowired @Qualifier("snow") TrxService snowSevice,
                                         @Autowired @Qualifier("nimiq") TrxService nimiqSevice,
                                         @Autowired @Qualifier("xdag") TrxService xdagSevice,
                                         @Autowired @Qualifier("veo") TrxService veoSevice) {

        List<TrxService> trxsRegistry = new ArrayList<>();

        trxsRegistry.add(snowSevice);
        trxsRegistry.add(nimiqSevice);
        trxsRegistry.add(xdagSevice);
        trxsRegistry.add(veoSevice);

        return trxsRegistry;
    }


}
