package by.rublevskaya.userservice.repository;

import by.rublevskaya.userservice.entity.CardInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardInfoRepository extends JpaRepository<CardInfo, Long> {
    Page<CardInfo> findAll(Pageable pageable);

    @Query("SELECT c FROM CardInfo c JOIN c.user u WHERE u.id = :userId")
    List<CardInfo> findCardsByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(c) > 0 FROM CardInfo c WHERE c.number = :number AND c.user.id = :userId")
    boolean existsByNumberAndUserId(@Param("number") String number, @Param("userId") Long userId);
}
