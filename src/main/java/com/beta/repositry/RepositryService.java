package com.beta.repositry;

import com.beta.pojo.BigredPacket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RepositryService extends JpaRepository<BigredPacket, Integer> {
    BigredPacket findByRedid(String red_id);
}
