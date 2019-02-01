package com.beta.service;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;

public interface RateLimiterService{

    public Boolean tryAcquire();
}
