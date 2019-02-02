package com.beta.repositry;

import com.beta.pojo.SmallRedPacket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SmallRedPacketRepositry extends JpaRepository<SmallRedPacket, Integer> {
    SmallRedPacket findByRedid(String id);
}
